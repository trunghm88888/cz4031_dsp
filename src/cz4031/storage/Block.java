package cz4031.storage;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.Arrays;

/**
 * Simulate a disk block:
 * 1. Block size is set at constructor
 * 2. The size for actual storage of records is the BLOCK_SIZE minus the size of
 * block headers
 * 3. Accessing is only allow at block level
 */

public class Block {
    private int total_records;
    private Record[] records;

    public Block(int BLOCK_SIZE) {
        this.total_records = 0;
        this.records = new Record[(BLOCK_SIZE - 4) / Record.size()]; // reduce 4B for the int total_records
    }

    public Record getRecord(int pos) {
        return records[pos];
    }

    public int insertRecord(Record r) {
        if (total_records < records.length) {
            records[total_records++] = r;
            return (total_records - 1); // offset of the just inserted record
        } else return -1;
    }

    public void deleteRecord(int pos) {
        if (records[pos] != null) {
            if (total_records - 1 - pos >= 0)
                System.arraycopy(records, pos + 1, records, pos, total_records - 1 - pos);
            total_records--;
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("Block@" + this.hashCode() + "\n"
                + "Total records: " + total_records + "\n"
                + "Content: ");
        for (Record r : records) {
            res.append(r).append(", ");
        }
        return res.toString();
    }
}
