package crawlers.twitter;

import twitter4j.Status;

import java.util.Date;

public class TwitterUserAction implements Comparable<TwitterUserAction>{
	
	private String text;
	private int timesRetweeted;
	private int timesFavourited;
	private Date date;
	
	public TwitterUserAction(String text, int timesRetweeted,
			int timesFavourited, Date date) {
		super();
		this.text = text;
		this.timesRetweeted = timesRetweeted;
		this.timesFavourited = timesFavourited;
		this.date = date;
	}
	
	public TwitterUserAction(Status status){
		this(status.getText(), status.getRetweetCount(), status.getFavoriteCount(), status.getCreatedAt());
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getTimesRetweeted() {
		return timesRetweeted;
	}

	public void setTimesRetweeted(int timesRetweeted) {
		this.timesRetweeted = timesRetweeted;
	}

	public int getTimesFavourited() {
		return timesFavourited;
	}

	public void setTimesFavourited(int timesFavourited) {
		this.timesFavourited = timesFavourited;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void updateWithTwitterStatus(Status status) {
	    text = status.getText();
	    timesRetweeted = status.getRetweetCount();
	    timesFavourited = status.getFavoriteCount();
	    date = status.getCreatedAt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + timesFavourited;
		result = prime * result + timesRetweeted;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitterUserAction other = (TwitterUserAction) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (timesFavourited != other.timesFavourited)
			return false;
		if (timesRetweeted != other.timesRetweeted)
			return false;
		if (!date.equals(other.date))
			return false;
		return true;
	}
	
	
	public static int compareImportance(TwitterUserAction ua1, TwitterUserAction ua2 ) {
		return Integer.compare(ua1.timesFavourited+ua1.timesRetweeted, ua2.timesFavourited+ua2.timesRetweeted);
	}

	@Override
	public int compareTo(TwitterUserAction arg0) {
		return compareImportance(this, arg0);
	}
	

}
