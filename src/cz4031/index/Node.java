package cz4031.index;

import java.util.ArrayList;

// n(keys) max is 4
// child max is 4 + 1
public abstract class Node {
    
    private ArrayList<Integer> keys;
    private NonLeafNode nonLeafNode;  //parent
    private boolean isLeaf;
    private boolean isRoot;


    // constructor
    public Node() {

        //key 'index' to a record or child
        keys = new ArrayList<Integer>();
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
        nonLeafNode = parentNode;
    }

    // get node's parent(Non-leaf)
    public NonLeafNode getParent() {
        return this.nonLeafNode;
    }


    // return arraylist containing the keys
    public ArrayList<Integer> getKeys() {
        return this.keys;
    }

    // get key at spcific index
    public int getKey(int index) {
        return this.keys.get(index);
    }

    // add key
    public int addKey(int key) {

        //default if record have no keys
        if (this.getKeys().size() == 0) {
            this.keys.add(key);
            return 0;
        }
        //add the keys and sort them ascending
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
        //return the index of the added key
        return i;
    }

    // delete key from specifiv index
    public void deleteKey(int index) {
        this.keys.remove(index);
    }

    // for deleting keys before splitting
    //delete all keys in node
    public void deleteKeys() {
        this.keys = new ArrayList<Integer>();
    }

    // find smallest key
    public int findSmallestKey() {
        int key;
        NonLeafNode copy;

        //not leaf
        if (!this.getIsLeaf()) {
            copy = (NonLeafNode) this;
            
            //traverse downward the tree to get LB smallest key at the leafNode
            while (!copy.getChildNode(0).getIsLeaf())
                copy = (NonLeafNode) copy.getChildNode(0);
            
            key = copy.getChildNode(0).getKey(0);
        }

        //if leadNode key will be at 0 index as sorted in ascending order
        else 
            key = this.getKey(0);

        return key;
    }

    // delete the node
    public void deleteNode() {
        //this have parent(non-leaf)
        if (nonLeafNode != null) {
            //remove this(child) form nonLeaf(parent)
            nonLeafNode.deleteChild(this);
            nonLeafNode = null;
        }

        //IS lead
        if (this.isLeaf) {
            LeafNode copy = (LeafNode) this;
            //free all records/space
            copy.deleteRecords();
            //set leaf next null
            copy.setNext(null);
        }

        else {
            NonLeafNode copy = (NonLeafNode) this;
            copy.deleteChildren();
        }

        //RESET TO EMPTY NODE
        this.isLeaf = false;
        this.isRoot = false;
        this.keys = new ArrayList<Integer>();
    }

    //To be implemented base of actual node type
    abstract void logStructure();
    
}
