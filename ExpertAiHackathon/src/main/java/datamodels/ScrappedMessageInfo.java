package datamodels;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class ScrappedMessageInfo {
	
	private String text;
	private String published_date;
	private String user;
	private String score;
	private String replies;
	private String partialSource;
	private String fullSource;
	private String extracted_date;
	private UUID uuid;
	private Optional<UUID> parent_uuid;
	
	private String analyzedText;
	

	public ScrappedMessageInfo(String text, String published_date, String user, String score, String comments, String partialSource,
			String fullSource, String extracted_date, Optional<UUID> parent_id) {
		super();
		this.text = text;
		this.published_date = published_date;
		this.user = user;
		this.score = score;
		this.replies = comments;
		this.partialSource = partialSource;
		this.fullSource = fullSource;
		this.extracted_date = extracted_date;
		this.uuid = java.util.UUID.randomUUID();
		this.parent_uuid = parent_id;
		
		this.analyzedText = "";
	}
	
	public ScrappedMessageInfo(String text, String published_date, String user, String score, String comments, String partialSource,
			String fullSource, String extracted_date, UUID uuid, Optional<UUID> parent_id, String analyzed_text) {
		super();
		this.text = text;
		this.published_date = published_date;
		this.user = user;
		this.score = score;
		this.replies = comments;
		this.partialSource = partialSource;
		this.fullSource = fullSource;
		this.extracted_date = extracted_date;
		this.uuid = uuid;
		this.parent_uuid = parent_id;
		
		this.analyzedText = analyzed_text;
	}


	public ScrappedMessageInfo() {
		// TODO Auto-generated constructor stub
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getPublishedDate() {
		return published_date;
	}


	public void setPublishedDate(String published_date) {
		this.published_date = published_date;
	}


	public String getPartialSource() {
		return partialSource;
	}


	public void setPartialSource(String partialSource) {
		this.partialSource = partialSource;
	}


	public String getFullSource() {
		return fullSource;
	}


	public void setFullSource(String fullSource) {
		this.fullSource = fullSource;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getScore() {
		return score;
	}


	public void setScore(String score) {
		this.score = score;
	}


	public String getComments() {
		return replies;
	}


	public void setComments(String comments) {
		this.replies = comments;
	}

	public String getPublished_date() {
		return published_date;
	}


	public void setPublished_date(String published_date) {
		this.published_date = published_date;
	}


	public String getExtracted_date() {
		return extracted_date;
	}


	public void setExtracted_date(String extracted_date) {
		this.extracted_date = extracted_date;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public Optional<UUID> getParent_uuid() {
		return parent_uuid;
	}


	public void setParent_uuid(UUID parent_uuid) {
		this.parent_uuid = Optional.of(parent_uuid);
	}
	
	public String getAnalyzedText() {
		return analyzedText;
	}


	public void setAnalyzedText(String analyzedText) {
		this.analyzedText = analyzedText;
	}


	@Override
	public String toString() {
		return "ScrappedMessageInfo [text=" + text + ", published_date=" + published_date + ", user=" + user
				+ ", score=" + score + ", replies=" + replies + ", partialSource=" + partialSource + ", fullSource="
				+ fullSource + ", extracted_date=" + extracted_date + ", uuid=" + uuid + ", parent_uuid=" + parent_uuid
				+ "]";
	}
	
	
	public void fixDates() {
	    //2021-05-25T13:13:33.627813300
		String date_format = "\\d\\d\\d\\d\\-\\d\\d\\-\\d\\dT\\d\\d\\:\\d\\d\\:\\d\\d\\.\\d*";
		if(!this.published_date.matches(date_format)) {
			this.published_date = "UNKNOWN";
		}
		if(!this.extracted_date.matches(date_format)) {
			this.extracted_date = "UNKNOWN";
		}
	}
	
	
	private static boolean isValidFormat(String format, String value, Locale locale) {
	    LocalDateTime ldt = null;
	    DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

	    try {
	        ldt = LocalDateTime.parse(value, fomatter);
	        String result = ldt.format(fomatter);
	        return result.equals(value);
	    } catch (DateTimeParseException e) {
	        try {
	            LocalDate ld = LocalDate.parse(value, fomatter);
	            String result = ld.format(fomatter);
	            return result.equals(value);
	        } catch (DateTimeParseException exp) {
	            try {
	                LocalTime lt = LocalTime.parse(value, fomatter);
	                String result = lt.format(fomatter);
	                return result.equals(value);
	            } catch (DateTimeParseException e2) {
	                // Debugging purposes
	                //e2.printStackTrace();
	            }
	        }
	    }

	    return false;
	}
	
	public static void main(String args[]){
		
		String date_format = "\\d\\d\\d\\d\\-\\d\\d\\-\\d\\dT\\d\\d\\:\\d\\d\\:\\d\\d\\.\\d*";
		if("Hola amigo".matches(date_format)) {
			System.out.println("Matches");
		}
		
	}
	
}
