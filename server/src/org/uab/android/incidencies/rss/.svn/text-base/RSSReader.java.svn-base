package org.uab.android.incidencies.rss;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.TimerTask;

import org.uab.android.incidencies.database.DataBaseConnection;
import org.uab.android.incidencies.database.DataBaseUtils;
import org.uab.android.incidencies.utils.DailyLog;
import org.uab.android.incidencies.utils.Utils;

public class RSSReader extends TimerTask {

	public static final int MIN_TO_MILIS = 60 * 1000;
	public static final int UPDATE_TASK_DELAY = 0 * MIN_TO_MILIS;
	public static final int UPDATE_TASK_PERIOD = 10 * MIN_TO_MILIS;
	
	private DataBaseConnection db = new DataBaseConnection();
	
	@Override
	public void run() {
		try {
			ArrayList<String> insertQueries = new ArrayList<String>();
			ArrayList<String> updateQueries = new ArrayList<String>();
			XMLRSSSourcesParser sParser = new XMLRSSSourcesParser();
			XMLRSSParser rParser = new XMLRSSParser();
			ArrayList<RSSSource> sources = sParser.parse(
					new InputStreamReader(new FileInputStream("rss_sources.xml"))
					);
			ArrayList<RSSFeed> dbFeeds = DataBaseUtils.getActiveFeeds();
			ArrayList<RSSFeed> newFeeds = new ArrayList<RSSFeed>();
			
			for ( RSSSource feedUrl : sources ) {
				URL url = new URL(feedUrl.getUrl());
				ArrayList<RSSFeed> feeds = rParser.parse(
						new InputStreamReader(url.openStream())
						);
				
				for ( RSSFeed feed : feeds ) {
					feed.setLineId(feedUrl.getLineId());
					newFeeds.add(feed);
				}
			}
			
			for ( RSSFeed feed : newFeeds ) {
				if ( !feed.isMemberOf(dbFeeds) ) {
					String insertStmt = "INSERT INTO RSS_SOURCE_INCIDENTS " +
							"(service, line_id, time, rss_source, title, description, status) VALUES " +
							"(" + Utils.RENFE_INCIDENTS + ", " + 
								  feed.getLineId() + ", " + 
								  feed.getPubDate() + ", " + 
								  "\"generalitat\"" + ", " + 
								  "\"" + feed.getTitle() + "\"" + ", " +
								  "\"" + feed.getDescription() + "\"" + ", " + 
								  1 + 
							")";
					insertQueries.add(insertStmt);
				}
			}
			
			for ( RSSFeed feed : dbFeeds ) {
				if ( !feed.isMemberOf(newFeeds) ) {
					String updateStmt = "UPDATE RSS_SOURCE_INCIDENTS SET status=-1 " +
							"WHERE line_id=" + feed.getLineId() + 
							" AND time=" + feed.getPubDate() + 
							" AND title=\"" + feed.getTitle() + "\"";
					updateQueries.add(updateStmt);
				}
			}
			
			db.connect();
			
			for ( String q : insertQueries ) 
				db.insert(q);
			
			for ( String q : updateQueries )
				db.insert(q);
			
			db.close();
			
		} catch (Exception e) {
			DailyLog.e("Error with RSSReader - " + e.getMessage());
		}
	}

}
