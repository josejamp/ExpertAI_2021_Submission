package utils;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import crawlers.twitter.TwitterConfig;

public class TwitterConfigReader {
	
	public static TwitterConfig readConfigFile(String filePath) throws InvalidFileFormatException, IOException {
		    
		Ini ini = new Ini(new File(filePath));
		Section twitterSection = ini.get("Twitter");
	    return new TwitterConfig(twitterSection.get("consumerKey"), twitterSection.get("consumerSecret"),
	         twitterSection.get("authAccessToken"), twitterSection.get("authAccessTokenSecret"));
	    
	 }

}
