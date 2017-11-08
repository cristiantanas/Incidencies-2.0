package org.uab.android.incidencies.rss;

import java.util.ArrayList;

public class RSSFeed {

	private int lineId;
	private String title;
	private String description;
	private long pubDate;
	
	public RSSFeed() {
		
	}
	
	public RSSFeed(int lineId, long pubDate, String title, String description) {
		this.lineId = lineId;
		this.pubDate = pubDate;
		this.title = title;
		this.description = description;
	}
	
	public int getLineId() {
		return lineId;
	}
	
	public void setLineId(int lineId) {
		this.lineId = lineId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getPubDate() {
		return pubDate;
	}
	
	public void setPubDate(long pubDate) {
		this.pubDate = pubDate;
	}
	
	public boolean isMemberOf(ArrayList<RSSFeed> feeds) {
		for ( RSSFeed f : feeds )
			if ( lineId==f.getLineId() && pubDate==f.getPubDate() && 
					title.equalsIgnoreCase(f.getTitle()) )
				return true;
		
		return false;
	}
}
