package cz4031.index;

import cz4031.storage.Address;

import java.util.ArrayList;
import java.util.Comparator;

public class BpTree {

    private static final int SIZE_POINTER = 8; // for 64 bits system
    private static final int SIZE_KEY = 4; // for int value
    int maxKeys;
    int nonLeafMinKeys;
    int leafMinKeys;
    Node root;
    int height;
    int nodeCount;

    public BpTree(int blockSize){
        maxKeys = (blockSize-SIZE_POINTER) / (SIZE_KEY+SIZE_POINTER); // n
        nonLeafMinKeys = maxKeys / 2;
        leafMinKeys = (maxKeys + 1) / 2;
        System.out.println("Initializing B+Tree with n = " + maxKeys + "...");
        root = new LeafNode();
        root.setIsRoot(true);
        nodeCount = 1;
        height = 1;
    }

    // to insert a record into the tree
    public void insert(int key, Address address) {
        this.insertToLeaf(this.searchLeaf(key), key, address);
    }

    // to search for the right leafnode for record insertion
    public LeafNode searchLeaf(int key) {
        if (this.root instanceof LeafNode)
            return (LeafNode) root;

        NonLeafNode parent = (NonLeafNode) root;
        ArrayList<Integer> keys;

        while (!parent.getChildNode(0).getIsLeaf()) {
            keys = parent.getKeys();
            if (key <= keys.get(0)) {
                parent = (NonLeafNode) parent.getChildNode(0);
            } else {
                int  i = 0;
                while (i < keys.size() && key >= keys.get(i))
                    i++;
                parent = (NonLeafNode) parent.getChildNode(i);
            }
        }
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
        while (i < keys.size() && key > keys.get(i))
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
        int parentSmallestKey = parent.findSmallestKey();
        NonLeafNode parent2 = new NonLeafNode();

        int i;

        if (key <= parentSmallestKey) {
            keys.add(0, parentSmallestKey);
            children.add(0, child);
        } else {
            for (i = 0; i < keys.size(); i++) {
                if (key <= keys.get(i))
                    break;
            }
            keys.add(i, key);
            children.add(i + 1, child);
        }

        //clearing old parent values
        parent.splitPrep();

        // putting the children into the two NonLeafNodes
        for (i = 0; i < nonLeafMinKeys + 1; i++)
            parent.addChild(children.get(i));

        for (i = nonLeafMinKeys + 1; i < maxKeys + 2; i++)
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
        int deletedCount = 0;

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
                    deletedCount = resetLeaf(leaf, deletedCount);
            } else break;
        }

        System.out.println("Number of nodes deleted = " + deletedCount);
        nodeCount -= deletedCount;
        treeStats();
    }

    // to update leafnode
    public int resetLeaf(LeafNode node, int deletedCount) {
        // if no need to change node, reset parent and finish
        if (node.getKeys().size() >= leafMinKeys) {
            resetParent(node.getParent(), deletedCount);
            return deletedCount;
        }

        LeafNode prev = node.getParent().getPrevLeaf(node);
        LeafNode next = node.next;
        int needed = leafMinKeys - node.getKeys().size();
        int prevShare = 0;
        int nextShare = 0;
        NonLeafNode parent = node.getParent();

        prevShare += prev.getKeys().size() - leafMinKeys;
        nextShare += next.getKeys().size() - leafMinKeys;

        // if need to merge, then just delete this node and re-insert
        if (needed > prevShare + nextShare) {
            System.out.println("Deleting leaf..." + node);
            for (int i = 0; i < node.getKeys().size(); i++) {
                insert(node.getKey(i), node.getRecord(i));
            }
            // change prev to point to next
            prev.setNext(node.getNext());
            // delete node
            node.deleteNode();
            deletedCount++;
        }
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
        }
        // update parents
        return resetParent(parent, deletedCount);
    }

    public int resetParent(NonLeafNode parent, int deletedCount) {
        if (parent.getIsRoot()) {
            if (parent.getChildren().size() > 1) {
                Node child = parent.getChildNode(0);
                parent.deleteChild(child);
                parent.addChild(child);
                return deletedCount;
            }
            else {
                parent.getChildNode(0).setIsRoot(true);
                root = parent.getChildNode(0);
                parent.deleteNode();
                System.out.println("Deleting parent...");
                deletedCount++;
                height--;
                return deletedCount;
            }
        }
        NonLeafNode prev = (NonLeafNode) parent.getParent().getPrevParent(parent);
        NonLeafNode next = (NonLeafNode) parent.getParent().getNextParent(parent);
        int needed = nonLeafMinKeys - parent.getKeys().size();
        int prevshare = 0;
        int nextshare = 0;
        NonLeafNode copy;

        if (prev != null)
            prevshare += prev.getKeys().size() - nonLeafMinKeys;
        if (next != null)
            nextshare += next.getKeys().size() - nonLeafMinKeys;

        // if need to merge
        if (needed > nextshare + prevshare) {
            if (prev != null && next != null) {
                for (int i = 0; i < maxKeys-(prevshare+nonLeafMinKeys)+1 && i < parent.getChildren().size(); i++)
                    prev.addChild(parent.getChildNode(i));

                for (int i = maxKeys-(prevshare+nonLeafMinKeys)+1; i < parent.getChildren().size(); i++)
                    next.addChild(parent.getChildNode(i));
            }
            else if (prev == null) {
                for (int i = 0; i < parent.getChildren().size(); i++)
                    next.addChild(parent.getChildNode(i));
            }
            else {
                for (int i = 0; i < parent.getChildren().size(); i++)
                    prev.addChild(parent.getChildNode(i));
            }
            // delete next merging
            copy = parent.getParent();
            parent.deleteNode();
            deletedCount++;
        }
        else {
            if (prev != null && next != null) {
                for (int i = 0; i < prevshare && i < needed; i++) {
                    parent.addChild(prev.getChildNode(prev.getChildren().size()-1), 0);
                    prev.deleteChild(prev.getChildNode(prev.getChildren().size()-1));
                    if (prev.getKeys().size() <= nonLeafMinKeys)
                        break;
                }
                for (int i = prevshare; i < needed; i++) {
                    parent.addChild(next.getChildNode(0));
                    next.deleteChild(next.getChildNode(0));
                }
            }
            else if (prev == null) {
                for (int i = 0; i < needed; i++) {
                    parent.addChild(next.getChildNode(0));
                    next.deleteChild(next.getChildNode(0));
                }
            }
            else {
                for (int i = 0; i < needed; i++) {
                    parent.addChild(prev.getChildNode(prev.getChildren().size()-1 -i), 0);
                    prev.deleteChild(prev.getChildNode(prev.getChildren().size()-1 -i));
                }
            }
            copy = parent.getParent();
        }
        return resetParent(copy, deletedCount);
    }

    public ArrayList<Address> getRecordsWithKey(int key){
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 0; // access the root
    
        System.out.println("Accessing root node");

        Node curNode = root;
        NonLeafNode nonLeafNode;
        // searching for leaf node with key
        while (!(curNode instanceof  LeafNode)){
            nonLeafNode = (NonLeafNode) curNode;
            blockAccess++;
            for (int i=0; i<nonLeafNode.getKeys().size(); i++) {
                if (key <= nonLeafNode.getKey(i)){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + ") as key{" + key +"} <= curKey{" + nonLeafNode.getKey(i) + "}\n");
                    curNode = nonLeafNode.getChildNode(i);
                    break;
                }
                if (i == nonLeafNode.getKeys().size()-1){
                    System.out.println("Current Node: " + curNode.toString());
                    System.out.println("Going to pointer (" + i + "+1) as key{" + key +"} > curKey{" + nonLeafNode.getKey(i) + "}\n");                    
                    curNode = nonLeafNode.getChildNode(i+1);
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curLeaf = (LeafNode) curNode;
        while(curLeaf != null){
            blockAccess++;
            System.out.println(curLeaf);

            for (int i=0; i < curLeaf.getKeys().size(); i++){
                // found same key, add into result list
                if (curLeaf.getKey(i) == key)
                    result.add(curLeaf.getRecord(i));
            }

            if (curLeaf.getKeys().get(curLeaf.getKeys().size() - 1) > key)
                break;

            curLeaf = curLeaf.getNext();
        }

        System.out.printf("input(%d): %d records found with %d node access\n", key, result.size(), blockAccess);
        return result;
    }

    public void treeStats() {
        NonLeafNode rootCopy = (NonLeafNode) root;
        Node first = rootCopy.getChildNode(0);

        System.out.println("n = " + maxKeys + ", Number of nodes = " + nodeCount + ", Height = " + height);
        System.out.println("Root node contents = " + root);
        System.out.println("First child contents = " + first);
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
        // next leaf node is found, find all records with same key
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
        System.out.println( "For the range of [" +min+ "," +max+ "] after "+nodeAccess+" node access " +result.size()+ " records found to satisfy the range. \n");
        return result;
    }

    // TODO for Experiment 5
    public ArrayList<Address> removeRecordsWithKey(){
        // list of address need to be return, so app can use it to delete records from disk
        return null;
    }
//
//
//
//    public void logStructure(){
//        logStructure(0, Integer.MAX_VALUE, root);
//    }
//
//    public void logStructure(int maxLevel){
//        logStructure(0, maxLevel, root);
//    }
//
//    // recursive logging of tree structure
//    private void logStructure(int level, int maxLevel,  Node curNode){
//        if (curNode == null){
//            curNode = root;
//        }
//        if (level > maxLevel){
//            return;
//        }
//
//        System.out.print("h="+level+"; ");
//        curNode.logStructure();
//        if (curNode.getIsLeaf()){
//            return;
//        }
//        NonLeafNode NonLeafNode = (NonLeafNode) curNode;
//        for (Node child: NonLeafNode.getChildren()) {
//            logStructure(level+1, maxLevel, child);
//        }
//    }
}
