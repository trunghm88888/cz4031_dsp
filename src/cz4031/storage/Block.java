package cz4031.storage;

//import javax.annotation.processing.SupportedSourceVersion;

/**
 * Simulate a disk block:
 * 1. Block size is set at constructor
 * 2. The size for actual storage of records is the BLOCK_SIZE minus the size of
 * block headers
 * 3. Accessing is only allow at block level
 */

public class Block {
    int maxRecords;
    int curRecords;
    Record[] records;

    //initialise new block
    public Block(int size){
        this.curRecords = 0;
        this.maxRecords = size / Record.size();
        this.records = new Record[maxRecords];
    }

    //check got space for additional record
    public boolean isAvailable(){
        return curRecords < maxRecords;
    }

    public int insertRecord(Record record) throws Exception {
        if (!isAvailable()){
            throw new Exception("Insufficient space to insert new record");
        }


        int offset = -1;

        // insert record, on the first empty space
        for (int i = 0; i < records.length ; i++) {
            if (records[i] == null ){
                records[i] = record;
                offset = i;
                curRecords++;
                break;
            }
        }
        return offset;
    }

    //delete record at given offset
    public boolean deleteRecordAt(int offset){
        boolean success = false;
        if (records[offset]!=null){
            records[offset] = null;
            curRecords--;
            success = true;
        }
        return success;
    }

    //get record at given offset
    public Record getRecordAt(int offset){
        return records[offset];
    }

    //enable printing of current record
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i=0; i< records.length; i++){
            if (i > 0){
                sb.append(", ");
            }
            sb.append(String.format("%d : {%s}", i, records[i].tConst ));
        }
        sb.append("}");
        return sb.toString();
    }
}