package org.uab.android.incidencies.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class LogRecord {
	public static final String ERROR_TAG = "LOG_ERROR";
	public static final String WARNING_TAG = "LOG_WARNING";
	public static final String INFO_TAG = "LOG_INFO";
	
	private String record = "";
	private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			DateFormat.MEDIUM, Locale.UK);
	
	public LogRecord(String tag, String message) {
		this.record = dateTimeFormat.format(new Date()) + " " + tag + ": " + message + "\n"; 
	}
	
	public byte[] getBytes() {
		return record.getBytes();
	}
}
