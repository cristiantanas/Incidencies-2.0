package org.uab.android.eventreporter.utils;

public class RouteHeader {
	private int resourceId = -1;
	private String beginStation = "";
	private String endStation = "";
	private String simpleName = "";
	private int stationsIdentifier = -1;
	
	public RouteHeader() {
		
	}
	
	public RouteHeader(int resource, String begin, String end, String name, int res) {
		this.resourceId = resource;
		this.beginStation = begin;
		this.endStation = end;
		this.simpleName = name;
		this.stationsIdentifier = res;
	}
	
	public int getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public String getBeginStation() {
		return beginStation;
	}
	
	public void setBeginStation(String beginStation) {
		this.beginStation = beginStation;
	}
	
	public String getEndStation() {
		return endStation;
	}
	
	public void setEndStation(String endStation) {
		this.endStation = endStation;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public void setSimpleName(String name) {
		this.simpleName = name;
	}
	
	public int getStationsIdentifier() {
		return stationsIdentifier;
	}
	
	public void setStationsIdentifier(int identifier) {
		this.stationsIdentifier = identifier;
	}
}
