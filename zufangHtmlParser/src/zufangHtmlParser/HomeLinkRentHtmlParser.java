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

public class HomeLinkRentHtmlParser extends BasicRentHtmlParser {

	public void Print(){
		System.out.println("Run HomeLinkRentHtmlParser");
	}
	
	public Map<String,String> Parse(String url) throws IOException{
		Map<String, String> aMap = new HashMap<String,String>();
		
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
				String tmp = districtPartInfoStr[i].replace("租房", "");
				aMap.put("district", tmp);
			}
			else if (i == 3)
			{
				String tmp = districtPartInfoStr[i].replaceAll("租房", "");
				aMap.put("local", tmp);
			}
			else if (i == 4)
			{
				String tmp = districtPartInfoStr[i].replaceAll("租房", "");
				aMap.put("local_detail", tmp);
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
			//statement.execute(sql);
			
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
			
			sql = "set @district_id = (select DISTRICT_ID from mlu_district where DISTRICT_NAME='" + aMap.get("district") + 
			"' limit 1);";
			statement.execute(sql);
			sql = "insert into mlu_local(MLU_DISTRICT_DISTRICT_ID,LOCAL_NAME) select @district_id, '" + aMap.get("local") + 
			"'  from (select 1) t where not exists (select * from mlu_local where LOCAL_NAME='"
			+ aMap.get("local") + "' and MLU_DISTRICT_DISTRICT_ID=@district_id);";
			statement.execute(sql);
			
			sql = "insert into mlu_open_company(OPEN_COMPANY_NAME) select '" + aMap.get("open_company") + 
			"' from (select 1) t where not exists (select OPEN_COMPANY_ID from mlu_open_company where OPEN_COMPANY_NAME='"
			+ aMap.get("open_company") + "');";
			statement.execute(sql);
			
			sql = "insert into lu_service_company(SERVICE_COMPANY_NAME) select '" + aMap.get("service_company") + 
			"' from (select 1) t where not exists (select SERVICE_COMPANY_ID from lu_service_company where SERVICE_COMPANY_NAME='"
			+ aMap.get("service_company") + "');";
			statement.execute(sql);
			
			sql = "insert into mlu_agent(AGENT_NAME,AGENT_TEL) select '" + aMap.get("agent_name") + "','" + aMap.get("agent_tel") +
			"' from (select 1) t where not exists (select AGENT_ID from mlu_agent where AGENT_NAME='"
			+ aMap.get("agent_name") + "' and AGENT_TEL='" + aMap.get("agent_tel") + "');";
			statement.execute(sql);
			
			sql = "insert into mlu_room_type(ROOM_SPACE_NUM,ROOM_ROOM_NUM) select " + aMap.get("space_num") + "," + aMap.get("room_num") +
			" from (select 1) t where not exists (select ROOM_TYPE_ID from mlu_room_type where ROOM_SPACE_NUM="
			+ aMap.get("space_num") + " and ROOM_ROOM_NUM=" + aMap.get("room_num") + ");";
			statement.execute(sql);
						
			sql = "insert into mlu_stair_type(CUR_STAIR_NUM,MOST_STAIR_NUM) select " + aMap.get("curr_floor") + "," + aMap.get("max_floor") +
			" from (select 1) t where not exists (select STAIR_TYPE_ID from mlu_stair_type where CUR_STAIR_NUM="
			+ aMap.get("curr_floor") + " and MOST_STAIR_NUM=" + aMap.get("max_floor") + ");";
			statement.execute(sql);
			
			sql = "insert into mlu_info_source(INFO_SOURCE_NAME) select '" + aMap.get("info_source") + 
			"' from (select 1) t where not exists (select INFO_SOURCE_ID from mlu_info_source where INFO_SOURCE_NAME='"
			+ aMap.get("info_source") + "');";
			statement.execute(sql);
			
			sql = "insert into mlu_pay_type(PAY_NUM,LOAN_NUM) select " + aMap.get("pay_num") + "," + aMap.get("loan_num") +
			" from (select 1) t where not exists (select PAY_TYPE_ID from mlu_pay_type where PAY_NUM="
			+ aMap.get("pay_num") + " and LOAN_NUM=" + aMap.get("loan_num") + ");";
			statement.execute(sql);
			
			sql = "set @local_id = (select LOCAL_ID from mlu_local where LOCAL_NAME='" + aMap.get("local") + 
			"' limit 1);";
			statement.execute(sql);
			sql = "set @open_company_id = (select OPEN_COMPANY_ID from mlu_open_company where OPEN_COMPANY_NAME='" + aMap.get("open_company") + 
			"' limit 1);";
			statement.execute(sql);
			sql = "set @service_company_id = (select SERVICE_COMPANY_ID from lu_service_company where SERVICE_COMPANY_NAME='" + aMap.get("service_company") + 
			"' limit 1);";
			statement.execute(sql);
			
			String tmp_green_rate = "";
			if (aMap.get("greent_rate") == "")
				tmp_green_rate = "0";
			else
				tmp_green_rate = aMap.get("greent_rate");
			sql = "	insert into mlu_detail_local(MLU_LOCAL_LOCAL_ID,MLU_OPEN_COMPANY_OPEN_COMPANY_ID,LU_SERVICE_COMPANY_SERVICE_COMPANY_ID,DETAIL_LOCAL_NAME,GREEN_RATE,OPEN_TIME) select @local_id,@open_company_id,@service_company_id, '" + aMap.get("local_detail") + 
			"','" + tmp_green_rate + "','" + aMap.get("open_time")  + "' from (select 1) t where not exists (select * from mlu_detail_local where MLU_LOCAL_LOCAL_ID=@local_id and MLU_OPEN_COMPANY_OPEN_COMPANY_ID=@open_company_id and LU_SERVICE_COMPANY_SERVICE_COMPANY_ID=@service_company_id and DETAIL_LOCAL_NAME='"
			 + aMap.get("local_detail") + "');";
			statement.execute(sql);
			
			sql = "set @room_type_id = (select ROOM_TYPE_ID from mlu_room_type where ROOM_SPACE_NUM=" + aMap.get("space_num") + " and ROOM_ROOM_NUM=" + aMap.get("room_num") +
			" limit 1);";
			statement.execute(sql);
			sql = "set @stair_type_id = (select STAIR_TYPE_ID from mlu_stair_type where CUR_STAIR_NUM=" + aMap.get("curr_floor") + " and MOST_STAIR_NUM=" + aMap.get("max_floor") +
			" limit 1);";
			statement.execute(sql);
			sql = "set @pay_type_id = (select PAY_TYPE_ID from mlu_pay_type where PAY_NUM=" + aMap.get("pay_num") + " and LOAN_NUM=" + aMap.get("loan_num") +  
			" limit 1);";
			statement.execute(sql);
			sql = "set @info_source_id = (select INFO_SOURCE_ID from mlu_info_source where INFO_SOURCE_NAME='" + aMap.get("info_source") + 
			"' limit 1);";
			statement.execute(sql);
			sql = "set @local_detail_id = (select DETAIL_LOCAL_ID from mlu_detail_local where DETAIL_LOCAL_NAME='" + aMap.get("local_detail") + "' and MLU_LOCAL_LOCAL_ID=@local_id and MLU_OPEN_COMPANY_OPEN_COMPANY_ID=@open_company_id and LU_SERVICE_COMPANY_SERVICE_COMPANY_ID=@service_company_id);";
			statement.execute(sql);
			
			sql = "insert into house_rent_info(MLU_ROOM_TYPE_ROOM_TYPE_ID,MLU_STAIR_TYPE_STAIR_TYPE_ID,MLU_INFO_SOURCE_INFO_SOURCE_ID,MLU_PAY_TYPE_PAY_TYPE_ID,MLU_DETAIL_LOCAL_DETAIL_LOCAL_ID,HOUSE_RENT_INFO_NAME,DIRECTION_TYPE,RENT_SIZE,RENT_MONEY,HOUSE_DETAIL_DESC,CRAWLER_TIME) values(@room_type_id,@stair_type_id,@info_source_id,@pay_type_id,@local_detail_id,'"
				+ aMap.get("local_detail") + "','" + aMap.get("direction") + "'," + aMap.get("size") + "," + aMap.get("Price") + ",'" + aMap.get("houseDetailDesc") + "',curdate());";
			statement.execute(sql);		
			
			System.out.println("Insert Execution End");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
