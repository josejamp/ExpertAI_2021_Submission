package crawlers.twitter;

public class TwitterConfig {
	
	private String consumerKey;
	private String consumerSecret;
	private String authAccessToken;
	private String authAccessTokenSecret;
	
	public TwitterConfig(String consumerKey, String consumerSecret,
			String authAccessToken, String authAccessTokenSecret) {
		super();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.authAccessToken = authAccessToken;
		this.authAccessTokenSecret = authAccessTokenSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getAuthAccessToken() {
		return authAccessToken;
	}

	public void setAuthAccessToken(String authAccessToken) {
		this.authAccessToken = authAccessToken;
	}

	public String getAuthAccessTokenSecret() {
		return authAccessTokenSecret;
	}

	public void setAuthAccessTokenSecret(String authAccessTokenSecret) {
		this.authAccessTokenSecret = authAccessTokenSecret;
	}
	
	

}
