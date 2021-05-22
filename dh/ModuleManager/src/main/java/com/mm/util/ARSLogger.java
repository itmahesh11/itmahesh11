package com.mm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;



/**
 * ARSLogger is the logger class defined in ARS Style
 * The format in which the logger logs is given by
 * the innner class ARSLogFormatter. 
 * 
 */

public class ARSLogger extends Logger {
	/** Directory separator */
	public static final String DIR_SEP = "\\";
	/** Indicates current memory in bytes used by the buffer */
	private static int bufferSizeInBytes = 0;
	/** Maximum size of the buffer in Bytes, it's value is computed
	 *  while initialization */
	private static int maxBufferSize = 0;	
	/** Internal Buffer for storing log record */
	private ArrayList<LogRecord> logList = new ArrayList<LogRecord>();
	/** Default max. size for the buffer */
	private static final int MAX_DEFAULT_BUFFER_SIZE = 1000000;
	// The following parameters need to be set before making any log 
	/** Maximum size of the log file in Bytes, Need to set by Application*/
	private static int maxLogfileSize =0;
	/** Log File Name without extension, Need to set by Application */
	private static String logfileName;
	/** Log File path, Need to set by Application */
	private static String logfilePath;
	/**Maximum number of backup files that logger needs to maintain */
	private static int maxNoOfBackups;
	/** File Extension for the log Files */
	public static final String LOGEXT = ".txt";
	/** Status flag indicating whether ARS logger is initiated */
	private static boolean isInitialized = false;
	private boolean disableBuffering;
	

	/**
	 * The inner class providing the format in which ARSLogger logs.
	 * @author Dharani Vijayakumar
	 */
	private class ARSLogFormatter extends Formatter {
		Date date = new Date();
		private final static String timeStampFormat = "[yyyy/MM/dd,HH:mm:ss.SSS]";
		private Format formatter;
		// Line separator string.  This is the value of the line.separator
		// property at the moment that the SimpleFormatter was created.
		private String lineSeparator = System.getProperty("line.separator"); 
		/**
		 * Defining format for the log which we use
		 * @param record - the log record to be formatted.
		 * @return a formatted log record 
		 */
		public synchronized String format(LogRecord record) {
			StringBuffer sb = new StringBuffer();		
			date.setTime(record.getMillis());		
			if (formatter == null) {
				formatter = new SimpleDateFormat(timeStampFormat);
			}		
			sb.append(formatter.format(date));// Adding formatted TimeStamp
			sb.append(" [");
			sb.append(record.getThreadID());//Adding the Log Level
			sb.append("] ");
			sb.append(" [");
			sb.append(record.getLevel().getLocalizedName());//Adding the Log Level
			sb.append("] ");
			sb.append(record.getSourceClassName());// Adding the source class name of logged record
			sb.append(".");
			sb.append(record.getSourceMethodName());// Adding the source method name of logged record
			sb.append("(): ");		
			String message = formatMessage(record);
			sb.append(message);// Adding actual Message content
			sb.append(lineSeparator);// Adding a line seperator to seperate each log file
			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (Exception ex) {
				}
			}
			return sb.toString();
		}
	} // The End of Inner Class ARSLogFormatter


	/**
	 * Constructor of the ARSLogger.
	 * 
	 * @param fileName -  (String) Log File Name without extension
	 * @param filePath -  (String) Log File path
	 * @param maxFileSize -(int) Maximum size of the log file in Bytes
	 * @param noOfBackupsFiles - (int)Maximum number of backup files that logger needs to maintain
	 */
	public ARSLogger(String fileName, String filePath, int maxFileSize, 
			int noOfBackupsFiles, boolean disableBuffering) {
		super("ARSLogger", null);	
		logfileName = fileName;
		logfilePath = filePath;
		maxLogfileSize = maxFileSize;
		maxNoOfBackups = noOfBackupsFiles;
		this.disableBuffering = disableBuffering;
		init();
	}	
	
	/**
	 * Constructor for ARSLogger
	 * @param fileName
	 * @param filePath
	 * @param maxFileSize
	 * @param noOfBackupsFiles
	 */
	public ARSLogger(String fileName, String filePath, int maxFileSize, 
			int noOfBackupsFiles) {
		super("ARSLogger", null);	
		logfileName = fileName;
		logfilePath = filePath;
		maxLogfileSize = maxFileSize;
		maxNoOfBackups = noOfBackupsFiles;
		this.disableBuffering = true;// Default disable buffering
		init();
	}

	/**
	 * This will do the Initialization process for the ARSLogger class.
	 * This function should be called once after created an object 
	 */
	private void init(){

		
		// Computing the maximum buffer size. It would be the minimum among
		// (10 percent of Max Log File size) and the preset MAX_DEFAULT_BUFFER_SIZE 
		maxBufferSize = ((maxLogfileSize/10)<MAX_DEFAULT_BUFFER_SIZE)? (maxLogfileSize/10): MAX_DEFAULT_BUFFER_SIZE;			
		isInitialized = true;

	}


	/**
	 * Starts the logger.Initialises file handler.
	 * @param bufferingDisabled 
	 */
	public void startLogger(boolean bufferingDisabled){
		
		try {
			String logFileFullName = logfileName + LOGEXT;
			
			if (initLogDir()) {
				
				logFileFullName = logfilePath + DIR_SEP + logFileFullName;
				
			}	
			// Create a file handler that write log record to a file specified by logFileName 
			FileHandler handler = new FileHandler(logFileFullName, maxLogfileSize,maxNoOfBackups,true);
			handler.setFormatter(new ARSLogFormatter());//Use ARS defined format for logging	       
			this.addHandler(handler); // Add file handler for the logger to capture log to a file.
			dump();
			this.disableBuffering = bufferingDisabled;
		}catch (Exception e) {
			System.out.println("ArsLogger.init(), Exception in assigning File Handler");    	
		}	
	}
	/**
	 * Check for Log file directory. if it is not present create
	 * and return the status of operation.
	 */
	private static boolean initLogDir() {
		File file = new File(logfilePath);
		return file.exists() || file.mkdirs();
	}

	/**
	 * Log the given message with its level. The log message
	 * shall be added and kept in an internal buffer of the ARSLogger.
	 * The logger shall write to a file only when the buffer exceeds
	 * a threshold limit or by explicitly invoking the dump() method
	 * @param message 
	 * @param level (int) 0:Off, 1:Severe, 2:Warning, 3:Info
	 */
	public synchronized void addLog(String message, int level){	
		synchronized (this) {
			if (message == null) {
				return;
			}
			StackTraceElement stack[] = (new Throwable()).getStackTrace();		
			StackTraceElement frame = stack[1];
			Level logLevel;

			//Show log on Console
			System.out.println(frame.getClassName()+"."+frame.getMethodName()+"(), "+message);

			if(isInitialized) { // if the log file is correctly initialized
				// Determining which level it belongs
				switch (level) {
				case 0:
					logLevel = Level.OFF;
					break;
				case 1:
					logLevel = Level.SEVERE;
					break;
				case 2:
					logLevel = Level.WARNING;
					break;
				case 3:
					logLevel = Level.INFO;
					break;
				default:
					logLevel = Level.ALL;
				}	
				// Creating a new logRecord from the available info
				LogRecord lr = new LogRecord(logLevel, message);
				lr.setSourceClassName(frame.getClassName());
				lr.setSourceMethodName(frame.getMethodName());
				//eg [2008/11/04,11:09:07.970] [INFO] com.ars.packageName.ClassName.FunctionName(): Log Message
				// 42:(minimum) bytes required to save time stamp and other formatting symbols
				bufferSizeInBytes += (42+frame.getClassName().length()+frame.getMethodName().length()+message.length());

				// adding the logRecord to the internal Buffer
				logList.add(lr);
				//check whether the size exceeds the buffer
				if(disableBuffering || bufferSizeInBytes > maxBufferSize)
					dump();
			}
		}
	}	

	/**
	 * Force the ARSLogger to dump all the buffer content into file.
	 * After writing it shall clear its internal buffer
	 */
	public synchronized void dump(){
		synchronized (this) {
			if(!initLogDir() && bufferSizeInBytes > maxBufferSize){//if Application fails to initialize ARSLogger, handle the buffer
				logList.clear();
			}			
			for (int i = 0; i < logList.size(); i++) {
				super.log(logList.get(i));
			}
			bufferSizeInBytes =0;
			logList.clear();
		}
	}


	/**
	 * method over ridden
	 */
	@Override
	public synchronized void log(Level level, String msg){
		synchronized (this) {
			if (msg == null) {
				return;
			}
			StackTraceElement stack[] = (new Throwable()).getStackTrace();
			StackTraceElement frame = stack[1];

			//Show log on Console
			System.out.println(frame.getClassName()+"."+frame.getMethodName()+"(), "+msg);	

			if(isInitialized) { // if the log file is correctly initialized
				LogRecord lr = new LogRecord(level, msg);
				lr.setSourceClassName(frame.getClassName());
				lr.setSourceMethodName(frame.getMethodName());
				//eg [2008/11/04,11:09:07.970] [INFO] com.ars.packageName.ClassName.FunctionName(): Log Message
				// 39: bytes required to save time stamp and other formatting symbols
				bufferSizeInBytes += (39+frame.getClassName().length()+frame.getMethodName().length()+msg.length());

				// adding the logRecord to the internal List
				logList.add(lr);
				//check whether the size exceeds the buffer		
				if(disableBuffering || bufferSizeInBytes > maxBufferSize)
					dump();
			}
		}
	}

	/**
	 * Write an error log to the current working directory
	 * 
	 * @param msg the message to be logged
	 */
	public static void logFileError(String msg) {
		BufferedWriter writer = null;
		try {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
			msg = "[" + cal.get(Calendar.HOUR_OF_DAY) 
			+ ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) +"]" + msg ;
			System.out.println(msg);
			String file = System.getProperty("user.dir") + File.separator + "error.log";
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(msg);
			writer.newLine();
		} catch (FileNotFoundException e) {
			// ignored
		} catch (IOException e) {
			// ignored
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
