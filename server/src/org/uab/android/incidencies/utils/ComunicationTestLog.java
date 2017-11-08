package org.uab.android.incidencies.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ComunicationTestLog {

	private static final String DEFAULT_PREFIX = "/var/log/reporting_test/";
	private static final String LOGFILE_NAME = "comm-prot";
	private static final String FINAL_LOG = ".log";
	private static FileOutputStream outputStream = null;
	
	public static void e(String message) {
		writeLogRecord(LogRecord.ERROR_TAG, message);
	}
	
	public static void w(String message) {
		writeLogRecord(LogRecord.WARNING_TAG, message);
	}
	
	public static void i(String message) {
		writeLogRecord(LogRecord.INFO_TAG, message);
	}
	
	private static void writeLogRecord(String tag, String message) {
		try {
			if (outputStream == null) {
				outputStream = new FileOutputStream(new File(getFileName()), true);
				
			} else if (!isFileOpen()) {
				outputStream.close(); 
				outputStream = new FileOutputStream(new File(getFileName()), true);
				
			}
			outputStream.write(new LogRecord(tag, message).getBytes());
			
		} catch (IOException e) {  }
	}
	
	private static String getFileName() {
		return DEFAULT_PREFIX + LOGFILE_NAME + FINAL_LOG;
	}
	
	private static boolean isFileOpen() {
		File f = new File(getFileName());
		
		return f.exists();
	}
}
