package com.ars.ODCC.Connection;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import java.text.ParseException;
import java.util.logging.Level;

import com.ars.ODCC.Utils.ARSLogger;
import com.ars.ODCC.Configuration.Configuration;
import com.ars.ODCC.Configuration.GeneralConfig;

/**
 * Main Class to start the ODCC module in OPENDRIS.
 * @author Dharanikumar
 * 
 */
public class Application {
	/**
	 * StopSystemBrokerService for accessing internal broker / SSB
	 */
	private static  StopSystemBrokerService stopSystemBroker;
	/**
	 * OpenDrisBrokerService for accessing central/OpenDris broker
	 */
	private static  OpenDrisBrokerService openDrisBroker;
	/**
	 * GeneralConfig to access settings.general
	 */
	private static GeneralConfig generalConfig;
	/**
	 * Configuration to access settings.json file
	 */
	private static Configuration config;
	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = null;
	/**
	 * version variable
	 */
	private static String version = "ODCC Release Version 1.0.3.2";
	
	
	/**
	 * Main method to start the ODCC module.
	 * @param args
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws InterruptedException, ParseException {
		new Application();
		startApplication();
	}
	
	/**
	 * Constructor for Application
	 */
	public Application() {

		config = Configuration.getInstance();
		logger = getLogger();
		logger.log(Level.SEVERE, "ODCC Module started with version "+version);
		logger.log(Level.SEVERE, "Application Started....");
		logger.startLogger(config.isLogBufferingDisabled());
		stopSystemBroker = new StopSystemBrokerService();
		openDrisBroker = new OpenDrisBrokerService();
		generalConfig = new GeneralConfig();
	}
	
	/**
	 * Method to start the ODCC module 
	 * @throws InterruptedException 
	 */
	public static  void startApplication() throws InterruptedException {

		//Thread that handles the functions related to stop system broker.
		new Thread(()->{ 
			try {
				stopSystemBroker.obtainInternalBrokerConnection();
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Unable to connect to stop system broker....");
				Thread.currentThread().interrupt();
			}
		}).start();
		
		Thread.sleep(2000);
		
		//Thread that handles the functions related to open dris broker.
		new Thread(()->{
			openDrisBroker.handleConnectionToCB();
		}).start();
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
		}
		return logger;
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
