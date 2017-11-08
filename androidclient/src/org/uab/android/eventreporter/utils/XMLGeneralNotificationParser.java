package org.uab.android.eventreporter.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLGeneralNotificationParser extends DefaultHandler {
	
	public static final String PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String REPORT_TAG = "report";
	public static final String REPORT_SEVERITY = "severity=\"";
	public static final String DESCRIPTION_TAG = "description";
	public static final String TIME_TAG = "time";	
	public static final String USERNAME_TAG = "username";
	public static final String GPS_TAG = "gps";
	public static final String GPS_LATITUDE_TAG = "latitude";
	public static final String GPS_LONGITUDE_TAG = "longitude";
	public static final String OPEN_TAG = "<";
	public static final String CLOSE_TAG = ">";
	public static final String END_TAG = "</";
	
	private XMLReader xmlReader = null;
	private GeneralNotification notification = null;
	private StringBuilder elementReader = null;
	private static Logger logger = Logger.getLogger(XMLIncidentNotificationParser.class.getName());
	
	public XMLGeneralNotificationParser() throws SAXException {
		xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
	}
	
	/**
	 * -- ENCODING METHODS --
	 * 
	 * Main method : encodes a new report in XML format
	 */
	public static String encode(int severity, String description, long time, 
			String username, double latitude, double longitude) {
		
		StringBuilder encodedXML = new StringBuilder();
		
		// Write preamble
		encodedXML.append(PREAMBLE);
		
		// Encode <report severity="">
		encodedXML.append(OPEN_TAG).append(REPORT_TAG).append(" ");
		encodedXML.append(REPORT_SEVERITY).append(severity).append("\"");
		encodedXML.append(CLOSE_TAG);
		
		// Encode <description> content </description>
		String d = new String(Base64.encodeBase64(description.getBytes()));
//		String d = Base64Coder.encodeLines(description.getBytes());
		encodeTag(encodedXML, DESCRIPTION_TAG, d);
		
		// Encode <time> time </time>
		encodeTag(encodedXML, TIME_TAG, "" + time);
		
		// Encode <username> username </username>
		encodeTag(encodedXML, USERNAME_TAG, username);
		
		// Encode <gps> <latitude> lat </latitude> <longitude> lon </longitude> </gps>
		encodedXML.append(OPEN_TAG).append(GPS_TAG).append(CLOSE_TAG);
		encodeTag(encodedXML, GPS_LATITUDE_TAG, "" + latitude);
		encodeTag(encodedXML, GPS_LONGITUDE_TAG, "" + longitude);
		encodedXML.append(END_TAG).append(GPS_TAG).append(CLOSE_TAG);
		
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
	public GeneralNotification parse(Reader input) throws SAXException, IOException {
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
			String s = elementReader.toString();
			notification.setDescription(s);
			
		} else if (localName.equalsIgnoreCase(TIME_TAG)) {
			notification.setLastUpdateTime(Long.parseLong(elementReader.toString()));
			
		} else if (localName.equalsIgnoreCase(USERNAME_TAG)) {
			notification.setUsername(elementReader.toString());
			
		} else if (localName.equalsIgnoreCase(GPS_LATITUDE_TAG)) {
			notification.setLatitude(Double.parseDouble(elementReader.toString()));
			
		} else if (localName.equalsIgnoreCase(GPS_LONGITUDE_TAG)) {
			notification.setLongitude(Double.parseDouble(elementReader.toString()));
			
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
		notification = new GeneralNotification();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementReader = new StringBuilder();
		
		if (localName.equalsIgnoreCase(REPORT_TAG)) {
			notification.setSeverity(Integer.parseInt(attributes.getValue(0)));
			
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
