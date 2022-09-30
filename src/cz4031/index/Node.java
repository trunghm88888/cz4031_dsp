package cz4031.index;

import java.util.ArrayList;

public abstract class Node {
    protected ArrayList<Integer> keys;
    protected NonLeafNode parentNode;  //parent
    protected boolean isLeaf;
    protected boolean isRoot;

    public Node() {
        keys = new ArrayList<>();
        isLeaf = false;
        isRoot = false;
    }
    
    // find smallest key
    public abstract int findSmallestKey();
    // delete the node
    public abstract void deleteNode();

    // get root status
    public boolean getIsRoot() {
        return this.isRoot;
    }
    // set root status
    public void setIsRoot(boolean root_status) {
        this.isRoot = root_status;
    }

    // get isLeaf status
    public boolean getIsLeaf() {
        return this.isLeaf;
    }
    // set isStatus
    public void setIsLeaf(boolean is_Leaf) {
        this.isLeaf = is_Leaf;
    }    
    
    // get node's parent(Non-leaf)
    public NonLeafNode getParent() {
        return this.parentNode;
    }
    // change node's parent
    public void setParent(NonLeafNode parentNode) {
        this.parentNode = parentNode;
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
        if (this.getKeys().size() == 0) {
            this.keys.add(key);
            return 0;
        }

        int i = 0;
        while (i < keys.size() && key > keys.get(i))
            i++;
        this.keys.add(i, key);
        return i;
    }

    //delete all keys in node
    public void deleteKeys() {
        this.keys = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.keys.toString();
    }
}
