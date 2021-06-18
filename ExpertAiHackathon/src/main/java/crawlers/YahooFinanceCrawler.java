package crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import datamodels.ScrappedMessageInfo;
import utils.YahooDateUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class YahooFinanceCrawler implements DateFilteredMessageCrawler {
	
	
	private String url;

	public YahooFinanceCrawler(String cripto_acronym) {
		super();
		this.url = "https://finance.yahoo.com/quote/"+ cripto_acronym + "-USD/community?p="+ cripto_acronym + "-USD";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private List<ScrappedMessageInfo> crawlCommentSection(String url, String lastCheckTime) {
		
		WebDriver driver;
	   // driver = new PhantomJSDriver();
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		
		List<ScrappedMessageInfo> scrappedInfo = new ArrayList<ScrappedMessageInfo>();
		
		try {
		
			while(!waitForJStoLoad(driver));
			driver.get(url);
			String htmlContent = driver.getPageSource();
			//System.out.println(htmlContent);
			
			// Accept all cookies if necessary to load the main page 
			List<WebElement> cookieButtons = driver.findElements(By.xpath("//button[@type='submit' and @class='btn primary' and @name='agree' and @value='agree']"));
			if(!cookieButtons.isEmpty()) {
				WebElement cookieButton = cookieButtons.get(0);
				//System.out.println(cookieButton.getText());
				((JavascriptExecutor)driver).executeScript("arguments[0].click();", cookieButton);
				
				// Wait after pressing the button to load the page
		        for(String winHandle : driver.getWindowHandles()){
		            driver.switchTo().window(winHandle);
		        }
		        WebDriverWait wait = new WebDriverWait(driver, 120);
		        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='Fz(12px) C(#828c93)']")));
			}
			
			List<WebElement> mainChain = driver.findElements(By.xpath("//li[@class='comment Pend(2px) Mt(5px) Mb(11px) P(12px) ']"));	
			
			for(int i=0; i < mainChain.size(); i++) {
				
				List<WebElement> times = mainChain.get(i).findElements(By.xpath(".//span[@class='Fz(12px) C(#828c93)']"));	
				List<WebElement> messages = mainChain.get(i).findElements(By.xpath(".//div[@class='C($c-fuji-grey-l) Mb(2px) Fz(14px) Lh(20px) Pend(8px)']"));
				List<WebElement> users = mainChain.get(i).findElements(By.xpath(".//button[starts-with(@aria-label,'See reaction history for')]"));	
				List<WebElement> scorings = mainChain.get(i).findElements(By.xpath(".//span[@class='D(ib) Va(m) Fz(12px) Mstart(6px) C(#828c93)']"));
				List<WebElement> replies = mainChain.get(i).findElements(By.xpath(".//button[@class='replies-button O(n):h O(n):a P(0) Bd(n) Cur(p) Fz(12px) Fw(n) C(#828c93) D(ib) Mend(20px)']"));	
				
				
				System.out.println(i + "/" + mainChain.size());
				
				String time = times.get(0).getText();
				
				if(messages.size() > 0) {
					
					System.out.println("Time: " + time);
					System.out.println("Message: " + messages.get(0).getText());
					
					boolean after = YahooDateUtils.yahooTimeNewer(time, lastCheckTime);
					System.out.println("After: " + after);
					//System.out.println("--------------------------------");
					
					if (after) {
						
						String message = messages.get(0).getText();
						
						String user = "UNKNOWN";
						if(users.size() > 0) {
							user = users.get(0).getText();
						}
						
						String score = "UNKNOWN";
						if(scorings.size() > 0) {
							score = scorings.get(0).getText();
						}
						
						String reply = "UNKNOWN";
						if(replies.size() > 0) {
							reply = replies.get(0).getText().replace("Replies (", "").replace(")", "");
						}
						
						ScrappedMessageInfo main_thread_message = new ScrappedMessageInfo(message, YahooDateUtils.yahooTimeToLocalDateTime(time).toString(), user, score, reply, "https://finance.yahoo.com", url, LocalDateTime.now().toString(), Optional.empty());
						scrappedInfo.add(main_thread_message);
						
						Optional<UUID> parent_uuid = Optional.empty();
						parent_uuid = Optional.of(main_thread_message.getUuid());
						
						
						if(replies.size() > 0) {
							
							
							WebElement replyButton = replies.get(0);
							System.out.println(replyButton.getText());
							((JavascriptExecutor)driver).executeScript("arguments[0].click();", replyButton);
							
							// Wait after pressing the button to load the page
					        for(String winHandle : driver.getWindowHandles()){
					            driver.switchTo().window(winHandle);
					        }
					        WebDriverWait wait = new WebDriverWait(driver, 120);
					        wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(mainChain.get(i), By.xpath("//ul[@class='comments-list List(n) Ovs(touch) Pos(r) Mstart(-10px)']")));
					        
					        htmlContent = driver.getPageSource();
							//System.out.println(htmlContent);
					        
					        List<WebElement> mainReplyChain = mainChain.get(i).findElements(By.xpath(".//ul[@class='comments-list List(n) Ovs(touch) Pos(r) Mstart(-10px)']"));
					        
					        if (mainReplyChain.size() > 0) {
					        
						        List<WebElement> replyChain = mainReplyChain.get(0).findElements(By.xpath(".//li[@class='comment Pend(2px) Py(10px) Pstart(10px) ']"));
						        System.out.println("Reply: " + replyChain.size());
						        			        
								for(int j=0; j < replyChain.size(); j++) {
									
							        List<WebElement> reply_times = replyChain.get(j).findElements(By.xpath(".//span[@class='Fz(12px) C(#828c93)']"));	
									List<WebElement> reply_messages = replyChain.get(j).findElements(By.xpath(".//div[@class='C($c-fuji-grey-l) Mb(2px) Fz(14px) Lh(20px) Pend(8px)']"));
									List<WebElement> reply_users = replyChain.get(j).findElements(By.xpath(".//button[starts-with(@aria-label,'See reaction history for')]"));	
									List<WebElement> reply_scorings = replyChain.get(j).findElements(By.xpath(".//span[@class='D(ib) Va(m) Fz(12px) Mstart(6px) C(#828c93)']"));
									
									System.out.println(i + "," + j);
									
									if(reply_messages.size() > 0) {
										
										String reply_time = reply_times.get(0).getText();
										
										//System.out.println("Time: " + time);
										//System.out.println("Message: " + message);
										
										boolean reply_after = YahooDateUtils.yahooTimeNewer(reply_time, lastCheckTime);
										//System.out.println("After: " + after);
										//System.out.println("--------------------------------");
										
										if (reply_after) {
											
											String reply_message = reply_messages.get(0).getText();
											
											String reply_user = "UNKNOWN";
											if(reply_users.size() > 0) {
												reply_user = reply_users.get(0).getText();
											}
											
											String reply_score = "UNKNOWN";
											if(reply_scorings.size() > 0) {
												reply_score = reply_scorings.get(0).getText();
											}
											
											
											ScrappedMessageInfo thread_message = new ScrappedMessageInfo(reply_message, YahooDateUtils.yahooTimeToLocalDateTime(reply_time).toString(), reply_user, reply_score, "0", "https://finance.yahoo.com", url, LocalDateTime.now().toString(), parent_uuid);
											scrappedInfo.add(thread_message);
											
										}
										
									}
									
								}
					        }
								
						}
						
					}
					
				}
				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		driver.close();
		
		return scrappedInfo;
	}
	
	@Override
	public List<ScrappedMessageInfo> crawl(String lastCheckedTime) {
		return crawlCommentSection(this.url, lastCheckedTime);
	}
	
	public static void main(String args[]){
		System.out.println("Running Yahoo Finance Crawler");
		System.out.println("----------------------");
		System.setProperty("webdriver.gecko.driver", "D:\\Programacion\\geckodriver-v0.29.1-win64\\geckodriver.exe");
		//System.setProperty("phantomjs.binary.path","D:\\TRABAJO\\HackIBMAI_2019\\phantomjs-2.1.1-windows\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
		//RedditCrawler.crawlMainPage();
		String lastCheckTime = "2021-05-24 18:20";
		//YahooFinanceCrawler.crawlCommentSection("https://finance.yahoo.com/quote/DOGE-USD/community?p=DOGE-USD", lastCheckTime);
	}
	
	//https://stackoverflow.com/questions/10720325/selenium-webdriver-wait-for-complex-page-with-javascript-to-load
	// User LINGS
	public static boolean waitForJStoLoad(WebDriver driver) {

	    WebDriverWait wait = new WebDriverWait(driver, 120);

	    // wait for jQuery to load
	    ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
	      public Boolean apply(WebDriver driver) {
	        try {
	          return ((Long)executeJavaScript("return jQuery.active", driver) == 0);
	        }
	        catch (Exception e) {
	          return true;
	        }
	      }
		
	    };

	    // wait for Javascript to load
	    ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
	      public Boolean apply(WebDriver driver) {
	        return executeJavaScript("return document.readyState", driver)
	            .toString().equals("complete");
	      }
	    };

	  return wait.until(jQueryLoad) && wait.until(jsLoad);
	}
	
	private static Object executeJavaScript(String string, WebDriver driver) {
		return ((JavascriptExecutor)driver).executeScript(string);
	}

	
}
