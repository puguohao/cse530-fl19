//Guohao Pu, Sihan Cai
package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import hw1.Field;
import hw1.IntField;
import hw1.StringField;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {
	
	private AggregateOperator op;
	private boolean groupBy;
	private TupleDesc desc;
	private Field defaultName;
	private HashMap<Field, ArrayList<Field>> groups;
//	private ArrayList<Field> values = new ArrayList<>();
//	private ArrayList<Tuple> tuples = new ArrayList<>();
	
	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		//your code here
		this.op = o;
		this.groupBy = groupBy;
		this.desc = td;
		this.defaultName = new StringField("default");
		this.groups = new HashMap<>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		//your code here
		//if groupBy is True, the first column will be groupName, the second column will be values to be aggregated.
		//if groupBy is False, the first column will be values to be aggregated, and there is no second column.
		Field name;
		Field value;
		if(groupBy) {
			name = t.getField(0);
			value = t.getField(1);
		}
		else {
			name = defaultName;
			value = t.getField(0);
		}
		if(groups.containsKey(name)) //groups contains this groupby name
			groups.get(name).add(value);
		else {
			ArrayList<Field> newGroupValues = new ArrayList<>();
			groups.put(name, newGroupValues);
			groups.get(name).add(value);
		}
	}
	
	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		// your code here
		ArrayList<Tuple> resultTuples = new ArrayList<>();
		ArrayList<Field> fields;
		
		//iterate all groups
		for (Field key : groups.keySet()) {
			fields = groups.get(key);
			// Check Field type
			//if Type is IntField, we can apply all the AggregateOperations
			if (fields.get(0).getClass() == IntField.class) {
				// initialize the first value
				IntField init = (IntField)(fields.get(0));
				int count = 0;
				//for each group, iterate all fields
				for(Field field: fields) {
					switch(op) {
						case MAX:
							init = (IntField)(init.compare(RelationalOperator.GT, field)? init : field);
							break;
						case MIN:
							init = (IntField)(init.compare(RelationalOperator.LT, field)? init : field);
							break;
						case COUNT:
							count++;
							break;
						case SUM:
							count += ((IntField)field).getValue();
							break;
						case AVG:
							count += ((IntField)field).getValue();
							break;
					}
				}
				
				Field newField;
				if(op == AggregateOperator.AVG) 
					count /= fields.size();
				if(op == AggregateOperator.AVG || op == AggregateOperator.SUM || op == AggregateOperator.COUNT)	
					newField = new IntField(count);
				else
					newField = init;
				
				//create a newTuple
				Tuple newTuple = new Tuple(desc);
				if(groupBy) {
					newTuple.setField(0, key);
					newTuple.setField(1, newField);
				}
				else 
					newTuple.setField(0, newField);
	
				resultTuples.add(newTuple);
			} 
			
			//if Type is StringField, SUM and AVG cannot be applied
			else {
				//initialize the first value
				StringField init = (StringField)(fields.get(0));
				int count = 0;
				
				//for each group, iterate all the fields
				for(Field field: fields) {
					switch(op) {
						case MAX:
							init = (StringField)(init.compare(RelationalOperator.GT, field)? init : field);
							break;
						case MIN:
							init = (StringField)(init.compare(RelationalOperator.LT, field)? init : field);
							break;
						case COUNT:
							count++;
							break;
						case SUM:
							System.out.printf("String Type cannot apply SUM operation");
							break;
						case AVG:
							System.out.printf("String Type cannot apply AVG operation");
							break;
					}
				}
				
				//create a newTuple
				Field newField;
				Tuple newTuple = new Tuple(desc);
				
				if(op == AggregateOperator.COUNT)
					newField = new IntField(count);
				else
					newField = new StringField(init.getValue());
				
				if(groupBy) {
					newTuple.setField(0, key);
					newTuple.setField(1, newField);
				}
				else
					newTuple.setField(0, newField);

				resultTuples.add(newTuple);
			}
			
		}
		return resultTuples;
	}
	
//	public ArrayList<Tuple> getResults() {
//		//your code here
//		switch(op) {
//		case COUNT:
//			if(groupBy) {
//				for(Field key : groups.keySet()) {
//					int size = groups.get(key).size();
//					IntField count = new IntField(size);
//					Tuple tuple = new Tuple(desc);
//					tuple.setField(0, key);
//					tuple.setField(1, count);
//					tuples.add(tuple);
//				}
//			}
//			else {
//				int size = values.size();
//				IntField count = new IntField(size);
//				Tuple tuple = new Tuple(desc);
//				tuple.setField(0, count);
//				tuples.add(tuple);
//			}
//			
//		case MAX:
//			if(groupBy) {
//				for(Field key : groups.keySet()) {
//					Field max = null;
//					for(Field value : groups.get(key)) 
//						max = value.compare(RelationalOperator.GT, max)? value : max;
//					Tuple tuple = new Tuple(desc);
//					tuple.setField(0, key);
//					tuple.setField(1, max);
//					tuples.add(tuple);
//				}
//			}
//			else {
//				Field max = null;
//				for(Field value : values) 
//					max = value.compare(RelationalOperator.GT, max)? value : max;
//				Tuple tuple = new Tuple(desc);
//				tuple.setField(0, max);
//				tuples.add(tuple);
//			}
//			
//		case MIN:
//			if(groupBy) {
//				for(Field key : groups.keySet()) {
//					Field min = null;
//					for(Field value : groups.get(key)) 
//						min = value.compare(RelationalOperator.LT, min)? value : min;
//					Tuple tuple = new Tuple(desc);
//					tuple.setField(0, key);
//					tuple.setField(1, min);
//					tuples.add(tuple);
//				}
//			}
//			else {
//				Field min = null;
//				for(Field value : values) 
//					min = value.compare(RelationalOperator.LT, min)? value : min;
//				Tuple tuple = new Tuple(desc);
//				tuple.setField(0, min);
//				tuples.add(tuple);
//			}
//			
//		case SUM:
//			if(groupBy) {
//				for(Field key : groups.keySet()) {
//					int sum = 0;
//					for(Field value : groups.get(key)) 
//						sum += ((IntField)value).getValue();
//					IntField groupSum = new IntField(sum);
//					Tuple tuple = new Tuple(desc);
//					tuple.setField(0, key);
//					tuple.setField(1, groupSum);
//					tuples.add(tuple);
//				}
//			}
//			else {
//				int sum = 0;
//				for(Field value : values) 
//					sum += ((IntField)value).getValue();
//				IntField groupSum = new IntField(sum);
//				Tuple tuple = new Tuple(desc);
//				tuple.setField(0, groupSum);
//				tuples.add(tuple);
//			}
//			
//		case AVG:
//			if(groupBy) {
//				for(Field key : groups.keySet()) {
//					int sum = 0;
//					for(Field value : groups.get(key)) 
//						sum += ((IntField)value).getValue();
//					IntField avg = new IntField(sum / groups.get(key).size());
//					Tuple tuple = new Tuple(desc);
//					tuple.setField(0, key);
//					tuple.setField(1, avg);
//					tuples.add(tuple);
//				}
//			}
//			else {
//				int sum = 0;
//				for(Field value : values) 
//					sum += ((IntField)value).getValue();
//				IntField avg = new IntField(sum / values.size());
//				Tuple tuple = new Tuple(desc);
//				tuple.setField(0, avg);
//				tuples.add(tuple);
//			}
//		}
//		
//		return tuples;
//	}

}
