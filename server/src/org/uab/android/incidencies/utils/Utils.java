package org.uab.android.incidencies.utils;

public class Utils {
	
	/**
	 * CONSTANTS
	 */
	public static final int MIN_TO_MILIS = 60 * 1000;
	
	public static final int DATAGRAM_VERSION = 1;
	public static final int DATAGRAM_PT_TYPE = 1;
	public static final int DATAGRAM_GN_TYPE = 2;
	public static final int DATAGRAM_DEFAULT_OPTIONS = 0;
	public static final int DATAGRAM_T11_PICKED_UP_OPTION = 1;
	
	public static final int INCIDENT_NOTIFICATION = 10;
	public static final int INCIDENT_CONFIRMATION = 15;
	public static final int AUTHENTICATE_USER = 11;
	public static final int GET_ALL_INCIDENTS = 12;
	public static final int GET_INCIDENTS_FOR_LINE = 14;
	public static final int GET_COMMENTS_FOR_INCIDENT = 16;
	public static final int UPDATE_USER_ACCOUNT = 17;
	public static final int GET_SINGLE_INCIDENT = 18;
	public static final int GET_GENERAL_INCIDENTS = 19;
	public static final int DATA_RECEIVED_CORECTLY = 21;
	public static final int ALL_INCIDENTS_INFO_RCVD = 22;
	public static final int LINE_INCIDENTS_INFO_RCVD = 25;
	public static final int COMMENTS_FOR_INCIDENT_RCVD = 26;
	public static final int ACCOUNT_CORECTLY_UPDATED = 27;
	public static final int SINGLE_INCIDENT_RCVD = 28;
	public static final int GENERAL_INC_INFO_RCVD = 29;
	public static final int NOTIFICATION_DUPLICATE = 32;
	public static final int INVALID_XML_FORMAT = 42;
	
	public static final int T11 = 8411;
	public static final int ALL_INCIDENTS = 9999;
	public static final int GENERAL_INCIDENTS = 99;
	public static final int RSS_FEEDS = 10000;
	public static final int RENFE_INCIDENTS = 0;
	public static final int FGC_INCIDENTS = 1;
	public static final int METRO_INCIDENTS = 2;
	public static final int UNKNOWN = -1;
	public static final int[] POSSIBLE_SERVICE_TYPE = { RENFE_INCIDENTS, FGC_INCIDENTS, METRO_INCIDENTS }; 
	
	public static final String VALID_XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String VALID_XML_BEGINING = "<report";
	public static final String INVALID_XML_LABEL = "Invalid XML encoding";
	
	public static final String HEADER_SEPARATOR = ";";
	public static final String BODY_SEPARATOR = "#";
	public static final String DATAGRAM_SEPARATOR = "##";
	
	static final String pkupMessage = "Ja saps que han agafat l\u0027\u00FAltima T-11 " +
	"que vas deixar? Gr\u00E0cies per la teva col\u00B7laboraci\u00F3!";
	public static final String T11_PICKED_UP_NOTIFICATION = pkupMessage;
	
	public static String createMessage(int identifier, String[] attr) {
		int BODY_LENGTH;
		if (attr != null) {
			BODY_LENGTH = attr.length;		}
		
		else {
			BODY_LENGTH = 0;		}
		
		StringBuilder header = new StringBuilder();
		header.append(DATAGRAM_VERSION).append(HEADER_SEPARATOR)
			  .append(Utils.DATAGRAM_DEFAULT_OPTIONS).append(HEADER_SEPARATOR)
			  .append(BODY_LENGTH).append(HEADER_SEPARATOR).append(identifier);
		
		StringBuilder body = new StringBuilder();
		if (BODY_LENGTH > 0) {
			body.append(attr[0]);
			for (int i = 1; i < BODY_LENGTH; i++) {
				body.append(BODY_SEPARATOR).append(attr[i]);			}
		}
		
		StringBuilder datagram = new StringBuilder();
		datagram.append(header).append(DATAGRAM_SEPARATOR).append(body).append("\n");
		
		return datagram.toString();
	}
	
	public static String createMessage(int identifier, int datagramOptions, String[] attr) {
		int BODY_LENGTH;
		if (attr != null) {
			BODY_LENGTH = attr.length;		}
		
		else {
			BODY_LENGTH = 0;		}
		
		StringBuilder header = new StringBuilder();
		header.append(DATAGRAM_VERSION).append(HEADER_SEPARATOR)
			  .append(datagramOptions).append(HEADER_SEPARATOR)
			  .append(BODY_LENGTH).append(HEADER_SEPARATOR).append(identifier);
		
		StringBuilder body = new StringBuilder();
		if (BODY_LENGTH > 0) {
			body.append(attr[0]);
			for (int i = 1; i < BODY_LENGTH; i++) {
				body.append(BODY_SEPARATOR).append(attr[i]);			}
		}
		
		StringBuilder datagram = new StringBuilder();
		datagram.append(header).append(DATAGRAM_SEPARATOR).append(body).append("\n");
		
		return datagram.toString();
	}
	
	public static int getHeaderIdentifier(String header) {
		String[] headerFields = header.split(HEADER_SEPARATOR);
		return Integer.parseInt(headerFields[3]);
	}
	
	public static int getPayloadLength(String header) {
		String[] headerFields = header.split(HEADER_SEPARATOR);
		return Integer.parseInt(headerFields[2]);
	}
	
	public static int getDatagramType(String header) {
		String[] headerFields = header.split(HEADER_SEPARATOR);
		return Integer.parseInt(headerFields[1]);
	}
	
	public static int getDatagramVersion(String header) {
		String[] headerFields = header.split(HEADER_SEPARATOR);
		return Integer.parseInt(headerFields[0]);
	}
}
