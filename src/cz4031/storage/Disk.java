package cz4031.storage;

import java.util.ArrayList;
import java.util.HashMap;

public class Disk {
    private static final String TAG = "Disk";
    
    int diskSize; 
    int blockSize;
    int maxBlockNum; 
    int recordNum; 
    ArrayList<Block> blocks; 
    
    public Disk(int diskSize, int blockSize) { 
    	this.diskSize = diskSize; 
    	this.blockSize = blockSize; 
    	this.maxBlockNum = diskSize / blockSize; 
    	this.blocks = new ArrayList<>(); 
    	this.recordNum = 0;
    }
   
    //returns the total number of blocks that exist in the storage  
    public int blockCount() {
    	return blocks.size(); 
    }
   
    //returns the total number of records that exist in the storage 
    public int recordNum() {
    	return recordNum; 
    }
    
    
    //returns the used size of storage
    public int usedSpace() { 
    	return blockCount() * blockSize; 
    }
    
    //insert the records into the first available block 
    public Address insertRecord(Record record ) throws Exception {
    	int blockId = availableBlockId(); 
    	return insertRecord(blockId, record); 
    }
    
    //insert record into last block 
    public Address appendRecord(Record record) throws Exception{
        int blockId = lastBlockId();
        return insertRecord(blockId, record);
    }
    
    private Address insertRecord(int blockId, Record record) throws Exception {
        Block block = null;
        if (blockId >= 0){
            block = getBlock(blockId);
        }

        // if block isn't available or doesn't exist, create a new block to insert to
        if (block == null || !block.isAvailable()) { //hasn't been declared in block class yet
            if (blocks.size() == maxBlockNum) {
                throw new Exception("Insufficient space on disk for insertion.");
            }
            block = new Block(blockSize);
            blocks.add(block);
            blockId = lastBlockId();
        }
        int offset = block.insertRecord(record);
        recordNum++;
        return new Address(blockId, offset);
    }

    public int availableBlockId(){
        int blockId = -1;
        for(int i=0; i<blocks.size(); i++){
            if(blocks.get(i).isAvailable()){ //hasn't been declared in block class, need to ask whoever wrote that to include it 
                blockId = i;
                break;
            }
        }
        return blockId;
    }

    public int lastBlockId(){
        return blocks.size()>0? blocks.size()-1:-1;
    }

    public Block getBlock(int blockId){
        return blocks.get(blockId);
    }

    public Record getRecord(int blockId, int offset){
        return getBlock(blockId).getRecord(offset);
    }

    public Record getRecord(Address address){
        return getRecord(address.getBlockId(), address.getOffset());
    }

    public ArrayList<Record> getRecords(ArrayList<Address> addresses ){
        HashMap<Integer, Block> cache = new HashMap<>();
        ArrayList<Record> records = new ArrayList<>();
        int blkAccess = 0;
        Block tempBlk = null;
        for (Address address: addresses) {
            //try searching from cache first, before accessing from disk
            tempBlk = cache.get(address.getBlockId());
            boolean cacheRead = tempBlk != null;
            if (tempBlk == null){
                tempBlk = getBlock(address.getBlockId());
                cache.put(address.getBlockId(), tempBlk);
                blkAccess++;
            } else { //accessing the block from cache, no block access
            }

            Record record = tempBlk.getRecord(address.getOffset());
            records.add( record ); //log is in utilities class
        }
        return records; //log is in utilities class
    }


    public boolean deleteRecord(int blockId, int offset) {
        boolean correct = getBlock(blockId).deleteRecord(offset);
        if (correct) {
            recordNum--;
        }
        return correct;
    }

    public void deleteRecords(ArrayList<Address> recordAddresses){
        for (Address address: recordAddresses) {
            deleteRecord(address.getBlockId(), address.getOffset());
        }
    }


}