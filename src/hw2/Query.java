//Guohao Pu, Sihan Cai
package hw2;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		
		//get catalog from database;
		Catalog catalog = Database.getCatalog();
		//get table from Query
		String tableName = ((Table)sb.getFromItem()).getName();
		//create Relation from table
		Relation relation = getRelationFromTable(tableName, catalog);
		
		//process relation
		//operation's order: JOIN, WHERE, SELECT(AGGREGATE, GROUP BY, AS)
		//1th: JOIN
		relation = joinOp(sb, relation, catalog);
		//2th: WHERE
		relation = whereOp(sb, relation);
		//3th: SELECT(AGG, GROUP BY, AS)
		relation = selectOp(sb, relation);
		
		return relation;
	}
	
	private Relation getRelationFromTable(String tableName, Catalog catalog) {
		int tableId = catalog.getTableId(tableName);
		TupleDesc desc = catalog.getTupleDesc(tableId);
		HeapFile heapfile = catalog.getDbFile(tableId);
		ArrayList<Tuple> tuples = heapfile.getAllTuples();
		
		Relation relation = new Relation(tuples, desc);
		return relation;
	}
	
	private Relation joinOp(PlainSelect sb, Relation rel, Catalog clg) {
		List<Join> joins = sb.getJoins();
		if(joins == null)
			return rel;
		else {
			Relation joinedRel = rel;
			for(Join join : joins) {
				String joinName = ((Table)join.getRightItem()).getName();
				//get JOIN On's expression, and get prior relation's fieldname and next relation's fieldname
				Expression joinOn = join.getOnExpression();
				String field1Name = ((Column)((BinaryExpression)joinOn).getLeftExpression()).getColumnName();
				String field2Name = ((Column)((BinaryExpression)joinOn).getRightExpression()).getColumnName();
				//create join's relation
				Relation joinRel = getRelationFromTable(joinName, clg);
				//get two relation's field int
				//match two fieldNames with two Relations
				int field1;
				int field2;
				try {
					field1 = joinedRel.getDesc().nameToId(field1Name);
					field2 = joinRel.getDesc().nameToId(field2Name);
				}
				catch(NoSuchElementException e) {
					field1 = joinedRel.getDesc().nameToId(field2Name);
					field2 = joinRel.getDesc().nameToId(field1Name);
				}
				//JOIN
				joinedRel = joinedRel.join(joinRel, field1, field2);
			}
			return joinedRel;
		}
	}
	
	private Relation whereOp(PlainSelect sb, Relation rel) {
		// WhereExpressionVisitor class is used to present WHERE
		Expression where = sb.getWhere();
		if(where == null)
			return rel;
		else {
			WhereExpressionVisitor whereVisitor = new WhereExpressionVisitor();
			where.accept(whereVisitor);
			//get op, fieldname, fieldvalue
			RelationalOperator op = whereVisitor.getOp();
			String fieldName = whereVisitor.getLeft();
			int fieldNumber = rel.getDesc().nameToId(fieldName);
			Field fieldValue = whereVisitor.getRight();
			//WHERE
			Relation afterWhereRel = rel.select(fieldNumber, op, fieldValue);
			
			return afterWhereRel;
		}
	}
	
	private Relation selectOp(PlainSelect sb, Relation rel) {
		List<SelectItem> items = sb.getSelectItems();
		List<Expression> groupInfo = sb.getGroupByColumnReferences();
		ArrayList<Integer> fieldIndex = new ArrayList<>();
		ArrayList<String> asName = new ArrayList<>();
		ArrayList<Integer> asNameFieldIndex = new ArrayList<>();
		Relation selectedRel;
		AggregateOperator op = null;
		boolean needGroupBy = (groupInfo!=null);
		boolean needAggregate = false;
		boolean needRename = false;
		
		if(items.isEmpty())
			return rel;
		else {
			for(SelectItem item : items) {
				ColumnVisitor columnVisitor = new ColumnVisitor();
				item.accept(columnVisitor);
				
				//1. select columns
				if(columnVisitor.getColumn().equals("*")) // select all column or not
					for(int i=0; i<rel.getDesc().numFields(); i++)
						fieldIndex.add(i);
				
				if(!columnVisitor.getColumn().equals("*")) {
					fieldIndex.add(rel.getDesc().nameToId(columnVisitor.getColumn()));
					//2. whether rename this column	
					Alias alia = ((SelectExpressionItem)item).getAlias();
					if(alia != null && alia.isUseAs()) {
						asNameFieldIndex.add(rel.getDesc().nameToId(columnVisitor.getColumn()));
						asName.add(alia.getName());
						needRename = true;
					}
				}
					
				//3. whether aggregate this column	
				if(columnVisitor.isAggregate()) {
					op = columnVisitor.getOp();
					needAggregate = true;
				}
			}
			
			//4. project rel into new rel
			selectedRel = rel.project(fieldIndex);
			
			//5. aggregate new rel
			if(needAggregate)
				selectedRel = selectedRel.aggregate(op, needGroupBy);
			
			//6. rename new rel
			if(needRename)
				selectedRel = selectedRel.rename(asNameFieldIndex, asName);
			
			return selectedRel;
		}
	}
}
