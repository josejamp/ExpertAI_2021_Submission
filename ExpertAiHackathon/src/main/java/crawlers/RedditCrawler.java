package crawlers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import datamodels.ScrappedMessageInfo;
import utils.CustomScrappedMessageInfoSerializer;
import utils.RedditDateUtils;
import utils.RedditRepliesParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.UUID;


public class RedditCrawler implements DateFilteredMessageCrawler{
	
	private String url;

	public RedditCrawler(String subredit) {
		super();
		this.url = "https://www.reddit.com/r/" + subredit + "/";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	private List<ScrappedMessageInfo> crawlMainPage(String url, String lastCheckTime) {
		WebDriver driver;
	   // driver = new PhantomJSDriver();
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		
		while(!waitForJStoLoad(driver));
		driver.get(url);
		String htmlContent = driver.getPageSource();
		//System.out.println(htmlContent);
		
		List<ScrappedMessageInfo> scrappedInfo = new ArrayList<ScrappedMessageInfo>();
		List<WebElement> attributes = driver.findElements(By.xpath("//a[@data-click-id='body']"));
		for(WebElement mver: attributes) {
			String threadUrl = mver.getAttribute("href");
			//System.out.println(threadUrl);
			
			String threadTitle = mver.findElements(By.xpath(".//*")).get(0).findElements(By.xpath(".//*")).get(0).getText();
			//System.out.println(threadTitle);
			
			scrappedInfo.addAll(crawlThread(threadUrl, lastCheckTime));
		}
		driver.close();
		
		return scrappedInfo;
	}

	private List<ScrappedMessageInfo> crawlThread(String url, String lastCheckTime) {
		
		WebDriver driver;
	   // driver = new PhantomJSDriver();
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		
		while(!waitForJStoLoad(driver));
		driver.get(url);
		String htmlContent = driver.getPageSource();
		//System.out.println(htmlContent);
		
		WebElement expandButton = driver.findElement(By.xpath("//button[@role='button' and @tabindex='0' and contains(text(), 'View Entire Discussion')]"));
		((JavascriptExecutor)driver).executeScript("arguments[0].click();", expandButton);
		
		
		List<WebElement> mainMessage = driver.findElements(By.xpath("//div[@data-test-id='post-content']"));
		List<WebElement> main_timestamp = mainMessage.get(0).findElements(By.xpath(".//a[@data-click-id='timestamp']"));
		List<WebElement> main_header = mainMessage.get(0).findElements(By.xpath(".//h1[@class]"));
		List<WebElement> main_text = mainMessage.get(0).findElements(By.xpath(".//p[@class]"));
		
		ScrappedMessageInfo main_thread_message = null;
		if(!main_timestamp.isEmpty() && !main_header.isEmpty() && !main_text.isEmpty()) {
			String user_name = "UNKNOWN";
			List<WebElement> main_user_name = mainMessage.get(0).findElements(By.xpath(".//a[starts-with(@href,'/user/')]"));
			List<WebElement> reply_number_box = mainMessage.get(0).findElements(By.xpath(".//i[starts-with(@class,'icon icon-comment')]"));
			
			user_name = main_user_name.get(0).getText();
			
			if(user_name.startsWith("/u")) {
				user_name = user_name.substring(2);
			}
			
			String main_comments = "UNKNOWN";
			String main_scoring = "UNKNOWN";
			if(!reply_number_box.isEmpty()) {
				List<WebElement> main_reply_number = mainMessage.get(0).findElements(By.xpath(".//span[@class and contains(text(), ' comments')]"));
				List<WebElement> main_upvotes = mainMessage.get(0).findElements(By.xpath(".//*[text()[contains(., '% Upvoted')]]"));
			
				if(!main_reply_number.isEmpty() && !main_upvotes.isEmpty()) {
					long comments = RedditRepliesParser.parseReplies(main_reply_number.get(0).getText());
					long main_calculated_upvotes = comments - (comments*Integer.parseInt(main_upvotes.get(0).getText().replace("% Upvoted", ""))/100);
					main_comments = Long.toString(comments);
					main_scoring = Long.toString(main_calculated_upvotes);
				}
			}
			main_thread_message = new ScrappedMessageInfo(main_timestamp.get(0).getText(), main_header.get(0).getText() + " " + main_text.get(0).getText(), 
					user_name, main_scoring, main_comments, 
					"https://www.reddit.com/r/CryptoCurrency", url, LocalDateTime.now().toString(), Optional.empty());
		}
		
		
		List<ScrappedMessageInfo> scrappedInfo = new ArrayList<ScrappedMessageInfo>();
		Optional<UUID> parent_uuid = Optional.empty();
		if(main_thread_message != null) {
			parent_uuid = Optional.of(main_thread_message.getUuid());
			scrappedInfo.add(main_thread_message);
		}
		
		
		List<WebElement> boxes = driver.findElements(By.xpath("//div[@id and @tabindex='-1' and starts-with(@style,'padding-left')]"));	
		for(WebElement comment_box: boxes) {
			List<WebElement> time_candidates = comment_box.findElements(By.xpath(".//div[@data-testid='post-comment-header']"));
			if(!time_candidates.isEmpty()) {
				//System.out.println(time_candidates.get(0).getText());
				List<WebElement> time_elements = time_candidates.get(0).findElements(By.xpath(".//a[@rel='noopener noreferrer']"));
				if(!time_elements.isEmpty()) {
					String time = time_elements.get(0).getText();
					//System.out.println("Time: " + time);
					if(RedditDateUtils.redditTimeNewer(time, lastCheckTime)) {
						
						List<WebElement> user_names = time_candidates.get(0).findElements(By.xpath(".//a[starts-with(@href,'/user/')]"));
						String user_name_string = "UNKNOWN";
						if(!user_names.isEmpty()) {
							user_name_string = user_names.get(0).getText();
						}
						
						
						List<WebElement> scoring_boxes = comment_box.findElements(By.xpath(".//div[starts-with(@id,'vote-arrows')]"));
						String score_string = "UNKNOWN";
						if(!scoring_boxes.isEmpty()) {
							List<WebElement> scores = scoring_boxes.get(0).findElements(By.xpath(".//div[starts-with(@class,'_1rZYMD')]"));
							
							if(!scores.isEmpty()) {
								score_string = scores.get(0).getText();
								if(scores.get(0).getText().equals("Vote")) {
									score_string = "0";
								}
							}
						}
						
						List<WebElement> comments = comment_box.findElements(By.xpath(".//div[@data-test-id='comment']"));
						for(WebElement comment: comments) {
							List<WebElement> texts = comment.findElements(By.xpath(".//p"));
							for(WebElement text: texts) {
								//System.out.println("Text: " + text.getText());
								scrappedInfo.add(new ScrappedMessageInfo(text.getText(), RedditDateUtils.redditTimeToLocalDateTime(time).toString(),
										user_name_string, score_string, "UNKNOWN",
										"https://www.reddit.com", url, LocalDateTime.now().toString(), parent_uuid));
							}
						}
					}
				}
			}
			//System.out.println("-------------------------------------");
		}
		driver.close();
		
		return scrappedInfo;
	}
	
	
	@Override
	public List<ScrappedMessageInfo> crawl(String lastCheckedTime) {
		return this.crawlMainPage(this.url, lastCheckedTime);
	}
	
	public static void main(String args[]){
		Ini ini;
		try {
			ini = new Ini(new File("D:\\Programacion\\Concursos\\Expertai-2021\\global_config.ini"));
			java.util.prefs.Preferences prefs = new IniPreferences(ini);
			
			String OUT_PATH = prefs.node("CONFIGURATION").get("OUT_PATH", null);
			String CRYPTO_CSV_FILE_PATH = prefs.node("CONFIGURATION").get("CRYPTO_CSV_FILE_PATH", null);
			String TWITTER_CSV_FILE_PATH = prefs.node("CONFIGURATION").get("TWITTER_CSV_FILE_PATH", null);
			String TWITTER_CREDENTIALS_FILE_PATH = prefs.node("CONFIGURATION").get("TWITTER_CREDENTIALS_FILE_PATH", null);
			String GECKODRIVER_PATH = prefs.node("CONFIGURATION").get("GECKODRIVER_PATH", null);
			
			System.out.println(GECKODRIVER_PATH);
			System.setProperty("webdriver.gecko.driver", GECKODRIVER_PATH);
			
			Map<String,String> cryptos = new HashMap<String,String>();
			Map<String,String> twitter_personalities = new HashMap<String,String>();
			
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
			
			
			RedditCrawler rc = new RedditCrawler("https://www.reddit.com/r/CryptoCurrency/");
			List<ScrappedMessageInfo> history = new ArrayList<ScrappedMessageInfo>();
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n7rl2y/you_hear_about_the_kid_who_put_in_500_into_a/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/noztp7/binance_ceo_cz_shades_elon_musk_in_tweet_when_you/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nch8rm/its_been_a_crazy_ride_these_past_7_years_but_im/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n43gno/how_much_will_the_price_of_litecoin_move_by/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/ngwelr/elon_musks_affect_on_crypto_is_completely/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nb0yz5/elon_musk_tesla_stops_accepting_bitcoin_as/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n8h7ky/if_someone_is_screaming_hold_the_line_they_really/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n9cby0/not_every_new_coin_is_a_shitcoin_how_to_spot_the/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nl0xwa/gamestop_is_building_an_nft_platform_on_ethereum/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nbk72b/yall_need_to_be_nicer_to_newbies_who_get_shaken/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nk30j0/banks_not_bitcoin_in_australia_laundered/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nc6qhm/we_wanted_decentralization_this_is_it/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/ndedgf/elon_musk_just_embarrassed_himself_in_front_of/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n66se9/coffeezilla_youtube_channel_just_got_deleted_by/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nhu312/china_is_repeatedly_attempting_to_fud_crypto/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n5wkwq/will_doge_be_worth_100_a_coin_can_it_become_as/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/njvog2/we_can_all_breath_a_collective_sigh_of_relief/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nfcs9r/ethereum_will_use_an_estimated_9995_less_energy/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nkolbg/china_is_anticrypto_because_they_cant_censor_or/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/niiuvk/a_list_of_things_banned_in_china/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nh5skp/us_government_just_admitted_crypto_is_here_to_stay/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nlng5w/paypal_to_begin_allowing_bitcoin_withdrawals/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n83wha/doge_is_bringing_down_the_quality_of_the_sub_like/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n57xbt/bitcoin_energy_usage_is_a_problem_and_the_crypto/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n3xin5/my_friend_found_his_dogecoin_core_wallet_form/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/njbgxt/the_bitcoin_halving_just_happened_get_ready_for/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nhnf4p/ethereum_cofounder_on_why_he_got_into_crypto/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/nm5lkl/ethereum_founder_vitalik_buterin_says_longawaited/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/njrbs8/cardano_ada_creator_you_guys_cant_get_900_gains/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/CryptoCurrency/comments/n4kgcd/40_consumers_are_planning_to_use_cryptocurrency/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/dogecoin/comments/n4lbr0/dogecoin_dogecoin/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/dogecoin/comments/n9s2zu/the_dogefather_has_spoken/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/dogecoin/comments/n7l51y/technoking_dogefather_brace_for_impact/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/dogecoin/comments/n8pr8i/doge_to_the_moon/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/dogecoin/comments/n6hb9p/thanks_to_doge_i_have_bought_nothing_because_i/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/ndunlj/ouch/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/ne9dxe/pls_stop_talking_about_elon_bitcoin_has_been/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nb0uas/tesla_suspended_vehicle_purchases_using_bitcoin/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nkjzlo/im_now_seeing_the_trend_of_the_new_investors/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nm2185/joshua_and_jessica_jarrett_the_nashville_couple/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/n3zb3c/printing_money_is_stealing_from_the_poor_once/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/np8r51/binance_ceo_cz_shades_elon_musk_in_tweet_when_you/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nk6vjg/yesterday_i_was_pleased_to_host_a_meeting_between/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nddg5x/mark_cuban_hits_back_at_elon_musk_says_mavs_will/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nc84sv/microstrategy_buys_15_million_bitcoin_rejects/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nlgei5/cryptocurrency_will_be_allowed_in_nigeria/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nf918l/saylor_and_microstrategy_bought_the_dip_229/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/njw171/bitcoin_is_officially_a_new_asset_class_goldman/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Bitcoin/comments/nbppnh/a_letter_to_elon/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nbd48y/cardano_ceo_cardano_is_16_million_times_more/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/njlhc8/i_own_a_small_soap_business_with_my_wife_and/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nd5akh/mark_cuban_bringing_awareness_to_cardano/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/negrnb/energy_use_optimization_cardano_for_the_flawless/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nnm8ez/ada_gets_full_support_from_denmark/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nb2wne/great_news_for_the_likes_of_cardano_with_pos_glad/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nhr87w/you_can_soon_rent_an_apartment_in_malta_with/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/ninfe8/mithril_meadery_just_became_commercially_licensed/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/n7jri6/charles_governor_of_gordon_and_university_of/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nmz3ul/cardano_begins_countdown_to_smart_contracts_with/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/n8agt5/germanys_second_largest_stock_exchange_adds_ada/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/n5ngcj/kraken_launches_cardano_ada_staking/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/nkwdgp/proof_of_stake_and_cardano_mentioned_in_nbc_news/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/cardano/comments/njz01y/cardano_the_most_trustable_coin_dailycoin/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/njdhxn/goldman_sachs_calls_ethereum_the_amazon_of/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nfbza3/ethereum_to_reduce_energy_consumption_by_9995/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/naw30o/vitalik_buterin_donates_500_eth_to_the_india/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nk42l9/goldman_sachs_believes_ethereum_beats_bitcoin_new/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/n6k7xi/psa_ethereum_classic_etc_is_a_dead_insecure_chain/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nlgbl8/gamestop_is_hiring_for_new_nft_platform_on/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nb963i/why_vitalik_made_a_500_iq_play_today_with_the_1/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/neslts/ethereum_network_revenue_set_to_smash_monthly/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/n97mzs/alexis_ohanian_cofounder_of_reddit_is_now/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/n3zga3/ebay_ceo_says_they_are_exploring_how_they_can/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nkponk/eth_gas_fees_dropped_love_it_but_what_caused_the/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/nlttk9/nvidia_ethereums_shift_to_proofofstake_could/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/n71ai7/eth_20_and_what_will_happen_to_your_eth/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/ethereum/comments/n4untw/ethereum_is_transitioning_from_rewards_to_miners/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/n8rty4/new_york_times_just_exposed_jay_clayton/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/ng4r8b/despite_the_sec_lawsuit_ripple_partners_with/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nncvm3/ripple_plans_to_go_public_after_sec_lawsuit_over/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nobmaa/judge_netburn_denies_secs_access_to_ripples_legal/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/n4w935/ripple_appoints_a_former_us_treasurer_to_its/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/n6gn0r/judge_netburn_has_affirmed_her_previous_ruling/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nb143j/tesla_suspending_purchases_using_bitcoin_due_to/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nf5w7b/sec_attempts_to_block_xrp_holders_from_presenting/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nfj535/xrp_has_utility_and_the_ability_to_execute_the/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nc4jvw/ripple_puts_sec_to_shame_for_deliberate_omissions/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/n5pp70/peter_brandt_says_he_was_wrong_about_xrp_warns/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/nmjssw/how_ripple_found_new_ways_to_grow_xrp_business/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/n49wni/sec_claims_no_such_documents_exist_as_it_pertains/", "2021-01-01 09:00"));
			history.addAll(rc.crawlThread("https://www.reddit.com/r/Ripple/comments/ngtjvg/ripple_legal_clarity_could_trigger_a_new_rally/", "2021-01-01 09:00"));
			
			
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = 
			  new SimpleModule("CustomScrappedMessageInfoSerializer", new Version(1, 0, 0, null, null, null));
			module.addSerializer(ScrappedMessageInfo.class, new CustomScrappedMessageInfoSerializer());
			mapper.registerModule(module);
			for(ScrappedMessageInfo smi: history) {
				
				System.out.println(smi);
				try {
					mapper.writeValue(new File(OUT_PATH + "\\" + smi.getUuid() + ".json"), smi);
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
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
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
