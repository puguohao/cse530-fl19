package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw4.BufferPool;
import hw4.Permissions;

public class YourHW4Tests {

	private Catalog c;
	private BufferPool bp;
	private HeapFile hf;
	private TupleDesc td;
	private int tid;
	private int tid2;
	
	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		Database.reset();
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		c.loadSchema("testfiles/test2.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		
		Database.resetBufferPool(BufferPool.DEFAULT_PAGES);

		bp = Database.getBufferPool();
		
		
		tid = c.getTableId("test");
		tid2 = c.getTableId("test2");
	}
	
	@Test
	public void testLocksAfterCompletion() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
	    bp.transactionComplete(0, true);
	    assertTrue(bp.holdsLock(0, tid, 0) == false); 
	    
	    bp.getPage(1, tid, 1, Permissions.READ_ONLY);
	    bp.transactionComplete(0, false);
	    assertTrue(bp.holdsLock(1, tid, 1) == false); 
	}
	
	@Test
	public void testNumLocksPerTransaction() throws Exception {
		int tableId = tid;
		int transId = 0;
		int pageSize = hf.getNumPages();
		bp.getPage(transId, tableId, 0, Permissions.READ_ONLY);
		bp.getPage(transId, tableId, 1, Permissions.READ_ONLY);
		int count = 0;
		for(int i=0; i<pageSize; i++) {
			if(bp.holdsLock(transId, tableId, i))
				count++;
		}
	    assertTrue(count == 4); 
	}
	
	@Test
	public void testMultiReadLock() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.getPage(1, tid, 0, Permissions.READ_ONLY);

	    assertTrue(bp.holdsLock(1, tid, 0)); 
	}
	
	@Test
	public void testOneWriteLock() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		bp.getPage(1, tid, 0, Permissions.READ_ONLY);

	    assertTrue(bp.holdsLock(1, tid, 0) == false); 
	}
	
	@Test
	public void testDeadLock() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		bp.getPage(1, tid, 1, Permissions.READ_WRITE);
		bp.getPage(0, tid, 1, Permissions.READ_WRITE);
		bp.getPage(1, tid, 0, Permissions.READ_WRITE);

		if(bp.holdsLock(0, tid, 0)) {
			assertTrue(bp.holdsLock(0, tid, 1)); 
			assertTrue(bp.holdsLock(1, tid, 0) == false); 
			assertTrue(bp.holdsLock(1, tid, 1) == false); 
		}
		else {
			assertTrue(bp.holdsLock(1, tid, 0)); 
			assertTrue(bp.holdsLock(1, tid, 1)); 
			assertTrue(bp.holdsLock(0, tid, 0) == false); 
			assertTrue(bp.holdsLock(0, tid, 1) == false); 
		}
	}
	
	@Test
	public void testCommit() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		bp.getPage(0, tid, 0, Permissions.READ_WRITE); //acquire lock for the page
		bp.insertTuple(0, tid, t); //insert the tuple into the page
		bp.transactionComplete(0, true); //should flush the modified page
		
		//reset the buffer pool, get the page again, make sure data is there
		Database.resetBufferPool(BufferPool.DEFAULT_PAGES);
		HeapPage hp = bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		Iterator<Tuple> it = hp.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testAbort() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		bp.getPage(0, tid, 0, Permissions.READ_WRITE); //acquire lock for the page
		bp.insertTuple(0, tid, t); //insert the tuple into the page
		bp.transactionComplete(0, false); //should abort, discard changes
		
		//reset the buffer pool, get the page again, make sure data is there
		Database.resetBufferPool(BufferPool.DEFAULT_PAGES);
		HeapPage hp = bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		Iterator<Tuple> it = hp.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testProperEvict() throws Exception {
		for(int i = 0; i < 50; i++) {
			bp.getPage(0, tid2, i, Permissions.READ_WRITE);
			Tuple t = new Tuple(td);
			t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
			byte[] s = new byte[129];
			s[0] = 2;
			s[1] = 98;
			s[2] = 121;
			t.setPid(i);
			bp.deleteTuple(0, tid2, t);
		}
		try {
			bp.getPage(0, tid2, 50, Permissions.READ_WRITE);
		} catch (Exception e) {
			assertTrue(true);
		}
		fail("Should have thrown an exception");

	}
	
	@Test
	public void testNoEviction() throws Exception {
		for(int i = 0; i < 49; i++) {
			bp.getPage(0, tid2, i, Permissions.READ_WRITE);
			Tuple t = new Tuple(td);
			t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
			byte[] s = new byte[129];
			s[0] = 2;
			s[1] = 98;
			s[2] = 121;
			t.setPid(i);
			bp.deleteTuple(0, tid2, t);
		}
		try {
			bp.getPage(0, tid2, 49, Permissions.READ_WRITE);
		} catch (Exception e) {
			fail("Should have thrown an exception");
		}
		assertTrue(true);
	}
}
