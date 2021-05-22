package com.ars.ssm;



import java.io.IOException;
import java.util.logging.Level;

import com.ars.ssm.connection.StopSystemBrokerManager;
import com.ars.ssm.configuration.Configuration;
import com.ars.ssm.configuration.GeneralConfig;
import com.ars.ssm.utils.ARSLogger;
import com.ars.ssm.utils.KeepAliveTask;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Abhijith Ravindran
 * The entry point of the StopSystemManagerModule.
 */
public class Application 
{
    /**
     * logger for logging general logs
     */
    private static ARSLogger logger = null;
    /**
     * logger for logging subscription logs
     */
    private static ARSLogger subscriptionLogger = null;
    /**
     * Handles the configuration file
     */
    private static Configuration config;
    /**
     * STATE accessible and changed across the module.
     * Value is set as STARTUP initially.
     */
    public static String STATE="STARTUP";
    /**
     * 
     */
    public static String STOP_SYSTEM_UNIQUE_ID;
    public static String LOCAL_URL = "127.0.0.1";
    public static String REMOTEL_URL;//"192.168.201.254";
    public static int REMOTE_PORT;
    private static GeneralConfig generalConfig = new GeneralConfig();
    
    public static String ssmVersion = "SSM VERSION 1.0.1.3";
	/**
	 * Main method
	 * @param args
	 */
    public static void main( String[] args )
    {
        try {
        	Application app = new Application();
        	app.startApp();
        }catch(Exception e) {
        	System.out.println("Error starting the application: "+e);
        }
    }

	/**
     * Constructor
	 * @throws IOException 
	 * @throws JsonProcessingException 
     */
    public Application() {
		config = Configuration.getInstance();
		REMOTEL_URL = config.generalConfig.getSSBURL();
		REMOTE_PORT = config.generalConfig.getSSBPort();
    	logger = getLogger();
    	logger.log(Level.INFO, "Stop System Manager Service Started...."+ssmVersion);
    	logger.startLogger(config.isLogBufferingDisabled());
    	generateStopSystemUniqueCode();
    }
    
    /**
     * The entry point of the application
     */
    private void startApp() {
    	logger.log(Level.INFO, "Starting the broker connection thread....");
    	logger.log(Level.INFO, "\nAPPLICATION STATE: "+Application.STATE);
		new Thread(()->{
			try {
				StopSystemBrokerManager.startBrokerConnection(REMOTEL_URL, REMOTE_PORT);//code to obtain connection using Mqtt5Client
			} catch (Exception  e) {
				logger.log(Level.SEVERE, "Error in broker connection thread:"+e.getMessage());
			}

		}).start();
	}
    /**
     * This method generates the stop system unique code by combining
     * subscriber owner code + subscriber type + serial number.
     */
    public static void generateStopSystemUniqueCode() {
		//String subscriberOwnerCode = "SUR";
		String subscriberOwnerCode=Configuration.getInstance().generalConfig.getSystemInfoSubscriberOwnerCode();
		//String subscriberType = "2";
		String subscriberType=String.valueOf(Configuration.getInstance().generalConfig.getSystemInfoSubscriberType());
		//String serialNumber = "ss34134134";
		String serialNumber=Configuration.getInstance().generalConfig.getSystemInfoSerialNumber();
		STOP_SYSTEM_UNIQUE_ID = subscriberOwnerCode.concat(subscriberType).concat(serialNumber);
		logger.log(Level.INFO, "\nSTOP_SYSTEM_UNIQUE_ID: "+Application.STOP_SYSTEM_UNIQUE_ID);
    }

    /**
     * Gets the application logger
     * @return ARSLogger
     */
    public static ARSLogger getLogger() {
	if (logger == null) {
	    logger = new ARSLogger(config.getLogFileName(),
	    		config.getLogFilePath(),
	    		config.getLogFileSize(),
	    		config.getLogFileNoOfBackup(), false);
	    logger.setLevel(getLevel());
//	    logger.setLevel(Level.ALL);
	    
	}
	return logger;
    }
    /**
     * Gets the subscription logger
     * @return
     */
    public static ARSLogger getSubscriptionLogger() {
    	if(subscriptionLogger == null) {
    		boolean isSubscriptionLogEnabled = true;
    		if(isSubscriptionLogEnabled) {
    			subscriptionLogger = new ARSLogger(config.ssmConfig.getSubscriptionLogFileName(),
    					config.getLogFilePath(), 
    					config.getLogFileSize(), 
    					config.getLogFileNoOfBackup(), 
    					false);
    			subscriptionLogger.setLevel(getLevel());	
//    			subscriptionLogger.setLevel(Level.ALL);	
    			subscriptionLogger.startLogger(config.isLogBufferingDisabled());
    		}
    	}
    	return subscriptionLogger;
    }
    /**
     * returns the log level specified in the config file. 
     * @return Level OFF, SEVERE, WARNING, INFO, ALL
     */
    private static Level getLevel() {
	switch (config.getLogLevel()) {
	case "OFF":
	    return Level.OFF;
	case "SEVERE":
	    return Level.SEVERE;
	case "WARNING":
	    return Level.WARNING;
	case "INFO":
	    return Level.INFO;
	default:
	    return Level.ALL;
	}
    }
}
