package org.uab.android.incidencies.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLIncidentNotificationParser extends DefaultHandler {

	public static final String PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String REPORT_TAG = "report";
	public static final String CONFIRMATION_TAG = "confirmation";
	public static final String REPORT_SERVICE_TYPE = "service=\"";
	public static final String REPORT_SEVERITY = "severity=\"";
	public static final String REPORT_CAUSE = "cause=\"";	
	public static final String DESCRIPTION_TAG = "description";
	public static final String TIME_TAG = "time";	
	public static final String USERNAME_TAG = "username";
	public static final String STATION_INFO_TAG = "station";
	public static final String UNIQUE_LINE_ID = "line=\"";
	public static final String LINE_DIRECTION = "direction=\"";
	public static final String OPEN_TAG = "<";
	public static final String CLOSE_TAG = ">";
	public static final String END_TAG = "</";
	
	private XMLReader xmlReader = null;
	private IncidentNotification notification = null;
	private StringBuilder elementReader = null;
	private static Logger logger = Logger.getLogger(XMLIncidentNotificationParser.class.getName());
	
	public XMLIncidentNotificationParser() throws SAXException {
		xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
	}
	
	/**
	 * -- ENCODING METHODS --
	 * 
	 * Main method : encodes a new report in XML format
	 */
	public static String encode(int service, int lineId, String station, int direction, 
			int cause, int severity, String description, long time, String username) {
		
		StringBuilder encodedXML = new StringBuilder();
		
		// Write preamble
		encodedXML.append(PREAMBLE);
		
		// Encode <report service="" cause="" severity="">
		encodedXML.append(OPEN_TAG).append(REPORT_TAG).append(" ");
		encodedXML.append(REPORT_SERVICE_TYPE).append(service).append("\"").append(" ");
		encodedXML.append(REPORT_CAUSE).append(cause).append("\"").append(" ");
		encodedXML.append(REPORT_SEVERITY).append(severity).append("\"");
		encodedXML.append(CLOSE_TAG);
		
		if (!description.equalsIgnoreCase("")) {
			// Encode <description> content </description>
			encodeTag(encodedXML, DESCRIPTION_TAG, description);
		}
		
		// Encode <time> time </time>
		encodeTag(encodedXML, TIME_TAG, "" + time);
		
		// Encode <username> user name </username>
		encodeTag(encodedXML, USERNAME_TAG, username);
		
		// Encode <station line=""> station_name </station>
		encodedXML.append(OPEN_TAG).append(STATION_INFO_TAG).append(" ");
		encodedXML.append(UNIQUE_LINE_ID).append(lineId).append("\"").append(" ");
		encodedXML.append(LINE_DIRECTION).append(direction).append("\"");
		encodedXML.append(CLOSE_TAG);
		encodedXML.append(station);
		encodedXML.append(END_TAG).append(STATION_INFO_TAG).append(CLOSE_TAG);
		
		// Close <report ... > TAG
		encodedXML.append(END_TAG).append(REPORT_TAG).append(CLOSE_TAG);
		
		return encodedXML.toString();
	}
	
	// Encodes < tag > content </tag>
	private static void encodeTag(StringBuilder sb, String tag, String content) {
		sb.append(OPEN_TAG).append(tag).append(CLOSE_TAG);
		sb.append(content);
		sb.append(END_TAG).append(tag).append(CLOSE_TAG);
	}
	
	/**
	 * -- DECODING METHODS --
	 * 
	 * Main method : begins parsing an incoming report
	 */
	public IncidentNotification parse(Reader input) throws SAXException, IOException {
		xmlReader.parse(new InputSource(input));
		return notification;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		elementReader.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equalsIgnoreCase(DESCRIPTION_TAG)) {
			notification.setDescription(elementReader.toString());
			
		} else if (localName.equalsIgnoreCase(TIME_TAG)) {
			notification.setTime(Long.parseLong(elementReader.toString()));
			
		} else if (localName.equalsIgnoreCase(USERNAME_TAG)) {
			notification.setUsername(elementReader.toString());
			
		} else if (localName.equalsIgnoreCase(STATION_INFO_TAG)) {
			notification.setStationHashValue(elementReader.toString());
			
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, 
					"ERROR on line " + e.getLineNumber() + ":" + e.getMessage());
		}
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, 
					"FATAL ERROR on line " + e.getLineNumber() + ":" + e.getMessage());
			
			throw e;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		notification = new IncidentNotification();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementReader = new StringBuilder();
		
		if (localName.equalsIgnoreCase(REPORT_TAG)) {
			notification.setTransportService(Integer.parseInt(attributes.getValue(0)));
			notification.setCause(Integer.parseInt(attributes.getValue(1)));
			notification.setSeverity(Integer.parseInt(attributes.getValue(2)));
			
		} else if (localName.equalsIgnoreCase(STATION_INFO_TAG)) {
			notification.setUniqueLineId(Integer.parseInt(attributes.getValue(0)));
			notification.setDirection(Integer.parseInt(attributes.getValue(1)));
			
		}
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, 
					"WARNING on line " + e.getLineNumber() + ":" + e.getMessage());
		}
	}
}
