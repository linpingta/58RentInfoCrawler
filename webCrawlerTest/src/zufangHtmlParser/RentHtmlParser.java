package zufangHtmlParser;

import java.io.IOException;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.*;
import org.jsoup.select.Elements;
import org.jsoup.nodes.*;

public class RentHtmlParser {
	public void Print(){
		System.out.println("Hello RentHtmlParser");
	}
	
	public String Parse(String url) throws IOException{
		Document doc = Jsoup.connect(url).get();
		System.out.println(doc.title());
		
		// get region info about house
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
		
		// get price info about house							
		Element house = doc.select("div.detailPrimary").first();
		String houseName = house.select("div.bigtitle > h1").text();
		int price = Integer.parseInt(house.select("div.su_con").first().select("span.bigpri").text());
		String houseSize = house.select("div.su_con").get(1).text();
		String tmpFloorInfo = house.select("div.su_con").get(3).text().replaceAll("层","");
		int index = -1, floor = -1, maxFloor = -1;
		index = tmpFloorInfo.indexOf("/");
		if (index != -1){
			floor = Integer.parseInt(tmpFloorInfo.substring(0, index));
			maxFloor = Integer.parseInt(tmpFloorInfo.substring(index + 1, tmpFloorInfo.length()));
		}
		String allInfo = "'" + houseName + "'," + price + ",'" + houseSize + 
		"'," + floor + "," + maxFloor + ",'" + cityName + "','" + districtName + "','" + regionName + "'";
		System.out.println(allInfo);
		
		return allInfo; 
	}
	
	public void Store(String data) throws IOException{
		try{
			Class.forName("com.mysql.jdbc.Driver");			
		} catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		
		String url = "jdbc:mysql://127.0.0.1:3306/linpingta";
		String user = "root";
		String password = "feixuluohua";
		try {
			Connection conn = DriverManager.getConnection(url,user,password);
			
			if (conn.isClosed()){
				System.out.println("Fail connection to the database");
			}
			Statement statement = conn.createStatement();
			String tableName = "houseData";
			System.out.println(data);
			String sql = "insert into " + tableName + " values (" + data + ");";
			//String sql = "select * from houseData";
			//ResultSet rs = statement.executeQuery(sql);
			statement.execute(sql);
			
			System.out.println("Insert Execution End");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
