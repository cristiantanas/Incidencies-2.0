package org.uab.android.incidencies.utils;

public class Report {
	private int id;
	private int state;
	private int type;
	private int reportClass;
	private int severity;
	private double lat;
	private double lon;
	private long time;
	private String description;
	private String username;
	
	public Report() {
		
	}
	
	public Report(int id, int state, int type, int reportClass, int severity, 
			double lat, double lon,	long time, String description, String username) {
		this.id = id;
		this.state = state;
		this.type = type;
		this.reportClass = reportClass;
		this.severity = severity;
		this.lat = lat;
		this.lon = lon;
		this.time = time;
		this.description = description;
		this.username = username;
	}
	
	public int getId() {
		return id;
	}
	
	public int getState() {
		return state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getReportClass() {
		return reportClass;
	}
	
	public void setReportClass(int reportClass) {
		this.reportClass = reportClass;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}