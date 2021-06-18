package utils;

public class RedditRepliesParser {
	
	
	public static long parseReplies(String replies) {
		
		String[] reply_split = replies.split(" ");
		
		if (reply_split[0].endsWith("k")){
			
			return (long)Float.parseFloat(reply_split[0].replace("k", "")) * 1000;
			
		}
		else {
			return Long.parseLong(reply_split[0]);
		}
		
		
		
	}
	

}
