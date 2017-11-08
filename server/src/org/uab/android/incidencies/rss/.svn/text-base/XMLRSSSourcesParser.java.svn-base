package org.uab.android.incidencies.rss;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLRSSSourcesParser extends DefaultHandler {

	private XMLReader reader;
	private StringBuilder elementReader;
	private static Logger log = Logger.getLogger(XMLRSSSourcesParser.class.getName());
	private ArrayList<RSSSource> sources;
	private RSSSource actual;
	
	public XMLRSSSourcesParser() throws SAXException {
		reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
	}
	
	public ArrayList<RSSSource> parse(Reader input) throws IOException, SAXException {
		reader.parse(new InputSource(input));
		return sources;
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
		if ( localName.equalsIgnoreCase("url") ) {
			actual.setUrl(elementReader.toString());
			sources.add(actual);
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		if (log.isLoggable(Level.SEVERE)) {
			log.log(Level.SEVERE, 
					"ERROR on line " + e.getLineNumber() + ":" + e.getMessage());
		}
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (log.isLoggable(Level.SEVERE)) {
			log.log(Level.SEVERE, 
					"FATAL ERROR on line " + e.getLineNumber() + ":" + e.getMessage());
			
			throw e;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		sources = new ArrayList<RSSSource>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementReader = new StringBuilder();
		if ( localName.equalsIgnoreCase("source") )
			actual = new RSSSource();
		
		else if ( localName.equalsIgnoreCase("url") )
			actual.setLineId(Integer.parseInt(attributes.getValue(0)));
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (log.isLoggable(Level.WARNING)) {
			log.log(Level.WARNING, 
					"WARNING on line " + e.getLineNumber() + ":" + e.getMessage());
		}
	}

}
