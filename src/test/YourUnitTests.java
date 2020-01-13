package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.TupleDesc;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}
	
	@Test
	public void Catalog_GetDbId_test() {
		int tableId = c.getTableId("test");
		try {
			c.getDbFile(tableId);
		} catch(NoSuchElementException e) {
			fail("HeapFile not found");
		}
	}
	
	@Test
	public void Catalog_GetTupleDesc_test() {
		int tableId = c.getTableId("test");
		try {
			c.getTupleDesc(tableId);
		}
		catch(NoSuchElementException e) {
			fail("HeapFile not found");
		}
	}

}
