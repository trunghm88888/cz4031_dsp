package cz4031.index;

import java.util.ArrayList;

public abstract class Node {
    protected ArrayList<Integer> keys;
    protected NonLeafNode parentNode;  //parent
    protected boolean isLeaf;
    protected boolean isRoot;

    // constructor
    public Node() {
        //key 'index' to a record or child
        keys = new ArrayList<>();
        //asume to be nonLead and not root 
        isLeaf = false;
        isRoot = false;
    }

    // set isStatus
    public void setIsLeaf(boolean is_Leaf) {
        this.isLeaf = is_Leaf;
    }    

    // get isLeaf status
    public boolean getIsLeaf() {
        return this.isLeaf;
    }

    // set root status
    public void setIsRoot(boolean root_status) {
        this.isRoot = root_status;
    }

    // get root status
    public boolean getIsRoot() {
        return this.isRoot;
    }

    // change node's parent
    public void setParent(NonLeafNode parentNode) {
        this.parentNode = parentNode;
    }

    // get node's parent(Non-leaf)
    public NonLeafNode getParent() {
        return this.parentNode;
    }


    // return arraylist containing the keys
    public ArrayList<Integer> getKeys() {
        return this.keys;
    }

    // get key at specific index
    public int getKey(int index) {
        return this.keys.get(index);
    }

    // add key
    public int addKey(int key) {

        // default if record have no keys
        if (this.getKeys().size() == 0) {
            this.keys.add(key);
            return 0;
        }

        int i = 0;
        while (i < keys.size() && key >= keys.get(i))
            i++;
        this.keys.add(i, key);
        return i;
    }

    // for deleting keys before splitting
    //delete all keys in node
    public void deleteKeys() {
        this.keys = new ArrayList<>();
    }

    // find smallest key
    public abstract int findSmallestKey();

    // delete the node
    public abstract void deleteNode();

    //To be implemented base of actual node type
    abstract void logStructure();
    
}
