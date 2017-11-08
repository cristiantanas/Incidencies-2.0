package org.uab.android.incidencies.utils;

public class Constants {
	/**
	 * Communication protocol constants
	 */	
	public static final int CONNECTION_ACCEPTED = 1;
	
	public static final int AUTHENTICATE_USER = 11;
	public static final int GET_EVENTS = 12;
	public static final int GET_LAT_LNG = 13;
	public static final int DATA_RECEIVED_CORECTLY = 21;
	public static final int USER_ACCESS_GRANTED = 22;
	public static final int EVENT_INFORMATION_RECEIVED = 23;
	public static final int LATLNG_INFORMATION_RECEIVED = 24;
	public static final int EVENT_CONFIRMATION = 31;
	public static final int ALREADY_CONFIRMED_EVENT = 32;
	public static final int BAD_INPUT_FORMAT = 41;
	public static final int INVALID_XML_FORMAT = 42;
	public static final int USER_UNAUTHORIZED = 43;
	public static final int USER_PASSWORD_FAILED = 44;
	public static final int LATLNG_INFORMATION_UNAVAILABLE = 45;
	public static final int SERVER_ERROR = 50;
	public static final int UNEXPECTED_MESSAGE = 51;
	
	public static final int T11_NOTIFICATION = 60;
	public static final int T11_RECEIVED_CORECTLY = 61;
	public static final int T11_GET = 62;
	public static final int T11_GET_DONE = 63;
	public static final int T11_PICK_UP = 64;
	public static final int T11_CORRECTLY_UPDATED = 65;
	
	public static final int ROUTE_NOTIFICATION = 70;
	public static final int END_OF_ROUTE_NOTIFICATION = 71;
	public static final int END_OF_ROUTE_RESPONSE = 72;

	
	public static final int GET_INCIDENTS = 100;
	public static final int INCIDENTS_INFO = 200;
	
	public static final String CONN_ACCEPTED_LABEL = CONNECTION_ACCEPTED + 
									"#Connection accepted" + "\n";
	public static final String DATA_RCV_LABEL = DATA_RECEIVED_CORECTLY + 
									"#Data received corectly" + "\n";
	public static final String ALREADY_CONF_EVENT_LABEL = ALREADY_CONFIRMED_EVENT + 
									"#Already confirmed event" + "\n";
	public static final String BAD_INPUT_LABEL = BAD_INPUT_FORMAT + 
									"#Bad input format" + "\n";
	public static final String INVALID_XML_LABEL = INVALID_XML_FORMAT + 
									"#Incorrect xml encoding" + "\n";
	public static final String UNEXPECTED_MESSAGE_LABEL = UNEXPECTED_MESSAGE + 
									"#Unexpected message received" + "\n";
	public static final String SERVER_ERROR_LABEL = SERVER_ERROR + 
									"#Server error" + "\n";
	
	public static String getMessageDescription(int identifier) {
		switch (identifier){
		case CONNECTION_ACCEPTED:
			return CONNECTION_ACCEPTED + "#Connection accepted" + "\n";
			
		case DATA_RECEIVED_CORECTLY:
			return DATA_RECEIVED_CORECTLY + "#Data received corectly" + "\n";
			
		case USER_ACCESS_GRANTED:
			return USER_ACCESS_GRANTED + "#Valid username and password" + "\n";
			
		case EVENT_INFORMATION_RECEIVED:
			return EVENT_INFORMATION_RECEIVED + "#Event query successfully satisfied";
			
		case LATLNG_INFORMATION_RECEIVED:
			return LATLNG_INFORMATION_RECEIVED + "#Geographic position retrieved correctly";
			
		case BAD_INPUT_FORMAT:
			return BAD_INPUT_FORMAT + "#Bad input format" + "\n";
			
		case INVALID_XML_FORMAT:
			return INVALID_XML_FORMAT + "#Incorrect XML encoding" + "\n";
			
		case USER_UNAUTHORIZED:
			return USER_UNAUTHORIZED + "#No user with that username" + "\n";
			
		case USER_PASSWORD_FAILED:
			return USER_PASSWORD_FAILED + "#Invalid password for user" + "\n";
			
		case LATLNG_INFORMATION_UNAVAILABLE:
			return LATLNG_INFORMATION_UNAVAILABLE + "#No location information available" + "\n";
			
		case UNEXPECTED_MESSAGE:
			return UNEXPECTED_MESSAGE + "#Unexpected message received" + "\n";
			
		case SERVER_ERROR:
			return SERVER_ERROR + "#Server error" + "\n";
			
		default:
			return null;
		}
	}
	
	public static final int ALL_EVENTS = 0;
	public static final int RENFE_REPORT = 1;
	public static final int FGC_REPORT = 2;
	public static final int METRO_REPORT = 3;
	public static final int UNKNOWN = 4;
	
	public static final int PUBLIC_TRANSPORT = 1;
	public static final int GENERAL = 2;
	public static final int ALL = 0;
	
	public static final int[] POSSIBLE_EVENT_TYPES = {RENFE_REPORT, FGC_REPORT, METRO_REPORT};
	
	/**
	 * Server state constants
	 */
	public static final int WAITING = 0;
	public static final int WAITING_REPORT_INFO = 1;
	public static final int REPORT_RECEIVED = 2;
	public static final int QUERY_RESULT_SEND = 3;
	public static final int ERROR_TREATMENT = 4;
	
	/**
	 * Validation utilities constants
	 */
	public static final int ACTIVE = 1;
	public static final int QUARANTINE = 0;
	public static final int TTL_EXPIRED = -1;
	
	public static final int REPORT_EXISTS = 1;
	public static final int REPORT_DO_NOT_EXISTS = 2;
	public static final int REPORT_ERROR = -1;
}
