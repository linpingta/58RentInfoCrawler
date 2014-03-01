package zufangHtmlParser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FiveIFiveJParser extends BasicRentHtmlParser {

	private CrawlerToDbTranslater mCrawlerToDbTranslater = new CrawlerToDbTranslater();
	
	public void Print(){
		System.out.println("Run FiveIFiveJParser");
	}
	
	public Map<String,String> Parse(String url) throws IOException{
		Map<String, String> aMap = new HashMap<String,String>();
		/*
		Document doc = Jsoup.connect(url).get();
		
		Element districtPart = doc.select("div.public").get(3);
		String districtPartStr = districtPart.text();
		String[] districtPartInfoStr = districtPartStr.split(">");
		int districtPartSize = districtPartInfoStr.length;
		for (int i = 0;i < districtPartSize;++i)
		{
			//System.out.println(districtPartInfoStr[i]);
			if (i == 1)
			{
				String tmp = districtPartInfoStr[i].substring(0,2);
				aMap.put("city", tmp);
			}
			else if (i == 2)
			{
				aMap.put("district", districtPartInfoStr[i]);
			}
			else if (i == 3)
			{
				aMap.put("local", districtPartInfoStr[i]);
			}
			else if (i == 4)
			{
				aMap.put("local_detail", districtPartInfoStr[i]);
			}
		}
		
		Elements e1 = doc.select(".xtitle");
		aMap.put("houseDetailDesc",e1.text());
		
		e1 = doc.select("div.jiageman > a");
		aMap.put("agent_name",e1.text());
		
		e1 = doc.select("div.jiageman > label");
		aMap.put("agent_tel",e1.text());
		
		e1 = doc.select("div.jiashou > ol > li > b");
		aMap.put("space_num",e1.get(1).text());
		
		e1 = doc.select("div.jiashou > ol > li > b");
		aMap.put("room_num",e1.get(0).text());
		
		aMap.put("pay_num", "0");
		aMap.put("loan_num", "0");
		
		Element e = doc.select("div.jiashou > ol > li").get(1);
		aMap.put("direction", e.text().substring(3));
		
		e = doc.select("div.jiashou > ol > li").get(3);
		String s1 = e.text();
		int index1 = s1.indexOf("/");
		int index2 = s1.indexOf("年");
		aMap.put("open_time", s1.substring(index1 + 1, index2)+"-01-01");
		
		e = doc.select("div.shoujia > ul > li").get(1);
		aMap.put("Price", e.text());
		
		e1 = doc.select("div.shoujia > ol > li");
		s1 = e1.text();
		index1 = s1.indexOf("积");
		index2 = s1.indexOf("平");
		aMap.put("size", s1.substring(index1 + 2, index2));
		
		e = doc.select("div.jiashou > ol > li").get(2);
		s1 = e.text();
		index1 = s1.indexOf("(");
		index2 = s1.indexOf(")");
		aMap.put("max_floor", s1.substring(index1 + 1, index2 - 1));
		int max_floor = Integer.parseInt(s1.substring(index1 + 1, index2 - 1));
		int cur_floor = -1;
		if (s1.contains("低") == true)
			cur_floor = (int)(Math.random() * max_floor / 3);
		else if (s1.contains("中") == true)
			cur_floor = (int)((1 + Math.random()) * max_floor / 3);
		else 
			cur_floor = (int)((2 + Math.random()) * max_floor / 3);
		aMap.put("curr_floor", String.valueOf(cur_floor));
		
		// Other info	
		aMap.put("wash_num", "0");
		aMap.put("greent_rate", "");
		aMap.put("info_source", "homelink");
		aMap.put("open_company", "");
		aMap.put("service_company", "");		
		
		Iterator<Map.Entry<String,String>> iter = aMap.entrySet().iterator();
		String itemKey = "", itemValue = "";
		int countOutput = 1;
		while (iter.hasNext()){
			Map.Entry<String, String> entry = (Map.Entry<String, String>)iter.next();
			itemKey = entry.getKey();
			itemValue = entry.getValue();
			System.out.println("Count: " + countOutput + " " + itemKey + " " + itemValue);
			++countOutput;
		}
		*/
		return aMap; 
	}
	
	public void Store(Map<String,String> aMap) throws IOException{
		try{
			Class.forName("com.mysql.jdbc.Driver");			
		} catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		
		String url = "jdbc:mysql://127.0.0.1:3306/rent_info";
		String user = "root";
		String password = "feixuluohua";
		try {
			Connection conn = DriverManager.getConnection(url,user,password);
			
			if (conn.isClosed()){
				System.out.println("Fail connection to the database");
			}
			Statement statement = conn.createStatement();
			String sql = "";
							
			InsertIntoTranslater sInsertIntoTrans = mCrawlerToDbTranslater.getInsertTrans();
			sInsertIntoTrans.InsertTableName("mlu_city");
			sInsertIntoTrans.InsertTableColumnID("CITY_NAME");
			
			SelectTranslater sSelectTrans = mCrawlerToDbTranslater.getSelTrans();
			sSelectTrans.Insert("beijing");
			
			WhereTranslater sWhereTrans = mCrawlerToDbTranslater.getWhereTrans();
			sWhereTrans.InsertTableNaem("mlu_city");
			sWhereTrans.InsertWhereMapElement("CITY_NAME", "XXX");
			
			System.out.println(mCrawlerToDbTranslater.Generate());
			mCrawlerToDbTranslater.Clear();
			
			GetIdTranslater sGetIdTranslater = mCrawlerToDbTranslater.getGetIdTrans();
			sGetIdTranslater.setTableName("mlu_city");
			sGetIdTranslater.setIdName("city_id");
			sGetIdTranslater.InsertWhereMapElement("CITY_NAME", "XXX");
			sGetIdTranslater.InsertWhereMapElement("CITY_NAME", "XXX1");
			System.out.println(sGetIdTranslater.Generate());
			String tId = sGetIdTranslater.GetId();
			System.out.println(tId);
			
			/*
			sql = "insert into mlu_city(CITY_NAME) select '" + aMap.get("city") + 
			"' from (select 1) t where not exists (select CITY_ID from mlu_city where CITY_NAME='"
			+ aMap.get("city") + "');";
			statement.execute(sql);
			
			sql = "set @city_id = (select CITY_ID from mlu_city where CITY_NAME='" + aMap.get("city") + 
			"' limit 1);";
			statement.execute(sql);
			sql = "insert into mlu_district(MLU_CITY_CITY_ID,DISTRICT_NAME) select @city_id, '" + aMap.get("district") + 
			"' from (select 1) t where not exists (select * from mlu_district where DISTRICT_NAME='"
			+ aMap.get("district") + "' and MLU_CITY_CITY_ID=@city_id);";
			statement.execute(sql);
			*/
			
			System.out.println("Insert Execution End");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
