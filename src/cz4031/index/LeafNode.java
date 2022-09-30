package cz4031.index;

import cz4031.storage.Address;
import java.util.ArrayList;

/**
 * 1st level is dense index off leafnodes pointing to another leafnode similar to linked list.
 * 
 */
public class LeafNode extends Node {
    private ArrayList<Address> records;
    protected LeafNode next;

    public LeafNode() {
        super();
        this.records = new ArrayList<>();
        this.isLeaf = true;
        this.next = null;
    }

    //by index
    public Address getRecord(int index) {
        return this.records.get(index);
    }
    public ArrayList<Address> getRecords() {
        return this.records;
    }
   
    //record must be sorted in ascending order
    public int addRecord(int key, Address address) {

        int index = super.addKey(key);
        records.add(index, address);
        return index;
    }

    public void setNext(LeafNode nextLeafNode) {
        this.next = nextLeafNode;
    }
    public LeafNode getNext() {
        return this.next;
    }

    

    @Override
    public int findSmallestKey() {
        if (this.records.size() == 0)
            throw new RuntimeException("Trying to find smallest key of empty node");

        return this.keys.get(0);
    }

    @Override
    public void deleteNode() {
        if (parentNode != null) {
            // remove this(child) form nonLeaf(parent)
            parentNode.deleteChild(this);
            parentNode = null;
        }

        // free all records/space
        this.records = new ArrayList<>();
        // set leaf next null
        this.next = null;
        // resetting
        this.isLeaf = false;
        this.isRoot = false;
        this.keys = new ArrayList<>();
    }

    // preprocessing leafnode for splitting
    public void splitPrep() {
        deleteKeys();
        records = new ArrayList<>();
    }

    // delete a record from leafnode
    public void deleteRecord(int index) {
        this.keys.remove(index);
        records.remove(index);
    }
}
