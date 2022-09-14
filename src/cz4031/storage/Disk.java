package cz4031.storage;
import java.util.*;

import cz4031.util.Log;
import cz4031.util.Utility;

public class Disk {
    public static final String TAG = "Disk";
    int diskSize;
    int maxBlockCount;
    int blockSize;
    int recordCounts;
    ArrayList<Block> blocks;

    public Disk(int diskSize, int blockSize){
        this.diskSize = diskSize;
        this.blockSize = blockSize;
        this.recordCounts = 0;
        this.blocks = new ArrayList<>();
        this.maxBlockCount = diskSize / blockSize;
        log();
    }

    /**
     * return total number of blocks in the storage / disk
     * @return
     */
    public int getBlocksCount(){
        return blocks.size();
    }

    /**
     * return  total number of records exiting in the storage
     * @return
     */
    public int getRecordCounts(){
        return recordCounts;
    }

    /**
     * return storage used size
     * @return
     */
    public int getUsedSize(){
        return getBlocksCount() * blockSize;
    }


    /**
     * insert the records into first available block for record insertion, however, it can be expensive!!!
     * @param record inserting record
     * @return address of the record being inserted
     * @throws Exception
     */
    public Address insertRecord(Record record) throws Exception {
        int blockId = getFirstAvailableBlockId();
        return insertRecordAt(blockId, record);
    }

    /**
     * Try inserting record into last block. if last block is not available / full, record will be inserted into newly created block.
     * checking of availability will done on previous blocks
     * @param record inserting record
     * @return address of record being inserted
     * @throws Exception
     */
    public Address appendRecord(Record record) throws Exception{
        int blockId = getLastBlockId();
        return insertRecordAt(blockId, record);
    }

    private Address insertRecordAt(int blockId, Record record) throws Exception {
        Block block = null;
        if (blockId >= 0){
            block = getBlockAt(blockId);
        }

        // if block not available/non exist, create a new block to insert the record
        if (block == null || !block.isAvailable()) {
            if (blocks.size() == maxBlockCount) {
                throw new Exception("Insufficient space available on disk");
            }
            block = new Block(blockSize);
            blocks.add(block);
            blockId = getLastBlockId();
        }
        int offset = block.insertRecord(record);
        recordCounts = recordCounts + 1;
        Log.v(String.format("Record inserted at %d-%d", blockId, offset));
        return new Address(blockId, offset);
    }


    public int getFirstAvailableBlockId(){
        int blockId = -1;
        for(int i=0; i < blocks.size(); i++){
            if(blocks.get(i).isAvailable()){
                blockId = i;
                break;
            }
        }
        return blockId;
    }

    //try get last blockId
    public int getLastBlockId(){
        if(blocks.size() > 0){
            return blocks.size() - 1;
        }
        else{
            return -1;
        }
    }

    //return block at given blockId
    public Block getBlockAt(int blockId){
        return blocks.get(blockId);
    }
    //return record at given blockId and record at given offset
    public Record getRecordAt(int blockId, int offset){
        return getBlockAt(blockId).getRecordAt(offset);
    }

    //return record at given address
    public Record getRecordAt(Address address){
        return getRecordAt(address.getBlockId(), address.getOffset());
    }

    public ArrayList<Record> getRecords(ArrayList<Address> addresses ){
        HashMap<Integer, Block> cache = new HashMap<>();
        ArrayList<Record> records = new ArrayList<>();
        int blockAccess = 0;
        Block tempBlock = null;
        for (Address address: addresses) {
            // try search from cache first, before access from disk
            tempBlock = cache.get(address.getBlockId());
            boolean cacheRead = tempBlock != null;
            if (tempBlock == null){
                tempBlock = getBlockAt(address.getBlockId());
               Log.v("Disk Access", String.format("Disk read: blockId=%d, offset=%d, block=%s", address.blockId, address.offset, tempBlock));
                cache.put(address.getBlockId(), tempBlock);
                blockAccess++;
            } else {// accessing the block from cache, no block access
                Log.v("Disk Access", String.format("Cache read: blockId=%d, offset=%d, block=%s", address.blockId, address.offset, tempBlock));
            }

            Record record = tempBlock.getRecordAt(address.getOffset());
			Log.v("Disk Access", String.format("%s read: blockId=%4d, \toffset=%d, \trecord=%s", cacheRead?"Cache":"Disk", address.blockId, address.offset, record));
            records.add( record );
        }
        Log.i(TAG, String.format("Retrieved %d records with %d block access", records.size(), blockAccess));
        return records;
    }



    //return result if deleted sucessfully or not
    public boolean deleteRecordAt(int blockId, int offset) {
        boolean result = getBlockAt(blockId).deleteRecordAt(offset);
        if (result) {
            //record removed
            recordCounts =  recordCounts - 1;
        }
        return result;
    }
    
    //delete all records
    public void deleteRecords(ArrayList<Address> recordAddresses){
        for (Address address: recordAddresses) {
            deleteRecordAt(address.getBlockId(), address.getOffset());
        }
    }


    // debugs only
    
    public void log(){
        Log.d(TAG, String.format("disk size = %s / %s", Utility.formatFileSize(getUsedSize()), Utility.formatFileSize(diskSize) ));
        Log.d(TAG, String.format("block size = %s", Utility.formatFileSize(blockSize)));
        Log.d(TAG, String.format("blocks = %,d / %,d", blocks.size(), maxBlockCount));
        Log.d(TAG, String.format("records = %,d", recordCounts));
    } 
    
}
