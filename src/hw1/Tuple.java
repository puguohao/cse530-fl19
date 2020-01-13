//Guohao Pu, Sihan Cai
package hw1;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {


	private TupleDesc desc;
	private HashMap<String, Field> field = new HashMap<>();
	private int Pid;
	private int Id;
	
	/**
	 * Creates a new tuple with the given description
	 * @param t the schema for this tuple
	 */
	public Tuple(TupleDesc t) {
		//your code here
		this.desc = t;
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.desc;
	}
	
	/**
	 * retrieves the page id where this tuple is stored
	 * @return the page id of this tuple
	 */
	public int getPid() {
		//your code here
		return this.Pid;
	}

	public void setPid(int pid) {
		//your code here
		this.Pid = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		//your code here
		return this.Id;
	}

	public void setId(int id) {
		//your code here
		this.Id = id;
	}
	
	public void setDesc(TupleDesc td) {
		//your code here;
		this.desc = td;
	}
	
	/**
	 * Stores the given data at the i-th field
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		//your code here
		String fieldName = this.desc.getFieldName(i);
		this.field.put(fieldName, v);
	}
	
	public Field getField(int i) {
		//your code here
		String fieldName = this.desc.getFieldName(i);
		return this.field.get(fieldName);
	}
	
	/**
	 * Creates a string representation of this tuple that displays its contents.
	 * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
	 * the String columns to readable text).
	 */
	public String toString() {
		//your code here
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i<field.size(); i++)
			buffer.append(field.get(desc.getFieldName(i)).toString() + "\n");	
		return buffer.toString();
	}
}
	