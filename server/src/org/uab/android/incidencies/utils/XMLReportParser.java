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

public class XMLReportParser extends DefaultHandler {
	
	/** Constants */
	public static final String PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String REPORT_TAG = "report";
	public static final String REPORT_TYPE = "type=\"";
	public static final String REPORT_SEVERITY = "severity=\"";
	public static final String REPORT_CLASS = "class=\"";
	
	public static final String DESCRIPTION_TAG = "description";
	
	public static final String GPS_TAG = "gps";
	public static final String GPS_LATITUDE = "lat=\"";
	public static final String GPS_LONGITUDE = "lon=\"";
	
	public static final String TIME_TAG = "time";
	
	public static final String USERNAME_TAG = "username";
	
	public static final String OPEN_TAG = "<";
	public static final String CLOSE_TAG = ">";
	public static final String END_TAG = "</";
	
	private XMLReader parser = null;
	private Report report = null;
	private StringBuilder elementReader = null;
	private static Logger logger = Logger.getLogger(XMLReportParser.class.getName());
	
	public XMLReportParser() throws SAXException {
		parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(this);
		parser.setErrorHandler(this);
	}
	
	/**
	 * -- ENCODING METHODS --
	 * 
	 * Main method : encodes a new report in XML format
	 */
	public static String encode(int type, int reportClass, int severity, 
			String description, double lat, double lon, long time, String username) {
		
		StringBuilder encodedXML = new StringBuilder();
		
		// Write preamble
		encodedXML.append(PREAMBLE).append("\n");
		
		// Encode <report type="" severity="">
		encodedXML.append(OPEN_TAG).append(REPORT_TAG).append(" ");
		encodedXML.append(REPORT_TYPE).append(type).append("\"").append(" ");
		encodedXML.append(REPORT_CLASS).append(reportClass).append("\"").append(" ");
		encodedXML.append(REPORT_SEVERITY).append(severity).append("\"");
		encodedXML.append(CLOSE_TAG);
		
		if (!description.equalsIgnoreCase("")) {
			// Encode <description> content </description>
			encodeTag(encodedXML, DESCRIPTION_TAG, description);
		}
		
		// Encode <gps lat="" lon="">
		encodedXML.append(OPEN_TAG).append(GPS_TAG).append(" ");
		encodedXML.append(GPS_LATITUDE).append(lat).append("\"").append(" ");
		encodedXML.append(GPS_LONGITUDE).append(lon).append("\"");
		encodedXML.append(CLOSE_TAG);
		
		// Encode <time> time </time>
		encodeTag(encodedXML, TIME_TAG, "" + time);
		
		// Close <gps ... > TAG
		encodedXML.append(END_TAG).append(GPS_TAG).append(CLOSE_TAG);
		
		// Encode <username> user name </username>
		encodeTag(encodedXML, USERNAME_TAG, username);
		
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
	public Report parse(Reader input) throws SAXException, IOException {
		parser.parse(new InputSource(input));
		return report;
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
			report.setDescription(elementReader.toString());
			
		} else if (localName.equalsIgnoreCase(TIME_TAG)) {
			report.setTime(Long.parseLong(elementReader.toString()));
			
		} else if (localName.equalsIgnoreCase(USERNAME_TAG)) {
			report.setUsername(elementReader.toString());
			
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
		report = new Report();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementReader = new StringBuilder();
		
		if (localName.equalsIgnoreCase(REPORT_TAG)) {
			report.setType(Integer.parseInt(attributes.getValue(0)));
			report.setReportClass(Integer.parseInt(attributes.getValue(1)));
			report.setSeverity(Integer.parseInt(attributes.getValue(2)));
			
		} else if (localName.equalsIgnoreCase(GPS_TAG)) {
			report.setLat(Double.parseDouble(attributes.getValue(0)));
			report.setLon(Double.parseDouble(attributes.getValue(1)));
			
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
