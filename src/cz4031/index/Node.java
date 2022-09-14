package cz4031.index;

import java.util.ArrayList;

public abstract class Node {
    
    private ArrayList<Integer> keys;
    private ParentNode parent;
    private boolean isLeaf;
    private boolean isRoot;


    // constructor
    public Node() {
        keys = new ArrayList<Integer>();
        //assume not lead and not root by default
        isLeaf = false;
        isRoot = false;
    }
    
    // check if node is lead
    public boolean getIsLeaf() {
        return isLeaf;
    }

    // set node is lead
    public void setIsLeaf(boolean leaf_status) {
        isLeaf = leaf_status;
    }

    // get whether it is root
    public boolean getIsRoot() {

        return isRoot;
    }

    // set whether it is root
    public void setIsRoot(boolean root_status) {
        isRoot = root_status;
    }

    // return node's parent
    public ParentNode getParent() {
        return parent;
    }

    // set node as a child of another node
    public void setParent(ParentNode parent_Node) {
        parent = parent_Node;
    }

    // get arraylist of all keys
    public ArrayList<Integer> getKeys() {
        return keys;
    }

    // get key at specific index
    public int getKey(int index) {
        return keys.get(index);
    }

    // add key
    public int addKey(int key) {
        if (this.getKeys().size() == 0) {
            this.keys.add(key);
            return 0;
        }

        int i;
        keys.add(key);
        for (i = keys.size() -2; i >= 0; i--) {

            if (keys.get(i) <= key) {

                i++;
                keys.set(i, key);
                break;
            }

            keys.set(i+1, keys.get(i));
            if (i == 0) {

                keys.set(i, key);
                break;
            }
        }
        
        return i;
    }

    // delete key from index
    public void deleteKey(int index) {

        keys.remove(index);
    }

    // for deleting keys before splitting
    public void deleteKeys() {

        keys = new ArrayList<Integer>();
    }

    // find smallest key (more for use by parentnode but placed here for first level of parents)
    public int findSmallestKey() {

        int key;
        ParentNode copy;

        if (!this.getIsLeaf()) {

            copy = (ParentNode) this;

            while (!copy.getChild(0).getIsLeaf())
                copy = (ParentNode) copy.getChild(0);
            
            key = copy.getChild(0).getKey(0);
        }

        else 
            key = this.getKey(0);

        return key;
    }

    // delete the node
    public void deleteNode() {

        //remove this node from its parent
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }

        //type casting
        if (this.isLeaf) {
            LeafNode copy = (LeafNode) this;
            copy.deleteRecords();
            copy.setNext(null);
        }

        else {
            ParentNode copy = (ParentNode) this;
            copy.deleteChildren();
        }

        isLeaf = false;
        isRoot = false;
        keys = new ArrayList<Integer>();
    }

    abstract void logStructure();
}
