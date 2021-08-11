package com.suren.shares;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MoneyControl {

	
	public static void main(String[] args) throws HttpException, IOException {
		
		ArrayList<String> links = new ArrayList<String>();
		Share shareDetail = null; 
		
		long startTme = System.currentTimeMillis();
		
		for(int count = 65; count < 90; count ++){
			
			String com = String.valueOf((char) count);
			HttpClient httpClient = new HttpClient();
			System.out.println("Com is "+com);
			String url = "https://www.moneycontrol.com/mccode/common/autosuggesion.php?query="+com+"&type=1&format=html";
//			String url = "http://www.moneycontrol.com/india/stockmarket/pricechartquote/"+com;
			PostMethod httpPost = new PostMethod(url);
			httpPost.getParams().setSoTimeout(300000);
//			httpPost.addRequestHeader("content-type","text/xml;charset='utf-8'");
			httpClient.executeMethod(httpPost);
			String resp = httpPost.getResponseBodyAsString(); 

			StringTokenizer token = new StringTokenizer(resp, "\"");
		// System.out.println(token.countTokens());
		// System.out.println(token.nextElement().toString()); 
		
			System.out.println("Url is " + url);
			while(token.hasMoreElements()){
	//			System.out.println(token.nextToken().toString()); 
				String temp = token.nextElement().toString(); 
				
				if(temp.contains("http://www.moneycontrol.com/india/stockpricequote"))
					links.add(temp);
				
			}
		}
		 System.out.println("The numberof companies are "+ links.size());
		 
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 
		 //Create a blank sheet     
		 XSSFSheet sheet = workbook.createSheet("Share Price");
		 
		 Map<String, Object[]> data = new TreeMap<String, Object[]>();   
		 data.put("1", new Object[] {"Company Name", "Link", "Price/Book", "P/E", "P/C", "Dividend", "Face Value"});   
		 
//		 for(String temp:links)
//			 System.out.println(temp);
		 
	 
		//This data needs to be written (Object[]) 
		 int exception =0;   
		 
		 for(int i=2;i<links.size()+2; i++) {
			 try {
				 
				 System.out.println("Link is " + links.get(i-2));
				 HttpClient httpClient = new HttpClient();
				 PostMethod httpPost = new PostMethod(links.get(i-2));
				 httpPost.getParams().setSoTimeout(300000);
				 httpPost.addRequestHeader("content-type","text/xml;charset='utf-8'");
				 httpClient.executeMethod(httpPost);
				 
				 String resp = httpPost.getResponseBodyAsString();
				 
				 if(resp.contains("is not traded on BSE in the last 30 days"))
					 continue;   
				 
				 else  {
					 
					 shareDetail =  getStockDetails(resp); 
					 data.put(new Integer(i).toString(), new Object[] {getShareName(links.get(i-2)), links.get(i-2), shareDetail.getPriceByBook(), shareDetail.getProfitByEquity(),  shareDetail.getProfitByCapital(),  shareDetail.getDividend(), shareDetail.getFaceValue()} );	 
				 }
			}
				catch(Exception e) {
					System.out.println(links.get(i-2));
					exception ++; 
				}
				}
			//Iterate over data and write to sheet
			
			System.out.println("Exception Count is  " +  exception  );
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset)
			{
			    Row row = sheet.createRow(rownum++);
			    Object [] objArr = data.get(key);
			    int cellnum = 0;
			    for (Object obj : objArr)
			    {
			       Cell cell = row.createCell(cellnum++);
			       if(obj instanceof String)
			            cell.setCellValue((String)obj);
			        else if(obj instanceof Integer)
			            cell.setCellValue((Integer)obj);
			    }
			}
			try 
			{
				//Write the workbook in file system
			    FileOutputStream out = new FileOutputStream(new File("ShareMarket_Links.xlsx"));
			    workbook.write(out);
			    out.close();
			    
			    long endTime =  System.currentTimeMillis();
			    
			    System.out.println("The time of Exceution is   "  + (endTime - startTme)/(1000*60) );
			    System.out.println("Excel Sheet  written successfully on disk.");
			     
			} 
			catch (Exception e) 
			{
			    e.printStackTrace(System.out);
			}

	}
	
	public static String getShareName(String shareName) {
		ArrayList names = new ArrayList();
		StringTokenizer token = new StringTokenizer(shareName, "/");
		while(token.hasMoreElements()){
			String temp = token.nextElement().toString();
			names.add(temp);
		}
		String resultString = (String) names.get(names.size()-2); 
		// System.out.println(resultString);
		
		return resultString;

	}
	
	public static Share getStockDetails(String resp) throws HttpException, IOException {
		
			
		
		String bookValue = null ;
		String marketCapital = null;
		String eps = null;
		String profitByEquity = null;
		String dividend = null;
		String dividendYield = null; 
		String marketLot = null;  
		String faceValue = null;  
		String industry = null; 
		String priceByBook = null;
		String profitByCapital = null;
		
		Share shareDetail = new Share();; 
		
		
		if(resp.contains("MARKET CAP (Rs Cr)")) {

			int index = resp.indexOf("MARKET CAP (Rs Cr)");
			marketCapital = resp.substring(index+ 67, index+  73);  
			
			if(marketCapital.contains("</div>"))
				marketCapital = ""; 
			else if(marketCapital.contains("<"))
				marketCapital=marketCapital.substring(0, marketCapital.indexOf("<"));
			
			shareDetail.setMarketCapital(marketCapital);
		} 
		
		if(resp.contains("EPS (TTM)")) {

			int index = resp.indexOf("EPS (TTM)");
			eps = resp.substring(index+48, index+54);  
			if(eps.contains("</div>"))
				eps = ""; 
			if(eps.contains("<"))
				eps=eps.substring(0, eps.indexOf("<"));
		}  
		

		if(resp.contains("P/E")) {

			int index = resp.indexOf("P/E");
			profitByEquity = resp.substring(index+42, index+49 );  
			if(profitByEquity.contains("</div>"))
				profitByEquity = ""; 
			else if(profitByEquity.contains("<"))
				profitByEquity=profitByEquity.substring(0, profitByEquity.indexOf("<"));  
			
			shareDetail.setProfitByEquity(profitByEquity);
		} 
		

		if(resp.contains("P/C")) {

			int index = resp.indexOf("P/C");
			profitByCapital = resp.substring(index+ 42, index+ 48 );  
			
			
			
			if(profitByCapital.contains("</div>"))
				profitByCapital = ""; 
			else if(profitByCapital.contains("<"))
				profitByCapital=profitByCapital.substring(0, profitByCapital.indexOf("<"));  
			
			shareDetail.setProfitByCapital(profitByCapital);
		} 
		
		
		if(resp.contains("DIV (%)")) {

			int index = resp.indexOf("DIV (%)");
			dividend = resp.substring(index+46, index+52 );  
			if(dividend.contains("</div>"))
				dividend = ""; 
			else if(dividend.contains("<"))
				dividend = dividend.substring(0, dividend.indexOf("<")); 
			
			shareDetail.setDividend(dividend);
		} 
		
		if(resp.contains("DIV YIELD.(%)")) {

			int index = resp.indexOf("DIV YIELD.(%)");
			dividendYield = resp.substring(index+ 52, index+58 );  
	
			if(dividendYield.contains("</div>"))
				dividendYield = ""; 
			else if(dividendYield.contains("<"))
				dividendYield = dividendYield.substring(0, dividendYield.indexOf("<")); 
			
			shareDetail.setDividendYield(dividendYield);    
			
		} 
		
		

		if(resp.contains("Market Lot")) {

			int index = resp.indexOf("Market Lot");
			marketLot = resp.substring(index+ 49, index+56 );  
			if(marketLot.contains("</div>"))
				marketLot = ""; 
			else   if(marketLot.contains("<"))
				marketLot = marketLot.substring(0, marketLot.indexOf("<"));    
			
			shareDetail.setMarketLot(marketLot);
		} 
		

		if(resp.contains("FACE VALUE (Rs)")) {

			int index = resp.indexOf("FACE VALUE (Rs)");
			faceValue = resp.substring(index+ 54, index+ 62 );  
			if(faceValue.contains("</div>"))
				faceValue = ""; 
			else  if(faceValue.contains("<"))
				faceValue=faceValue.substring(0, faceValue.indexOf("<"));    
			
			shareDetail.setFaceValue(faceValue);
			
		}    
		

		if(resp.contains("INDUSTRY P/E")) {

			int index = resp.indexOf("INDUSTRY P/E");
			industry = resp.substring(index+ 51, index+ 58 );  
			if(industry.contains("</div>"))
				industry = ""; 
			else if(industry.contains("<"))
				industry = industry.substring(0, industry.indexOf("<"));   
			shareDetail.setIndustry(industry);    
			
		} 
		
		if(resp.contains("BOOK VALUE")) {

			int index = resp.indexOf("BOOK VALUE");
			bookValue = resp.substring(index+54, index+60);  
			
			if(bookValue.contains("</div>"))
				bookValue = ""; 
			else if(bookValue.contains("<"))
				bookValue=bookValue.substring(0, bookValue.indexOf("<"));   
			
			shareDetail.setBookValue(bookValue);
			
		} 
		
		
		if(resp.contains("PRICE/BOOK")) {

			int index = resp.indexOf("PRICE/BOOK");
			System.out.println("Index is "+index);
			priceByBook = resp.substring(index+49, index+55);
			if(priceByBook.contains("</div>"))
				priceByBook = ""; 
			else if(priceByBook.contains("<"))
				priceByBook=priceByBook.substring(0, priceByBook.indexOf("<")); 
			
			shareDetail.setPriceByBook(priceByBook);

		}
		
		
		return shareDetail;

	}
}
