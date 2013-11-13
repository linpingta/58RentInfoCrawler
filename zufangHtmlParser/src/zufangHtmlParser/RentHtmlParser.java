package zufangHtmlParser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.parser.*;
import org.jsoup.nodes.*;

public class RentHtmlParser {
	public void Print(){
		System.out.println("Hello RentHtmlParser");
	}
	
	public void Parse(String url) throws IOException{
		Document doc = Jsoup.connect(url).get();
		System.out.println(doc.title());
	}
}
