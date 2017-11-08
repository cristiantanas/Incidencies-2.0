package org.uab.android.incidencies.database;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.uab.android.incidencies.rss.RSSFeed;
import org.uab.android.incidencies.utils.Constants;
import org.uab.android.incidencies.utils.GeneralNotification;
import org.uab.android.incidencies.utils.Incident;
import org.uab.android.incidencies.utils.IncidentClassification;
import org.uab.android.incidencies.utils.IncidentNotification;
import org.uab.android.incidencies.utils.Utils;

public class DataBaseUtils {
	
	static boolean mutex = true;
	
	private static DataBaseConnection db = new DataBaseConnection();
	private static Random generator = new Random();
	private static final int MAX_RNG = (int) Math.pow(2, 128);

	public static int getUserId(String username, int version) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		int userId = obtainUserId(username, version);
		db.close();
		
		return userId;
	}
	
	public static int obtainUserId(String username, int version)
						throws SQLException, ClassNotFoundException {
		int userId = -1;
		
		String selectStmt = "SELECT id, version " +
							"FROM USERS " +
							"WHERE username = \'" + username + "\' OR email = \'" + username + "\'";
		ResultSet rs = db.query(selectStmt);
		if (rs.next()) {
			userId = rs.getInt(1);
			if ( version!=rs.getInt(2) ) {
				db.insert("UPDATE USERS SET version=" + version + ", versionUpdate=CURRENT_TIMESTAMP WHERE id=" + userId);
			}
		}
		else {
			rs = db.query("SELECT MAX(id) FROM USERS");
			rs.next();
			userId = rs.getInt(1) + 1;
			int OS = username.startsWith("i.") ? 3 : 1;
			
			String insertStmt = "INSERT INTO USERS (id, username, OS, version, versionUpdate, created) VALUES " +
								"(" + userId + "," +
									"\'" + username + "\'" + "," +
									OS + "," +
									version + "," +
									"CURRENT_TIMESTAMP" + "," +
									"NULL" + 
								")";
			db.insert(insertStmt);
		}
		
		return userId;
	}
	
	public static int submitReport(int version, IncidentNotification not, int status) 
			throws SQLException, ClassNotFoundException {
		db.connect();
		db.setAutoCommit(false);
		
		int userId = obtainUserId(not.getUsername(), version);
			
		String coordsSelect = 
				"SELECT S1.id, S1.latitude, S1.longitude " +
				"FROM STATION_LOCATION AS S1, STATION_CLASSIFICATION AS S2 " +
				"WHERE S1.id = S2.station AND S2.line_id = " + not.getUniqueLineId() + " AND S1.hashVal = \'" + not.getStationHashValue() + "\'";
		ResultSet rs = db.query(coordsSelect);
			
		if (rs.next()) {
			try {
				int newId = getNewEventId("INCIDENTS");
				int ttl = 10 + (not.getSeverity() * 20);
				String insertStmt = 
					"INSERT INTO INCIDENTS " +
					"(id, latitude, longitude, cause, severity, status, ttl, last_modified, reportTime) VALUES " +
					"(" + newId + "," +
						  rs.getDouble(2) + "," + 
						  rs.getDouble(3) + "," + 
						  not.getCause() + "," + 
						  not.getSeverity() + "," +
						  status + "," + 
						  ttl + "," +
						  System.currentTimeMillis() + "," +
						  "FROM_UNIXTIME(" + not.getTime()/1000 + ")" +
					")";
				db.insert(insertStmt);
					
				insertStmt = "INSERT INTO INCIDENT_NOTIFICATIONS " +
					"(time,description,user_id,p_event) VALUES " +
					"(" + not.getTime() + "," + 
						  "\"" + not.getDescription() + "\"" + "," +
						  userId + "," + 
						  newId + 
					")";
				db.insert(insertStmt);
					
				insertStmt = "INSERT INTO INCIDENT_CLASSIFICATION " +
						"(service, line, station, direction, incident) VALUES " +
						"(" + not.getTransportService() + "," +
							  not.getUniqueLineId() + "," + 
							  rs.getInt(1) + "," + 
							  not.getDirection() + "," + 
							  newId + 
						")";
				db.insert(insertStmt);
				db.commit();
				
				db.close();
				return newId;
					
			} catch (SQLException e) {
				db.rollback();
				db.close();
				throw e;
			}
				
		} else {
			db.close();
			throw new SQLException("No data available for station with hash value " +
					not.getStationHashValue() + " on line " + not.getUniqueLineId());
		}
	}
	
	public static int submitReport(int version, GeneralNotification not, int status) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		db.setAutoCommit(false);
		
		int userId = obtainUserId(not.getUsername(), version);
		try {
			int newId = getNewEventId("GENERAL_INCIDENTS");
			int ttl = 10 + (not.getSeverity() * 20);
			String insertStmt = 
				"INSERT INTO GENERAL_INCIDENTS " +
				"(id, latitude, longitude, severity, status, ttl, last_modified, reportTime) VALUES " + 
				"(" + newId + "," +
					  not.getLatitude() + "," + 
					  not.getLongitude() + "," + 
					  not.getSeverity() + "," +
					  status + "," + 
					  ttl + "," +
					  System.currentTimeMillis() + "," +
					  "FROM_UNIXTIME(" + not.getLastUpdateTime()/1000 + ")" +
				")";
			db.insert(insertStmt);
			
			insertStmt = "INSERT INTO GENERAL_INCIDENT_NOTIFICATIONS " +
				"(time,description,user_id,p_event) VALUES " +
				"(" + not.getLastUpdateTime() + "," + 
				  	"\"" + not.getDescription() + "\"" + "," +
				  	userId + "," + 
				  	newId + 
				")";
			db.insert(insertStmt);
			db.commit();
			
			db.close();
			return newId;
			
		} catch (SQLException e) {
			db.rollback();
			db.close();
			throw e;
		}
	}
	
	public static int recordT11(int version, GeneralNotification not) throws SQLException, ClassNotFoundException {
		db.connect();
		
		int userId = obtainUserId(not.getUsername(), version);
		try {
			int newId = getNewEventId("T11");
			int status = not.getSeverity() > 0 ? 1 : -1;
			String insertStmt = "INSERT INTO T11 VALUES " +
					"(" + newId + "," +
						  "\"" + not.getDescription() + "\"" + "," +
						  not.getLatitude() + "," + 
						  not.getLongitude() + "," +
						  "\"" + not.getLineReference() + "\"" + "," +
						  not.getSeverity() + "," +
						  status + "," +
						  System.currentTimeMillis() + "," +
						  System.currentTimeMillis() + "," +
						  userId + 
					")";
			db.insert(insertStmt);
			
			db.close();
			return newId;
			
		} catch (SQLException e) {
			db.close();
			throw e;
		}
	}
	
	public static void updateReportList(int version, IncidentNotification not, int parent)
						throws SQLException, ClassNotFoundException {
		db.connect();
		
//		String userIdSelect = "SELECT id FROM USERS WHERE username=\'" + not.getUsername() + "\'";
//		ResultSet rs = db.query(userIdSelect);
		
//		if (rs.next()) {
		int userId = obtainUserId(not.getUsername(), version);
		
		/** Insert a new event in the INCIDENT_NOTIFICATION table corresponding to 'parent' event */
		String insertStmt = "INSERT INTO INCIDENT_NOTIFICATIONS " + 
			"(time,description,user_id,p_event) VALUES " +
			"(" + not.getTime() + "," + 
				  not.getDescription() + "," + 
				  userId + "," + 
				  parent + 
			")";
		db.insert(insertStmt);
		db.close();
			
//		} else {
//			db.close();
//			throw new SQLException("The user with username = " + not.getUsername() +
//					"doesn't exists in our database!");
//		}
	}
	
	public static void updateReportStatus(int id, int status) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String updateStmt = "UPDATE INCIDENTS SET status = " + status + 
							" WHERE id = " + id;
		db.insert(updateStmt);
		db.close();
	}
	
	public static void updateGenIncStatus(int id, int status) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String updateStmt = "UPDATE GENERAL_INCIDENTS SET status=" + status + 
							" WHERE id=" + id;
		db.insert(updateStmt);
		db.close();
	}
	
	public static void updateReportTTL(int id, int TTL, long lastModified)
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String updateStmt = "UPDATE INCIDENTS SET ttl = " + TTL + 
					", reportTime = FROM_UNIXTIME(" + lastModified/1000 + ") WHERE id = " + id;
		db.insert(updateStmt);
		db.close();
	}
	
	public static void updateGenIncTTL(int id, int ttl, long lastModified) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String updateStmt = "UPDATE GENERAL_INCIDENTS SET ttl=" + ttl + 
					", reportTime = FROM_UNIXTIME(" + lastModified/1000 + ") WHERE id = " + id;
		db.insert(updateStmt);
		db.close();
	}
	
	private static int getNewEventId(String table) throws SQLException, ClassNotFoundException {
		int newId = generator.nextInt(MAX_RNG);
		
		String selectStmt = "SELECT id FROM " + table + " WHERE id = ";
		ResultSet rs = db.query(selectStmt + newId);
		
		while (rs.next()) {
			++newId;
			rs = db.query(selectStmt + newId);
		}
		
		return newId;
	}
	
	public static Vector<Integer> getUserConfirmationList(int eventId) 
							throws SQLException, ClassNotFoundException {
		db.connect();
		Vector<Integer> userList = new Vector<Integer>();
		
		String selectStmt = "SELECT user_id FROM INCIDENT_NOTIFICATIONS WHERE p_event = " + eventId;
		ResultSet rs = db.query(selectStmt);
		
		while (rs.next()) {
			userList.add(rs.getInt(1));
		}
		db.close();
		
		return userList;
	}
	
	public static Vector<Integer> getGeneralConfirmationList(int eventId) 
									throws SQLException, ClassNotFoundException {
		db.connect();
		Vector<Integer> userList = new Vector<Integer>();
		
		String selectStmt = "SELECT user_id FROM GENERAL_INCIDENT_NOTIFICATIONS WHERE p_event = " + eventId;
		ResultSet rs = db.query(selectStmt);
		
		while (rs.next()) {
			userList.add(rs.getInt(1));
		}
		db.close();
		
		return userList;
	}
	
	public static List<IncidentClassification> getReports() 
					throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
		db.connect();
		ArrayList<IncidentClassification> dbRows = new ArrayList<IncidentClassification>();
		
		String selectStmt = "SELECT I1.id, I1.cause, I2.service, I2.line, S1.hashVal " +
							"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
							"WHERE I1.id = I2.incident AND I2.station = S1.id AND (I1.status = 0 OR I1.status = 1)";
		ResultSet rs = db.query(selectStmt);
	
		while (rs.next()) {
			IncidentClassification dbReg = new IncidentClassification(
					rs.getInt(3),
					rs.getInt(4),
					new String(rs.getBytes(5), "UTF-8"), 
					rs.getInt(2),
					rs.getInt(1));
			dbRows.add(dbReg);
		}
		db.close();
		
		return dbRows;
	}
	
	public static Incident getReport(int reportId) 
					throws SQLException, ClassNotFoundException {
		db.connect();
		String selectStmt = 
			"SELECT I1.id,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime) " + 
			"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2 " +
			"WHERE I1.id = I2.incident AND I1.id = " + reportId;
		ResultSet rs = db.query(selectStmt);
		
		if (rs.next()) {
			Incident incident = new Incident(
					rs.getInt(1),
					-1,
					-1,
					-1,
					-1,
					null, 
					rs.getInt(2), 
					rs.getInt(3), 
					rs.getInt(4),
					rs.getInt(5), 
					rs.getLong(6)*1000);
			
			db.close();
			return incident;
			
		} else {
			db.close();
			throw new SQLException("The report with report ID = " +  reportId + 
					" does not exist in the database!");
		}
	}
	
	public static GeneralNotification getGeneralNotification(int incidentId) 
										throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = 
			"SELECT G1.id, G1.severity, G1.status, G1.ttl, UNIX_TIMESTAMP(G1.reportTime) " +
			"FROM GENERAL_INCIDENTS AS G1 " +
			"WHERE G1.id=" + incidentId;
		ResultSet rs = db.query(selectStmt);
		
		if ( rs.next() ) {
			GeneralNotification incident = new GeneralNotification(
					rs.getInt(1), 
					null, 
					rs.getInt(2), 
					rs.getInt(4), 
					rs.getLong(5)*1000, 
					null, 
					.0, 
					.0, 
					rs.getInt(3));
			
			db.close();
			return incident;
			
		} else {
			db.close();
			throw new SQLException("No record with ID = " +  incidentId + 
					" exist in the database!");
		}
	}
	
	public static double getUserReputation(String username)
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		double reputation;
		String selectStmt = "SELECT reputation " +
							"FROM USERS " +
							"WHERE username = \'" + username + "\' OR email = \'" + username + "\'";
		ResultSet rs = db.query(selectStmt);
		
		if (rs.next()) {
			reputation = rs.getDouble(1);		} 
		
		else {
			reputation = 0;
			int OS = username.startsWith("i.%") ? 3 : 1;
			String insertStmt = "INSERT INTO USERS (username, OS, created) VALUES " +
								"(" + "\'" + username + "\'" + "," +
									OS + "," +
									"NULL" + 
								")";
			db.insert(insertStmt);
		}
		db.close();
		
		return reputation;
	}
	
	public static int getParentElements(int id)
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT count(*) FROM INCIDENT_NOTIFICATIONS WHERE p_event = " + id;
		ResultSet rs = db.query(selectStmt);
		rs.next();
		
		return rs.getInt(1);
	}
	
	public static int getGenNotConfirmations(int id) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = 
			"SELECT count(*) " +
			"FROM GENERAL_INCIDENT_NOTIFICATIONS " +
			"WHERE p_event=" + id;
		ResultSet rs = db.query(selectStmt);
		rs.next();
		
		return rs.getInt(1);
	}
	
	public static int[] getTransactionCount(String username)
							throws SQLException, ClassNotFoundException {
		db.connect();
		
		int[] trans = new int[2];
		String selectStmt = "SELECT positive_tran, negative_tran " +
							"FROM USERS " +
							"WHERE username = \'" + username + "\'";
		ResultSet rs = db.query(selectStmt);
		
		if (rs.next()) {
			trans[0] = rs.getInt(1); trans[1] = rs.getInt(2);		} 
		
		else {
			trans[0] = 0; trans[1] = 0;
			int OS = username.startsWith("i.%") ? 3 : 1;
			String insertStmt = "INSERT INTO USERS (username, OS, created) VALUES " +
								"(" + "\'" + username + "\'" + "," +
									OS + "," +
									"NULL" + 
								")";
			db.insert(insertStmt);
		}
		db.close();
		
		return trans;
	}
	
	public static void updateUserReputation(int version, String username, double reputation, 
			int[] transactions) throws SQLException, ClassNotFoundException {
		db.connect();
		
//		String userIdSelect = "SELECT id FROM USERS WHERE username=\'" + username + "\'";
//		ResultSet rs = db.query(userIdSelect);
		
//		if (rs.next()) {
		int userId = obtainUserId(username, version);
		
		String updateStmt = "UPDATE USERS SET reputation = " + reputation + ", " + 
							"positive_tran = " + transactions[0] + ", " + 
							"negative_tran = " + transactions[1] + " " + 
							"WHERE id = " + userId;
		db.insert(updateStmt);
		db.close();
			
//		} else {
//			db.close();
//			throw new SQLException("The user with username = " + username +
//					"doesn't exists in our database!");
//		}
	}
	
	public static int updateUserAccount(String username, int version, String email, String passwd) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		int userId = obtainUserId(username, version);
		String updateStmt = "UPDATE USERS SET " +
							"email = \'" + email + "\'" + "," + 
							"passwd = md5(\'" + passwd + "\') " +
							"WHERE id = " + userId;
		db.insert(updateStmt);
		db.close();
		return Utils.ACCOUNT_CORECTLY_UPDATED;
	}
	
	public static void addConfirmation(Incident incident, int userId, String comment) 
						throws SQLException, ClassNotFoundException {
		db.connect();
			
		/** Insert a new event in the INCIDENT_NOTIFICATIONS table corresponding to 'parent' event */
		String insertStmt = "INSERT INTO INCIDENT_NOTIFICATIONS " + 
			"(time, description, user_id, p_event, confirmation) VALUES " +
			"(" + incident.getLastUpdateTime() + "," +  
				  "\'" + comment + "\'" + "," +
				  userId + "," + 
				  incident.getId() + "," + 
				  1 + 
			")";
		db.insert(insertStmt);
		db.close();
	}
	
	public static void addConfirmation(GeneralNotification incident, int userId, String comment) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String insertStmt = 
			"INSERT INTO GENERAL_INCIDENT_NOTIFICATIONS " + 
			"(time, description, user_id, p_event, confirmation) VALUES " +
			"(" + incident.getLastUpdateTime() + ", " + 
				  "\'" + comment + "\'" + ", " + 
				  userId + ", " + 
				  incident.getId() + ", " + 
				  1 + 
			")";
		db.insert(insertStmt);
		
		db.close();
	}
	
	public static void t11HasBeenPickedUp(int t11Id, long currentTime) 
						throws SQLException, ClassNotFoundException {
		db.connect();

		String updateStmt =
			"UPDATE T11 SET eta=0, status=-1, last_modified=" + currentTime + " " +
			"WHERE id=" + t11Id;
		db.insert(updateStmt);

		db.close();
	}

	public static void t11PickUpRegister(int t11Id, String username, int version, long currentTime) 
					throws SQLException, ClassNotFoundException {
		db.connect();
	
		int userId = obtainUserId(username, version);
	
		String insertStmt = 
			"INSERT INTO T11_PICKED_UP " +
			"(t11_id, time, user_id) VALUES " +
			"(" + t11Id + ", " + 
				  currentTime + ", " +
				  userId + 
			")";
		db.insert(insertStmt);
	
		db.close();
	}

	public static int authenticateUser(String username, String password) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String userIdSelect = "SELECT id FROM USERS WHERE username=\'" + username + "\'";
		ResultSet rs = db.query(userIdSelect);
		
		if (rs.next()) {
			String userPasswdSelect = "SELECT id FROM USERS WHERE username=\'" + username + "\' " +
					"AND passwd = md5(\'" + password + "\')";
			rs = db.query(userPasswdSelect);
			
			if (rs.next()) { 
				db.close(); 
				return Constants.USER_ACCESS_GRANTED; 
			}
			
			db.close();
			return Constants.USER_PASSWORD_FAILED;
		}
		
		db.close();
		return Constants.USER_UNAUTHORIZED;
	}
	
	public static String getEventsDescription(int eventType) 
							throws SQLException, ClassNotFoundException {
		if (eventType == Constants.ALL_EVENTS) {
			JSONArray jArray = new JSONArray();
			for (int i : Constants.POSSIBLE_EVENT_TYPES) {
				JSONArray jsonData = getPrintableEvents(i);
				jArray.put(jsonData);					
			}
			return jArray.toString();
			
		} else {
			return getPrintableEvents(eventType).toString();
		}
	}
	
	public static String getJSONEncodedIncidents(int lineId) 
							throws SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		if (lineId == Utils.ALL_INCIDENTS) {
			return getAllIncidents();		}
		
		db.connect();
		
		boolean returnsValue = false;
		
		JSONArray jArray = new JSONArray();
		JSONArray incArray = new JSONArray();
		JSONArray rssArray = new JSONArray();
		
		String selectStmt = 
			"SELECT I1.id,I1.latitude,I1.longitude,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime),I2.service,I2.line,I2.direction,S1.name " +
			"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
			"WHERE I1.id=I2.incident AND I2.station=S1.id AND I2.line=" + lineId + " AND (I1.status=0 OR I1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		incArray.put(lineId);
		while ( rs.next() ) {
			returnsValue = true;
			
			int id = rs.getInt(1);
			JSONObject json = new JSONObject();
			json.put("id", 			id);
			json.put("latitude", 	rs.getDouble(2));
			json.put("longitude", 	rs.getDouble(3));
			json.put("cause", 		rs.getInt(4));
			json.put("severity", 	rs.getInt(5));
			json.put("status", 		rs.getInt(6));
			json.put("ttl", 		rs.getInt(7));
			json.put("lastUpdate",	rs.getLong(8)*1000);
			json.put("service", 	rs.getInt(9));
			json.put("line", 		rs.getInt(10));
			json.put("direction", 	rs.getInt(11));
			json.put("station", 	new String(rs.getBytes(12), "UTF-8"));
			
			selectStmt = 
				"SELECT description " +
				"FROM INCIDENT_NOTIFICATIONS " +
				"WHERE p_event=" + id + " AND description <> \'null\'";
			ResultSet res = db.query(selectStmt);
			if (res.next()) {
				json.put("description", new String(res.getBytes(1), "UTF-8"));			}
			
			else {
				json.put("description", "");			}
			
			incArray.put(json);
		}
		
		selectStmt = 
			"SELECT R.id, R.service, R.line_id, R.time, R.rss_source, R.title, R.description " + 
			"FROM RSS_SOURCE_INCIDENTS AS R " + 
			"WHERE R.line_id=" + lineId + " AND R.status=1";
		rs = db.query(selectStmt);
		
		rssArray.put(Utils.RSS_FEEDS);
		while ( rs.next() ) {
			returnsValue = true;
			
			JSONObject json = new JSONObject();
			json.put("id", 				rs.getInt(1));
			json.put("service", 		rs.getInt(2));
			json.put("line", 			rs.getInt(3));
			json.put("time",            rs.getLong(4));
			json.put("source",          new String(rs.getBytes(5), "UTF-8"));
			json.put("title",         	new String(rs.getBytes(6), "UTF-8"));
			json.put("description", 	new String(rs.getBytes(7), "UTF-8"));
			
			rssArray.put(json);
		}
		
		jArray.put(incArray);
		jArray.put(rssArray);
		
		db.close();
		
		String encodedIncidents = returnsValue ? "1#"+jArray.toString() : jArray.toString();
		return encodedIncidents;
	}
	
	public static String getJSONEncGeneralIncidents() throws SQLException, ClassNotFoundException {
		db.connect();
		
		boolean returnsValue = false;
		
		JSONArray jArray = new JSONArray();
		String selectStmt = 
			"SELECT G1.id, G1.latitude, G1.longitude, G1.severity, G1.status, G1.ttl, UNIX_TIMESTAMP(G1.reportTime), G2.description, U1.username " +
			"FROM GENERAL_INCIDENTS AS G1, GENERAL_INCIDENT_NOTIFICATIONS AS G2, USERS AS U1 " +
			"WHERE G1.id=G2.p_event AND G2.user_id=U1.id AND G2.confirmation=0 AND (G1.status=0 OR G1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		while( rs.next() ) {
			returnsValue = true;
			
			JSONObject json = new JSONObject();
			json.put("id", rs.getInt(1));
			json.put("latitude", rs.getDouble(2));
			json.put("longitude", rs.getDouble(3));
			json.put("severity", rs.getInt(4));
			json.put("status", rs.getInt(5));
			json.put("ttl", rs.getInt(6));
			json.put("lastUpdate", rs.getLong(7)*1000);
			json.put("description", rs.getString(8));
			json.put("username", rs.getString(9));
			
			jArray.put(json);
		}
		
		db.close();
		
		String encodedIncidents = returnsValue ? "1#"+jArray.toString() : jArray.toString();
		return encodedIncidents;
	}
	
	public static String getAvailableT11() throws SQLException, ClassNotFoundException {
		db.connect();
		
		boolean returnsValue = false;
		
		JSONArray t11Array = new JSONArray();
		String selectStmt = 
			"SELECT T1.id, T1.description, T1.latitude, T1.longitude, T1.source_line, T1.eta, T1.last_modified, U1.username " +
			"FROM T11 as T1, USERS as U1 " +
			"WHERE T1.user_id=U1.id AND T1.status=1";
		
		ResultSet rs = db.query(selectStmt);
		while ( rs.next() ) {
			returnsValue = true;
			
			JSONObject T11 = new JSONObject();
			T11.put("id", 			rs.getInt(1));
			T11.put("desc", 		rs.getString(2));
			T11.put("latitude", 	rs.getDouble(3));
			T11.put("longitude", 	rs.getDouble(4));
			T11.put("line_ref", 	rs.getString(5));
			T11.put("eta", 			rs.getInt(6));
			T11.put("lastUpdate", 	rs.getLong(7));
			T11.put("username", 	rs.getString(8));
			
			t11Array.put(T11);
		}
		
		db.close();
		
		String encodedT11 = returnsValue ? "1#"+t11Array.toString() : t11Array.toString();
		return encodedT11;
	}
	
	public static boolean mustNotifyUser(String username, int version) throws SQLException, ClassNotFoundException {
		db.connect();
		
		boolean notify = false;
		
		int userId = obtainUserId(username, version);
		String searchStmt = 
			"SELECT * " +
			"FROM T11_PICKED_UP " +
			"WHERE (" +
					"SELECT id " +
					"FROM T11 " +
					"WHERE user_id=" + userId + " " +
					"ORDER BY received_at DESC LIMIT 1" +
				  ")=t11_id";
		
		ResultSet rs = db.query(searchStmt);
		if ( rs.next() ) notify = true;
		
		db.close();
		return notify;
	}

	
	public static String getJSONEncAllIncidents() 
							throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
		db.connect();
		
		boolean returnsValue = false;
		
		JSONArray incidentsArray = new JSONArray();
		for (int i : Utils.POSSIBLE_SERVICE_TYPE) {
			String selectStmt = 
				"SELECT I1.id,I1.latitude,I1.longitude,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime),I2.service,I2.line,I2.direction,S1.name " +
				"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
				"WHERE I1.id=I2.incident AND I2.station=S1.id AND I2.service=" + i + " AND (I1.status=0 OR I1.status=1)";
			ResultSet rs = db.query(selectStmt);
			
			JSONArray jArray = new JSONArray();
			jArray.put(i);
			while (rs.next()) {
				returnsValue = true;
				
				JSONObject json = new JSONObject();
				int id = rs.getInt(1);
				json.put("id", 			id);
				json.put("latitude", 	rs.getDouble(2));
				json.put("longitude", 	rs.getDouble(3));
				json.put("cause", 		rs.getInt(4));
				json.put("severity", 	rs.getInt(5));
				json.put("status", 		rs.getInt(6));
				json.put("ttl", 		rs.getInt(7));
				json.put("lastUpdate",	rs.getLong(8)*1000);
				json.put("service", 	rs.getInt(9));
				json.put("line", 		rs.getInt(10));
				json.put("direction", 	rs.getInt(11));
				json.put("station", 	new String(rs.getBytes(12), "UTF-8"));
				
				selectStmt = 
					"SELECT description " +
					"FROM INCIDENT_NOTIFICATIONS " +
					"WHERE p_event=" + id + " AND description <> \'null\'";
				ResultSet res = db.query(selectStmt);
				if (res.next()) {
					json.put("description", new String(res.getBytes(1), "UTF-8"));			}
				
				else {
					json.put("description", "");			}
				
				jArray.put(json);
			}
			
			incidentsArray.put(jArray);
		}
		
		JSONArray j2Array = new JSONArray();
		j2Array.put(Utils.GENERAL_INCIDENTS);
		String selectStmt = 
			"SELECT G1.id, G1.latitude, G1.longitude, G1.severity, G1.status, G1.ttl, UNIX_TIMESTAMP(G1.reportTime), G2.description, U1.username " +
			"FROM GENERAL_INCIDENTS AS G1, GENERAL_INCIDENT_NOTIFICATIONS AS G2, USERS AS U1 " +
			"WHERE G1.id=G2.p_event AND G2.user_id=U1.id AND G2.confirmation=0 AND (G1.status=0 OR G1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		while( rs.next() ) {
			returnsValue = true;
			
			JSONObject json = new JSONObject();
			json.put("id", rs.getInt(1));
			json.put("latitude", rs.getDouble(2));
			json.put("longitude", rs.getDouble(3));
			json.put("severity", rs.getInt(4));
			json.put("status", rs.getInt(5));
			json.put("ttl", rs.getInt(6));
			json.put("lastUpdate", rs.getLong(7)*1000);
			json.put("description", rs.getString(8));
			json.put("username", rs.getString(9));
			
			j2Array.put(json);
		}
		incidentsArray.put(j2Array);
		
		db.close();
		
		String encodedIncidents = returnsValue ? "1#"+incidentsArray.toString() : incidentsArray.toString();
		return encodedIncidents;
	}
	
	public static String getComments(int incidentId) throws 
							SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		db.connect();
		
		String selectStmt = "SELECT time, description " +
							"FROM INCIDENT_NOTIFICATIONS " +
							"WHERE p_event = " + incidentId + " AND description <> \'null\'";
		ResultSet rs = db.query(selectStmt);
		
		JSONArray jArray = new JSONArray();
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("date", rs.getLong(1));
			json.put("desc", new String(rs.getBytes(2), "UTF-8"));
			
			jArray.put(json);
		}
		db.close();
		
		return jArray.toString();
	}
	
	public static String getGeneralIncComments(int incidentId) 
							throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT time, description " + 
							"FROM GENERAL_INCIDENT_NOTIFICATIONS " +
							"WHERE p_event=" + incidentId + " AND description <> \'null\'";
		ResultSet rs = db.query(selectStmt);
		
		JSONArray jArray = new JSONArray();
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("date", rs.getLong(1));
			json.put("desc", rs.getString(2));
			
			jArray.put(json);
		}
		db.close();
		
		return jArray.toString();
	}
	
	public static String getSingleIncident(int incidentId) 
							throws SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		db.connect();
		
		String selectStmt = 
			"SELECT I1.id,I1.latitude,I1.longitude,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime),I2.service,I2.line,I2.direction,S1.name " +
			"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
			"WHERE I1.id=I2.incident AND I2.station=S1.id AND I1.id=" + incidentId + " AND (I1.status=0 OR I1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		JSONObject json = new JSONObject();
		if (rs.next()) {
			json.put("id", 			rs.getInt(1));
			json.put("latitude", 	rs.getDouble(2));
			json.put("longitude", 	rs.getDouble(3));
			json.put("cause", 		rs.getInt(4));
			json.put("severity", 	rs.getInt(5));
			json.put("status", 		rs.getInt(6));
			json.put("ttl", 		rs.getInt(7));
			json.put("lastUpdate",	rs.getLong(8)*1000);
			json.put("service", 	rs.getInt(9));
			json.put("line", 		rs.getInt(10));
			json.put("direction", 	rs.getInt(11));
			json.put("station", 	new String(rs.getBytes(12), "UTF-8"));
			
			selectStmt = 
				"SELECT description " +
				"FROM INCIDENT_NOTIFICATIONS " +
				"WHERE p_event=" + incidentId + " AND description <> \'null\'";
			ResultSet res = db.query(selectStmt);
			if (res.next()) {
				json.put("description", new String(res.getBytes(1), "UTF-8"));			}
			
			else {
				json.put("description", "");			}
		}
		
		db.close();
		
		return json.toString();
	}
	
	public static String getSingleIncident(int incidentId, int type) 
							throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = 
			"SELECT G1.id, G1.latitude, G1.longitude, G1.severity, G1.status, G1.ttl, UNIX_TIMESTAMP(G1.reportTime), G2.description, U1.username " +
			"FROM GENERAL_INCIDENTS AS G1, GENERAL_INCIDENT_NOTIFICATIONS AS G2, USERS AS U1 " +
			"WHERE G1.id=G2.p_event AND G2.user_id=U1.id AND G2.confirmation=0 AND G1.id=" + incidentId + " AND (G1.status=0 OR G1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		JSONObject json = new JSONObject();
		if ( rs.next() ) {
			json.put("id", rs.getInt(1));
			json.put("latitude", rs.getDouble(2));
			json.put("longitude", rs.getDouble(3));
			json.put("severity", rs.getInt(4));
			json.put("status", rs.getInt(5));
			json.put("ttl", rs.getInt(6));
			json.put("lastUpdate", rs.getLong(7)*1000);
			json.put("description", rs.getString(8));
			json.put("username", rs.getString(9));
		}
		
		db.close();
		return json.toString();
	}
	
	private static String getAllIncidents() 
							throws SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		
		boolean returnsValue = false;
		
		JSONArray jArray = new JSONArray();
		for (int i : Utils.POSSIBLE_SERVICE_TYPE) {
			JSONArray jsonData = getIncidentsForService(i);
			jArray.put(jsonData);
			
			if ( jsonData.length()>1 )
				returnsValue = true;
		}
		
		String encodedIncidents = returnsValue ? "1#"+jArray.toString() : jArray.toString();
		return encodedIncidents;
	}

	private static JSONArray getIncidentsForService(int serviceType) 
								throws SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		db.connect();
		
		String selectStmt = 
			"SELECT I1.id,I1.latitude,I1.longitude,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime),I2.service,I2.line,I2.direction,S1.name " +
			"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
			"WHERE I1.id=I2.incident AND I2.station=S1.id AND I2.service=" + serviceType + " AND (I1.status=0 OR I1.status=1)";
		ResultSet rs = db.query(selectStmt);
		
		JSONArray jArray = new JSONArray();
		jArray.put(serviceType);
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("id", 			rs.getInt(1));
			json.put("latitude", 	rs.getDouble(2));
			json.put("longitude", 	rs.getDouble(3));
			json.put("cause", 		rs.getInt(4));
			json.put("severity", 	rs.getInt(5));
			json.put("status", 		rs.getInt(6));
			json.put("ttl", 		rs.getInt(7));
			json.put("lastUpdate",	rs.getLong(8)*1000);
			json.put("service", 	rs.getInt(9));
			json.put("line", 		rs.getInt(10));
			json.put("direction", 	rs.getInt(11));
			json.put("station", 	new String(rs.getBytes(12), "UTF-8"));
			
			jArray.put(json);
		}
		db.close();
		
		return jArray;
	}
	
	public static void recordQuery(int lineId, String username, int version, double lat, double lon, int returnsValue) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		int userId = obtainUserId(username, version);
		String insertStmt;
		if ((lat != .0) && (lon != .0)) {
			insertStmt = 
					"INSERT INTO QUERIES " +
					"(line_id, time, user_id, user_lat, user_lon, returnsValue) VALUES " +
			 		"(" + lineId + "," + 
			 			  System.currentTimeMillis() + "," + 
			 			  userId + "," + 
			 			  lat + "," +
			 			  lon + "," +
			 			  returnsValue +
			 		")";		
		}
		else {
			insertStmt = 
					"INSERT INTO QUERIES " +
					"(line_id, time, user_id, returnsValue) VALUES " +
			 		"(" + lineId + "," + 
			 			  System.currentTimeMillis() + "," + 
			 			  userId + "," +
			 			  returnsValue +
			 		")";		
		}
							
		db.insert(insertStmt);
		
		db.close();
	}
	
	public static int recordRoute(int routeId, String username, String beginStation, String endStation, int service, long startTime, int version) 
						throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = 
				"SELECT id, USID " +
				"FROM STATION_LOCATION " +
				"WHERE hashVal in (" + "\'" + beginStation + "\', \'" + endStation + "\')";
		ResultSet rs = db.query(selectStmt);
		
		rs.next(); int beginStationId = rs.getInt(1); //int beginStationUSID = rs.getInt(2);
		rs.next(); int endStationId = rs.getInt(1); //int endStationUSID = rs.getInt(2);
		
		int duradaTrajecte = -1;
		
		// TRY TO OBTAIN ROUTE TIME TABLE INFORMATION FROM INTERNET SERVICES
//		switch ( service ) 
//		{
//		case 0: // INTENDED TO SAVE RENFE ROUTE
//			try {
//				mutex = true;
//				TempsTrajecteHelper trajectHelper = new TempsTrajecteHelper();
//				duradaTrajecte = trajectHelper.recuperaTrajecteRodalies(beginStationUSID, endStationUSID, startTime) * 60000;
//			}
//			catch (Exception e) {
//				DailyLog.w("Temps de trajecte no disponible online: " + e.getCause().getMessage());
//			}
//		}
		
		// UNAVAILABLE TIME TABLE INFORMATION ONLINE -> PRECALCULATED_ROUTES
		if ( duradaTrajecte == -1 ) 
		{
			selectStmt = 
				"SELECT temps " +
				"FROM PRECALCULATED_ROUTES " +
				"WHERE estacio1 IN (" + beginStationId + ", " + endStationId + ") AND " +
					  "estacio2 IN (" + beginStationId + ", " + endStationId + ")";
			rs = db.query(selectStmt);
			rs.next(); duradaTrajecte = rs.getInt(1) * 60000;
		}
		
		// SAVE ROUTE INFORMATION TO THE DATABASE
		int userId = obtainUserId(username, version);
		String insertStmt = 
			"INSERT INTO ROUTES " +
			"(id, beginStation, endStation, transpType, startTime, expectedDuration, userId, created) VALUES " +
			"(" + routeId + ", " +
				  "\"" + beginStationId + "\"" + ", " +
				  "\"" + endStationId + "\"" + ", " +
				  service + ", " + 
				  startTime + ", " + 
				  duradaTrajecte + ", " +
				  userId + ", " +
				  "null" + 
			")";
		
		db.insert(insertStmt);
		
		//mutex = false;
		
		db.close();
		return userId;
	}
	
	public static String endOfRoute(int routeId, String username, long stopTime, int datagramVersion) 
							throws SQLException, ClassNotFoundException, InterruptedException {
		
		//if ( mutex ) Thread.sleep(5000);
		
		db.connect();
		
		JSONObject response = new JSONObject();
		
		int userId = obtainUserId(username, datagramVersion);
		String selectStmt = 
			"SELECT id, startTime, expectedDuration " +
			"FROM ROUTES " +
			"WHERE id = " + routeId + " AND userId = " + userId;
		
		ResultSet rs = db.query(selectStmt);
		if ( rs.next() ) {
			long startTime = rs.getLong(2);
			int elapsed = (int) (stopTime - startTime);
			
			response.put("duration", elapsed);
			response.put("expectedDuration", rs.getInt(3));
			
			String updateStmt = 
				"UPDATE ROUTES SET " +
				"stopTime = " + stopTime + ", " +
				"duration = " + elapsed + " " + 
				"WHERE id = " + routeId + " AND userId = " + userId;
			db.insert(updateStmt);
		}
		
		db.close();
		return response.toString();
		
	}

	public static JSONArray getPrintableEvents(int eventType) 
							throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT * from INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2 " +
				"WHERE I1.id = I2.incident AND I2.service = " + eventType + 
					" AND (I1.status = 0 OR I1.status = 1)";
		ResultSet rs = db.query(selectStmt);
		JSONArray jArray = new JSONArray();
		jArray.put(eventType);
		
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("id", rs.getInt(1));
			json.put("latitude", rs.getDouble(2));
			json.put("longitude", rs.getDouble(3));
			json.put("typeClass", rs.getInt(4));
			json.put("event", rs.getInt(5));
			json.put("severity", rs.getInt(6));
			json.put("state", rs.getInt(7));
			json.put("ttl", rs.getInt(8));
			
			jArray.put(json);
		}
		db.close();
		
		return jArray;
	}
	
	public static String getLatLng(String name) throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "select latitude, longitude from geopoints where " + 
							"name = \'" + name + "\'";
		ResultSet rs = db.query(selectStmt);
		if (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("lat", rs.getDouble(1));
			json.put("lng", rs.getDouble(2));
			db.close();
			return json.toString();
			
		}
		db.close();
		
		return null;
	}
	
	public static int getTTL(int id) throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT ttl FROM INCIDENTS WHERE id = " + id;
		ResultSet rs = db.query(selectStmt);
		if (rs.next()) {
			int ttl = rs.getInt(1);
			db.close();
			return ttl;
			
		} else {
			db.close();
			throw new SQLException("The event with event id = " + id + 
					" doesn't exist in out database");
		}
	}
	
	public static int getGenIncTTL(int id) throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT ttl FROM GENERAL_INCIDENTS WHERE id=" + id;
		ResultSet rs = db.query(selectStmt);
		if ( rs.next() ) {
			int ttl = rs.getInt(1);
			db.close();
			
			return ttl;
			
		} else {
			db.close();
			throw new SQLException("The event with event id = " + id + 
					" doesn't exist in out database");
		}
	}
	
	public static long getLastModified(int id) throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT last_modified FROM INCIDENTS WHERE id = " + id;
		ResultSet rs = db.query(selectStmt);
		if (rs.next()) {
			long lastModified = rs.getLong(1);
			db.close();
			return lastModified;
			
		} else {
			db.close();
			throw new SQLException("The event with event id = " + id + 
					" doesn't exist in out database");
		}
	}
	
	public static long getGenIncLastModified(int id) throws SQLException, ClassNotFoundException {
		db.connect();
		
		String selectStmt = "SELECT last_modified FROM GENERAL_INCIDENTS WHERE id=" + id;
		ResultSet rs = db.query(selectStmt);
		if ( rs.next() ) {
			long lastModified = rs.getLong(1);
			db.close();
			return lastModified;
			
		} else {
			db.close();
			throw new SQLException("The event with event id = " + id + 
					" doesn't exist in out database");
		}
	}
	
	public static ArrayList<RSSFeed> getActiveFeeds() 
					throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
		db.connect();
		
		ArrayList<RSSFeed> dbFeeds = new ArrayList<RSSFeed>();
		String selectStmt = "SELECT line_id, time, title, description " +
							"FROM RSS_SOURCE_INCIDENTS " +
							"WHERE status=1";
		ResultSet rs = db.query(selectStmt);
		while ( rs.next() ) {
			RSSFeed feed = new RSSFeed(
					rs.getInt(1),
					rs.getLong(2),
					rs.getString(3),
					rs.getString(4)
					);
			dbFeeds.add(feed);
		}
		db.close();
		
		return dbFeeds;
	}
	
	public static final String md5(String s) {
		MessageDigest md = null;
		
		try { md = MessageDigest.getInstance("MD5"); }
		catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		
		md.update(s.getBytes(), 0, s.length());
		return new BigInteger(1, md.digest()).toString(16);
	}
	
	public static final String getIncidents(int kindOfIncident) 
								throws SQLException, ClassNotFoundException, NullPointerException, UnsupportedEncodingException {
		db.connect();
		
		JSONArray ptIncidentsArray = new JSONArray();
		for (int i : Utils.POSSIBLE_SERVICE_TYPE) {
			String selectStmt = 
				"SELECT I1.id,I1.latitude,I1.longitude,I1.cause,I1.severity,I1.status,I1.ttl,UNIX_TIMESTAMP(I1.reportTime),I2.service,I2.line,I2.direction,S1.name " +
				"FROM INCIDENTS AS I1, INCIDENT_CLASSIFICATION AS I2, STATION_LOCATION AS S1 " +
				"WHERE I1.id=I2.incident AND I2.station=S1.id AND I2.service=" + i;
			ResultSet rs = db.query(selectStmt);
			
			JSONArray jArray = new JSONArray();
			jArray.put(i);
			while (rs.next()) {
				JSONObject json = new JSONObject();
				int id = rs.getInt(1);
				json.put("id", 			id);
				json.put("latitude", 	rs.getDouble(2));
				json.put("longitude", 	rs.getDouble(3));
				json.put("cause", 		rs.getInt(4));
				json.put("severity", 	rs.getInt(5));
				json.put("status", 		rs.getInt(6));
				json.put("ttl", 		rs.getInt(7));
				json.put("lastUpdate",	rs.getLong(8)*1000);
				json.put("service", 	rs.getInt(9));
				json.put("line", 		rs.getInt(10));
				json.put("direction", 	rs.getInt(11));
				json.put("station", 	new String(rs.getBytes(12), "UTF-8"));
				
				selectStmt = 
					"SELECT description " +
					"FROM INCIDENT_NOTIFICATIONS " +
					"WHERE p_event=" + id + " AND description <> \'null\'";
				ResultSet res = db.query(selectStmt);
				if (res.next()) {
					json.put("description", new String(res.getBytes(1), "UTF-8"));			}
				
				else {
					json.put("description", "");			}
				
				jArray.put(json);
			}
			
			ptIncidentsArray.put(jArray);
		}
		
		JSONArray j2Array = new JSONArray();
		j2Array.put(Utils.GENERAL_INCIDENTS);
		String selectStmt = 
			"SELECT G1.id, G1.latitude, G1.longitude, G1.severity, G1.status, G1.ttl, UNIX_TIMESTAMP(G1.reportTime), G2.description, U1.username " +
			"FROM GENERAL_INCIDENTS AS G1, GENERAL_INCIDENT_NOTIFICATIONS AS G2, USERS AS U1 " +
			"WHERE G1.id=G2.p_event AND G2.user_id=U1.id AND G2.confirmation=0";
		ResultSet rs = db.query(selectStmt);
		
		while( rs.next() ) {
			JSONObject json = new JSONObject();
			json.put("id", rs.getInt(1));
			json.put("latitude", rs.getDouble(2));
			json.put("longitude", rs.getDouble(3));
			json.put("severity", rs.getInt(4));
			json.put("status", rs.getInt(5));
			json.put("ttl", rs.getInt(6));
			json.put("lastUpdate", rs.getLong(7)*1000);
			json.put("description", rs.getString(8));
			json.put("username", rs.getString(9));
			
			j2Array.put(json);
		}
		
		JSONArray result = new JSONArray();
		if ( kindOfIncident == Constants.PUBLIC_TRANSPORT )
			result.put(ptIncidentsArray);
		
		else if ( kindOfIncident == Constants.GENERAL )
			result.put(j2Array);
		
		else if ( kindOfIncident == Constants.ALL ) {
			result.put(ptIncidentsArray);
			result.put(j2Array);
		}
		
		db.close();
		return result.toString();
	}
}
