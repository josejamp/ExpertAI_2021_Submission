package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 

public class YahooDateUtils {
	
	public static LocalDateTime yahooTimeToLocalDateTime(String yahooTime) {
		
		LocalDateTime translatedTime = null;
		LocalDateTime now = LocalDateTime.now();
		
		String[] parsed_date = yahooTime.split(" ");
		
		if(parsed_date.length==1) {
			if(parsed_date[0].equals("yesterday")) {
				translatedTime = now.minusDays(1);
			}
		}
		else {
			
			String yahooTimeUnit = parsed_date[1];
			long yahooTimeValue = Long.parseLong(parsed_date[0]);
			
			switch(yahooTimeUnit){
				
				case "seconds": translatedTime = now.minusSeconds(yahooTimeValue); break;
				
				case "second": translatedTime = now.minusSeconds(yahooTimeValue); break;
				
				case "minutes": translatedTime = now.minusMinutes(yahooTimeValue); break;
				
				case "minute": translatedTime = now.minusMinutes(yahooTimeValue); break;
				
				case "hours": translatedTime = now.minusHours(yahooTimeValue); break;
				
				case "hour": translatedTime = now.minusHours(yahooTimeValue); break;
				
				case "days": translatedTime = now.minusDays(yahooTimeValue); break;
				
				case "day": translatedTime = now.minusDays(yahooTimeValue); break;
				
				case "months": translatedTime = now.minusMonths(yahooTimeValue); break;
				
				case "month": translatedTime = now.minusMonths(yahooTimeValue); break;
				
				case "years": translatedTime = now.minusYears(yahooTimeValue); break;
				
				case "year": translatedTime = now.minusYears(yahooTimeValue); break;
				
				default: translatedTime = now; 
			}
			
		}
		return translatedTime;
	}
	
	
	public static boolean yahooTimeNewer(String yahooTime, String lastCheckTime) {
		
		LocalDateTime translatedYahooTime = YahooDateUtils.yahooTimeToLocalDateTime(yahooTime);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime translatedLastCheckTime = LocalDateTime.parse(lastCheckTime, formatter);
		
		return translatedYahooTime.isAfter(translatedLastCheckTime);
		
	}

}
