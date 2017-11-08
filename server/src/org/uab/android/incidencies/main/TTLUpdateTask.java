package org.uab.android.incidencies.main;

import java.util.TimerTask;

import org.uab.android.incidencies.database.DataBaseConnection;
import org.uab.android.incidencies.utils.Constants;
import org.uab.android.incidencies.utils.DailyLog;

public class TTLUpdateTask extends TimerTask {
	public static final int MIN_TO_MILIS = 60 * 1000;
	public static final int UPDATE_TASK_DELAY = 10 * MIN_TO_MILIS;
	public static final int UPDATE_TASK_PERIOD = 10 * MIN_TO_MILIS;
	private DataBaseConnection db = new DataBaseConnection();

	@Override
	public void run() {
		try {
			db.connect();
			
			db.execute("LOCK TABLES INCIDENTS WRITE");
			db.insert("UPDATE INCIDENTS SET ttl = ttl - (" + 
					System.currentTimeMillis() + " - last_modified) / " + MIN_TO_MILIS + "," +
							"last_modified = " + System.currentTimeMillis() + " WHERE ttl > 0");
			db.insert("UPDATE INCIDENTS SET status = " + Constants.TTL_EXPIRED + 
					" WHERE ttl <= 0");
			db.execute("UNLOCK TABLES");
			
			db.execute("LOCK TABLES GENERAL_INCIDENTS WRITE");
			db.insert("UPDATE GENERAL_INCIDENTS SET ttl = ttl - (" + 
					System.currentTimeMillis() + " - last_modified) / " + MIN_TO_MILIS + "," +
							"last_modified = " + System.currentTimeMillis() + " WHERE ttl > 0");
			db.insert("UPDATE GENERAL_INCIDENTS SET status = " + Constants.TTL_EXPIRED + 
					" WHERE ttl <= 0");
			db.execute("UNLOCK TABLES");
			
			db.execute("LOCK TABLES T11 WRITE");
			db.insert("UPDATE T11 set eta = eta - (" +
					System.currentTimeMillis() + " - last_modified) / " + MIN_TO_MILIS + "," +
							"last_modified = " + System.currentTimeMillis() + " WHERE eta > 0");
			db.insert("UPDATE T11 SET status = -1 WHERE eta <= 0");
			db.execute("UNLOCK TABLES");
			
			db.close();
			
		} catch (Exception e) {
			DailyLog.e("Error with TTLUpdateTask - " + e.getMessage());
		}
	}

}
