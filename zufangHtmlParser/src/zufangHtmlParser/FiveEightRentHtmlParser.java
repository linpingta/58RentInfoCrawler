package zufangHtmlParser;

import java.io.IOException;
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
		aMap.put("open_time", "");
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
			aMap.put("direction", "");
		}else{
			aMap.put("direction", houseDirection);	
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
	
	public void Store(Map<String,String> data) throws IOException{
		
	}

}
