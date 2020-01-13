//Guohao Pu, Sihan Cai
package hw1;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc {

	private Type[] types;
	private String[] fields;
	
    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr array specifying the number of and types of fields in
     *        this TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
    	this.types = typeAr;
    	this.fields = fieldAr;
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        //your code here
    	return this.fields.length;	
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        //your code here
    	if(i>=0 && i<fields.length)
    		return this.fields[i];
    	else
    		throw new NoSuchElementException();
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int nameToId(String name) throws NoSuchElementException {
        //your code here
		for(int i = 0; i<fields.length; i++) {
			if(fields[i].equals(name))
			return i;
		}
		throw new NoSuchElementException();
    	
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getType(int i) throws NoSuchElementException {
        //your code here
    	if(i>=0 && i<types.length)
    		return this.types[i];
    	else
    		throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
    	//your code here
    	int size = 0;
    	for(int i = 0; i<this.types.length; i++) 
    		size += this.types[i]==Type.INT? 4 : 129; 
    	
    	return size;
    }

    /**
     * Compares the specified object with this TupleDesc for equality.
     * Two TupleDescs are considered equal if they are the same size and if the
     * n-th type in this TupleDesc is equal to the n-th type in td.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(TupleDesc o) {
    	//your code here
//    	boolean flag = true;
//    	for(int i = 0; i<this.types.length; i++) {
//    		if(o.getType(i)!=this.getType(i)) {
//    			flag = false;
//    			break;
//    		}
//    	}
    	return Arrays.equals(o.types, this.types);
    }
    

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
    	return this.fields.hashCode();
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * @return String describing this descriptor.
     */
    public String toString() {
        //your code here
    	StringBuffer buffer = new StringBuffer();
    	for(int i = 0; i<this.types.length; i++) 
    		buffer.append(this.types[i] + "(").append(this.fields[i] + ")" + "\n");
    	return buffer.toString();
    }

	public Type[] getAllTypes() {
		// TODO Auto-generated method stub
		return types;
	}
	
	public String[] getAllFields() {
		return fields;
	}
}
