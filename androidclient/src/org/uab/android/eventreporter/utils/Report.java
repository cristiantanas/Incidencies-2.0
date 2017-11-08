package org.uab.android.eventreporter.utils;

public class Report {
	private int type = -1;
	private int reportClass = -1;
	private int severity = -1;
	private double lat = 0.0;
	private double lon = 0.0;
	private long time = 0;
	private String description = "";
	private String username = "";
	
	public Report() {
		
	}
	
	public Report(int type, int reportClass, int severity, double lat, double lon,
			long time, String description, String username) {
		this.type = type;
		this.reportClass = reportClass;
		this.severity = severity;
		this.lat = lat;
		this.lon = lon;
		this.time = time;
		this.description = description;
		this.username = username;
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
