package zufangHtmlParser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FiveEightRentHtmlParser extends BasicRentHtmlParser {

	private HashMap<String,Integer> numberMapping = new HashMap<String,Integer>();
	
	public FiveEightRentHtmlParser() {
		// TODO Auto-generated constructor stub4
		numberMapping.put("一", 1); numberMapping.put("二", 2); numberMapping.put("三", 3);
		numberMapping.put("四", 4); numberMapping.put("五", 5); numberMapping.put("六", 6);
		numberMapping.put("七", 7); numberMapping.put("八", 8); numberMapping.put("九", 9);
		numberMapping.put("零", 0); 
	}
	
	public void Print(){
		System.out.println("Run FiveEightRentHtmlParser");
	}
	
	public Map<String,String> Parse(String url) throws IOException{
		Map<String, String> aMap = new HashMap<String,String>();
		
		Document doc = Jsoup.connect(url).get();
		
		// region info
		String cityName = "",districtName = "",regionName = "";
		Element city = doc.select("div.breadCrumb").first();		
		Elements cityDetailInfos = city.select("span.crb_i");		
		int count = 0;
		for (Element e : cityDetailInfos)
		{
			if (count == 0)
				cityName = e.text().replaceAll("租房", "");
			else if (count == 1)
				districtName = e.text().replaceAll("租房", "");
			else if (count == 2)
				regionName = e.text().replaceAll("租房", "");
			++count;
		}		
		System.out.println(cityName + ":" + districtName + ":" + regionName);
		aMap.put("city", cityName);
		aMap.put("district", districtName);
		aMap.put("local", regionName);
		
		Element house = doc.select("div.detailPrimary").first();
		// price info		
		int price = Integer.parseInt(house.select("div.su_con").first().select("span.bigpri").text());
		aMap.put("Price", String.valueOf(price));
		String payType = house.select("div.su_con").first().text();
		Pattern p = Pattern.compile("押.付.");
		Matcher m = p.matcher(payType);
		if (m.find()){
			Integer tmpResult = numberMapping.get(m.group().substring(1,2));			
			aMap.put("loan_num", String.valueOf(tmpResult));
			tmpResult = numberMapping.get(m.group().substring(3,4));
			aMap.put("pay_num", String.valueOf(tmpResult));
		} else{
			aMap.put("loan_num", "");
			aMap.put("pay_num", "");
		}
		
		//System.out.println(payType);
		// houseName info		
		String houseName = house.select("div.bigtitle > h1").text();
		aMap.put("houseName", houseName);		
		// size info
		String houseSize = house.select("div.su_con").get(1).text();
		Pattern pHouseSize = Pattern.compile(".室");
		Matcher mHouseSize = pHouseSize.matcher(houseSize);
		
		String aRoomNum = "", aSpaceNum = "", aWashNum = "";
		if (mHouseSize.find()){
			String tmpRoomNum = mHouseSize.group().substring(0,1);
			aMap.put("room_num",tmpRoomNum);
			aRoomNum = mHouseSize.group();
		}else{
			aMap.put("room_num","");
		}
		if (!aRoomNum.isEmpty()){
			int index = houseSize.indexOf(aRoomNum);
			houseSize = houseSize.substring(index + aRoomNum.length());
		}
		
		pHouseSize = Pattern.compile(".厅");
		mHouseSize = pHouseSize.matcher(houseSize);
		if (mHouseSize.find()){
			String tmpSpaceNum = mHouseSize.group().substring(0,1);
			aMap.put("space_num",tmpSpaceNum);
			aSpaceNum = mHouseSize.group();
		}else{
			aMap.put("space_num","");
		}
		if (!aSpaceNum.isEmpty()){
			int index = houseSize.indexOf(aSpaceNum);
			houseSize = houseSize.substring(index + aSpaceNum.length());
		}
		
		pHouseSize = Pattern.compile(".卫");
		mHouseSize = pHouseSize.matcher(houseSize);
		if (mHouseSize.find()){
			String tmpWashSize = mHouseSize.group().substring(0,1);
			aMap.put("wash_num",tmpWashSize);
			aWashNum = mHouseSize.group();			
		}else{
			aMap.put("size","");
			System.out.println("here");
		}
		if (!aWashNum.isEmpty()){
			int index = houseSize.indexOf(aWashNum);
			houseSize = houseSize.substring(index + aWashNum.length());
		}
		System.out.println(houseSize);
		
		pHouseSize = Pattern.compile("[0-9]+");
		mHouseSize = pHouseSize.matcher(houseSize);
		if (mHouseSize.find()){
			String tmpSize = mHouseSize.group();			
			aMap.put("size",tmpSize);			
			System.out.println(tmpSize);
		}else{
			aMap.put("size","");
			System.out.println("here");
		}
		
		// local detail info
		String houseDetail = house.select("a[href^=http://bj.58.com/xiaoqu]").first().text();		
		aMap.put("local_detail", houseDetail);
		aMap.put("greent_rate", "");
		aMap.put("open_time", "0000-00-00");
		// direction info
		String houseDirection = house.select("div.su_con").get(2).text();
		ArrayList<String> directionList = new ArrayList<String>();
		directionList.add("南北");
		directionList.add("东西");
		directionList.add("南");
		directionList.add("北");
		directionList.add("东");
		directionList.add("西");
		boolean bHouseDirectionFlag = false;
		for (String dirWord : directionList){
			if (houseDirection.contains(dirWord)){
				houseDirection = dirWord;
				bHouseDirectionFlag = true;
				break;
			}			
		}
		if (bHouseDirectionFlag){
			aMap.put("direction", houseDirection);
		}else{
			aMap.put("direction", "");	
		}		
		// floor info
		String tmpFloorInfo = house.select("div.su_con").get(3).text().replaceAll("层","");
		int index = -1, floor = -1, maxFloor = -1;
		index = tmpFloorInfo.indexOf("/");
		if (index != -1){
			floor = Integer.parseInt(tmpFloorInfo.substring(0, index));
			maxFloor = Integer.parseInt(tmpFloorInfo.substring(index + 1, tmpFloorInfo.length()));
		}
		aMap.put("curr_floor", String.valueOf(floor));
		aMap.put("max_floor", String.valueOf(maxFloor));
		
		// Agent info
		Elements agentInfoList = doc.select("script");
		int start = 11; int end = 12;
		for (int i = start;i < end;++i){
			Element agentInfo = doc.select("script").get(i);
			//System.out.println(agentInfo);	
		}
		aMap.put("agent_name", "");
		aMap.put("agent_tel", "");
		
		// Other info
		aMap.put("info_source", "58");
		aMap.put("open_company", "");
		aMap.put("service_company", "");
		
//		// Store all info
//		String allInfo = "'" + houseName + "'," + price + ",'" + houseSize + 
//		"'," + floor + "," + maxFloor + ",'" + cityName + "','" + districtName + "','" + regionName + "'";
//		System.out.println(allInfo);
		
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
			
			sql = "insert into house_rent_info(MLU_ROOM_TYPE_ROOM_TYPE_ID,MLU_STAIR_TYPE_STAIR_TYPE_ID,MLU_INFO_SOURCE_INFO_SOURCE_ID,MLU_PAY_TYPE_PAY_TYPE_ID,MLU_DETAIL_LOCAL_DETAIL_LOCAL_ID,HOUSE_RENT_INFO_NAME,DIRECTION_TYPE,RENT_SIZE,RENT_MONEY) values(@room_type_id,@stair_type_id,@info_source_id,@pay_type_id,@local_detail_id,'"
				+ aMap.get("local_detail") + "','" + aMap.get("direction") + "'," + aMap.get("size") + "," + aMap.get("Price") + ");";
			statement.execute(sql);		
			
			System.out.println("Insert Execution End");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
