package cz4031.index;

import cz4031.storage.Address;
import cz4031.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

public class BpTree {
    private static final String TAG = "B+Tree";
    private static final int SIZE_POINTER = 8; // for 64 bits system
    private static final int SIZE_KEY = 4; // for int value
    int maxKeys;
    int parentMinKeys;
    int leafMinKeys;
    Node root;
    int height;
    int nodeCount;
    int deletedCount;

    public BpTree(int blockSize){
        // n keys + n+1 pointer
        maxKeys = (blockSize-SIZE_POINTER) / (SIZE_KEY+SIZE_POINTER); // n
        parentMinKeys = maxKeys / 2;
        leafMinKeys = (maxKeys + 1) / 2;
        Log.defaut(TAG, "init: blockSize = "+blockSize+", maxKeys = "+maxKeys);
        Log.defaut(TAG, "MinKeys: parent="+parentMinKeys+", leaf="+leafMinKeys);
        root = new LeafNode();
        root.setIsRoot(true);
        nodeCount = 1;
        height = 1;
        deletedCount = 0;
    }

    // to insert a record into the tree
    public void insert(int key, Address address) {
        this.insertToLeaf(this.searchLeaf(key), key, address);
    }

    // to search for the right leafnode for record insertion
    public LeafNode searchLeaf(int key) {

        // if root is a leaf, return root
        if (this.root.getIsLeaf())
            return (LeafNode) root;

        NonLeafNode parent = (NonLeafNode) root;
        ArrayList<Integer> keys;

        // finding correct first level internal node
        while (!parent.getChildNode(0).getIsLeaf()) {

            keys = parent.getKeys();
            if (key < keys.get(0)) {
                parent = (NonLeafNode) parent.getChildNode(0);
            } else {
                int  i = 0;
                while (i < keys.size() && key >= keys.get(i))
                    i++;
                parent = (NonLeafNode) parent.getChildNode(i);
            }
        }

        // finding correct leaf
        keys = parent.getKeys();
        int  i = 0;
        while (i < keys.size() && key >= keys.get(i))
            i++;
        return (LeafNode) parent.getChildNode(i);
    }

    // to insert record into leafnode
    public void insertToLeaf(LeafNode leaf, int key, Address address) {

        if (leaf.getKeys().size() < maxKeys)
            leaf.addRecord(key, address);

        else {
            splitLeaf(leaf, key, address);
        }
    }

    //to split a full leafnode
    public void splitLeaf(LeafNode old, int key, Address address) {
        // create new overloading list for keys
        ArrayList<Integer> keys = new ArrayList<>(old.getKeys());
        ArrayList<Address> addresses = new ArrayList<>(old.getRecords());
        LeafNode leaf2 = new LeafNode();

        // insert the new key into the overloading list
        int i = 0;
        while (i < keys.size() && key >= keys.get(i))
            i++;
        keys.add(i, key);
        addresses.add(i, address);

        //clearing old leafnode values
        old.splitPrep();

        //putting the keys and addresses into the two leafnodes
        for (i = 0; i < leafMinKeys; i++)
            old.addRecord(keys.get(i), addresses.get(i));

        for (i = leafMinKeys; i < maxKeys + 1; i++)
            leaf2.addRecord(keys.get(i), addresses.get(i));

        //setting old leafnode to point to new leafnode and new leafnode to point to next leafnode
        leaf2.setNext(old.getNext());
        old.setNext(leaf2);

        //setting parents for new leafnode
        if (old.getIsRoot()) {
            NonLeafNode newRoot = new NonLeafNode();
            old.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.addChild(old);
            newRoot.addChild(leaf2);
            root = newRoot;
            height++;
        }

        else if (old.getParent().getKeys().size() < maxKeys)
            old.getParent().addChild(leaf2);

        else
            splitParent(old.getParent(), leaf2);

        // updating nodeCount
        nodeCount++;
    }

    //to split a full parent node
    public void splitParent(NonLeafNode parent, Node child) {

        ArrayList<Node> children = new ArrayList<>(parent.getChildren());
        ArrayList<Integer> keys = new ArrayList<>(parent.getKeys());
        int key = child.findSmallestKey();
        NonLeafNode parent2 = new NonLeafNode();

        int i = 0;
        while (i < keys.size() && key >= keys.get(i))
            i++;
        keys.add(i, key);
        children.add(i, child);

        //clearing old parent values
        parent.splitPrep();

        // putting the children into the two NonLeafNodes
        for (i = 0; i < parentMinKeys + 2; i++)
            parent.addChild(children.get(i));

        for (i = parentMinKeys + 2; i < maxKeys + 2; i++)
            parent2.addChild(children.get(i));

        //setting parent for the new NonLeafNode
        if (parent.getIsRoot()) {
            NonLeafNode newRoot = new NonLeafNode();
            parent.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.addChild(parent);
            newRoot.addChild(parent2);
            root = newRoot;
            height++;
        }
        else if (parent.getParent().getKeys().size() < maxKeys)
            parent.getParent().addChild(parent2);

        else
            splitParent(parent.getParent(), parent2);

        // updating nodeCount
        nodeCount++;
    }

    // to delete all records of a certain key
    public void deleteKey(int key) {

        LeafNode leaf;
        ArrayList<Integer> keys;

        while (true) {
            leaf = searchLeaf(key);
            keys = leaf.getKeys();
            ArrayList<Integer> toDelete = new ArrayList<>();
            if (keys.contains(key)) {
                System.out.println(leaf);
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i) == key)
                        toDelete.add(i);
                }
                toDelete.stream().sorted(Comparator.reverseOrder()).forEach(leaf::deleteRecord);
                if (!leaf.isRoot)
                    resetLeaf(leaf);
            } else break;
        }

//        // while there are still records with given key value
//        while (getRecordsWithKey(key, false).size() != 0) {
//
//            leaf = searchLeaf(key);
//            keys = leaf.getKeys();
//
//            // delete one record and update tree
//            for (int i = 0; i < keys.size(); i++) {
//
//                if (keys.get(i) == key) {
//
//                    leaf.deleteRecord(i);
//
//                    // if leafnode is not root then update tree
//                    if (!leaf.getIsRoot())
//                        resetLeaf(leaf);
//
//                    break;
//                }
//            }
//        }

        Log.defaut("deletion", "number of nodes deleted = " + deletedCount);
        nodeCount -= deletedCount;
        treeStats();
    }

    // to update leafnode
    public void resetLeaf(LeafNode node) {

        // if no need to change node, reset parent and finish
        if (node.getKeys().size() >= leafMinKeys) {
            resetParent(node.getParent());
            return;
        }

        LeafNode prev = (LeafNode) node.getParent().getPrevLeaf(node);
        LeafNode next = (LeafNode) node.next;
        int needed = leafMinKeys - node.getKeys().size();
        int prevShare = 0;
        int nextShare = 0;
        NonLeafNode parent = node.getParent();

        // getting number of keys that prev and next nodes can share
//        if (prev != null)
        prevShare += prev.getKeys().size() - leafMinKeys;

//        if (next != null)
        nextShare += next.getKeys().size() - leafMinKeys;

        // if need to merge, then just delete this node and re-insert
        if (needed > prevShare + nextShare) {
            System.out.println("Deleting leaf..." + node);
            for (int i = 0; i < node.getKeys().size(); i++) {
                insert(node.getKey(i), node.getRecord(i));
            }
//            }
//            if (prev != null) {
//                while (curIx < node.getRecords().size()) {
//                    insertToLeaf(prev, node.getKey(curIx), node.getRecord(curIx));
//                    curIx++;
//                }
//            }
//
//            // if node only has next node
//            else if (prev == null) {
//
//                for (int i = 0; i < node.getKeys().size(); i++)
//                    next.addRecord(node.getKey(i), node.getRecord(i));
//            }
//
//            // if node only has prev node
//            else {
//
//                for (int i = 0; i < node.getKeys().size(); i++)
//                    prev.addRecord(node.getKey(i), node.getRecord(i));
//            }
//
//            // have to copy parent to reset next deleting leafnode
//            copy = node.getParent();
//
//            // have to look for prev node if it is not from the same parent
//            if (prev == null) {
//
//                if (!copy.getIsRoot())
//                    prev = searchLeaf(copy.findSmallestKey()-1);
//            }

            // change prev to point to next
            prev.setNext(node.getNext());

            // delete node
            node.deleteNode();
            deletedCount++;
        }

        // if able to borrow keys
        else {
            for (int i = 0; i < needed; i++) {
                node.addRecord(prev.getKey(prev.getKeys().size()-1 -i), prev.getRecord(prev.getRecords().size()-1 -i));
                prev.deleteRecord(prev.getKeys().size()-1 -i);
                if (prev.getKeys().size() <= leafMinKeys)
                    break;
            }

            for (int i = prevShare, j = 0; i < needed; i++, j++) {
                node.addRecord(next.getKey(j), next.getRecord(j));
                next.deleteRecord(j);
            }
//                // take the last few keys from prev node that can be spared
//                for (int i = 0; i < needed; i++) {
//                    node.addRecord(prev.getKey(prev.getKeys().size()-1 -i), prev.getRecord(prev.getKeys().size()-1 -i));
//                    prev.deleteRecord(prev.getKeys().size()-1 -i);
//                }
//
//                // take the rest from next node
//                for (int i = prevShare, j = 0; i < needed; i++, j++) {
//
//                    node.addRecord(next.getKey(j), next.getRecord(j));
//                    next.deleteRecord(j);
//                }
//            }
//
//                // take all from next node
//                for (int i = 0; i < needed; i++) {
//
//                    node.addRecord(next.getKey(i), next.getRecord(i));
//                    next.deleteRecord(i);
//                }
//
//            else {
//
//                // take all from prev node
//                for (int i = 0; i < needed; i++) {
//
//                    node.addRecord(prev.getKey(prev.getKeys().size()-1 -i), prev.getRecord(prev.getKeys().size()-1 -i));
//                    prev.deleteRecord(prev.getKeys().size()-1 -i);
//                }
            }

        // update parents
        resetParent(parent);
    }

    public void resetParent(NonLeafNode parent) {

        // if node is root
        if (parent.getIsRoot()) {

            // if root has at least 2 children, reset and return
            if (parent.getChildren().size() > 1) {

                // lazy man's reset
                Node child = parent.getChildNode(0);
                parent.deleteChild(child);
                parent.addChild(child);
                return;
            }

            // if root has 1 child, delete root level
            else {

                parent.getChildNode(0).setIsRoot(true);
                root = parent.getChildNode(0);
                parent.deleteNode();
                System.out.println("Deleting parent...");
                deletedCount++;
                height--;
                return;
            }
        }

        NonLeafNode before = (NonLeafNode) parent.getParent().getBefore(parent);
        NonLeafNode after = (NonLeafNode) parent.getParent().getAfter(parent);
        int needed = parentMinKeys - parent.getKeys().size();
        int bSpare = 0;
        int aSpare = 0;
        NonLeafNode copy;

        if (before != null)
            bSpare += before.getKeys().size() - parentMinKeys;

        if (after != null)
            aSpare += after.getKeys().size() - parentMinKeys;

        // if need to merge
        if (needed > aSpare + bSpare) {

            // if node has both before and after nodes
            if (before != null && after != null) {

                // insert as many records as possible into before node
                for (int i = 0; i < maxKeys-(bSpare+parentMinKeys)+1 && i < parent.getChildren().size(); i++)
                    before.addChild(parent.getChildNode(i));

                // insert the rest into after node
                for (int i = maxKeys-(bSpare+parentMinKeys)+1; i < parent.getChildren().size(); i++)
                    after.addChild(parent.getChildNode(i));
            }

            // if node only has after node
            else if (before == null) {

                for (int i = 0; i < parent.getChildren().size(); i++)
                    after.addChild(parent.getChildNode(i));
            }

            // if node only has before node
            else {

                for (int i = 0; i < parent.getChildren().size(); i++)
                    before.addChild(parent.getChildNode(i));
            }

            // delete after merging
            copy = parent.getParent();
            parent.deleteNode();
            deletedCount++;
        }

        // if able to borrow keys
        else {

            if (before != null && after != null) {

                // take the last few keys from before node that can be spared
                for (int i = 0; i < bSpare && i < needed; i++) {

                    parent.addChild(before.getChildNode(before.getChildren().size()-1), 0);
                    before.deleteChild(before.getChildNode(before.getChildren().size()-1));
                }

                // take the rest from after node
                for (int i = bSpare; i < needed; i++) {

                    parent.addChild(after.getChildNode(0));
                    after.deleteChild(after.getChildNode(0));
                }
            }

            else if (before == null) {

                // take all from after node
                for (int i = 0; i < needed; i++) {

                    parent.addChild(after.getChildNode(0));
                    after.deleteChild(after.getChildNode(0));
                }
            }

            else {

                // take all from before node
                for (int i = 0; i < needed; i++) {

                    parent.addChild(before.getChildNode(before.getChildren().size()-1 -i), 0);
                    before.deleteChild(before.getChildNode(before.getChildren().size()-1 -i));
                }
            }

            copy = parent.getParent();
        }

        resetParent(copy);
    }

    public ArrayList<Address> getRecordsWithKey(int key){
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1; // access the root
        
        System.out.println("========================\n" +
                           "Searching Key in B+ Tree\n" +
                           "========================\n");
        System.out.println("Entered Root Node");
        Node curNode = root;
        NonLeafNode NonLeafNode;
        // searching for leaf node with key
        while (!curNode.getIsLeaf()){
            NonLeafNode = (NonLeafNode) curNode;
            for (int i=0; i<NonLeafNode.getKeys().size(); i++) {
                if ( key <= NonLeafNode.getKey(i)){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + ") as key{" + key +"} <= curKey{" + NonLeafNode.getKey(i) + "}\n");
                    curNode = NonLeafNode.getChildNode(i);
                    blockAccess++;
                    break;
                }
                if (i == NonLeafNode.getKeys().size()-1){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + "+1) as key{" + key +"} > curKey{" + NonLeafNode.getKey(i) + "}\n");
                    curNode = NonLeafNode.getChildNode(i+1);
                    blockAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curLeaf = (LeafNode) curNode;
        boolean done = false;
        while(!done && curLeaf!=null){
            // finding same keys within leaf node
            for (int i=0; i<curLeaf.getKeys().size(); i++){
                // found same key, add into result list
                if (curLeaf.getKey(i) == key){
                    result.add(curLeaf.getRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeaf.getKey(i) > key){
                    done = true;
                    break;
                }
            }
            if (!done){
                // trying to check sibling node has remaining records of same key
                if (curLeaf.getNext()!= null){
                    curLeaf = curLeaf.getNext();
                    blockAccess++;
                } else {
                    break;
                }
            }
        }
        System.out.println( "For Key {"+key+"} "+ result.size()+" records was found after " +blockAccess+" node access");
        return result;
    }

    public void treeStats() {

        ArrayList<Integer> rootKeys = new ArrayList<Integer>();
        ArrayList<Integer> firstKeys = new ArrayList<Integer>();
        NonLeafNode rootCopy = (NonLeafNode) root;
        Node first = rootCopy.getChildNode(0);

        for (int i = 0; i < root.getKeys().size(); i++) {

            rootKeys.add(root.getKey(i));
        }

        for (int i = 0; i < first.getKeys().size(); i++) {

            firstKeys.add(first.getKey(i));
        }

        Log.defaut("treeStats", "n = " + maxKeys + ", number of nodes = " + nodeCount + ", height = " + height);
        Log.defaut("rootContents", "root node contents = " + rootKeys);
        Log.defaut("firstContents", "first child contents = " + firstKeys);
        Log.defaut("secondContents", "second child contents = " + ((NonLeafNode) root).getChildNode(1));
    }

    public ArrayList<Address> getRecordsInRange(int min, int max){
        ArrayList<Address> result = new ArrayList<>();
        int nodeAccess = 1; // access the root
        int siblingAccess = 0;

        System.out.println("==============================\n" +
                           "Searching Key Range in B+ Tree\n" +
                           "==============================\n");
        System.out.println("Entered Root Node");
        Node curNode = root;
        NonLeafNode NonLeafNode;
        // searching for leaf node with key
        while (!curNode.getIsLeaf()){
            NonLeafNode = (NonLeafNode) curNode;
            for (int i=0; i<NonLeafNode.getKeys().size(); i++) {
                if ( min <= NonLeafNode.getKey(i)){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + ") as min{" + min +"} <= curKey{" + NonLeafNode.getKey(i) + "}\n");
                    curNode = NonLeafNode.getChildNode(i);
                    nodeAccess++;
                    break;
                }
                if (i == NonLeafNode.getKeys().size()-1){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + "+1) as min{" + min +"} > curKey{" + NonLeafNode.getKey(i) + "}\n");
                    curNode = NonLeafNode.getChildNode(i+1);
                    nodeAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curLeaf = (LeafNode) curNode;
        boolean done = false;
        while(!done && curLeaf!=null){
            // finding same keys within leaf node
            for (int i=0; i<curLeaf.getKeys().size(); i++){
                // found same key, add into result list
                if (curLeaf.getKey(i) >= min && curLeaf.getKey(i) <= max){
                    result.add(curLeaf.getRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeaf.getKey(i) > max){
                    done = true;
                    break;
                }
            }
            if (!done){
                // trying to check sibling node has remaining records of same key
                if (curLeaf.getNext()!= null){
                    curLeaf = (LeafNode) curLeaf.getNext();
                    nodeAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }
        if (siblingAccess > 0){
            System.out.println("A total of "+ siblingAccess +" sibiling node was accessed");
        }
        System.out.println( "For the range of [" +min+ "," +max+ "] after "+nodeAccess+" node access " +result.size()+ " records found to satisfy the range. \n");
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
        NonLeafNode NonLeafNode = (NonLeafNode) curNode;
        for (Node child: NonLeafNode.getChildren()) {
            logStructure(level+1, maxLevel, child);
        }
    }
}
