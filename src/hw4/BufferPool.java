package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    private int maxNum;
    private HashMap<String, Page> map;
    
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
    	this.maxNum = numPages;
    	this.map = new HashMap<>();
    }
    
    class Page{
    	int tableId;
    	int pid;
    	HeapPage heapPage;
    	boolean dirty = false;  //dirty or not
    	boolean write = false;  //has a XLock or not
    	HashSet<Integer> SLocks;
    	int XLock = -1;
    	
    	Page(int tableId, int pid, HeapPage page){
    		this.tableId = tableId;
    		this.pid = pid;
    		this.heapPage = page;
    		this.SLocks = new HashSet<>();
    	}
    }
    
    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {

    	String id = tableId + "|" + pid;
    	//this page exists in map
    	if(map.containsKey(id)) {
    		Page page = map.get(id);
    		if(page.write == true) {//there is a XLock on this page
    			if(page.XLock == tid)//this transaction has a XLock on this page
    				return page.heapPage;
    			else
    				return hold(tid, id, perm);
    		}
    		else {//there is no XLock on this page
    			if(perm.toString().equals("READ_ONLY")) {// transaction acquires a SLock on this page
    				page.SLocks.add(tid);
    				return page.heapPage;
    			}	
    			else {// transaction acquires a XLock on this page
    				if(page.SLocks.size() == 1 && page.SLocks.contains(tid)) {// update SLock to XLock
    					page.write = true;
    					page.SLocks.clear();
    					page.XLock = tid;
    					return page.heapPage;
    				}
    				else if(page.SLocks.size() == 0) {//don't have any Lock at all
    					page.write = true;
    					page.XLock = tid;
    					return page.heapPage;
    				}
    				else
    					return hold(tid, id, perm);
    			}
    		}
    	}
    	//add new page to bufferPool
    	else {
    		if(map.size() == maxNum) 
    			evictPage();
    		
    		HeapPage heapPage = Database.getCatalog().getDbFile(tableId).readPage(pid);
    		Page page = new Page(tableId, pid, heapPage);
    		if(perm.toString().equals("READ_ONLY"))
    			page.SLocks.add(tid);
    		else {
    			page.XLock = tid;
    			page.write = true;
    		}
    		map.put(id, page);
    		return page.heapPage;
    	}
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(int tid, int tableId, int pid) {
        // your code here
    	String id = tableId + "|" + pid;
    	if(map.containsKey(id)) {
    		Page page = map.get(id);
    		if(page.SLocks.contains(tid))
    			page.SLocks.remove(tid);
    		else if(page.XLock == tid) {
    			page.XLock = -1;
    			page.write = false;
    		}
    	}
    	else
    		return;
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public   boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
    	String id = tableId + "|" + pid;
    	if(map.containsKey(id)) {
    		Page page = map.get(id);
    		if(page.XLock == tid || page.SLocks.contains(tid))
    			return true;
    	}
    	return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
    	ArrayList<Entry<String, Page>> lists = new ArrayList<>();

		for(Entry<String, Page> entry : map.entrySet()) {
			Page page = entry.getValue();
			if(page.XLock == tid || page.SLocks.contains(tid))
    			lists.add(entry);
		}
    	
    	if(lists.isEmpty())
    		throw new IOException("no such transaction");
    	
    	if(commit == true) {
        	for(Entry<String, Page> entry : lists) {
        		Page page = entry.getValue();
        		if(page.dirty == true)
    				flushPage(page.tableId, page.pid);
    			else {
    				if(page.XLock == tid) {
    					page.XLock = -1;
    					page.write = false;
    				}
    				if(page.SLocks.contains(tid))
    					page.SLocks.remove(tid);
    			}
        	}
    	}
    	else {
    		for(Entry<String, Page> entry : lists) {
    			Page page = entry.getValue();
    			String key = entry.getKey();
        		if(page.dirty == true)
    				map.remove(key);
    			else {
    				if(page.XLock == tid) {
    					page.XLock = -1;
    					page.write = false;
    				}
	
    				if(page.SLocks.contains(tid))
    					page.SLocks.remove(tid);
    			}
        	}
    	}
    	
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {

		HeapPage heapPage = Database.getCatalog().getDbFile(tableId).getFirstAvailablePage();
		int pid = heapPage.getId();
    	
    	String id = tableId + "|" + pid;
    	if(map.containsKey(id) && map.get(id).XLock == tid) {
    		Page page = map.get(id);
			page.heapPage.addTuple(t);
	    	page.dirty = true;
    	}
    	else
    		throw new Exception("should acquire a XLock on this page first");
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	int pid = t.getPid();
    	
    	String id = tableId + "|" + pid;
    	if(map.containsKey(id) && map.get(id).XLock == tid) {
    		Page page = map.get(id);
    		page.heapPage.deleteTuple(t);
	    	page.dirty = true;
    	}
    	else
    		throw new Exception("should acquire a XLock on this page first");
    }
    
    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        // your code here
    	String id = tableId + "|" + pid;
    	if(map.containsKey(id)) {
    		Page page = map.get(id);
    		if(page.dirty == true) {
    			HeapPage heapPage = page.heapPage;
    			Database.getCatalog().getDbFile(tableId).writePage(heapPage);
    		}
    		map.remove(id);
    	}
    	else
    		throw new IOException("no such page in this bufferpool");
    }

    
    private synchronized  void evictPage() throws Exception {
        // your code here

    	for(Entry<String, Page> entry : map.entrySet()) {
    		Page page = entry.getValue();
    		String key = entry.getKey();
    		if(page.dirty == false) {
    			map.remove(key);
    			return;
    		}
    	}
    	throw new Exception("cannot evict page from this bufferpool");
    }

    private HeapPage hold(int tid, String id, Permissions perm) throws IOException {
    	boolean getLock = false;
    	long start_time = System.currentTimeMillis(); // get the current time, used for timeout
		while (getLock == false && (System.currentTimeMillis() - start_time) < 500) 
			getLock = qualify(tid, id, perm);
		if (getLock) {// get the qualification to get Lock
			Page page = map.get(id);
			if(perm.toString().equals("READ_ONLY"))
    			page.SLocks.add(tid);
    		else {
    			page.XLock = tid;
    			page.write = true;
    		}
			return page.heapPage;
		} 
		else { // didn't get the qualification to get lock before time run out
			transactionComplete(tid, false); // abort
			return null;
		}
    }

	private boolean qualify(int tid, String id, Permissions perm) {
		Page page = map.get(id);
		if(page.write == true) {
			if(page.XLock == tid)
				return true;
			else
				return false;
		}
		else {
			if(perm.toString().equals("READ_ONLY")) 
				return true;
			else {
				if(page.SLocks.size() == 1 && page.SLocks.contains(tid))
					return true;
				else
					return false;
			}
		}
	}
	
}
