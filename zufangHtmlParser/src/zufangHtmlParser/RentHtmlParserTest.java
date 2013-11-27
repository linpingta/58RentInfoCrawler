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
		//System.out.println("Hello Rent");
		BasicRentHtmlParser p = new FiveEightRentHtmlParser();
		p.Print();
		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add("http://bj.58.com/zufang/15993457673606x.shtml");
		//urlList.add("http://bj.58.com/zufang/15591720966274x.shtml");
		for (String url : urlList){
			Map parseResult = p.Parse(url);
			p.Store(parseResult);	
		}
		System.out.println("End");
	}

}
