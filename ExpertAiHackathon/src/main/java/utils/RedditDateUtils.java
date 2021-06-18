package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;  

public class RedditDateUtils {
	
	public static LocalDateTime redditTimeToLocalDateTime(String redditTime) {
		
		LocalDateTime translatedTime = null;
		LocalDateTime now = LocalDateTime.now();
		
		System.out.println(redditTime);
		
		if(redditTime.equals("") || redditTime.equals(" ")) {
			return now;
		}
		
		if(redditTime.equals("just now")){
			return now;
		}
		else {
		
			char redditTimeUnit = redditTime.substring(redditTime.length() - 1).charAt(0);
			long redditTimeValue = Long.parseLong(redditTime.substring(0, redditTime.length() - 1));
			
			switch(redditTimeUnit){
				
				case 'm': translatedTime = now.minusMinutes(redditTimeValue); break;
				
				case 'h': translatedTime = now.minusHours(redditTimeValue); break;
				
				case 'd': translatedTime = now.minusDays(redditTimeValue); break;
				
				default: translatedTime = now; 
			}
			return translatedTime;
		}
	}
	
	
	public static boolean redditTimeNewer(String redditTime, String lastCheckTime) {
		
		LocalDateTime translatedRedditTime = RedditDateUtils.redditTimeToLocalDateTime(redditTime);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime translatedLastCheckTime = LocalDateTime.parse(lastCheckTime, formatter);
		
		return translatedRedditTime.isAfter(translatedLastCheckTime);
		
	}

}
