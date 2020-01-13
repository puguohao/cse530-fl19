//Guohao Pu, Sihan Cai
package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	private File f;
	private TupleDesc type;
	private int id;
	public static final int PAGE_SIZE = 4096;
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		//your code here
		this.f = f;
		this.type = type;
		this.id = getId();
	}
	
	public File getFile() {
		//your code here
		return this.f;
	}
	
	public TupleDesc getTupleDesc() {
		//your code here
		return this.type;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		//your code here
		int begin = PAGE_SIZE*id;
		byte[] buffer = new byte[PAGE_SIZE];
		try {
			RandomAccessFile file = new RandomAccessFile(this.f, "r");
			file.seek(begin);
			file.readFully(buffer);
			file.close();
			return new HeapPage(id, buffer, this.id);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public HeapPage getFirstAvailablePage() throws Exception {
		for(int i=0; i<getNumPages(); i++) {
			HeapPage page = readPage(i);
			if(page.haveAvailSlot()) {
				return page;
			}
		}
		HeapPage page = new HeapPage(getNumPages(), new byte[PAGE_SIZE], this.id);
		return page;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//your code here
		return this.f.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) throws IOException{
		//your code here
		byte[] data = p.getPageData();
		try {
			RandomAccessFile file = new RandomAccessFile(this.f, "rws");
			file.seek(p.getId()*PAGE_SIZE);
			file.write(data);
			file.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 * @throws Exception 
	 */
	public HeapPage addTuple(Tuple t) throws Exception {
		//your code here
		boolean find = false;
		int i = 0;
		for(; i<getNumPages(); i++) {
			HeapPage page = readPage(i);
			if(page.haveAvailSlot()) {
				page.addTuple(t);
				writePage(page);
				find = true;
				break;
			}
		}
		if(find)
			return readPage(i);
		else {
			HeapPage page = new HeapPage(getNumPages(), new byte[PAGE_SIZE], this.id);
			page.addTuple(t);
			writePage(page);
			return page;
		}
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 * @throws Exception 
	 */
	public void deleteTuple(Tuple t) throws Exception{
		//your code here
		HeapPage hp = readPage(t.getPid());
		try {
			hp.deleteTuple(t);
			this.writePage(hp);
		}
		catch(Exception e) {
			throw new Exception();
		}
			
		
		
			
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		//your code here
		ArrayList<Tuple> list = new ArrayList<>();
		for(int i = 0; i<getNumPages(); i++) {
			HeapPage page = readPage(i);
			Iterator<Tuple> itr = page.iterator();
			while(itr.hasNext()) {
				Tuple item = itr.next();
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		//your code here
		long result = this.f.length();
		return (int)Math.ceil(result/PAGE_SIZE);
	}
}
