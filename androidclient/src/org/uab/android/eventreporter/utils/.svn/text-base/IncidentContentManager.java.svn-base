package org.uab.android.eventreporter.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uab.android.eventreporter.net.NetworkUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class IncidentContentManager {
	public static final String CLASSTAG = IncidentContentManager.class.getSimpleName().toUpperCase();

	private static IncidentContentManager _instance;
	private HashMap<Integer, HashMap<Integer, Incident>> incidentList;
	private HashMap<Integer, GeneralNotification> generalIncList;
	
	private IncidentContentManager() {
		incidentList = new HashMap<Integer, HashMap<Integer,Incident>>();
		generalIncList = new HashMap<Integer, GeneralNotification>();
	}
	
	public static IncidentContentManager getInstance() {
		if (_instance == null) {
			_instance = new IncidentContentManager();		}
		
		return _instance;		
	}
	
	public HashMap<Integer, HashMap<Integer, Incident>> updateIncidentList(int lineId, String username, 
			double userLatitude, double userLongitude, Context context) throws NullPointerException {
		incidentList = new HashMap<Integer, HashMap<Integer, Incident>>();
		String json = NetworkUtils.getIncidentsForLine(lineId, username, userLatitude, 
				userLongitude, new Handler(), context);
		
		if ( json==null )
			throw new NullPointerException("JSON null construction");
		
		try {
			JSONArray jArray = new JSONArray(json);
			HashMap<Integer, Incident> indexedIncidents = new HashMap<Integer, Incident>();
			for ( int i=0; i<jArray.length(); i++ ) {
				JSONArray incArray = jArray.getJSONArray(i);
				for ( int j=1; j<incArray.length(); j++ ) {
					JSONObject incident = incArray.getJSONObject(j);
					Incident incd;
					if ( incArray.getInt(0) == Utils.RSS_FEEDS ) {
						String description = incident.getString("description");
						if ( description.equalsIgnoreCase("") ) 
							description = incident.getString("title");
						
						incd = new Incident(
								incident.getInt("id"), 
								.0, 
								.0, 
								incident.getInt("service"), 
								incident.getInt("line"), 
								"via@" + incident.getString("source"), 
								0, 
								-1, 
								-1, 
								Utils.RSS_FEEDS, 
								0, 
								incident.getLong("time"), 
								description);
						
					} else {
						incd = new Incident(
								incident.getInt("id"),
								incident.getDouble("latitude"),
								incident.getDouble("longitude"),
								incident.getInt("service"),
								incident.getInt("line"),
								incident.getString("station"),
								incident.getInt("direction"),
								incident.getInt("cause"),
								incident.getInt("severity"),
								incident.getInt("status"),
								incident.getInt("ttl"),
								incident.getLong("lastUpdate"),
								new String(Base64.decodeBase64(incident.getString("description").getBytes())));
					}
					indexedIncidents.put(incd.getId(), incd);
				}
			}
			incidentList.put(lineId, indexedIncidents);
			
			return incidentList;
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
			return null;
		} 
	}
	
	public HashMap<Integer, GeneralNotification> updateIncidentList(int incidentType, 
			String username, double userLatitude, double userLongitude) throws NullPointerException {
		generalIncList = new HashMap<Integer, GeneralNotification>();
		String json = NetworkUtils.getGeneralIncidentsList(username, userLatitude, userLongitude);
		
		if ( json==null )
			throw new NullPointerException("JSON null construction");
		
		try {
			JSONArray jArray = new JSONArray(json);
			for ( int i=0; i<jArray.length(); i++ ){
				JSONObject incident = jArray.getJSONObject(i);
				GeneralNotification generalInc = new GeneralNotification(
						incident.getInt("id"),
						new String(Base64.decodeBase64(incident.getString("description").getBytes())),
						incident.getInt("severity"),
						incident.getInt("ttl"),
						incident.getLong("lastUpdate"),
						incident.getString("username"),
						incident.getDouble("latitude"),
						incident.getDouble("longitude"),
						incident.getInt("status"));
				
				generalIncList.put(generalInc.getId(), generalInc);
			}
			
			return generalIncList;
			
		} catch (JSONException e) {
			Log.e("INCIDENT_CONTENT_MANAGER", "JSONException accessing data " + e.getMessage());
			return generalIncList;
		}
	}
	
	public ArrayList<Incident> getIncidentList(int lineId) {
		if (incidentList != null) {
			ArrayList<Incident> iList = new ArrayList<Incident>();
			ArrayList<Incident> rList = new ArrayList<Incident>();
			HashMap<Integer, Incident> incidents = incidentList.get(lineId);
			Iterator<Incident> it = incidents.values().iterator();
			
			while (it.hasNext()) {
				Incident actual = it.next();
				if ( actual.getStatus()==Utils.RSS_FEEDS )
					rList.add(actual);
				else
					iList.add(actual);		
			}
			
			ArrayList<Incident> sortedRList = Utils.quicksort(rList);
			ArrayList<Incident> sortedIList = Utils.quicksort(iList);
			
			ArrayList<Incident> sorted = new ArrayList<Incident>();
			for ( Incident i : sortedRList )
				sorted.add(i);
			
			for ( Incident i : sortedIList )
				sorted.add(i);
			
			return sorted;	
			
		} else {
			return new ArrayList<Incident>();		}
		
	}
	
	public ArrayList<GeneralNotification> getGeneralIncidentList(int incidentType) {
		ArrayList<GeneralNotification> list = new ArrayList<GeneralNotification>();
		if ( generalIncList!=null ) {
			Iterator<GeneralNotification> it = generalIncList.values().iterator();
			
			while ( it.hasNext() ) {
				list.add(it.next());			}
			
			ArrayList<GeneralNotification> sorted = Utils.sortByTime(list);
			return sorted;
		}
		
		return list;
	}
	
	public Incident getIncident(int incidentId, boolean requestUpdate) {
		if ( incidentList != null && !requestUpdate ) {
			Collection<HashMap<Integer, Incident>> valuesSet = incidentList.values();
			Iterator<HashMap<Integer, Incident>> it = valuesSet.iterator();
			while ( it.hasNext() ) {
				HashMap<Integer, Incident> aux = it.next();
				if ( aux.containsKey(incidentId) ) {
					return aux.get(incidentId); 				}
			}
			
			return getIncidentFromDB(incidentId);
			
		} else {
			return getIncidentFromDB(incidentId);
		}
	}
	
	public GeneralNotification getGeneralIncident(int incidentId, boolean requestUpdate) {
		if ( generalIncList!=null && !requestUpdate ) {
			if ( generalIncList.containsKey(incidentId)) {
				return generalIncList.get(incidentId);
			}
			
			return getGeneralIncFromDB(incidentId);
			
		} else {
			return getGeneralIncFromDB(incidentId);
		}
	}
	
	private Incident getIncidentFromDB(int incidentId) {
		String json = NetworkUtils.getSingleIncident(incidentId, 0);
		try {
			JSONObject incident = new JSONObject(json);
			return new Incident(
					incident.getInt("id"),
					incident.getDouble("latitude"),
					incident.getDouble("longitude"),
					incident.getInt("service"),
					incident.getInt("line"),
					incident.getString("station"),
					incident.getInt("direction"),
					incident.getInt("cause"),
					incident.getInt("severity"),
					incident.getInt("status"),
					incident.getInt("ttl"),
					incident.getLong("lastUpdate"),
					new String(Base64.decodeBase64(incident.getString("description").getBytes())));
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
			return null;
		}
	}
	
	private GeneralNotification getGeneralIncFromDB(int incidentId) {
		String json = NetworkUtils.getSingleIncident(incidentId, Utils.GENERAL_INCIDENTS);
		try {
			JSONObject incident = new JSONObject(json);
			return new GeneralNotification(
					incident.getInt("id"), 
					new String(Base64.decodeBase64(incident.getString("description").getBytes())), 
					incident.getInt("severity"), 
					incident.getInt("ttl"), 
					incident.getLong("lastUpdate"), 
					incident.getString("username"), 
					incident.getDouble("latitude"), 
					incident.getDouble("longitude"), 
					incident.getInt("status"));
			
		} catch (JSONException e) {
			Log.e(CLASSTAG, "JSONException accessing data " + e.getMessage());
			return null;
		}
	}
}
