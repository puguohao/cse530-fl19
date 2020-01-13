//Guohao Pu, Sihan Cai
package test;
/******************************
   search
   	1. exist or doesn't exist
   	2. just root
   	3. search string / int
   	
   insert
   	1. no split
   	2. split leaf but not parent
   	3. split leaf and parent
   	4. split the root
   	5. only the root
   	6. duplicate
   	
   delete
   	1. not in the tree
   	2. merging
   	3. push through
   	4. borrowing from right or left sibling
   	5. collapse a level
   	6. simple case
   	7. root
*************************************/
import static org.junit.Assert.*;

import org.junit.Test;

import hw1.IntField;
import hw1.StringField;
import hw1.Type;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;

public class YourHW3Tests {

	@Test
	public void testSearchExist() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		//these values should exist
		assertTrue(bt.search(new IntField(1)) != null);

		//these values should not exist
		assertTrue(bt.search(new IntField(11)) == null);
	}
	
	@Test
	public void testSearchRoot() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));

		assertTrue(bt.getRoot().isLeafNode());

		LeafNode l = (LeafNode)bt.getRoot();
		assertTrue(l.getEntries().get(0).getField().equals(new IntField(9)));
		assertTrue(l.getEntries().get(0).getPage() == 0);
	}
	
	@Test
	public void testSearchInt() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		InnerNode l = (InnerNode)bt.getRoot();
		assertTrue(((LeafNode)l.getChildren().get(0)).getEntries().get(0).getField().getType() == Type.INT);
	}
	
	@Test
	public void testSearchString() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new StringField("A"), 0));
		bt.insert(new Entry(new StringField("O"), 0));
		bt.insert(new Entry(new StringField("H"), 0));
		bt.insert(new Entry(new StringField("I"), 0));
		bt.insert(new Entry(new StringField("V"), 0));
		bt.insert(new Entry(new StringField("R"), 0));
		bt.insert(new Entry(new StringField("J"), 0));

		InnerNode l = (InnerNode)bt.getRoot();
		assertTrue(((LeafNode)l.getChildren().get(0)).getEntries().get(0).getField().getType() == Type.STRING);
	}
	
	@Test
	public void testInsertNoSplit() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		LeafNode l = (LeafNode)((InnerNode)((InnerNode)bt.getRoot()).getChildren().get(1)).getChildren().get(0);
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 9);
		
		//after insert 8 into bt
		bt.insert(new Entry(new IntField(8), 0));
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 8);
	}
	
	@Test
	public void testInsertSplitButNoParent() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		
		InnerNode root = (InnerNode)bt.getRoot();
		assertTrue(root.getChildren().size() == 2);
		assertTrue(((IntField)root.getKeys().get(0)).getValue() == 9);
		
		//after insert 7 into bt
		bt.insert(new Entry(new IntField(7), 0));
		assertTrue(root.getChildren().size() == 3);
		assertTrue(((IntField)root.getKeys().get(0)).getValue() == 7);
		assertTrue(((IntField)root.getKeys().get(1)).getValue() == 9);
	}
	
	@Test
	public void testInsertSplitLeafAndParent() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		
		InnerNode root = (InnerNode)bt.getRoot();
		assertTrue(root.getChildren().size() == 3);
		assertTrue(((IntField)root.getKeys().get(0)).getValue() == 7);
		assertTrue(((IntField)root.getKeys().get(1)).getValue() == 9);
		LeafNode l = (LeafNode)root.getChildren().get(0);
		assertTrue(((IntField)l.getEntries().get(1).getField()).getValue() == 7);
		
		//after insert 2 into bt
		assertTrue(((IntField)root.getKeys().get(0)).getValue() == 7);
		assertTrue(root.getChildren().size() == 2);
		assertFalse(root.getChildren().get(0).isLeafNode());
	}
	
	@Test
	public void testInsertOnlyRoot() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		
		assertTrue(bt.getRoot().isLeafNode());
		
		//after insert 12
		assertFalse(bt.getRoot().isLeafNode());
	}
	
	@Test
	public void testInsertDup() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		LeafNode l = (LeafNode)((InnerNode)((InnerNode)bt.getRoot()).getChildren().get(1)).getChildren().get(0);
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 9);
		
		//after insert a duplicate 9
		bt.insert(new Entry(new IntField(9), 0));
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 9);
	}
	
	@Test
	public void testDeleteNoInTheTree() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		assertTrue(bt.search(new IntField(8)) == null);
		
		//after delete 8
		bt.delete(new Entry(new IntField(8), 0));
		assertTrue(bt.search(new IntField(8)) == null);
	}
	
	@Test
	public void testDeleteSimpleCase() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		LeafNode l = (LeafNode)((InnerNode)((InnerNode)bt.getRoot()).getChildren().get(0)).getChildren().get(2);
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 6);
		assertTrue(((IntField)l.getEntries().get(1).getField()).getValue() == 7);
		//after delete 7 from bt
		bt.delete(new Entry(new IntField(7), 0));
		assertTrue(bt.search(new IntField(7)) == null);
		assertTrue(((IntField)l.getEntries().get(0).getField()).getValue() == 6);
		assertTrue(((IntField)l.getEntries().get(1).getField()) == null);
	}
	
	@Test
	public void testDeleteBorrowFromLeft() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		
		LeafNode l1 = (LeafNode)((InnerNode)((InnerNode)bt.getRoot()).getChildren().get(0)).getChildren().get(0);
		assertTrue(((IntField)l1.getEntries().get(0).getField()).getValue() == 1);
		assertTrue(((IntField)l1.getEntries().get(1).getField()).getValue() == 2);
		
		//after delete 4 from bt
		bt.delete(new Entry(new IntField(4), 0));
		assertTrue(bt.search(new IntField(4)) == null);
		assertTrue(((IntField)l1.getEntries().get(0).getField()).getValue() == 1);
		assertTrue(((IntField)l1.getEntries().get(1).getField()) == null);
		LeafNode l2 = (LeafNode)((InnerNode)((InnerNode)bt.getRoot()).getChildren().get(0)).getChildren().get(1);
		assertTrue(((IntField)l2.getEntries().get(0).getField()).getValue() == 2);
	}
	
	@Test
	public void testDeleteRoot() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));

		assertTrue(bt.getRoot().isLeafNode());
		LeafNode root = (LeafNode)bt.getRoot();
		assertTrue(((IntField)root.getEntries().get(0).getField()).getValue() != 9);
		
		//after delete 4 from bt
		bt.delete(new Entry(new IntField(4), 0));
		assertTrue(bt.search(new IntField(4)) == null);
		assertTrue(bt.getRoot().isLeafNode());
		assertTrue(((IntField)root.getEntries().get(0).getField()).getValue() == 9);
	}
	
	@Test
	public void testDeleteMerge() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(10), 0));
		
		InnerNode inode = (InnerNode)((InnerNode)bt.getRoot()).getChildren().get(0);
		assertTrue(inode.getKeys().size() == 2);
		assertTrue(inode.getChildren().size() == 3);
		
		//after delete 2 from bt
		bt.delete(new Entry(new IntField(2), 0));
		assertTrue(bt.search(new IntField(2)) == null);
		assertTrue(inode.getKeys().size() == 1);
		assertTrue(inode.getChildren().size() == 2);
	}
	
	@Test
	public void testDeleteCollapse() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(10), 0));
		bt.delete(new Entry(new IntField(2), 0));
		
		InnerNode root = (InnerNode)bt.getRoot();
		assertFalse(root.getChildren().get(0).isLeafNode());
		
		//after delete 6 from bt
		bt.delete(new Entry(new IntField(6), 0));
		assertTrue(bt.search(new IntField(6)) == null);
		assertTrue(root.getChildren().get(0).isLeafNode());
	}
}
