package test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import java.io.FileReader;
import java.io.FileWriter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvException;

import crawlers.CryptoPriceCrawler;
import crawlers.DateFilteredMessageCrawler;
import crawlers.RedditCrawler;
import crawlers.YahooFinanceCrawler;
import crawlers.YahooFinancePriceCrawler;
import crawlers.twitter.TwitterIndividualCrawler;
import datamodels.ScrappedMessageInfo;
import utils.CustomScrappedMessageInfoSerializer;


public class CrawlerTests {
	
	
	public static String readDateFromFile(String path) {
		String date = LocalDateTime.now().toString();
		try {
		      File myObj = new File(path);
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        date = myReader.nextLine();
		      }
		      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
		return date;
	}
	
	
	public static void writeDateInFile(String path, String date) {
		try {
			File myFoo = new File(path);
			FileWriter fooWriter = new FileWriter(myFoo, false); // true to append
			                                                     // false to overwrite.
			fooWriter.write(date);
			fooWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String args[]){
		
		Ini ini;
		try {
			//ini = new Ini(new File("D:\\Programacion\\Concursos\\Expertai-2021\\global_config.ini"));
			ini = new Ini(new File(args[0]));
			java.util.prefs.Preferences prefs = new IniPreferences(ini);
			
			String OUT_PATH = prefs.node("CONFIGURATION").get("OUT_PATH", null);
			String CRYPTO_CSV_FILE_PATH = prefs.node("CONFIGURATION").get("CRYPTO_CSV_FILE_PATH", null);
			String SUBREDDIT_CSV_FILE_PATH = prefs.node("CONFIGURATION").get("SUBREDDIT_CSV_FILE_PATH", null);
			String TWITTER_CSV_FILE_PATH = prefs.node("CONFIGURATION").get("TWITTER_CSV_FILE_PATH", null);
			String TWITTER_CREDENTIALS_FILE_PATH = prefs.node("CONFIGURATION").get("TWITTER_CREDENTIALS_FILE_PATH", null);
			String GECKODRIVER_PATH = prefs.node("CONFIGURATION").get("GECKODRIVER_PATH", null);
			String DATE_FILE_PATH = prefs.node("CONFIGURATION").get("DATE_FILE_PATH", null);
			
			String date = readDateFromFile(DATE_FILE_PATH);
			
			System.out.println("-----------------------------------");
			System.out.println("Started running with date: " + date);
			System.out.println("-----------------------------------");
			
			System.out.println(GECKODRIVER_PATH);
			System.setProperty("webdriver.gecko.driver", GECKODRIVER_PATH);
			
			Map<String,String> cryptos = new HashMap<String,String>();
			Map<String,String> twitter_personalities = new HashMap<String,String>();
			List<String> subreddits = new ArrayList<String>();
			
			CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator
			try(CSVReader reader = new CSVReaderBuilder(
			          new FileReader(CRYPTO_CSV_FILE_PATH))
			          .withCSVParser(csvParser)   // custom CSV parser
			          .withSkipLines(1)           // skip the first line, header info
			          .build()){
			      List<String[]> r = reader.readAll();
			      for(String[] row : r) {
			    	  cryptos.put(row[0], row[1]);
			      }
			  } catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CsvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			try(CSVReader reader = new CSVReaderBuilder(
			          new FileReader(TWITTER_CSV_FILE_PATH))
			          .withCSVParser(csvParser)   // custom CSV parser
			          .withSkipLines(1)           // skip the first line, header info
			          .build()){
			      List<String[]> r = reader.readAll();
			      for(String[] row : r) {
			    	  twitter_personalities.put(row[0], row[1]);
			      }
			  } catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CsvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try(CSVReader reader = new CSVReaderBuilder(
			          new FileReader(SUBREDDIT_CSV_FILE_PATH))
			          .withCSVParser(csvParser)   // custom CSV parser
			          .withSkipLines(1)           // skip the first line, header info
			          .build()){
			      List<String[]> r = reader.readAll();
			      for(String[] row : r) {
			    	  subreddits.add(row[0]);
			      }
			  } catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CsvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<CryptoPriceCrawler> priceCrawlers = new ArrayList<CryptoPriceCrawler>();
			List<DateFilteredMessageCrawler> crawlers = new ArrayList<DateFilteredMessageCrawler>();
			
			
			for(String subreddit: subreddits) {
				crawlers.add(new RedditCrawler(subreddit));
			}
			
			for(Map.Entry<String, String> entry : twitter_personalities.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				  
				crawlers.add(new TwitterIndividualCrawler(value, TWITTER_CREDENTIALS_FILE_PATH, 100, 50));
			}
			
			  
			for(Map.Entry<String, String> entry : cryptos.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				  
				crawlers.add(new YahooFinanceCrawler(value));
				//priceCrawlers.add(new YahooFinancePriceCrawler(value));
			}
			
			
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = 
			  new SimpleModule("CustomScrappedMessageInfoSerializer", new Version(1, 0, 0, null, null, null));
			module.addSerializer(ScrappedMessageInfo.class, new CustomScrappedMessageInfoSerializer());
			mapper.registerModule(module);
			
			final String lastCheckTime = date;
			for(DateFilteredMessageCrawler crawler: crawlers) {
				try {
					List<ScrappedMessageInfo> crawled_results = crawler.crawl(lastCheckTime);
					for(ScrappedMessageInfo smi: crawled_results) {
						smi.fixDates();
						System.out.println(smi);
						try {
							mapper.writeValue(new File(OUT_PATH + "/" + smi.getUuid() + ".json"), smi);
						} catch (JsonGenerationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
					
				
			}
			
			priceCrawlers.stream().map(x -> x.crawl()).forEach(l -> {System.out.println(l); System.out.println("-----------------");});
			
			
			LocalDateTime new_date_object = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			String new_date = formatter.format(new_date_object);
			writeDateInFile(DATE_FILE_PATH, new_date);
			
			System.out.println("-----------------------------------");
			System.out.println("Finished running with date: " + new_date);
			System.out.println("-----------------------------------");
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
	}
	
	
	

}

