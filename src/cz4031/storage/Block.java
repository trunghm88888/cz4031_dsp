package cz4031.storage;

import javax.annotation.processing.SupportedSourceVersion;
import javax.xml.crypto.Data;

import java.util.Arrays;

/**
 * Simulate a disk block:
 * 1. Block size is set at constructor
 * 2. The size for actual storage of records is the BLOCK_SIZE minus the size of
 * block headers
 * 3. Accessing is only allow at block level
 */

public class Block {
    int curRecords;    //amount of records in the block currently
    int totalRecords; //total amount of record per block
    Record[] records; //records in a block

    public Block(int BLOCK_SIZE) {
        this.curRecords = 0;
        this.totalRecords = BLOCK_SIZE / Record.size();
        this.records = new Record[this.totalRecords]; // reduce 4B for the int 
    }

    public boolean isAvailable(){
        return curRecords < totalRecords;
    }

    public Record getRecord(int pos) {
        return records[pos];
    }

    public int insertRecord(Record r) {
        //insert into first available space
        for (int i = 0;i< records.length;i++) {
            if(records[i] == null){
                records[i] = r;
                this.curRecords++;
                return i;
            }
        }
        // no space to insert record
        return -1;
    }

    public boolean deleteRecord(int pos) {
        if (records[pos] != null) {
            //clear the entry
            records[pos] = null;
            curRecords--;
            //deletion successful
            return true;
        }
        //uncessfull deletion
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("Total records: " + totalRecords + "\n" + "Content: ");

        for (int i = 0; i < curRecords; i++) {
            Record r = records[i];
            if (r != null)
                res.append(r).append(", ");
        }

        res.replace(res.length() - 2, res.length(), ".");
        return res.toString();
    }
}
