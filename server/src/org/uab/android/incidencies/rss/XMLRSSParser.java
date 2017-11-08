package org.uab.android.incidencies.rss;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLRSSParser extends DefaultHandler {

	public static final String ITEM_TAG = "item";
	public static final String TITLE_TAG = "title";
	public static final String DESCRIPTION_TAG = "description";
	public static final String PUB_DATE_TAG = "pubDate";
	
	private XMLReader reader;
	private StringBuilder elementReader;
	private boolean isItem = false;
	private static Logger log = Logger.getLogger(XMLRSSParser.class.getName());
	private DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private ArrayList<RSSFeed> feeds;
	private RSSFeed feedMessage;
	
	public XMLRSSParser() throws SAXException {
		reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
	}
	
	public ArrayList<RSSFeed> parse(Reader input) throws IOException, SAXException {
		reader.parse(new InputSource(input));
		return feeds;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		elementReader.append(ch, start, length);
	}
	
	@Override
	public void endDocument() throws SAXException {

	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if ( isItem ) {
			if ( localName.equalsIgnoreCase(TITLE_TAG) ) {
				try {
					feedMessage.setTitle(new String(
							elementReader.toString().getBytes(),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new SAXException(e);
				}			
			}
			
			else if ( localName.equalsIgnoreCase(DESCRIPTION_TAG) ) {
				try {
					feedMessage.setDescription(new String(
							elementReader.toString().getBytes(),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new SAXException(e);
				}		
			}
			
			else if ( localName.equalsIgnoreCase(PUB_DATE_TAG) ) {
				try {
					feedMessage.setPubDate(df.parse(elementReader.toString()).getTime());
					
				} catch (ParseException e) {
					throw new SAXException(e);
				}
			}
				
			else if ( localName.equalsIgnoreCase(ITEM_TAG) ) {
				feeds.add(feedMessage);
				isItem = false;
			}
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
		feeds = new ArrayList<RSSFeed>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		elementReader = new StringBuilder();
		if ( localName.equalsIgnoreCase(ITEM_TAG) ) {
			feedMessage = new RSSFeed();
			isItem = true;
			
		} 
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (log.isLoggable(Level.WARNING)) {
			log.log(Level.WARNING, 
					"WARNING on line " + e.getLineNumber() + ":" + e.getMessage());
		}
	}
}
