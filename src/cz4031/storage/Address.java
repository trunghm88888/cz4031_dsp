package cz4031.storage;

/**
 * Class representing the logical address of a record
 */

public class Address {
    int blockId;
    int offset;

    public Address(int blockId, int offset){
        this.blockId=blockId;
        this.offset=offset;
    }

    //return block id
    public int getBlockId() {
        return blockId;
    }

    //set block id
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    //return offset
    public int getOffset() {
        return offset;
    }

    //set offset
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("@%d-%d", blockId, offset);
    }
}
