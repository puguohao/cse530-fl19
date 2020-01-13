//created by: Guohao Pu, Sihan Cai
package hw3;


import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {
    
	private Node root;
	private int Indegree;
	private int Lfdegree;
	
    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
    	this.Indegree = pInner;
    	this.Lfdegree = pLeaf;
    	this.root = new LeafNode(pLeaf);
    	root.addParent(null);
    }
    
    public LeafNode search(Field f) {
    	//your code here
    	LeafNode LfNode = findLeafNode(root, f);

		if(LfNode.getAllFields().contains(f))
			return LfNode;
		else 
			return null;
    }
    
    private LeafNode findLeafNode(Node node, Field f) {
    	if(node.isLeafNode())
    		return (LeafNode)node;
    	else {
    		InnerNode thisNode = (InnerNode)node;
    		Node child = thisNode.getChild(f);
    		return findLeafNode(child, f);
    	}
    }
    
    public void insert(Entry e) {
    	insert(root, e);
//    	if(root.isLeafNode() && root.getNumber()>root.getDegree() || !root.isLeafNode() && root.getNumber()>root.getDegree()-1)
    	if(root.checkCapacity() == Capacity.OVERSIZE)
        	splitAndConnect(root, null);
    }
    
    private void insert(Node node, Entry e) {
    	//reach the leaf
    	if(node.isLeafNode()) {
    		LeafNode LfNode = (LeafNode) node;
    		LfNode.insert(e);
    	}
    	
    	else {
    		InnerNode InNode = (InnerNode) node;
    		Node child = InNode.getChild(e.getField());
    		//recursion
    		insert(child, e);
    		
    		//meets the split standard
//    		if(child.isLeafNode() && child.getNumber()>child.getDegree() || !child.isLeafNode() && child.getNumber()>child.getDegree()-1)
    		if(child.checkCapacity() == Capacity.OVERSIZE)
    			splitAndConnect(child, InNode);
    	}
    }
    
    private void splitAndConnect(Node child, InnerNode parent) {
    	Node lNode;
    	Node rNode;
    	Field keyToUp;
    	if(child.isLeafNode()) {
    		LeafNode node = (LeafNode) child;
    		lNode = LeafSplit(node).get(0);
    		rNode = LeafSplit(node).get(1);
    		int index = (int) Math.ceil(Lfdegree/2);
    		keyToUp = node.getAllFields().get(index);
    	}
    	else {
    		InnerNode node = (InnerNode) child;
    		lNode = InnerSplit(node).get(0);
    		rNode = InnerSplit(node).get(1);
    		keyToUp = ((InnerNode)lNode).getKeys().get(lNode.getNumber()-1);
    		((InnerNode)lNode).deleteKey(keyToUp);
    	}
    	//child is the root
    	if(parent == null) {
    		InnerNode creatParent = new InnerNode(Indegree);
    		creatParent.insertKey(keyToUp);
    		int pos = creatParent.findPosition(keyToUp);
    		creatParent.insertChild(lNode, pos);
    		creatParent.insertChild(rNode, pos+1);
    		lNode.addParent(creatParent);
    		rNode.addParent(creatParent);
    		this.root = creatParent;
    		root.addParent(null);
    	}
    	else {
    		parent.insertKey(keyToUp);
    		int pos = parent.findPosition(keyToUp);
    		parent.deleteChild(pos);
    		parent.insertChild(lNode, pos);
    		parent.insertChild(rNode, pos+1);
    		lNode.addParent(parent);
    		rNode.addParent(parent);
    	}
    }
    
    private ArrayList<LeafNode> LeafSplit(LeafNode node){
    	ArrayList<LeafNode> nodes = new ArrayList<>();
    	ArrayList<Entry> entries = node.getEntries();
    	
    	//split the older LeafNode into two new LeafNode
    	int divide = (int) Math.ceil(entries.size()/2.0);
		LeafNode lLfNode = new LeafNode(Lfdegree);
		LeafNode rLfNode = new LeafNode(Lfdegree);
    	ArrayList<Entry> lEntries = new ArrayList<Entry>(entries.subList(0, divide));
    	ArrayList<Entry> rEntries = new ArrayList<Entry>(entries.subList(divide, entries.size()));
    	
    	for(Entry entry : lEntries)
    		lLfNode.insert(entry);
    	for(Entry entry : rEntries)
    		rLfNode.insert(entry);
    	
    	nodes.add(lLfNode);
    	nodes.add(rLfNode);
    	return nodes;
    }
    
    private ArrayList<InnerNode> InnerSplit(InnerNode node){
    	ArrayList<InnerNode> nodes = new ArrayList<>();
    	
    	//get older InnerNode's all Fields and all children
		ArrayList<Field> fields = node.getKeys();
    	ArrayList<Node> children = node.getChildren();
    	
    	//split the older InnerNode into two new InnerNode
    	int divide = (int) Math.ceil(fields.size()/2.0);
		InnerNode lInNode = new InnerNode(Indegree);
		InnerNode rInNode = new InnerNode(Indegree);
    	ArrayList<Field> lfields = new ArrayList<Field>(fields.subList(0, divide));
    	ArrayList<Field> rfields = new ArrayList<Field>(fields.subList(divide, fields.size()));
    	ArrayList<Node> lchildren = new ArrayList<Node>(children.subList(0, divide));
    	ArrayList<Node> rchildren = new ArrayList<Node>(children.subList(divide, children.size()));
    	
    	for(Field field : lfields)
    		lInNode.insertKey(field);
    	for(Field field : rfields)
    		rInNode.insertKey(field);
    	for(Node child : lchildren) {
    		lInNode.getChildren().add(child);
    		child.addParent(lInNode);
    	}
    	for(Node child : rchildren) {
    		rInNode.getChildren().add(child);
    		child.addParent(rInNode);
    	}
    	
    	nodes.add(lInNode);
    	nodes.add(rInNode);
    	return nodes;
    }
    
    
    public void delete(Entry e) {
    	//your code here
    	LeafNode LfNode = search(e.getField());
    	//e is not existed in this tree
    	if(LfNode == null)
    		return;
    	//top down 
    	else {
    		delete(root, e);
    	}
    }
    
    private void delete(Node node, Entry e) {
    	//reach the LeafNode needed to delete e
    	if(node.isLeafNode()) {
    		((LeafNode)node).delete(e);
    	}
    	else {
    		Node child = ((InnerNode)node).getChild(e.getField());
    		delete(child, e);
    	}
    	
    	borrowOrMerge(node);
    }
    
    private void borrowOrMerge(Node node) {
    	//if node is root, root has no parent
    	if(node == root) {
    		if(node.isLeafNode())
    			return;
    		else {
    			if(((InnerNode)node).getChildren().size()<2)
    				root = ((InnerNode)node).getChildren().get(0);
    			return;
    		}
    	}
    	
    	//node's number meets the standard, do nothing
//    	if(node.isLeafNode() && node.getNumber() >= (int)Math.ceil(Lfdegree/2) || !node.isLeafNode() && node.getNumber() >= (int)Math.ceil(Indegree/2)-1)
    	if(node.checkCapacity() == Capacity.HALF || node.checkCapacity() == Capacity.ABOVE_HALF)
    		return;
    	//node's number is under the standard, do borrow or merge
    	else {
    		//get node's sibling node
    		Node getSibling = getLeftSibling(node);
    		int borrowPos;
    		boolean isLeftSibling;
    		//rightSibling
    		if(getSibling == null) {
    			getSibling = getRightSibling(node);
    			borrowPos = 0;
    			isLeftSibling = false;
    		}
    		//leftSibling
    		else {
    			borrowPos = getSibling.getNumber()-1;
    			isLeftSibling = true;
    		}
    			
    			
    		//LeafNode
    		if(node.isLeafNode()) {
    			LeafNode sibling = (LeafNode) getSibling;
    			LeafNode LfNode = (LeafNode) node;
    			
				//if sibling lends one, its number still meets the standard
    			//we can borrow
//    			if(sibling.getNumber() > (int)Math.ceil(Lfdegree/2)) 
    			if(sibling.checkCapacity() == Capacity.ABOVE_HALF)
    			{
    				
    				Entry borrow = sibling.getEntries().get(borrowPos);
    				sibling.delete(borrow);
    				LfNode.insert(borrow);
    				//update parent: only need to update key, not children
    				if(isLeftSibling) {
    					Field keyToUp = sibling.getAllFields().get(sibling.getNumber()-1);
    					Field oldKey = LfNode.getAllFields().get(0);
    					InnerNode parent = LfNode.getParent();
    					parent.insertKey(keyToUp);
    					parent.deleteKey(oldKey);
    				}
    				else {
    					Field keyToUp = LfNode.getAllFields().get(LfNode.getNumber()-1);
    					InnerNode parent = LfNode.getParent();
    					parent.insertKey(keyToUp);
    					parent.getKeys().remove(0);
    				}
    			}
    			//otherwise, merge those two nodes
    			else {
    				InnerNode parent = LfNode.getParent();
    				ArrayList<Entry> entries = sibling.getEntries();
    				for(Entry entry : entries)
    					LfNode.insert(entry);
    				//update parent: update key and children
    				if(isLeftSibling) {
    					Field siblingFirstField = sibling.getAllFields().get(0);
    					int deletePos = parent.findPosition(siblingFirstField);
    					parent.getKeys().remove(deletePos);
    					parent.deleteChild(deletePos);
    				}
    				else {
    					parent.getKeys().remove(0);
    					parent.deleteChild(1);
    				}
    					
    			}
    		}
    		//InnerNode
    		else {
    			InnerNode sibling = (InnerNode) getSibling;
    			InnerNode InNode = (InnerNode) node;
    			InnerNode parent = InNode.getParent();
    			
				//if sibling lends one, its number still meets the standard
    			//then put down one of its parent's keys, and put up one of its sibling's key
    			//and borrow one child from sibling
//    			if(sibling.getNumber() > (int)Math.ceil(Indegree/2) - 1) 
    			if(sibling.checkCapacity() == Capacity.ABOVE_HALF)
    			{
    				if(isLeftSibling) {
    					Field keyToUp = sibling.getKeys().get(borrowPos);
    					int keyPos = parent.findPosition(keyToUp);
    					Field keyToDown = parent.getKeys().get(keyPos);
	    				sibling.deleteKey(keyToUp);
	    				parent.insertKey(keyToUp);
	    				parent.deleteKey(keyToDown);
	    				InNode.insertKey(keyToDown);
	    				//update children
	    				int borrowChildPos = sibling.getChildren().size() - 1;
	    				Node borrowChild = sibling.getChildren().get(borrowChildPos);
	    				InNode.insertChild(borrowChild, 0);
	    				sibling.deleteChild(borrowChildPos);
	    				borrowChild.addParent(InNode);
    				}
    				else {
    					Field keyToUp = sibling.getKeys().get(borrowPos);
    					Field keyToDown = parent.getKeys().get(0);
    					sibling.deleteKey(keyToUp);
    					parent.insertKey(keyToUp);
    					parent.deleteKey(keyToDown);
    					InNode.insertKey(keyToDown);
    					//update children
    					int insertChildPos = (int)Math.ceil(Indegree/2) - 1;
    					Node borrowChild = sibling.getChildren().get(0);
    					InNode.insertChild(borrowChild, insertChildPos);
    					sibling.deleteChild(0);
    					borrowChild.addParent(InNode);
    				}
    			}
    			//otherwise, merge this node, its parent, and its sibling into one node
    			else {
    				ArrayList<Field> siblingFields = sibling.getKeys();
    				ArrayList<Node> siblingChildren = sibling.getChildren();
    				
    				if(isLeftSibling) {
    					Field siblingLastField = siblingFields.get(siblingFields.size()-1);
    					int parentDownFieldPos = parent.findPosition(siblingLastField);
    					Field keyToDown = parent.getKeys().get(parentDownFieldPos);
    					for(Field field : siblingFields)
	    					InNode.insertKey(field);
	    				InNode.insertKey(keyToDown);
	    				parent.deleteKey(keyToDown);
	    				parent.deleteChild(parentDownFieldPos);
	    				for(int i=0; i<siblingChildren.size(); i++)
	    					InNode.insertChild(siblingChildren.get(i), i);
    				}
    				else {
    					Field keyToDown = parent.getKeys().get(0);
    					for(Field field : siblingFields)
	    					InNode.insertKey(field);
	    				InNode.insertKey(keyToDown);
	    				parent.deleteKey(keyToDown);
	    				parent.deleteChild(1);
	    				int pos = InNode.getChildren().size();
	    				for(int i=0; i<siblingChildren.size(); i++)
	    					InNode.insertChild(siblingChildren.get(i), pos + i);
    				}
    			}
    		}
    		return;
    	}
    }
    	 
    		
    private Node getLeftSibling(Node node) {
    	if(node.getParent() == null)
    		return null;
    	else {
    		InnerNode parent = node.getParent();
    		ArrayList<Node> children = parent.getChildren();
    		int i = 0;
    		for(; i<children.size(); i++)
    			if(children.get(i) == node)
    				break;
    		if(i==0)
    			return null;
    		else 
    			return children.get(i-1);
    	}
    }
    
    private Node getRightSibling(Node node) {
    	if(node.getParent() == null)
    		return null;
    	else {
    		InnerNode parent = node.getParent();
    		ArrayList<Node> children = parent.getChildren();
    		int i = 0;
    		for(; i<children.size(); i++)
    			if(children.get(i) == node)
    				break;
    		if(i==children.size()-1)
    			return null;
    		else 
    			return children.get(i+1);
    	}
    }
    
    public Node getRoot() {
    	//your code here
    	if(root.getNumber()>0)
    		return root;
    	else
    		return null;
    }
}
