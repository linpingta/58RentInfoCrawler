package zufangHtmlParser;

import java.io.IOException;

public class RentHtmlParserTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println("Hello Rent");
		RentHtmlParser p = new RentHtmlParser();
		p.Parse("http://bj.58.com/xibeiwang/zufang/");
	}

}
