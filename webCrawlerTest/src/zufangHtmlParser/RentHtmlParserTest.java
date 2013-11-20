package zufangHtmlParser;

import java.io.IOException;
import java.util.ArrayList;

public class RentHtmlParserTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println("Hello Rent");
		RentHtmlParser p = new RentHtmlParser();
		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add("http://bj.58.com/dongcheng/zufang/");
		urlList.add("http://bj.58.com/dongcheng/zufang/");
		for (String url : urlList){
			String parseResult = p.Parse(url);
			p.Store(parseResult);	
		}
	}

}
