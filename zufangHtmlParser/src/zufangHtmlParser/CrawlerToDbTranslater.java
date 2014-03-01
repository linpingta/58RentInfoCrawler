package zufangHtmlParser;

import java.util.*;
import java.util.Map.Entry;

public class CrawlerToDbTranslater {
	private SelectTranslater selTrans = new SelectTranslater();
	private InsertIntoTranslater  insertTrans = new InsertIntoTranslater();
	private WhereTranslater whereTrans = new WhereTranslater();
	private GetIdTranslater getIdTrans = new GetIdTranslater();

	public SelectTranslater getSelTrans() {
		return selTrans;
	}

	public void setSelTrans(SelectTranslater selTrans) {
		this.selTrans = selTrans;
	}

	public InsertIntoTranslater getInsertTrans() {
		return insertTrans;
	}

	public void setInsertTrans(InsertIntoTranslater insertTrans) {
		this.insertTrans = insertTrans;
	}

	public WhereTranslater getWhereTrans() {
		return whereTrans;
	}

	public void setWhereTrans(WhereTranslater whereTrans) {
		this.whereTrans = whereTrans;
	}
	
	public String Generate(){
		return insertTrans.Generate() + " " + selTrans.Generate() + " " + whereTrans.Generate();
	}
	
	public void Clear()
	{
		insertTrans.Clear();
		whereTrans.Clear();
		selTrans.Clear();		
	}

	public GetIdTranslater getGetIdTrans() {
		return getIdTrans;
	}

	public void setGetIdTrans(GetIdTranslater getIdTrans) {
		this.getIdTrans = getIdTrans;
	}
}

class SelectTranslater{
	private List<String> name = new ArrayList<String>();
	public void Insert(String s)
	{
		name.add(s);
	}
	public String Generate()
	{
		Iterator<String> iter = name.iterator();
		String tmp = "";
		while(iter.hasNext()){
			tmp += iter.next();
			if (iter.hasNext())
				tmp += ", ";
		}		
		return "select " + tmp;
	}
	
	public void Clear()
	{
		name.clear();
	}
}

class InsertIntoTranslater{
	private String tableName = "";
	private List<String> tableColumnList = new ArrayList<String>();
	public void InsertTableName(String s)
	{
		tableName = s;
	}
	public void InsertTableColumnID(String s)
	{
		tableColumnList.add(s);
	}
	public String Generate()
	{
		String tmp = "insert into " + tableName + "(";
		Iterator<String> iter = tableColumnList.iterator();
		while (iter.hasNext()){
			tmp += iter.next();
			if (iter.hasNext())
				tmp += ",";
		}
		tmp += ")";
		return tmp;
	}
	public void Clear()
	{
		tableColumnList.clear();
	}
}

class WhereTranslater{
	private String tableName = "";
	private Map<String,String> whereMap = new HashMap<String,String>();
	public void InsertTableNaem(String s)
	{
		tableName = s;
	}
	public void InsertWhereMapElement(String ikey,String iValue)
	{
		whereMap.put(ikey, iValue);
	}
	public String Generate()
	{
		String tmp = " from (select 1) t where not exists (select * from " + tableName + " where ";
		Iterator<Entry<String, String>> iter = whereMap.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, String> e = (Map.Entry<String, String>)iter.next();
			tmp += e.getKey();
			tmp += " = ";
			tmp += e.getValue();
			if (iter.hasNext())
				tmp += " and ";
		}
		tmp += ");";
		return tmp;
	}
	public void Clear()
	{
		whereMap.clear();
	}
}

class GetIdTranslater{
	private String idName = "";
	private String tableName = "";
	private Map<String,String> whereMap = new HashMap<String,String>();
	public String getIdName() {
		return idName;
	}
	public void setIdName(String idName) {
		this.idName = idName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public void InsertWhereMapElement(String ikey,String iValue)
	{
		whereMap.put(ikey, iValue);
	}
	public String GetId()
	{
		return "@" + idName;
	}
	public String Generate()
	{
		String tmp = "set @" + idName + " = (select " + idName +
				" from " + tableName + " where ";
		Iterator<Entry<String, String>> iter = whereMap.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, String> e = (Map.Entry<String, String>)iter.next();
			tmp += e.getKey();
			tmp += " = ";
			tmp += e.getValue();
			if (iter.hasNext())
				tmp += " and ";
		}
		tmp += ");";
		return tmp;
	}
	public void Clear()
	{
		whereMap.clear();
	}
}
