package datamodels;

public class CrawledCryptoPrice {
	
	
	private String crypto;
	private String source;
	private String price;
	private String extracted_date;
	
	
	public CrawledCryptoPrice(String crypto, String source, String price, String extracted_date) {
		super();
		this.crypto = crypto;
		this.source = source;
		this.price = price;
		this.extracted_date = extracted_date;
	}


	public String getCrypto() {
		return crypto;
	}


	public void setCrypto(String crypto) {
		this.crypto = crypto;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public String getPrice() {
		return price;
	}


	public void setPrice(String price) {
		this.price = price;
	}


	@Override
	public String toString() {
		return "CrawledCryptoPrice [crypto=" + crypto + ", source=" + source + ", price=" + price + "]";
	}
	
	
	
	
	

}
