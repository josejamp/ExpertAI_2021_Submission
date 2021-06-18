package crawlers.twitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import datamodels.ScrappedMessageInfo;
import twitter4j.User;

public class TwitterUserInfo {
	
	private String screenName;
	private String fullName;
	private Date dateCreated;
	private int followersNumber;
	private int friendsCount;
	
	private ArrayList<TwitterUserAction> tweets;
	private ArrayList<TwitterUserAction> retweets;
	private ArrayList<TwitterUserAction> replies;
	
	public TwitterUserInfo(String screenName, String fullName,
			Date dateCreated, int followersNumber, int friendsCount,
			ArrayList<TwitterUserAction> tweets,
			ArrayList<TwitterUserAction> retweets,
			ArrayList<TwitterUserAction> replies) {
		super();
		this.screenName = screenName;
		this.fullName = fullName;
		this.dateCreated = dateCreated;
		this.followersNumber = followersNumber;
		this.friendsCount = friendsCount;
		this.tweets = tweets;
		this.retweets = retweets;
		this.replies = replies;
	}

	public TwitterUserInfo(String screenName, String fullName,
			Date dateCreated, int followersNumber, int friendsCount) {
		super();
		this.screenName = screenName;
		this.fullName = fullName;
		this.dateCreated = dateCreated;
		this.followersNumber = followersNumber;
		this.friendsCount = friendsCount;
	}
	
	public TwitterUserInfo(User user){
		this(user.getScreenName(), user.getName(), user.getCreatedAt(), user.getFollowersCount(), user.getFriendsCount());
	}

	public TwitterUserInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public int getFollowersNumber() {
		return followersNumber;
	}

	public void setFollowersNumber(int followersNumber) {
		this.followersNumber = followersNumber;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	public ArrayList<TwitterUserAction> getTweets() {
		return tweets;
	}

	public void setTweets(ArrayList<TwitterUserAction> tweets) {
		this.tweets = tweets;
	}

	public ArrayList<TwitterUserAction> getRetweets() {
		return retweets;
	}

	public void setRetweets(ArrayList<TwitterUserAction> retweets) {
		this.retweets = retweets;
	}

	public ArrayList<TwitterUserAction> getReplies() {
		return replies;
	}

	public void setReplies(ArrayList<TwitterUserAction> replies) {
		this.replies = replies;
	}
	
	public void updateWithTwitterUser(User user) {
	    screenName = user.getScreenName();
	    fullName = user.getName();
	    dateCreated = user.getCreatedAt();
	    followersNumber = user.getFollowersCount();
	    friendsCount = user.getFriendsCount();
	}
	
	public void updateImportantTweets(int max) {
		ArrayList<TwitterUserAction> copy = new ArrayList<TwitterUserAction>();
	    copy.addAll(TwitterUserInfo.filterBestTweets(tweets, max));
	    tweets = new ArrayList<TwitterUserAction>(copy.stream().distinct().collect(Collectors.toList()));
	}
	
	public List<ScrappedMessageInfo> generateScrappedMessageInfo(String lastCheckedTime) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime translatedLastCheckTime = LocalDateTime.parse(lastCheckedTime, formatter);
		
		List<ScrappedMessageInfo> scrappedMessages = new ArrayList<ScrappedMessageInfo>();
		
		for(TwitterUserAction tua : tweets) {
			if(convertToLocalDateTimeViaInstant(tua.getDate()).isAfter(translatedLastCheckTime)) {
				scrappedMessages.add( new ScrappedMessageInfo(tua.getText(), convertToLocalDateTimeViaInstant(tua.getDate()).toString(), 
						this.fullName, Integer.toString(tua.getTimesFavourited()), Integer.toString(tua.getTimesRetweeted()), 
						"https://twitter.com/", "https://twitter.com/" + this.screenName,
						LocalDateTime.now().toString(), Optional.empty()) );
			}
		}
		
		for(TwitterUserAction tua : replies) {
			if(convertToLocalDateTimeViaInstant(tua.getDate()).isAfter(translatedLastCheckTime)) {
				scrappedMessages.add( new ScrappedMessageInfo(tua.getText(), convertToLocalDateTimeViaInstant(tua.getDate()).toString(), 
						this.fullName, Integer.toString(tua.getTimesFavourited()), Integer.toString(tua.getTimesRetweeted()), 
						"https://twitter.com/", "https://twitter.com/" + this.screenName,
						LocalDateTime.now().toString(), Optional.empty()) );
			}
		}
		
		return scrappedMessages;
		
	}
	
	public static ArrayList<TwitterUserAction> filterBestTweets(ArrayList<TwitterUserAction> tweets, int max) {
		ArrayList<TwitterUserAction> copy = new ArrayList<TwitterUserAction>(tweets);
		Collections.sort(copy);
		int limit = max;
		if(copy.size() < max) {
			limit = copy.size();
		}
		return new ArrayList<TwitterUserAction>(copy.subList(0, limit));
	}
	
	public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDateTime();
	}
		
}