package org.uab.android.incidencies.utils;

public class IncidentNotification {

	private int transportService;
	private int uniqueLineId;
	private String stationHashValue;
	private int direction;
	private int cause;
	private int severity;
	private String description;
	private long time;
	private String username;
	
	public int getTransportService() {
		return transportService;
	}
	
	public void setTransportService(int transportService) {
		this.transportService = transportService;
	}
	
	public int getUniqueLineId() {
		return uniqueLineId;
	}
	
	public void setUniqueLineId(int uniqueLineId) {
		this.uniqueLineId = uniqueLineId;
	}
	
	public String getStationHashValue() {
		return stationHashValue;
	}
	
	public void setStationHashValue(String stationName) {
		this.stationHashValue = stationName;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public int getCause() {
		return cause;
	}
	
	public void setCause(int cause) {
		this.cause = cause;
	}
	
	public int getSeverity() {
		return severity;
	}
	
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}
