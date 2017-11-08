package org.uab.android.incidencies.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
	private String defaultLog = "/var/log/reporting/server.log";
	private FileOutputStream out = null;
	private DateFormat df = null;
	
	public static final String INFO = "LOG_INFO";
	public static final String WARNING = "LOG_WARNING";
	public static final String ERROR = "LOG_ERROR";
	
	public Log() throws FileNotFoundException {
		out = new FileOutputStream(new File(defaultLog), true);
		df = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, 
				DateFormat.MEDIUM, 
				Locale.UK);
	}
	
	public Log(String filename) throws FileNotFoundException {
		out = new FileOutputStream(new File(filename), true);
		df = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, 
				DateFormat.MEDIUM, 
				Locale.FRANCE);
	}
	
	public void log(String tag, String message) {
		String formatted_msg = df.format(new Date()) + " " + tag + ": " + message + "\n";
		try { out.write(formatted_msg.getBytes()); }
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
