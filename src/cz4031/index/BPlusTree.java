package cz4031.index;
import cz4031.storage.Address;
import cz4031.util.Log;

import java.util.ArrayList;
public class BPlusTree {
    //private static final String TAG = "B+Tree";
    // for 64 bits system; RAM use 64bit for addressing -> 2^6 = 6B
    // for 64 bits system; RAM use 64bit for addressing -> 2^5
    private static final int SIZE_POINTER = 6; 
    private static final int SIZE_KEY = 4; // for int value
    private static final String TAG = null;
    int maxKeys;
    int parentMinKeys;
    int leafMinKeys;
    Node root;
    int height;
    int nodeCount;
    int deletedCount;

    public BPlusTree(int blockSize){
        // n keys + n+1 pointer
        maxKeys = (blockSize-SIZE_POINTER) / (SIZE_KEY+SIZE_POINTER); // n
        parentMinKeys = (int) Math.floor(maxKeys/2);
        leafMinKeys = (int) Math.floor((maxKeys+1)/2);
        Log.i(TAG, "init: blockSize = "+blockSize+", maxKeys = "+maxKeys);
        Log.i(TAG, "MinKeys: parent="+parentMinKeys+", leaf="+leafMinKeys);
        root = createFirst();
        nodeCount = 0;
        deletedCount = 0;
    }

    // initialize the first node
    public LeafNode createFirst() {
        LeafNode newRoot = new LeafNode();
        newRoot.setIsRoot(true);
        height = 1;
        nodeCount = 1;
        return newRoot;
    }

    //insert a record into the tree
    public void insert(int key, Address address) {
        this.insertToLeaf(this.searchLeaf(key), key, address);
    }

    //search for the right leafnode for record insertion
    public LeafNode searchLeaf(int key) {

        // if root is a leaf, return root
        if (this.root.getIsLeaf())
            return (LeafNode) root;

        ParentNode parent = (ParentNode) root;
        ArrayList<Integer> keys;

        // finding correct first level parent
        while (!parent.getChild(0).getIsLeaf()) {

            keys = parent.getKeys();

            for (int i = keys.size() -1; i >= 0; i--) {

                if (keys.get(i) <= key) {

                    parent = (ParentNode) parent.getChild(i+1);
                    break;
                }

                else if (i == 0)
                    parent = (ParentNode) parent.getChild(0);
            }
        }

        // finding correct leaf
        keys = parent.getKeys();
        for (int i = keys.size() -1; i >= 0; i--) {

            if (keys.get(i) <= key)
                return (LeafNode) parent.getChild(i+1);
        }

        return (LeafNode) parent.getChild(0);
    }

    //insert record into leafnode
    public void insertToLeaf(LeafNode leaf, int key, Address address) {

        if (leaf.getKeys().size() < maxKeys) 
            leaf.addRecord(key, address);

        else {

            splitLeaf(leaf, key, address);
        }
    }

    //split a full leafnode
    public void splitLeaf(LeafNode oldLeafNode,int key, Address address) {

        int keys[] = new int[maxKeys+1];
        Address addresses[] = new Address[maxKeys+1];
        LeafNode newLeadNode = new LeafNode();
        int i;

        //getting full lists of keys and addresses
        for (i = 0; i < maxKeys; i++) {

            keys[i] = oldLeafNode.getKey(i);
            addresses[i] = oldLeafNode.getRecord(i);
        }
        
        //getting full lists of keys and addresses
        for (i = maxKeys - 1; i >= 0; i--) {
            
            if (keys[i] <= key) {
                i++;
                keys[i] = key;
                addresses[i] = address;
                break;
            }

            keys[i+1] = keys[i];
            addresses[i+1] = addresses[i];
        } 

        //clearing old leafnode values
        oldLeafNode.splitPrep();

        //putting the keys and addresses into the two leafnodes
        for (i = 0; i < leafMinKeys; i++) 
            oldLeafNode.addRecord(keys[i], addresses[i]);

        for (i = leafMinKeys; i < maxKeys+1; i++) 
            newLeadNode.addRecord(keys[i], addresses[i]);

        //setting old leafnode to point to new leafnode and new leafnode to point to next leafnode
        newLeadNode.setNext(oldLeafNode.getNext());
        oldLeafNode.setNext(newLeadNode);

        //setting parents for new leafnode
        if (oldLeafNode.getIsRoot()) {

            ParentNode newRoot = new ParentNode();
            oldLeafNode.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.addChild(oldLeafNode);
            newRoot.addChild(newLeadNode);
            root = newRoot;
            height++;
        }

        else if (oldLeafNode.getParent().getKeys().size() < maxKeys)
            oldLeafNode.getParent().addChild(newLeadNode);

        else 
            splitParent(oldLeafNode.getParent(), newLeadNode);

        // updating nodeCount
        nodeCount++;
    }

    //to split a full parent node
    public void splitParent(ParentNode parentNode, Node childNode) {

        Node children[] = new Node[maxKeys+2];
        int keys[] = new int[maxKeys+2];
        int key = childNode.findSmallestKey();
        ParentNode newParentNode = new ParentNode();

        // getting full and sorted lists of keys and children
        for (int i = 0; i < maxKeys+1; i++)  {

            children[i] = parentNode.getChild(i);
            keys[i] = children[i].findSmallestKey();
        }
        
        for (int i = maxKeys; i >= 0; i--) {

            if (keys[i] <= key) {

                i++;
                keys[i] = key;
                children[i] = childNode;
                break;
            }

            keys[i+1] = keys[i];
            children[i+1] = children[i];
        }

        //clearing old parent values
        parentNode.splitPrep();

        // putting the children into the two parentnodes
        for (int i = 0; i < parentMinKeys+2; i++) 
            parentNode.addChild(children[i]);

        for (int i = parentMinKeys+2; i < maxKeys+2; i++) 
            newParentNode.addChild(children[i]);

        //setting parent for the new parentnode
        if (parentNode.getIsRoot()) {

            ParentNode newRoot = new ParentNode();
            parentNode.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.addChild(parentNode);
            newRoot.addChild(newParentNode);
            root = newRoot;
            height++;
        }

        else if (parentNode.getParent().getKeys().size() < maxKeys)
            parentNode.getParent().addChild(newParentNode);

        else 
            splitParent(parentNode.getParent(), newParentNode);

        // updating nodeCount
        nodeCount++;
    }

    // to delete all records of a certain key
    public void deleteKey(int key) {

        ArrayList<Integer> keys;
        LeafNode leaf;

        // while there are still records with given key value
        while (getRecordsWithKey(key, false).size() != 0) {

            leaf = searchLeaf(key);
            keys = leaf.getKeys();
            
            // delete one record and update tree 
            for (int i = 0; i < keys.size(); i++) {
                
                if (keys.get(i) == key) {

                    leaf.deleteRecord(i);

                    // if leafnode is not root then update tree
                    if (!leaf.getIsRoot())
                        resetLeaf(leaf);

                    break;
                }
            }
        }

        Log.d("deletion", "number of nodes deleted = " + deletedCount);
        nodeCount = nodeCount - deletedCount;
        treeStats();
    }

    // to update leafnode
    public void resetLeaf(LeafNode leafNode) {

        // if no need to change node, reset parent and finish
        if (leafNode.getKeys().size() >= leafMinKeys) {

            resetParent(leafNode.getParent());
            return;
        }
        
        LeafNode beforeNode = (LeafNode) leafNode.getParent().getBefore(leafNode);
        LeafNode afterNode = (LeafNode) leafNode.getParent().getAfter(leafNode);
        int needed = leafMinKeys - leafNode.getKeys().size();
        int beforeSpare = 0;
        int afterSpare = 0;
        ParentNode copy;

        // getting number of keys that before and after nodes can spare
        if (beforeNode != null) 
            beforeSpare += beforeNode.getKeys().size() - leafMinKeys;

        if (afterNode != null) 
            afterSpare += afterNode.getKeys().size() - leafMinKeys;

        // if need to merge
        if (needed > afterSpare + beforeSpare) {

            // if node has both before and after nodes
            if (beforeNode != null && afterNode != null) {

                // insert as many records as possible into before node
                for (int i = 0; i < maxKeys-(beforeSpare+leafMinKeys); i++) 
                    beforeNode.addRecord(leafNode.getKey(i), leafNode.getRecord(i));
                
                // insert the rest into after node
                for (int i = maxKeys-(beforeSpare+leafMinKeys); i < leafNode.getKeys().size(); i++) 
                    afterNode.addRecord(leafNode.getKey(i), leafNode.getRecord(i));
            }

            // if node only has after node
            else if (beforeNode == null) {

                for (int i = 0; i < leafNode.getKeys().size(); i++) 
                    afterNode.addRecord(leafNode.getKey(i), leafNode.getRecord(i));
            }

            // if node only has before node
            else {

                for (int i = 0; i < leafNode.getKeys().size(); i++) 
                    beforeNode.addRecord(leafNode.getKey(i), leafNode.getRecord(i));
            }

            // have to copy parent to reset after deleting leafnode
            copy = leafNode.getParent();

            // have to look for before node if it is not from the same parent
            if (beforeNode == null) {

                if (!copy.getIsRoot())
                    beforeNode = searchLeaf(copy.findSmallestKey()-1);
            }

            // change before to point to after
            beforeNode.setNext(leafNode.getNext());

            // delete node
            leafNode.deleteNode();
            deletedCount++;
        }

        // able to borrow keys
        else {

            if (beforeNode != null && afterNode != null) {

                // take the last few keys from before node that can be spared
                for (int i = 0; i < beforeSpare; i++) {

                    leafNode.addRecord(beforeNode.getKey(beforeNode.getKeys().size()-1 -i), beforeNode.getRecord(beforeNode.getKeys().size()-1 -i));
                    beforeNode.deleteRecord(beforeNode.getKeys().size()-1 -i);
                }
                
                // take the rest from after node
                for (int i = beforeSpare, j = 0; i < needed; i++, j++) {

                    leafNode.addRecord(afterNode.getKey(j), afterNode.getRecord(j));
                    afterNode.deleteRecord(j);
                }
            }

            else if (beforeNode == null) {

                // take all from after node
                for (int i = 0; i < needed; i++) {

                    leafNode.addRecord(afterNode.getKey(i), afterNode.getRecord(i));
                    afterNode.deleteRecord(i);
                }
            }

            else {

                // take all from before node
                for (int i = 0; i < needed; i++) {

                    leafNode.addRecord(beforeNode.getKey(beforeNode.getKeys().size()-1 -i), beforeNode.getRecord(beforeNode.getKeys().size()-1 -i));
                    beforeNode.deleteRecord(beforeNode.getKeys().size()-1 -i);
                }
            }
            
            copy = leafNode.getParent();
        }

        // update parents
        resetParent(copy);
    }

    public void resetParent(ParentNode parentNode) {

        // if node is root
        if (parentNode.getIsRoot()) {

            // if root has at least 2 children, reset and return
            if (parentNode.getChildren().size() > 1) {

                //reset
                Node child = parentNode.getChild(0);
                parentNode.deleteChild(child);
                parentNode.addChild(child);
                return;
            }

            // if root has 1 child, delete root level
            else {
                parentNode.getChild(0).setIsRoot(true);
                root = parentNode.getChild(0);
                parentNode.deleteNode();
                deletedCount++;
                height--;
                return;
            }
        }

        ParentNode beforeNode = (ParentNode) parentNode.getParent().getBefore(parentNode);
        ParentNode afterNode = (ParentNode) parentNode.getParent().getAfter(parentNode);
        int needed = parentMinKeys - parentNode.getKeys().size();
        int beforeSpare = 0;
        int afterSpare = 0;
        ParentNode copyParentNode;

        if (beforeNode != null) 
            beforeSpare += beforeNode.getKeys().size() - parentMinKeys;

        if (afterNode != null) 
            afterSpare += afterNode.getKeys().size() - parentMinKeys;

        // if need to merge
        if (needed > afterSpare + beforeSpare) {

            // if node has both before and after nodes
            if (beforeNode != null && afterNode != null) {

                // insert as many records as possible into before node
                for (int i = 0; i < maxKeys-(beforeSpare+parentMinKeys)+1 && i < parentNode.getChildren().size(); i++) 
                    beforeNode.addChild(parentNode.getChild(i));
                
                // insert the rest into after node
                for (int i = maxKeys-(beforeSpare+parentMinKeys)+1; i < parentNode.getChildren().size(); i++) 
                    afterNode.addChild(parentNode.getChild(i));
            }

            // if node only has after node
            else if (beforeNode == null) {

                for (int i = 0; i < parentNode.getChildren().size(); i++) 
                    afterNode.addChild(parentNode.getChild(i));
            }

            // if node only has before node
            else {

                for (int i = 0; i < parentNode.getChildren().size(); i++) 
                    beforeNode.addChild(parentNode.getChild(i));
            }

            // delete after merging
            copyParentNode = parentNode.getParent();
            parentNode.deleteNode();
            deletedCount++;
        }

        // if able to borrow keys
        else {

            if (beforeNode != null && afterNode != null) {

                // take the last few keys from before node that can be spared
                for (int i = 0; i < beforeSpare && i < needed; i++) {

                    parentNode.addChild(beforeNode.getChild(beforeNode.getChildren().size()-1), 0);
                    beforeNode.deleteChild(beforeNode.getChild(beforeNode.getChildren().size()-1));
                }
                
                // take the rest from after node
                for (int i = beforeSpare; i < needed; i++) {

                    parentNode.addChild(afterNode.getChild(0));
                    afterNode.deleteChild(afterNode.getChild(0));
                }
            }

            else if (beforeNode == null) {

                // take all from after node
                for (int i = 0; i < needed; i++) {

                    parentNode.addChild(afterNode.getChild(0));
                    afterNode.deleteChild(afterNode.getChild(0));
                }
            }

            else {

                // take all from before node
                for (int i = 0; i < needed; i++) {

                    parentNode.addChild(beforeNode.getChild(beforeNode.getChildren().size()-1 -i), 0);
                    beforeNode.deleteChild(beforeNode.getChild(beforeNode.getChildren().size()-1 -i));
                }
            }
            
            copyParentNode = parentNode.getParent();
        }

        resetParent(copyParentNode);
    }


    // TODO for Experiment 2 (partially done)


    // TODO for Experiment 3
    public  ArrayList<Address> getRecordsWithKey(int key){
        return getRecordsWithKey(key, true);
    }

    public ArrayList<Address> getRecordsWithKey(int key, boolean showLog){
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1; // access the root??
        int siblingAccess = 0;
        if (showLog){
            Log.d("B+Tree.keySearch","[Node Access] Access root node");
        }
        Node curNode = root;
        ParentNode parentNode;
        // searching for leaf node with key
        while (!curNode.getIsLeaf()){
            parentNode = (ParentNode) curNode;
            for (int i=0; i<parentNode.getKeys().size(); i++) {
                if ( key <= parentNode.getKey(i)){
                    if (showLog){
                        Log.v("B+Tree.keySearch", curNode.toString());
                        Log.d("B+Tree.keySearch",String.format("[Node Access] follow pointer [%d]: key(%d)<=curKey(%d)", i, key, parentNode.getKey(i) ));
                    }
                    curNode = parentNode.getChild(i);
                    blockAccess++;
                    break;
                }
                if (i == parentNode.getKeys().size()-1){
                    if (showLog){
                        Log.v("B+Tree.keySearch", curNode.toString());
                        Log.d("B+Tree.keySearch",String.format("[Node Access] follow pointer [%d+1]: last key and key(%d)>curKey(%d)", i, key, parentNode.getKey(i) ));
                    }
                    curNode = parentNode.getChild(i+1);
                    blockAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curLeafNode = (LeafNode) curNode;
        boolean done = false;
        while(!done && curLeafNode!=null){
            // finding same keys within leaf node
            for (int i=0; i<curLeafNode.getKeys().size(); i++){
                // found same key, add into result list
                if (curLeafNode.getKey(i) == key){
                    result.add(curLeafNode.getRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeafNode.getKey(i) > key){
                    done = true;
                    break;
                }
            }
            if (!done){
                // trying to check sibling node has remaining records of same key
                if (curLeafNode.getNext()!= null){
                    curLeafNode = curLeafNode.getNext();
                    blockAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }

        if (siblingAccess > 0){
            if (showLog) {
                Log.d("B+Tree.keySearch", "[Node Access] " + siblingAccess + " sibling node access");
            }
        }
        if (showLog) {
            Log.i("B+Tree.keySearch", String.format("input(%d): %d records found with %d node access", key, result.size(), blockAccess));
        }
        return result;
    }

    public void treeStats() {

        ArrayList<Integer> rootKeys = new ArrayList<Integer>();
        ArrayList<Integer> firstKeys = new ArrayList<Integer>();
        ParentNode rootNodeCopy = (ParentNode) root;
        Node first = rootNodeCopy.getChild(0);

        for (int i = 0; i < root.getKeys().size(); i++) {

            rootKeys.add(root.getKey(i));
        }

        for (int i = 0; i < first.getKeys().size(); i++) {

            firstKeys.add(first.getKey(i));
        }

        Log.d("treeStats", "n = " + maxKeys + ", number of nodes = " + nodeCount + ", height = " + height);
        Log.d("rootContents", "root node contents = " + rootKeys);
        Log.d("firstContents", "first child contents = " + firstKeys);
    }

    // TODO for Experiment 4
    public ArrayList<Address> getRecordsWithKeyInRange(int min, int max) {
        return getRecordsWithKeyInRange(min, max, true);
    }
    public ArrayList<Address> getRecordsWithKeyInRange(int min, int max, boolean showLog){
        ArrayList<Address> result = new ArrayList<>();
        int nodeAccess = 1; // access the root??
        int siblingAccess = 0;
        if (showLog) {
            Log.d("B+Tree.rangeSearch", "[Node Access] Access root node");
        }
        Node curNode = root;
        ParentNode parentNode;
        // searching for leaf node with key
        while (!curNode.getIsLeaf()){
            parentNode = (ParentNode) curNode;
            for (int i=0; i<parentNode.getKeys().size(); i++) {
                if ( min <= parentNode.getKey(i)){
                    if (showLog) {
                        Log.v("B+Tree.rangeSearch", curNode.toString());
                        Log.d("B+Tree.rangeSearch", String.format("[Node Access] follow pointer [%d]: min(%d)<=curKey(%d)", i, min, parentNode.getKey(i)));
                    }
                    curNode = parentNode.getChild(i);
                    nodeAccess++;
                    break;
                }
                if (i == parentNode.getKeys().size()-1){
                    if (showLog) {
                        Log.v("B+Tree.rangeSearch", curNode.toString());
                        Log.d("B+Tree.rangeSearch", String.format("[Node Access] follow pointer [%d+1]: last key and min(%d)>curKey(%d)", i, min, parentNode.getKey(i)));
                    }
                    curNode = parentNode.getChild(i+1);
                    nodeAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curLeafNode = (LeafNode) curNode;
        boolean completed = false;
        while(!completed && curLeafNode!=null){
            // finding same keys within leaf node
            for (int i=0; i<curLeafNode.getKeys().size(); i++){
                // found same key, add into result list
                if (curLeafNode.getKey(i) >= min && curLeafNode.getKey(i) <= max){
                    result.add(curLeafNode.getRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeafNode.getKey(i) > max){
                    completed = true;
                    break;
                }
            }
            if (!completed){
                // trying to check sibling node has remaining records of same key
                if (curLeafNode.getNext()!= null){
                    curLeafNode = (LeafNode) curLeafNode.getNext();
                    nodeAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }
        if (siblingAccess > 0){
            if (showLog) {
                Log.d("B+Tree.rangeSearch", "[Node Access] " + siblingAccess + " sibling node access");
            }
        }
        if (showLog) {
            Log.i("B+Tree.rangeSearch", String.format("input(%d, %d): %d records found with %d node access", min, max, result.size(), nodeAccess));
        }
        return result;
    }

    // TODO for Experiment 5
    public ArrayList<Address> removeRecordsWithKey(){
        // list of address need to be return, so app can use it to delete records from disk
        return null;
    }



    public void logStructure(){
        logStructure(0, Integer.MAX_VALUE, root);
    }

    public void logStructure(int maxLevel){
        logStructure(0, maxLevel, root);
    }

    // recursive logging of tree structure
    private void logStructure(int level, int maxLevel,  Node curNode){
        if (curNode == null){
            curNode = root;
        }
        if (level > maxLevel){
            return;
        }

        System.out.print("h="+level+"; ");
        curNode.logStructure();
        if (curNode.getIsLeaf()){
            return;
        }
        ParentNode parentNode = (ParentNode) curNode;
        for (Node child: parentNode.getChildren()) {
            logStructure(level+1, maxLevel, child);
        }
    }
}
