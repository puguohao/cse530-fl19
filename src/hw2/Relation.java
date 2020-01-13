//Guohao Pu, Sihan Cai
package hw2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;


/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		this.tuples = l;
		this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> newTuples = new ArrayList<>();
		for(Tuple tuple : tuples) {
			Field value = tuple.getField(field);
			if(value.compare(op, operand))
				newTuples.add(tuple);
		}
		
		Relation newRel = new Relation(newTuples, td);
		return newRel;
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) {
		//your code here
		//names and fields cannot be null
		if(names != null && fields != null) {
			String[] newfields = td.getAllFields();
			
			for(int i=0; i<fields.size(); i++) {
				int num = fields.get(i);
				if(names.get(i) != "") {
					newfields[num] = names.get(i);
				}
					
			}
			
			HashSet<String> set = new HashSet<>();
			boolean dup = false;
			for(int i=0; i<newfields.length; i++) {
				String fieldName = newfields[i];
				if(!set.contains(fieldName))
					set.add(fieldName);
				else 
					dup = true;
			}
			if(dup)
				throw new IllegalArgumentException();
				
			
			TupleDesc desc = new TupleDesc(td.getAllTypes(), newfields);
			ArrayList<Tuple> newTuples = tuples;
			for (Tuple tuple : newTuples) {
				tuple.setDesc(desc);
			}
			Relation rel = new Relation(tuples, desc);
			return rel;
		}
		else
			return null;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		//your code here
		Type[] types = new Type[fields.size()];
		String[] newfields = new String[fields.size()];
		
		if(fields.isEmpty()) {
			ArrayList<Tuple> newtuples = new ArrayList<>();
			TupleDesc desc = new TupleDesc(types, newfields);
			Relation rel = new Relation(newtuples, desc);
			return rel;
		}
			
		try {
			int k = 0;
			for(int i=0; i<fields.size(); i++) {
				int num = fields.get(i);
				types[k] = td.getType(num);
				newfields[k] = td.getFieldName(num);
				k++;
			}
		}
		catch(NoSuchElementException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
		
		TupleDesc desc = new TupleDesc(types, newfields);
		ArrayList<Tuple> newtuples = new ArrayList<>();
		
		try {
			for(int i=0; i<this.tuples.size(); i++) {
				Tuple newtuple = new Tuple(desc);
				Tuple oldtuple = tuples.get(i);
				for(int j=0; j<desc.numFields(); j++) {
					Field field = oldtuple.getField(fields.get(j));
					newtuple.setField(j, field);
				}
				newtuples.add(newtuple);
			}
			
		}
		catch(NoSuchElementException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
		
		Relation rel = new Relation(newtuples, desc);
		return rel;
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		int fieldSize = this.td.numFields() + other.td.numFields();
		Type[] types = new Type[fieldSize];
		String[] newfields = new String[fieldSize];
		
		//construct new tupledesc 
		int i = 0;
		for(int k=0; k<td.numFields(); k++) {
			types[i] = td.getType(k);
			newfields[i] = td.getFieldName(k);
			i++;
		}	
		for(int j=0; j<other.td.numFields(); j++) {
			types[i] = other.td.getType(j);
			newfields[i] = other.td.getFieldName(j);
			i++;
		}
		// compare one by one
		TupleDesc desc = new TupleDesc(types, newfields);
		ArrayList<Tuple> newtuples = new ArrayList<>();
		
		for(Tuple tuple1 : tuples) {
			for(Tuple tuple2 : other.tuples) {
				if(tuple1.getField(field1).compare(RelationalOperator.EQ, tuple2.getField(field2))) {
					Tuple newtuple = new Tuple(desc);
					
					for(int u=0; u<td.numFields(); u++) 
						newtuple.setField(u, tuple1.getField(u));
	
					for(int v=0; v<other.td.numFields(); v++) 
						newtuple.setField(v+td.numFields(), tuple2.getField(v));

					newtuples.add(newtuple);
				}	
			}
		}
		
		Relation rel = new Relation(newtuples, desc);
		return rel;
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator aggregator = new Aggregator(op, groupBy, td);
		for(Tuple tuple : tuples) 
			aggregator.merge(tuple);
		ArrayList<Tuple> newtuples = aggregator.getResults();
		Relation newRel = new Relation(newtuples, td);
		return newRel;
	}
	
	public TupleDesc getDesc() {
		//your code here
		return td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		StringBuffer buffer = new StringBuffer();
		buffer.append(td.toString() + "\n");
		for(int i=0; i<tuples.size(); i++)
			buffer.append(tuples.get(i).toString() + "\n");
		return buffer.toString();
	}
}
