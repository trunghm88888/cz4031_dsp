package cz4031.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import cz4031.util.Utilities;

public class Disk {
    //size of block
    int blockSize;
    //max amount of block in disk
    int maxBlockNum;
    //size of disk
    int diskSize;
    //blocks in a disk implemented with arraylist
    ArrayList<Block> blocks; 

    public Disk(int diskSize, int blockSize) { 
    	this.diskSize = diskSize; 
    	this.blockSize = blockSize; 
    	this.maxBlockNum = diskSize / blockSize; 
    	this.blocks = new ArrayList<>();
    }
   
    //returns the total number of blocks that exist in the storage  
    public int blockCount() {
    	return blocks.size(); 
    }

    //returns the used size of storage
    public int usedSpace() { 
    	return blocks.size() * blockSize;
    }
    
    //insert the records into the first available block
    public Address insertRecord(Record record) throws Exception {
    	int blockId = firstAvailableBlockId(); 
    	return insertRecord(blockId, record); 
    }
    
    //insert record into last block ,if last block not available, record will be inserted into a newly created block
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
        if (block == null || !block.isAvailable()) { 
            if (blocks.size() == maxBlockNum) {
                throw new Exception("Insufficient space on Disk for Insertion.");
            }
            block = new Block(blockSize);
            blocks.add(block);
            blockId = lastBlockId();
        }
        int offset = block.insertRecord(record);
        return new Address(blockId, offset);
    }

    public int firstAvailableBlockId(){
        int blockId = -1;
        for(int i=0; i<blocks.size(); i++){
            if(blocks.get(i).isAvailable()){ 
                blockId = i;
                break;
            }
        }
        return blockId;
    }

    public int lastBlockId(){
        if(blocks.size()>0){
            return blocks.size() - 1;     
        }
        else{
            return -1;
        }
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

    public boolean deleteRecord(int blockId, int offset) {
        return getBlock(blockId).deleteRecord(offset);
    }

    public void deleteRecords(ArrayList<Address> recordAddresses){
        for (Address address: recordAddresses) {
            deleteRecord(address.getBlockId(), address.getOffset());
        }
    }
    public ArrayList<Record> getRecords(ArrayList<Address> addresses){
        ArrayList<Record> records = new ArrayList<>();
        Set<Integer> accessedBlockIds = null; // Storing Blocks accessed
        accessedBlockIds = new HashSet<>();

        Block tempBlk;
        for (Address address: addresses) {
            tempBlk = getBlock(address.getBlockId());
            Record record = tempBlk.getRecord(address.getOffset());
            accessedBlockIds.add(address.getBlockId());
            records.add(record);
        }

        // Print accessed Blocks
        System.out.printf("Accessed %d data blocks. Contents-", accessedBlockIds.size());
        accessedBlockIds
                .stream().limit(5).collect(Collectors.toSet())
                .forEach(id -> System.out.printf("Block %d: " + getBlock(id) + "\n", id));

        return records;
    }
    public void diskStats(){
        System.out.println("================================");
        System.out.println("Disk Statistics: ");
        System.out.format(" block size = %s \n", Utilities.formatFileSize(blockSize));
        System.out.format(" blocks = %,d / %,d \n", blocks.size(), maxBlockNum);
        System.out.format(" disk size = %s / %s \n", Utilities.formatFileSize(usedSpace()), Utilities.formatFileSize(diskSize) );
    }
}