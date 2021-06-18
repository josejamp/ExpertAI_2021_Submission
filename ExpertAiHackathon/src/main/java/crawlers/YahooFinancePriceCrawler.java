package crawlers;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import datamodels.CrawledCryptoPrice;
import utils.YahooDateUtils;

import java.time.LocalDateTime;


public class YahooFinancePriceCrawler implements CryptoPriceCrawler {
	
	private String url;
	private String crypto;

	public YahooFinancePriceCrawler(String cripto_acronym) {
		super();
		this.crypto = cripto_acronym;
		this.url = "https://finance.yahoo.com/quote/"+ cripto_acronym + "-USD/";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	private CrawledCryptoPrice crawlCommentSection(String url) {
		
		WebDriver driver;
	   // driver = new PhantomJSDriver();
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		
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
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='Trsdu(0.3s) Fw(b) Fz(36px) Mb(-4px) D(ib)' and @data-reactid='32']")));
		}
		
		WebElement price = driver.findElement(By.xpath("//span[@class='Trsdu(0.3s) Fw(b) Fz(36px) Mb(-4px) D(ib)' and @data-reactid='32']"));		
		
		CrawledCryptoPrice crawledPrice = new CrawledCryptoPrice(this.crypto, this.url, price.getText(), LocalDateTime.now().toString());
		
		driver.close();
		
		return crawledPrice;
	}

	@Override
	public CrawledCryptoPrice crawl() {
		return crawlCommentSection(this.url);
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
