package cz4031.index;

import cz4031.storage.Address;
import cz4031.util.Log;
import java.util.ArrayList;

/**
 * 1st level is dense index off leafnodes pointing to another leafnode similar to linked list.
 * 
 */
public class LeafNode extends Node {
    private static final String TAG = "Leaf-Node";

    private ArrayList<Address> records;
    private LeafNode next;

    // constructor
    public LeafNode() {

        super();
        records = new ArrayList<Address>();
        setIsLeaf(true);
        setNext(null);
    }

    // return arraylist of all records
    public ArrayList<Address> getRecords() {

        return this.records;
    }

    // return record at index
    public Address getRecord(int index) {

        return this.records.get(index);
    }

    // add record
    //record must be sorted in ascending order
    public int addRecord(int key, Address address) {

        if (this.getRecords().size() == 0) {

            this.records.add(address);
            this.addKey(key);
            return 0;
        }

        int index;
        index = super.addKey(key);

        records.add(address);

        for (int i = records.size() -2; i >= index; i--) 
            records.set(i+1, records.get(i));
        
        records.set(index, address);

        return index;
    }

    // get next leafnode
    public LeafNode getNext() {
        return this.next;
    }

    // set next leafnode
    public void setNext(LeafNode nextLeafNode) {
        this.next = nextLeafNode;
    }

    // preprocessing leafnode for splitting
    public void splitPrep() {
        deleteKeys();
        records = new ArrayList<Address>();
    }

    // delete a record from leafnode
    public void deleteRecord(int index) {

        deleteKey(index);
        records.remove(index);
    }

    // delete all records
    public void deleteRecords() {

        records = new ArrayList<Address>();
    }

    @Override
    void logStructure() {
        Log.defaut(TAG, this.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i<getKeys().size(); i++){
            if (i>0){
                sb.append(", ");
            }
             sb.append(String.format("%d:{%d=>%s}", i, getKey(i), getRecord(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
