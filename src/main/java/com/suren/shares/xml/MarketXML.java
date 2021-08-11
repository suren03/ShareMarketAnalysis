package com.suren.shares.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


public class MarketXML {

	public static void main(String[] args) {
		MarketXML market = new MarketXML(); 
		market.StringtoXML();
		
	}
	
	public void StringtoXML () {
		
		HttpClient httpClient = new HttpClient();
		String url = "http://www.moneycontrol.com/india/stockpricequote/constructioncontractingcivil/ncc/NCC01";
		PostMethod httpPost = new PostMethod("http://www.moneycontrol.com/india/stockpricequote/constructioncontractingcivil/ncc/NCC01");
		httpPost.getParams().setSoTimeout(300000);
		
		httpPost.addRequestHeader("content-type","text/xml;charset='utf-8'");
		try {
			httpClient.executeMethod(httpPost); 
			String resp = httpPost.getResponseBodyAsString(); 
			//System.out.println(resp);
			if(resp.contains("MARKET CAP (Rs Cr)")) {
				System.out.println(resp.substring(0,40));
				int index = resp.indexOf("<html");
				System.out.println(index);
				String res = resp.substring(0, index);  
				System.out.println(res.charAt(0));
			}
			//Document doc = convertStringToDocument(resp); 
			//System.out.println(doc.getElementById("bse_volume").getTextContent());
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private  Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try 
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }
}
