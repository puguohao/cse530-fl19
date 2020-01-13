//created by: Guohao Pu, Sihan Cai
package hw3;

public interface Node {
	
	public int getDegree();
	public boolean isLeafNode();
	public int getNumber();
	public InnerNode getParent();
	public void addParent(InnerNode node);
	public Capacity checkCapacity();
}
