package crawlers.twitter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import utils.TwitterConfigReader;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import crawlers.DateFilteredMessageCrawler;
import datamodels.ScrappedMessageInfo;

public class TwitterIndividualCrawler implements DateFilteredMessageCrawler {
	
	private String userName;
	private String conifgPath;
	private int numberTweets;
	private int numTweetsFilter;
	
	public TwitterIndividualCrawler(String username, String configPath, int numberTweets, int numTweetsFilter) {
		super();
		this.userName = username;
		this.conifgPath = configPath;
		this.numberTweets = numberTweets;
		this.numTweetsFilter = numTweetsFilter;
	}


	public TwitterUserInfo getTwitterInfo(){
		TwitterConfig twitterConfig;
		TwitterUserInfo userInfo = new TwitterUserInfo();
		try {
			twitterConfig = TwitterConfigReader.readConfigFile(conifgPath);
			
			ConfigurationBuilder cb = new ConfigurationBuilder();
			 cb.setDebugEnabled(true)
			    .setOAuthConsumerKey(twitterConfig.getConsumerKey())
			    .setOAuthConsumerSecret(twitterConfig.getConsumerSecret())
			    .setOAuthAccessToken(twitterConfig.getAuthAccessToken())
			    .setOAuthAccessTokenSecret(twitterConfig.getAuthAccessTokenSecret());
			 TwitterFactory tf = new TwitterFactory(cb.build());
			 Twitter twitter = tf.getInstance();
			 
			 
			 User user = twitter.users().showUser(userName);
			 System.out.println(user);
			 userInfo = new TwitterUserInfo(user);
			 System.out.println(userInfo);
			 
			 long lastID = Long.MAX_VALUE;
			 Paging pg = new Paging();
			 ArrayList<Status> tweets = new ArrayList<Status>();
			 while(tweets.size() < numberTweets) {
			      tweets.addAll(twitter.getUserTimeline(userInfo.getScreenName(), pg));
			      for (Status t : tweets){
			    	  if(t.getId() < lastID){
			    		  lastID = t.getId();
			    	  }
			      }
			      pg.setMaxId(lastID-1);
			 }
			 
			 ArrayList<TwitterUserAction> retweets  = new ArrayList<TwitterUserAction>();
			 for(Status status : tweets){
				 if(status.isRetweet()){
					 retweets.add(new TwitterUserAction(status));
				 }
			 }
			 userInfo.setRetweets(retweets);
			 
			 ArrayList<TwitterUserAction> replies  = new ArrayList<TwitterUserAction>();
			 for(Status status : tweets){
				 if(status.getInReplyToStatusId() > 0){
					 replies.add(new TwitterUserAction(status));
				 }
			 }
			 userInfo.setReplies(replies);
			 
			 ArrayList<TwitterUserAction> tweetsC  = new ArrayList<TwitterUserAction>();
			 for(Status status : tweets){
				 if(status.getInReplyToStatusId() <= 0 && !status.isRetweet()){
					 tweetsC.add(new TwitterUserAction(status));
				 }
			 }
			 userInfo.setTweets(tweetsC);
			 userInfo.updateImportantTweets(numTweetsFilter);
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userInfo;
	}


	@Override
	public List<ScrappedMessageInfo> crawl(String lastCheckedTime) {
		return this.getTwitterInfo().generateScrappedMessageInfo(lastCheckedTime);
	}
	
	
}
