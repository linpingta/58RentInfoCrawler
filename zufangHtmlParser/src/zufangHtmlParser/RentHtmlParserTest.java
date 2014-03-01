package zufangHtmlParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;

public class RentHtmlParserTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//BasicRentHtmlParser p = new FiveIFiveJParser();
		BasicRentHtmlParser p = new HomeLinkRentHtmlParser();
		p.Print();
		ArrayList<String> urlList = new ArrayList<String>();
		//urlList.add("http://beijing.homelink.com.cn/zufang/BJHD86624833.shtml");
		urlList.add("http://beijing.homelink.com.cn/zufang/BJCY87025842.shtml");
		for (String url : urlList){
			Map parseResult = p.Parse(url);
			p.Store(parseResult);	
		}
		System.out.println("End");
	}

}
