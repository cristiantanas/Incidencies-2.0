package org.uab.android.incidencies.utils;

public class IncidentClassification {

	private int transportService;
	private int uniqueLineId;
	private String stationName;
	private int cause;
	private int incidentId;
	
	public IncidentClassification() {
		
	}
	
	public IncidentClassification(int service, int lineId, String station, int cause, int incident) {
		this.transportService = service;
		this.uniqueLineId = lineId;
		this.stationName = station;
		this.incidentId = incident;
		this.cause = cause;
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
	
	public int getCause() {
		return cause;
	}
	
	public void setCause(int cause) {
		this.cause = cause;
	}
	
	public int getIncidentId() {
		return incidentId;
	}
	
	public void setIncidentId(int incidentId) {
		this.incidentId = incidentId;
	}
}
