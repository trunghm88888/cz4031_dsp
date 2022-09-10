package cz4031.storage;

/**
 * Simulate a disk block:
 * 1. Block size is pre-set
 * 2. The size for actual storage of records is the BLOCK_SIZE minus the size of
 * block headers
 * 3. Accessing is only allow at block level
 */

public class Block {
    public int total_records;
    // To-do: add the record array

    public Block(int BLOCK_SIZE) {
        this.total_records = 0;
        // To-do: add the record array
    }


}
