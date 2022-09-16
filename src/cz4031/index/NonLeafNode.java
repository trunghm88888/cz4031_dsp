package cz4031.index;
import cz4031.util.Log;
import cz4031.storage.Address;
import java.util.ArrayList;

//NodeLeafNode parent/root of other nodes internel or not
// n keys
// n + 1 children
public class NonLeafNode extends Node{
    private static final String TAG = "NonLeaf-Node";
    private ArrayList<Node> children;

    // constructor
    public NonLeafNode() {
        super();
        this.children = new ArrayList<Node>();
    }

    // return arraylist of all children it points to
    public ArrayList<Node> getChildren() {
        return children;
    }

    // get child at specific index
    public Node getChildNode(int index) {
        return children.get(index);
    }

    // add child
    public int addChild(Node childNode) {

        //initially no children default easy case
        if (children.size() == 0) {
            children.add(childNode);
            childNode.setParent(this);
            //index whre child-Node was added
            return 0;
        }

        //get smallest key in its child (subtree)
        int smallest_childNode_key = childNode.findSmallestKey();

        //get smallest key in itself
        int smallest_key = this.findSmallestKey();

        int index;
        if (smallest_childNode_key < smallest_key) {

            this.addKey(smallest_key);
            this.children.add(0, childNode);
            index = 0;
        }

        else {
            index = this.addKey(smallest_childNode_key);
            this.children.add(index + 1, childNode);
        }
        //set this node parent of childNode
        childNode.setParent(this);

        //return index where key inserted
        return index;
    }

    // add child at index 0 
    public void addChild(Node child, int index) {

        children.add(0, child);
        //this node parent of a child
        child.setParent(this);
        //node removes all its keys
        deleteKeys();
        
        //add keys to this node
        for (int i = 0; i < children.size(); i++) {

            if (i != 0)
                addKey(children.get(i).findSmallestKey());
        }
    }

    // preprocess parentnode for splitting
    public void splitPrep() {
        
        //node removes all its keys
        deleteKeys();
        children = new ArrayList<Node>();
    }

    // delete a child
    public void deleteChild(Node childNode) {

        children.remove(childNode);
        deleteKeys();
        
        for (int i = 0; i < children.size(); i++) {

            if (i != 0)
                addKey(children.get(i).findSmallestKey());
        }
    }

    // delete all children
    public void deleteChildren() {
        this.children = new ArrayList<Node>();
    }

    // get the child before given child Node
    public Node getBefore(Node childNode) {

        if (children.indexOf(childNode) != 0)
            return children.get(children.indexOf(childNode)-1);

        return null;
    }

    // get the child after given chold Node
    public Node getAfter(Node childNode) {

        if (children.indexOf(childNode) != children.size()-1)
            return children.get(children.indexOf(childNode)+1);

        return null;
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
            sb.append(String.format("%d:{%d}", i, getKey(i) ));
        }
        sb.append("]");
        return sb.toString();
    }
}
