package cz4031.storage;

public class Address{
    int blockID;
    int offset;

    public Address(int blockID, int offset){
        this.blockID=blockID;
        this.offset=offset;
    }

    public int getBlockId(){
        return blockID;
    }
    public int getOffset() {
        return offset;
    }

    public void setBlockId(int blockID) {
        this.blockID = blockID;
    }

    public void setOffset(int offset){
        this.offset = offset;
    }

    public String toString(){
        return String.format("@%d-%d", blockID, offset);
    }
}