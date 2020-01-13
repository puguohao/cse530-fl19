//created by: Guohao Pu, Sihan Cai
package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {
	
	//degree is the maximum number of its children
	private int degree;
	private InnerNode parent;
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	
	public InnerNode(int degree) {
		//your code here
		this.degree = degree;
		this.parent = null;
		this.keys = new ArrayList<Field>();
		this.children = new ArrayList<Node>();
	}
	
	public boolean isLeafNode() {
		return false;
	}
	
	public ArrayList<Field> getKeys() {
		//your code here
		return keys;
	}
	
	public ArrayList<Node> getChildren() {
		//your code here
		return children;
	}
	
	public Node getChild(Field f) {
			int pos = findPosition(f);
	    	return getChildren().get(pos);
		}
	
	public int getDegree() {
		//your code here
		return degree;
	}
	
	public int getNumber() {
		return this.keys.size();
	}
	
	public InnerNode getParent() {
		return this.parent;
	}
	
	public void insertKey(Field field) {
		int pos = findPosition(field);
		keys.add(pos, field);
	}
	
	public void insertChild(Node node, int pos) {
		children.add(pos, node);
	}
	
	public void deleteKey(Field f) {
		int pos = findPosition(f);
		this.keys.remove(pos);
	}
	
	public void deleteChild(int pos) {
		children.remove(pos);
	}
	
//	public void deleteChild(Node node) {
//		children.remove(node);
//	}
	
	public void addParent(InnerNode parent) {
		this.parent = parent;
	}
	
	
	
	public boolean haveParent() {
		return (parent != null);
	}
	
	public int findPosition(Field field) {
		if(keys.isEmpty())
			return 0;
		else {
			int i = 0;
			while(i<keys.size() && keys.get(i).compare(RelationalOperator.LT, field))
				i++;
			return i;
		}
	}
	
	public Capacity checkCapacity() {
		int halfSize = (int)Math.ceil(degree/2.0);
		int currentSize = children.size();
		
		if(currentSize < halfSize) 
			return Capacity.UNDER_HALF;
		else if(currentSize == halfSize) 
			return Capacity.HALF;
		else if(currentSize <= degree) 
			return Capacity.ABOVE_HALF;
		else 
			return Capacity.OVERSIZE;
	}

	
}