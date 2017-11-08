package org.uab.android.eventreporter.utils;


public class Incident {
	private int id;
	private double latitude;
	private double longitude;
	private int transportService;
	private int uniqueLineId;
	private String stationName;
	private int direction;
	private int cause;
	private int severity;
	private int status;
	private int TTL;
	private long lastUpdateTime;
	private String firstComment;
	
	public Incident() {
		
	}
	
	public Incident(int id, double latitude, double longitude, int service, int lineId, 
			String station, int direction, int cause, int severity, int status, int TTL, 
			long time, String comment) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.transportService = service;
		this.uniqueLineId = lineId;
		this.stationName = station;
		this.direction = direction;
		this.cause = cause;
		this.severity = severity;
		this.status = status;
		this.TTL = TTL;
		this.lastUpdateTime = time;
		this.firstComment = comment;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
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
	
	public String getStationName() {
		return stationName;
	}
	
	public void setStationName(String stationName) {
		this.stationName = stationName;
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
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getTTL() {
		return TTL;
	}
	
	public void setTTL(int tTL) {
		TTL = tTL;
	}
	
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public String getFirstComment() {
		return firstComment;
	}
	
	public void setFirstComment(String comment) {
		this.firstComment = comment;
	}
}
