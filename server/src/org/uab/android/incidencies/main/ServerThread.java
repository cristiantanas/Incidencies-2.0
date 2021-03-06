package org.uab.android.incidencies.main;

import static org.uab.android.incidencies.utils.Constants.getMessageDescription;
import static org.uab.android.incidencies.utils.Utils.createMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.sql.SQLException;

import org.uab.android.incidencies.database.DataBaseUtils;
import org.uab.android.incidencies.utils.Constants;
import org.uab.android.incidencies.utils.DailyLog;
import org.uab.android.incidencies.utils.GeneralNotification;
import org.uab.android.incidencies.utils.IncidentNotification;
import org.uab.android.incidencies.utils.Utils;
import org.uab.android.incidencies.utils.XMLGeneralNotificationParser;
import org.uab.android.incidencies.utils.XMLIncidentNotificationParser;
import org.uab.android.incidencies.utils.XMLT11Parser;
import org.uab.android.incidencies.validation.ValidationUtilities;
import org.xml.sax.SAXException;

public class ServerThread extends Thread {
	private Socket clientSocket = null;
	private XMLIncidentNotificationParser decoder = null;
	private XMLGeneralNotificationParser generalNotDecoder;
	private XMLT11Parser t11Decoder;
	private static int state = Constants.WAITING;

	public ServerThread(Socket client) {
		this.clientSocket = client;
		
		start();
	}
	
	@Override
	public void run() {
		try {
			decoder = new XMLIncidentNotificationParser();
			generalNotDecoder = new XMLGeneralNotificationParser();
			t11Decoder = new XMLT11Parser();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			
			writer.write(Constants.CONN_ACCEPTED_LABEL);
			writer.flush();
			
			String inputLine, outputLine;
			while ((inputLine = reader.readLine()) != null) {
								
				outputLine = processInput(inputLine);
				
				if (outputLine.equalsIgnoreCase("WAITING")) {
					continue;
					
				} else if (outputLine.equalsIgnoreCase("EXIT")) {
					break;
					
				}
				writer.write(outputLine);
				writer.flush();
				
			}
						
			writer.close();
			reader.close();
			clientSocket.close();
		} catch (IOException e) {
			DailyLog.e("IOException with client - " + e.getMessage());
			
		} catch (SAXException e) {
			DailyLog.e("SAXException couldn't create parser - " + e.getMessage());		
		}
	}
	
	private String processInput(String inputLine) {
		String output = null;
		int resultRouteNotification = 0;
		
		if (state == Constants.WAITING) {
			
			String[] contents = inputLine.split(Utils.DATAGRAM_SEPARATOR);
			int code = Utils.getHeaderIdentifier(contents[0]);
			int payloadLength = Utils.getPayloadLength(contents[0]);
			int datagramType = Utils.getDatagramType(contents[0]);
			int datagramVersion = Utils.getDatagramVersion(contents[0]);
			
			switch (code) {
			
			case Constants.ROUTE_NOTIFICATION:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received ROUTE_NOTIFICATON from " + bodyContents[1]);
					resultRouteNotification = DataBaseUtils.recordRoute(
							Integer.parseInt(bodyContents[0]),
							bodyContents[1],
							bodyContents[2],
							bodyContents[3],
							Integer.parseInt(bodyContents[4]),
							System.currentTimeMillis(),
							datagramVersion);
					output = "EXIT";
					
				} catch (Exception e) {
					DailyLog.e("Exception saving route information - " + resultRouteNotification + " " + e.getMessage());
					output = "EXIT";
				}
				break;
				
			case Constants.END_OF_ROUTE_NOTIFICATION:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received END_OF_ROUTE_NOTIFICATION from " + bodyContents[1]);
					String json = DataBaseUtils.endOfRoute(
							Integer.parseInt(bodyContents[0]),
							bodyContents[1],
							System.currentTimeMillis(),
							datagramVersion);
					output = createMessage(Constants.END_OF_ROUTE_RESPONSE, new String[] {json});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.e("Exception updating route information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
			
			case Constants.GET_INCIDENTS:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					String json = DataBaseUtils.getIncidents(Integer.parseInt(bodyContents[0]));
					output = createMessage(Constants.INCIDENTS_INFO, new String[] {json});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.e("Exception getting incidents information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Constants.T11_GET:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received T11 information request from " + bodyContents[0]);
					String jsonEnc = DataBaseUtils.getAvailableT11();
					int returnsValue = jsonEnc.startsWith("1#") ? 1 : 0;
					String returnedValue = jsonEnc.startsWith("1#") ? jsonEnc.substring(2) : jsonEnc;
					
					if ( payloadLength==1 ) {
						DataBaseUtils.recordQuery(Utils.T11, bodyContents[0], datagramVersion, .0, .0, returnsValue);				}
					
					else if ( payloadLength==3 ) {
						DataBaseUtils.recordQuery(Utils.T11, bodyContents[0], datagramVersion,
								Double.parseDouble(bodyContents[1]), Double.parseDouble(bodyContents[2]), 
								returnsValue);
					}
					
					output = createMessage(Constants.T11_GET_DONE, new String[] {returnedValue});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.e("Exception getting T11 information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
			
			case Utils.AUTHENTICATE_USER: // User authentication request
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received authentication request for user " + bodyContents[0]);
					int result = DataBaseUtils.authenticateUser(bodyContents[0], bodyContents[1]);
					output = createMessage(result, null);
					state = Constants.QUERY_RESULT_SEND; 
					
				} catch (Exception e) {
					DailyLog.e("Exception authenticating user - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.UPDATE_USER_ACCOUNT:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received register request for e-mail address " + bodyContents[1]);
					int result = DataBaseUtils.updateUserAccount(bodyContents[0], datagramVersion,
							bodyContents[1], bodyContents[2]);
					output = createMessage(result, null);
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.e("Exception registering user - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.GET_INCIDENTS_FOR_LINE: // Incidents request for specific line
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received incidents request for line id " + bodyContents[0]);
					String jsonEnc = DataBaseUtils.getJSONEncodedIncidents(Integer.parseInt(bodyContents[0]));
					int returnsValue = jsonEnc.startsWith("1#") ? 1 : 0;
					String returnedValue = jsonEnc.startsWith("1#") ? jsonEnc.substring(2) : jsonEnc;
					
					if (payloadLength == 2) {
						DataBaseUtils.recordQuery(Integer.parseInt(bodyContents[0]), bodyContents[1],
								datagramVersion, .0, .0, returnsValue);					}
					
					else if (payloadLength == 4) {
						DataBaseUtils.recordQuery(Integer.parseInt(bodyContents[0]), bodyContents[1], 
								datagramVersion, Double.parseDouble(bodyContents[2]), Double.parseDouble(bodyContents[3]), 
								returnsValue);
					}
					
					output = createMessage(Utils.LINE_INCIDENTS_INFO_RCVD, new String[] {returnedValue});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.i("Exception extracting incidents information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.GET_COMMENTS_FOR_INCIDENT: // Comments request for particular incident
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received comments request for incident id " + bodyContents[0]);
					String jsonEnc = "";
					if ( datagramType==Utils.DATAGRAM_GN_TYPE ) {
						jsonEnc = DataBaseUtils.getGeneralIncComments(Integer.parseInt(bodyContents[0]));
						
					} else if ( datagramType==Utils.DATAGRAM_PT_TYPE ) {
						jsonEnc = DataBaseUtils.getComments(Integer.parseInt(bodyContents[0]));
					}
					output = createMessage(Utils.COMMENTS_FOR_INCIDENT_RCVD, new String[] {jsonEnc});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.i("Exception extracting incident comments - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.GET_SINGLE_INCIDENT:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received incident information request for incident id " + bodyContents[0]);
					String jsonEnc = "";
					if ( datagramType==Utils.DATAGRAM_GN_TYPE ) {
						jsonEnc = DataBaseUtils.getSingleIncident(Integer.parseInt(bodyContents[0]), 
								Utils.GENERAL_INCIDENTS);
					} else if ( datagramType==Utils.DATAGRAM_PT_TYPE ) {
						jsonEnc = DataBaseUtils.getSingleIncident(Integer.parseInt(bodyContents[0]));
					}
					
					output = createMessage(Utils.SINGLE_INCIDENT_RCVD, new String[] {jsonEnc});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.i("Exception extracting incident information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.GET_GENERAL_INCIDENTS:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received general incidents request from " + bodyContents[0]);
					String jsonEnc = DataBaseUtils.getJSONEncGeneralIncidents();
					int returnsValue = jsonEnc.startsWith("1#") ? 1 : 0;
					String returnedValue = jsonEnc.startsWith("1#") ? jsonEnc.substring(2) : jsonEnc;
					
					if ( payloadLength==1 ) {
						DataBaseUtils.recordQuery(Utils.GENERAL_INCIDENTS, bodyContents[0], 
								datagramVersion, .0, .0, returnsValue);				}
					
					else if ( payloadLength==3 ) {
						DataBaseUtils.recordQuery(Utils.GENERAL_INCIDENTS, bodyContents[0], 
								datagramVersion, Double.parseDouble(bodyContents[1]), Double.parseDouble(bodyContents[2]), 
								returnsValue);
					}
					
					output = createMessage(Utils.GENERAL_INC_INFO_RCVD, new String[] {returnedValue});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.i("Exception extracting incidents information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.GET_ALL_INCIDENTS: //All incidents
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received all incidents request from " + bodyContents[0]);
					String jsonEnc = DataBaseUtils.getJSONEncAllIncidents();
					int returnsValue = jsonEnc.startsWith("1#") ? 1 : 0;
					String returnedValue = jsonEnc.startsWith("1#") ? jsonEnc.substring(2) : jsonEnc;
					
					if ( payloadLength == 1 ) {
						DataBaseUtils.recordQuery(Utils.ALL_INCIDENTS, bodyContents[0], 
								datagramVersion, .0, .0, returnsValue);
					}
					else if ( payloadLength == 3 ) {
						DataBaseUtils.recordQuery(Utils.ALL_INCIDENTS, bodyContents[0], 
								datagramVersion, Double.parseDouble(bodyContents[1]), Double.parseDouble(bodyContents[2]), 
								returnsValue);
					}
					
					output = createMessage(Utils.ALL_INCIDENTS_INFO_RCVD, new String[] {returnedValue});
					state = Constants.QUERY_RESULT_SEND;
					
				} catch (Exception e) {
					DailyLog.i("Exception extracting incidents information - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.INCIDENT_NOTIFICATION: // New incident notification received
				if (isXMLFormatCorrect(contents[1])) {
					try {
						if ( datagramType==Utils.DATAGRAM_PT_TYPE ) {
							IncidentNotification notification = decoder.parse(new StringReader(contents[1]));
							DailyLog.i("Received incidence notification from " + notification.getUsername());
							output = ValidationUtilities.validate(datagramVersion, notification, true);
							
						} else if ( datagramType==Utils.DATAGRAM_GN_TYPE ) {
							GeneralNotification notification = generalNotDecoder.parse(new StringReader(contents[1]));
							DailyLog.i("Received general incidence notification from " + notification.getUsername());
							output = ValidationUtilities.validate(datagramVersion, notification, true);
						}
						state = Constants.REPORT_RECEIVED;
						
					} catch (IOException e) {
						DailyLog.e("IOException parsing data - " + e.getMessage());
						output = createMessage(Constants.SERVER_ERROR, null);
						state = Constants.ERROR_TREATMENT;
						
					} catch (SAXException e) {
						DailyLog.e("SAXException parsing data - " + e.getMessage());
						output = createMessage(Constants.SERVER_ERROR, null);
						state = Constants.ERROR_TREATMENT;
						
					} catch (SQLException e) {
						DailyLog.e("SQLException inserting data to database - " + e.getMessage());
						output = createMessage(Constants.SERVER_ERROR, null);
						state = Constants.ERROR_TREATMENT;
						
					} catch (ClassNotFoundException e) {
						DailyLog.e("ClassNotFoundException JDBC connector - " + e.getMessage());
						output = createMessage(Constants.SERVER_ERROR, null);
						state = Constants.ERROR_TREATMENT;
					}
					
				} else {
					output = createMessage(Utils.INVALID_XML_FORMAT, new String[] {Utils.INVALID_XML_LABEL});
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Utils.INCIDENT_CONFIRMATION: // Incident confirmation received
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received incidence confirmation with id " + bodyContents[0] + 
							" from " + bodyContents[2 - (4 - payloadLength)]);
					if ( datagramType==Utils.DATAGRAM_PT_TYPE ) {
						if (payloadLength == 3) {
							output = ValidationUtilities.processConfirmation(
									Integer.parseInt(bodyContents[0]),
									"",
									bodyContents[1],
									datagramVersion,
									Long.parseLong(bodyContents[2]));
							
						} else if (payloadLength == 4) {
							output = ValidationUtilities.processConfirmation(
									Integer.parseInt(bodyContents[0]),
									bodyContents[1],
									bodyContents[2],
									datagramVersion,
									Long.parseLong(bodyContents[3]));
						}
						
					} else if ( datagramType==Utils.DATAGRAM_GN_TYPE ) {
						if (payloadLength == 3) {
							output = ValidationUtilities.processGeneralIncConfirmation(
									Integer.parseInt(bodyContents[0]),
									"",
									bodyContents[1],
									datagramVersion,
									Long.parseLong(bodyContents[2]));
							
						} else if (payloadLength == 4) {
							output = ValidationUtilities.processGeneralIncConfirmation(
									Integer.parseInt(bodyContents[0]),
									bodyContents[1],
									bodyContents[2],
									datagramVersion,
									Long.parseLong(bodyContents[3]));
						}
					}
					state = Constants.REPORT_RECEIVED;
					
				} catch (SQLException e) {
					DailyLog.e("SQLException inserting data to database - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
					
				} catch (ClassNotFoundException e) {
					DailyLog.e("ClassNotFoundException JDBC connector - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Constants.T11_NOTIFICATION:
				try {
					GeneralNotification t11Notification = t11Decoder.parse(new StringReader(contents[1]));
					DailyLog.i("Received T11 from " + t11Notification.getUsername());
					
					// Comprovar si hem d'enviar alguna notificaci� a l'usuari
					boolean note = DataBaseUtils.mustNotifyUser(t11Notification.getUsername(), datagramVersion);
					DataBaseUtils.recordT11(datagramVersion, t11Notification);
					
					if ( note )
						output = createMessage(Constants.T11_RECEIVED_CORECTLY, 
									Utils.DATAGRAM_T11_PICKED_UP_OPTION, null);
						
					else
						output = createMessage(Constants.T11_RECEIVED_CORECTLY, null);
					
					state = Constants.REPORT_RECEIVED;
					
				} catch (SAXException e) {
					DailyLog.e("SAXException parsing data - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
					
				} catch (IOException e) {
					DailyLog.e("IOException parsing data - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
					
				} catch (SQLException e) {
					DailyLog.e("SQLException inserting data to database - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
					
				} catch (ClassNotFoundException e) {
					DailyLog.e("ClassNotFoundException JDBC connector - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				}
				break;
				
			case Constants.T11_PICK_UP:
				try {
					String[] bodyContents = contents[1].split(Utils.BODY_SEPARATOR);
					DailyLog.i("Received T11 pick up notification with id " + bodyContents[0] + 
							" from " + bodyContents[1]);
					output = ValidationUtilities.processT11PickUpNotification(
							Integer.parseInt(bodyContents[0]), 
							bodyContents[1], 
							datagramVersion,
							Double.parseDouble(bodyContents[2]), 
							Double.parseDouble(bodyContents[3]), 
							Long.parseLong(bodyContents[4]));
					
					state = Constants.REPORT_RECEIVED;
					
				} catch (SQLException e) {
					DailyLog.e("SQLException inserting data to database - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
					
				} catch (ClassNotFoundException e) {
					DailyLog.e("ClassNotFoundException JDBC connector - " + e.getMessage());
					output = createMessage(Constants.SERVER_ERROR, null);
					state = Constants.ERROR_TREATMENT;
				} 
				break;
			}
			
		} else if (state == Constants.WAITING_REPORT_INFO) {
						
			if (inputLine.startsWith("<report")) {
				try {
					IncidentNotification report = decoder.parse(new StringReader(inputLine));
					DailyLog.i("Received incidence notification from " + report.getUsername());
					output = ValidationUtilities.validate(1, report, true);
					state = Constants.REPORT_RECEIVED;
					
				} catch (SAXException e) { 
					DailyLog.e("SAXException parsing data - " + e.getMessage());
					output = Constants.SERVER_ERROR_LABEL;
					state = Constants.ERROR_TREATMENT;
					
				} catch (IOException e) { 
					DailyLog.e("IOException parsing data - " + e.getMessage());
					output = Constants.SERVER_ERROR_LABEL;
					state = Constants.ERROR_TREATMENT;
					
				} catch (SQLException e) { 
					DailyLog.e("SQLException inserting data to database - " + e.getMessage());
					output = Constants.SERVER_ERROR_LABEL;
					state = Constants.ERROR_TREATMENT;
					
				} catch (ClassNotFoundException e) { 
					DailyLog.e("ClassNotFoundException JDBC connector - " + e.getMessage());
					output = Constants.SERVER_ERROR_LABEL;
					state = Constants.ERROR_TREATMENT;
				}
				
			} else {
				output = Constants.BAD_INPUT_LABEL;
				state = Constants.ERROR_TREATMENT;	
			}
			
		} else if (state == Constants.REPORT_RECEIVED) {
			
			if (inputLine.equalsIgnoreCase("EXIT")) {
				output = "EXIT";
				state = Constants.WAITING;
			} else {
				output = Constants.BAD_INPUT_LABEL;
				state = Constants.ERROR_TREATMENT;
			}
			
		} else if (state == Constants.QUERY_RESULT_SEND) {
			
			if (inputLine.equalsIgnoreCase("EXIT")) {
				output = "EXIT";
				state = Constants.WAITING;
			} else {
				output = Constants.BAD_INPUT_LABEL;
				state = Constants.ERROR_TREATMENT;
			}
			
		} else if (state == Constants.ERROR_TREATMENT) {
			
			if (inputLine.equalsIgnoreCase("EXIT")) {
				output = "EXIT";
				state = Constants.WAITING;
			} else {
//				output = Constants.BAD_INPUT_LABEL;
//				state = Constants.ERROR_TREATMENT;
				output = "EXIT";
				state = Constants.WAITING;
			}
		}
		
		return output;
	}
	
	private boolean isXMLFormatCorrect(String xml) {
		int indexOfReportTag = xml.indexOf(Utils.VALID_XML_BEGINING);
		if (indexOfReportTag != -1) {
			String preamble = xml.substring(0, xml.indexOf(Utils.VALID_XML_BEGINING));
			if ( preamble.equalsIgnoreCase(Utils.VALID_XML_PREAMBLE) ) {
				return true;		}
		}
		
		return false;
	}

	public boolean confirmationMsg(String input) {
//		String[] parts = input.split("#");
//		if ((Integer.parseInt(parts[0]) == Constants.EVENT_CONFIRMATION) && 
//			(parts.length == 4)) {
//			DailyLog.i("Received incidence confirmation from " + parts[2]);
//			return true;
//		}
//		
		return false;
	}
	
	public String processInputQuery(String query) {
		String[] queryContents = query.split(Utils.DATAGRAM_SEPARATOR);
		int code = Utils.getHeaderIdentifier(queryContents[0]);
		
		switch (code) {
		case Constants.AUTHENTICATE_USER:
			try {
				String[] bodyContents = queryContents[1].split("#");
				DailyLog.i("Received authentication request for user " + bodyContents[0]);
				int result = DataBaseUtils.authenticateUser(bodyContents[0], bodyContents[1]);
				state = Constants.QUERY_RESULT_SEND;
				return createMessage(result, null);
				
			} catch (Exception e) {
				DailyLog.e("Exception authenticating user - " + e.getMessage());
				state = Constants.ERROR_TREATMENT;
				return createMessage(Constants.SERVER_ERROR, null);
			}
			
		case Constants.GET_EVENTS:
//			try {
//				DailyLog.i("Received request for incidents of type " + queryParts[1]);
//				String data = DataBaseUtils.getEventsDescription(Integer.parseInt(queryParts[1]));
//				state = Constants.QUERY_RESULT_SEND;
//				return getMessageDescription(Constants.EVENT_INFORMATION_RECEIVED) + "#" + data + "\n";
//				
//			} catch (Exception e) {
//				DailyLog.e("Exception retrieving events information - " + e.getMessage());
//				state = Constants.ERROR_TREATMENT;
//				return getMessageDescription(Constants.SERVER_ERROR);
//			}
			
		case Constants.GET_LAT_LNG:
//			try {
//				DailyLog.i("Received GPS coordenates request for station id " + queryParts[1]);
//				String jsonData = DataBaseUtils.getLatLng(queryParts[1]);
//				
//				if (jsonData != null) {
//					state = Constants.QUERY_RESULT_SEND;
//					return getMessageDescription(Constants.LATLNG_INFORMATION_RECEIVED) + 
//								"#" + jsonData + "\n";
//					
//				} else {
//					state = Constants.QUERY_RESULT_SEND;
//					return getMessageDescription(Constants.LATLNG_INFORMATION_UNAVAILABLE);
//				}
//				
//			} catch (Exception e) {
//				DailyLog.e("Exception retrieving location information - " + e.getMessage());
//				state = Constants.ERROR_TREATMENT;
//				return getMessageDescription(Constants.SERVER_ERROR);
//			}
			
		default:
			state = Constants.ERROR_TREATMENT;
			return getMessageDescription(Constants.UNEXPECTED_MESSAGE);
		}
	}
}