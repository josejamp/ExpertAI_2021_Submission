package crawlers;

import datamodels.ScrappedMessageInfo;
import java.util.List;

public interface DateFilteredMessageCrawler {

	
	public List<ScrappedMessageInfo> crawl(String lastCheckedTime);
	
	
}
