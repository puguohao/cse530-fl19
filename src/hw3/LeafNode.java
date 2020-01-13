//created by: Guohao Pu, Sihan Cai
package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {
	
	private int degree;
	private ArrayList<Entry> entries;
	private InnerNode parent;
	
	public LeafNode(int degree) {
		//your code here
		this.degree = degree;
		this.entries = new ArrayList<Entry>();
		this.parent = null;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return entries;
	}
	
	public ArrayList<Field> getAllFields(){
		ArrayList<Field> fields = new ArrayList<Field>();
		for(int i=0; i<entries.size(); i++) {
			Field field = entries.get(i).getField();
			fields.add(field);
		}
		return fields;
	}
	
	public int getDegree() {
		//your code here
		return degree;
	}
	
	public int getNumber() {
		return entries.size();
	}
	
	public InnerNode getParent() {
		return this.parent;
	}
	
	public void insert(Entry e) {
    	Field field = e.getField();
    	int pos = findPosition(field);
	    entries.add(pos, e);

    }
	
	public void delete(Entry e) {
		Field field = e.getField();
		int pos = findPosition(field);
		entries.remove(pos);
	}
	
	private int findPosition(Field field) {
		if(entries.isEmpty())
			return 0;
		else {
			int i = 0;
			while(i<entries.size() && getAllFields().get(i).compare(RelationalOperator.LT, field))
				i++;
			return i;
		}
	}

	public void addParent(InnerNode parent) {
		this.parent = parent;
	}
	
	public Capacity checkCapacity() {
		int halfSize = (int)Math.ceil(degree/2.0);
		int currentSize = entries.size();
		
		// Four different Capacity Status
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