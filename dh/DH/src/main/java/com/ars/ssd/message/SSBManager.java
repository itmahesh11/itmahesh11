
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

package com.ars.ssd.message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ars.ssd.message.KeepAlive;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ars.ODCC.connection.ODMessage.Subscribe;
import com.ars.ODCC.connection.ODMessage.SubscriptionResponse.Status;
import com.ars.ssd.configuration.ARSLogger;
import com.ars.ssd.configuration.Configuration;
import com.ars.ssd.configuration.DHConfig;
import com.ars.ssd.configuration.GeneralConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import ars.protobuf.DHMessage.travelinfo;

public class SSBManager {

	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = null;
	/**
	 * Handles the configuration file
	 */
	private static Configuration config;
	/**
	 * add trip info to map
	 */
	private static HashMap<String, com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passTimeMapObj = null;
	/**
	 * DHConfig Class
	 */
	public static DHConfig dhConfig = null;
	private static Mqtt5BlockingClient clientObj;
	private static GeneralConfig generalConfig = null;

	private static final String DH_VERSION_NUMBER = " 1.0.3.0 ";

	private static HashMap<String, DisplayRegistrationMessage> displayRegMessageMapObj = null;
	private static ArrayList<DisplayRegistrationMessage> displayRegMessageList = null;

	private static DisplayRegistrationMessage displayregMessageObj = null;
	private static boolean odccConnected = false;
	private static int lastConnectionStatusTime = -1;
	private static ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args)  {

		try {
			Application();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.log(Level.INFO, " Application Not Started "+ LocalDateTime.now());

		}
	}
	
	/**
	 * Method to start the DH_Handler module
	 * 
	 * @throws InterruptedException
	 */
	public static void Application() throws InterruptedException {

		try {
			config = Configuration.getInstance();
			config.initialize();
			initializeGeneralConfig();

			// create logger
			logger = getLogger();
			logger.log(Level.INFO, " DH_VERSION_NUMBER : " + DH_VERSION_NUMBER + "  " + LocalDateTime.now());
			logger.log(Level.INFO, "Application Started.... " + LocalDateTime.now());
			logger.startLogger(config.isLogBufferingDisabled());

			/**
			 * initialize
			 */
			ListContainer.initializeMap();
			initializeMap();
			initializeListContainer();
			// subscribing a message through thread.
			Thread t1 = new Thread(() -> {
				try {
					
					
					String passageFilePath = getBaseFilePath(SSBManager.getConfig().getDhConfig().getPassageFileWithPath());
					File isPassageFileAvailable = new File(passageFilePath);
					
					String freeTextFilePath = getBaseFilePath(SSBManager.getConfig().getDhConfig().getFreeTextFileWithPath() );
					File isFreeTextFileAvailable = new File(freeTextFilePath);

					// checking passage file is available or not and load the values from passage
					// file add to map

					if (isPassageFileAvailable.exists()) {
						ListContainer.loadPassTimesToMap();
					}
					if (isFreeTextFileAvailable.exists()) {
						ListContainer.loadGeneralMessageToMap();
					}

					// code to get connection using Mqtt5Client
					getInternalBrokerConnection();

					if (clientObj != null) {

						// RDR trigger message published
						publishRDRRegistrationTrigger();
						// code to subscribe the topics from different module.
						subscribeToInternalBroker();
					}
				} catch (ConnectionFailedException | InterruptedException e) {
					logger.log(Level.ALL,
							" Thread Not Able To Start Application .... " + e.getMessage() + LocalDateTime.now());
				}
			});
			t1.start();

		} catch (Exception e1) {

			logger.log(Level.ALL, "Not Able To Start Application .... " + e1.getMessage() + LocalDateTime.now());
		}
	}

	public static String getBaseFilePath( String subFolderPath) {
		
		String basePath  = "";
		try {
			basePath  = generalConfig.getBasePath()+"\\"+subFolderPath;
			
		}catch(Exception e) {
			if( logger != null) { 
				logger.log(Level.ALL, " Get The BaseFile Path Failed .... " + e.getMessage() + LocalDateTime.now());

			}
		}
		if( logger != null) {
			logger.log(Level.ALL, " Configured  Path .... " + basePath);

		}
		
		return basePath;
	}
	
	/**
	 * RDR Trigger message pulished
	 * 
	 * @throws InterruptedException
	 */
	public static boolean publishRDRRegistrationTrigger() throws InterruptedException {

		boolean containMessage = false;
		try {

			String topicForRDRTrigger = config.getDhConfig().getTopicForTriggerRDRMessage("topic");
			int rdrTriggerQos = config.getDhConfig().getRDRTriggerQos();
			
			Integer currentTime = getCurrentTimeAsInteger();
			DHTriggerMessage dhTriggerMessage = new DHTriggerMessage();
			
			dhTriggerMessage.setDhStart(true);
			dhTriggerMessage.setCurrentTime(currentTime);
			Gson gson = new Gson();
		    String jsonString = gson.toJson(dhTriggerMessage);
		    
			clientObj.publishWith().topic(topicForRDRTrigger).qos(getQos(rdrTriggerQos))
					.payload(jsonString.getBytes()).send();
			logger.log(Level.ALL, " RDR Trigger Message Published To The Topic --> " + topicForRDRTrigger
					+ " RDR Registration Trigger Message --> " + jsonString.replace("\n", " ") + " " + LocalDateTime.now());

		} catch (Exception e2) {
			logger.log(Level.ALL, " RDR Trigger Message Publish Failed ...." + e2.getMessage() + LocalDateTime.now());
		}
		return containMessage;
	}

	public static void prepareConfiguration() {
		try {
			config = Configuration.getInstance();
			config.initialize();
			initializeGeneralConfig();

			// create logger
			logger = getLogger();

			ListContainer.initializeMap();
			initializeListContainer();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initializeMap() {

		try {
			displayRegMessageMapObj = new HashMap<>();// create map

		} catch (Exception e) {
			logger.log(Level.ALL,
					" Display Registration Message Map Not Initialized  .... " + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * Keep Alive Message
	 * 
	 * @param message
	 */
	public static void publishKeepAliveMessage(String message) {

		if (clientObj != null && clientObj.getState().isConnected()) {
			logger.log(Level.SEVERE, "Sending KeepAlive message...." + message + " " + LocalDateTime.now());
			clientObj.publishWith().topic(generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE)
					.payload((message).getBytes()).send();
		}
	}

	/**
	 * getting Configuration class object
	 * 
	 * @return
	 */
	public static Configuration getConfig() {
		return config;
	}

	/**
	 * Initialize Map GeneralConfigClass
	 */
	public static void initializeGeneralConfig() {

		try {
			generalConfig = new GeneralConfig();

		} catch (Exception e) {
			logger.log(Level.ALL, "GeneralConfig Not Initialize  ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * Initiaize ListContainer
	 */
	public static void initializeListContainer() {

		try {
			new ListContainer();
		} catch (Exception e) {
			logger.log(Level.ALL, "Map Not Initialize  ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	public static GeneralConfig getGeneralConfig() {

		return generalConfig;
	}

	/**
	 * Get Map Object
	 * 
	 * @return
	 */
	public static HashMap<String, com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> getMapObjeHashMap() {
		return passTimeMapObj;
	}

	/**
	 * Get SSB Manager Class
	 * 
	 * @return
	 */
	public SSBManager getSsbManager() {
		return this;
	}

	/**
	 * Gets the application logger
	 * 
	 * @return ARSLogger
	 */
	public static ARSLogger getLogger() {
		if (logger == null) {

			String logFilePath = SSBManager.getBaseFilePath(config.getLogFilePath());
			logger = new ARSLogger(config.getLogFileName(), logFilePath, config.getLogFileSize(),
					config.getLogFileNoOfBackup(), false);
			logger.setLevel(getLevel());
		}
		return logger;
	}

	/**
	 * return
	 * 
	 * @return logger
	 */
	public static ARSLogger getLoggerObject() {
		return logger;
	}

	/**
	 * returns the log level specified in the config file.
	 * 
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

	/**
	 * Subscribe InternalBroker
	 * 
	 * @throws InterruptedException
	 */
	private static void subscribeToInternalBroker() throws InterruptedException {

		try {

			// Listens the message received from the Publisher
			Mqtt5BlockingClient.Mqtt5Publishes publish = clientObj.publishes(MqttGlobalPublishFilter.ALL);

			String containerMessageTopic = config.getDhConfig().getOdccContainerTopic("topic");
			String displayRegistrationMsgTopic = config.getDhConfig().getRendererRegistrationMessageTopic("topic");
			String odccConnectionStatusMsgTopic = config.getDhConfig().getOdccConnectionStatus("topic");
			String subscribeSSMMsgTopic = config.getDhConfig().getTopicForSubscribeSSMMessage("topic");

			int containerMessageQos = config.getDhConfig().getOdccContainerTriggerQos();
			int displayRegistrationMsgQos = config.getDhConfig().getRendererRegistrationMessageTriggerQos();
			int odccConnectionStatusMsgQos = config.getDhConfig().getOdccConnectionStatusTriggerQos();
			int subscribeSSMMsgQos = config.getDhConfig().getSsmSubscribeTriggerQos();

			clientObj.subscribeWith().addSubscription().topicFilter(containerMessageTopic)
					.qos(getQos(containerMessageQos)).applySubscription().addSubscription()
					.topicFilter(displayRegistrationMsgTopic).qos(getQos(displayRegistrationMsgQos)).applySubscription()
					.addSubscription().topicFilter(odccConnectionStatusMsgTopic).qos(getQos(odccConnectionStatusMsgQos))
					.applySubscription().addSubscription().topicFilter(subscribeSSMMsgTopic)
					.qos(getQos(subscribeSSMMsgQos)).applySubscription().send();

			logger.log(Level.INFO, "Subscribed To All Configured Topics..." + LocalDateTime.now());
			onReceive(publish);

		} catch (Exception e2) {
			logger.log(Level.ALL, " Message Subscription Failed ...." + e2.getMessage() + LocalDateTime.now());
		}
	}

	public static MqttQos getQos(int qos) {
		MqttQos mqttQos;
		if (qos == 0) {
			mqttQos = MqttQos.AT_MOST_ONCE;
		}
		if (qos == 1) {
			mqttQos = MqttQos.AT_LEAST_ONCE;
		} else {
			mqttQos = MqttQos.EXACTLY_ONCE;
		}
		return mqttQos;
	}

	/**
	 * Message Handling
	 * 
	 * @param publish
	 * @throws ConnectionFailedException
	 */
	@SuppressWarnings("static-access")
	public static void onReceive(Mqtt5BlockingClient.Mqtt5Publishes publish) throws ConnectionFailedException {

		try {

			Optional<Mqtt5Publish> receivedMessage;
			// Listens the message received from the Publisher
			int nCounter = 0;
			Integer nAliveSendTime = getCurrentTimeAsInteger();
			publishKeepAliveMsg(nAliveSendTime);

			Integer tripRemovalTime = nAliveSendTime;
			Integer lastTripSendTime = nAliveSendTime;
			Integer updateTripTime = nAliveSendTime;

			while (true) {

				receivedMessage = publish.receive(500, TimeUnit.MILLISECONDS);

				if (receivedMessage.isPresent()) {

					String topic = receivedMessage.get().getTopic().toString();
					logger.log(Level.INFO, " Received Topic... " + topic);

					if (topic.equals(config.getDhConfig().getOdccContainerTopic("topic"))) {

						String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
						logger.log(Level.ALL, " Received Travel Informatiom From SSB ......." + LocalDateTime.now());
						logger.log(Level.ALL, " Subscribed Topic --> " + topic + "  MSG : " + payload.replace("\n", " ")
								+ " " + LocalDateTime.now());
						ListContainer.getListConatiner(payload);

					} else if (topic.equals(config.getDhConfig().getRendererRegistrationMessageTopic("topic"))) {

						String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
						logger.log(Level.ALL,
								" Received Display Registration Message From RDR ....... " + LocalDateTime.now());
						logger.log(Level.ALL, " Subscribed Topic --> " + topic + "  MSG : " + payload.replace("\n", " ")
								+ " " + LocalDateTime.now());
						readDisplayRegistrationMessage(payload);

					} else if (topic.equals(config.getDhConfig().getOdccConnectionStatus("topic"))) {

						String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
						logger.log(Level.ALL,
								"Received Odcc Connection Status Message From ODCC ......." + LocalDateTime.now());
						logger.log(Level.ALL, " Subscribed Topic --> " + topic + "  MSG : " + payload.replace("\n", " ")
								+ " " + LocalDateTime.now());
						readOdccConnectionStatusMessage(payload);

					} else if (topic.equals(config.getDhConfig().getTopicForSubscribeSSMMessage("topic"))) {

						String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
						logger.log(Level.ALL, "Received Request From SSM  ......." + LocalDateTime.now());
						logger.log(Level.ALL, " Subscribed Topic --> " + topic + "  MSG : " + payload.replace("\n", " ")
								+ " " + LocalDateTime.now());
						readSSMRequestMessage();
					}
					nCounter = 0;

				} else {

					nCounter++;
					if (nCounter == 20) {
						logger.log(Level.ALL, "  Listening to topics SSB  " + LocalDateTime.now());

						nCounter = 0;
					}

				}
				Integer nCurrentTime = getCurrentTimeAsInteger();
				if (nCurrentTime - nAliveSendTime > generalConfig.getIntervalInSec()) {

					nAliveSendTime = nCurrentTime;
					publishKeepAliveMsg(nAliveSendTime);

				}
				if (nCurrentTime - tripRemovalTime > config.cleanupSettings("removalIntervalInSec")) {

					tripRemovalTime = nCurrentTime;
					ListContainer.removeOldTrips(tripRemovalTime);
					ListContainer.removeOldFreeText(tripRemovalTime);
					ListContainer.systemStatusMessageForTripAvailable();// no trip available

					odccConnected = getOdccConnectionStatus();
					lastConnectionStatusTime = getOdccLastConnectionStatusTime();

					if (odccConnected == false) {

						int vrijetekstTimeOut = generalConfig.getVrijetekstTimeOutInMin();

						if ((nCurrentTime - lastConnectionStatusTime) > (vrijetekstTimeOut * 60)) {

							ListContainer.removeAllFreeTextFromMemory();

						}
					}
				}

				if(nCurrentTime - updateTripTime >  config.updateFileIntervalInSec("updateFileIntervalInSec")	) {
					updateTripTime = nCurrentTime ;
					
					boolean fileMode = config.getFileMode();
					if (fileMode == true) {
						ListContainer.dumpPassageTimesMsgToPassageFile( );

					}
				}
				
				if (nCurrentTime - lastTripSendTime > config.getPublishTripIntervalInSec()) {

					lastTripSendTime = nCurrentTime;
					publishTripToAllRDR();
				}
			}

		} catch (Exception e) {
			logger.log(Level.ALL, " Exception In Message Handling  ...." + e.getMessage() + LocalDateTime.now());
		}

	}

	public static Integer getCurrentTimeAsInteger() {
		Date now = new Date();
		Long longTime = Long.valueOf(now.getTime() / 1000);
		return longTime.intValue();

	}

	public static void publishKeepAliveMsg(Integer time) {

		if (clientObj != null && clientObj.getState().isConnected()) {
			KeepAlive keepAliveMessage = new KeepAlive();
			keepAliveMessage.setModuleId(config.getDhConfig().getDHModuleId());
			keepAliveMessage.setUpdateTime(time);
			Gson gson = new Gson();
			String jsonString = gson.toJson(keepAliveMessage);
			try {
				logger.log(Level.ALL, " Sending KeepAlive message.... " + jsonString + LocalDateTime.now());
				clientObj.publishWith().topic(generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE)
						.payload((jsonString).getBytes()).send();
			} catch (Exception e) {

				logger.log(Level.ALL, " Exception occured while sending KeepAlive message.... " + e.getMessage() + " "
						+ LocalDateTime.now());
			}
		} else {
			logger.log(Level.ALL,
					" Keep alive not sent Client State is  " + clientObj.getState() + " " + LocalDateTime.now());
		}
	}

	/**
	 * read ssm request message
	 */

	public static void readSSMRequestMessage() {

		try {
			ListContainer.prepareTravelInfoResponseSSM();// SSM
		} catch (Exception e) {
			logger.log(Level.ALL, " Reading SSM Message Failed ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	public static void publishSystemStatusMessage(String tripMessage) {

		try {

			if (tripMessage.length() > 0) {

				int systemStatusMessageTriggerQos = config.getDhConfig().getSystemStatusMessageTriggerQos();
				String topicForPublishingSystemStatusMessage = config.getDhConfig()
						.getTopicForPublishSystemStatusMessage("topic");
				clientObj.publishWith().topic(topicForPublishingSystemStatusMessage)
						.qos(getQos(systemStatusMessageTriggerQos)).payload(tripMessage.getBytes()).send();

				logger.log(Level.ALL,
						" System Status Message Published To The Topic  --> " + topicForPublishingSystemStatusMessage
								+ " System Status Message -->" + tripMessage.replace("\n", " ") + " "
								+ LocalDateTime.now());
			}
		} catch (Exception e) {
			logger.log(Level.ALL,
					" Publish System Status Message To RDR Failed  .... " + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * trip message publish to SSM
	 */
	public static void tripMessagePublishToSSM(String tripMessage) {
		try {

			if (tripMessage.length() > 0) {

				int ssmPublishTriggerQos = config.getDhConfig().getSsmPublishTriggerQos();
				String topicForPublishingTripToSSM = config.getDhConfig().getTopicForPublishTripToSSM("topic");
				clientObj.publishWith().topic(topicForPublishingTripToSSM).qos(getQos(ssmPublishTriggerQos))
						.payload(tripMessage.getBytes()).send();
				logger.log(Level.ALL, " Trip Published to the Topic  --> " + topicForPublishingTripToSSM
						+ " Trip Message --> " + tripMessage.replace("\n", " ") + " " + LocalDateTime.now());
			}

		} catch (Exception e) {
			logger.log(Level.ALL,
					" Publish Trip And Freetext To SSM Failed  ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * read Odcc connection status message
	 */

	public static boolean readOdccConnectionStatusMessage(String payload) {
		try {
			JsonNode node;
			try {
				node = mapper.readTree(payload);
				lastConnectionStatusTime = node.get("statechangeFrom").asInt();
				odccConnected = node.get("od_connected").asBoolean();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ALL,
						" Reading Odcc Connection Status Message Failed ...." + e.getMessage() + LocalDateTime.now());
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Invalid connection response from SSB: " + e.getMessage());
		}
		return odccConnected;

	}

	public static boolean getOdccConnectionStatus() {
		return odccConnected;
	}

	public static int getOdccLastConnectionStatusTime() {
		return lastConnectionStatusTime;

	}

	/**
	 * read display registration message
	 * 
	 * @param diplayRegMessage
	 */
	public static DisplayRegistrationMessage readDisplayRegistrationMessage(String diplayRegMessage) {

		displayRegMessageList = new ArrayList<DisplayRegistrationMessage>();// new

		try {
			if (displayRegMessageMapObj == null) {
				displayRegMessageMapObj = new HashMap<String, DisplayRegistrationMessage>();
			}

			if (diplayRegMessage.length() > 0) {

				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(diplayRegMessage);

				ArrayList<String> stopCodeList = null;
				if (jsonObject != null) {

					stopCodeList = new ArrayList<>();
					String diplayName = (String) jsonObject.get("DisplayName");

					JSONArray jsonArray = (JSONArray) jsonObject.get("QuayCodes");
					if (jsonArray != null) {

						// Iterating the contents of the array
						Iterator<String> iterator = jsonArray.iterator();
						while (iterator.hasNext()) {
							String stopCodeObj = iterator.next();
							stopCodeList.add(stopCodeObj);
						}
					}

					long toontiJDTimeInMin = (long) jsonObject.get("TOONTIJD_InMin");
					boolean sendFreeTextFlag = (boolean) jsonObject.get("SendFreeText");
					String tripSelectionTime = (String) jsonObject.get("TimeFieldToBeUsedForTripSelection");
					String topicPublicName = (String) jsonObject.get("TopicForPublicName");
					String topicTravelInfo = (String) jsonObject.get("TopicForTravelInfo");

					displayregMessageObj = new DisplayRegistrationMessage(diplayName, stopCodeList, toontiJDTimeInMin,
							sendFreeTextFlag, tripSelectionTime, topicPublicName, topicTravelInfo);

					displayRegMessageMapObj.put(diplayName, displayregMessageObj);
					displayRegMessageList.add(displayregMessageObj);// new

					// publishing publicname in the configurable topic
					ListContainer.getPublicNameDetails(displayregMessageObj);
				}
			}

		} catch (Exception e) {
			logger.log(Level.ALL,
					" Reading DisplayRegistrationMessage  Failed ...." + e.getMessage() + LocalDateTime.now());
		}
		return displayregMessageObj;

	}

	public static void publishTripToAllRDR() {
		try {

			if (displayRegMessageMapObj != null && displayRegMessageMapObj.size() > 0) {

				for (Entry<String, DisplayRegistrationMessage> displayRegistrationMessageEntry : displayRegMessageMapObj
						.entrySet()) {

					logger.log(Level.ALL, " RDR Display --> "
							+ displayRegistrationMessageEntry.getValue().getDisplayName() + " " + LocalDateTime.now());
					publishTripToRDR(displayRegistrationMessageEntry.getValue());
				}
			}

		} catch (Exception e) {
			logger.log(Level.ALL, " Publish Trip To All RDR Failed ...." + e.getMessage() + LocalDateTime.now());

		}
	}

	public static void sendPublicNameMessageToRDR(com.ars.ODCC.connection.ODMessage.PublicName publicNameObj) {
		try {

			if (publicNameObj.getStopCodeList().size() > 0 || publicNameObj.getPublicNamePlaceList().size() > 0
					|| publicNameObj.getPublicNameQuayList().size() > 0
					|| publicNameObj.getPublicNameStopPlaceList().size() > 0) {

				if (displayRegMessageMapObj != null && displayRegMessageMapObj.size() > 0) {

					for (Entry<String, DisplayRegistrationMessage> displayRegistrationMessageEntry : displayRegMessageMapObj
							.entrySet()) {

						logger.log(Level.ALL,
								" DisplayName  :  " + displayRegistrationMessageEntry.getValue().getDisplayName() + " "
										+ LocalDateTime.now());
						ListContainer.getPublicNameDetails(displayRegistrationMessageEntry.getValue());
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.ALL, " Publish Public Name Failed ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	public static void publishTripToRDR(DisplayRegistrationMessage diplayRegMessageObj) {
		try {

			ListContainer.prepareTripOnTimeWindow(diplayRegMessageObj);

		} catch (Exception e) {
			logger.log(Level.ALL, "prepare one hour trip info failed  ...." + e.getMessage() + LocalDateTime.now());

		}
	}

	/**
	 * public name publish to configurable topic
	 * 
	 * @param publicName
	 */
	public static void publishPublicName(String publicName, DisplayRegistrationMessage displayRegMessage) {
		try {

			if (publicName.length() > 0) {

				String topicForPublicName = displayRegMessage.getTopicForPublicName();
				clientObj.publishWith().topic(topicForPublicName).qos(MqttQos.AT_LEAST_ONCE)
						.payload(publicName.getBytes()).send();
				logger.log(Level.ALL, " PublicName Published to the Topic --> " + topicForPublicName
						+ " PublicNames Message --> " + publicName.replace("\n", " ") + " " + LocalDateTime.now());
			}

		} catch (Exception e) {
			logger.log(Level.ALL, " Publishing PublicName Failed  ...." + e.getMessage() + LocalDateTime.now());

		}
	}

	/**
	 * 
	 * @param publish trip info
	 */

	public static void publishTripMessage(String tripMessage, DisplayRegistrationMessage displayRegMessageObj) {
		try {

			if (tripMessage.length() > 0) {

				String topicForTrip = displayRegMessageObj.getTopicForTravelInfo();
				clientObj.publishWith().topic(topicForTrip).qos(MqttQos.AT_LEAST_ONCE).payload(tripMessage.getBytes())
						.send();
				logger.log(Level.ALL,
						" Trip Info Published To The Topic --> " + topicForTrip + " Published Message To RDR  --> "
								+ tripMessage.replace("\n", " ") + " " + LocalDateTime.now());
			}

		} catch (Exception e) {
			logger.log(Level.ALL, " Publishing a PublicName Failed  ...." + e.getMessage() + LocalDateTime.now());

		}
	}

	/**
	 * Get InternalBroker Connection
	 * 
	 * @param brokerUrl
	 * @throws ConnectionFailedException
	 */
	public static boolean getInternalBrokerConnection() throws ConnectionFailedException {
		boolean isConnected = false;

		try {
			int clientId = config.getDhConfig().getDHModuleId();
			clientObj = Mqtt5Client.builder().identifier(String.valueOf(clientId)).serverHost(generalConfig.getSSBURL())
					.serverPort(generalConfig.getSSBPort()).automaticReconnect()
					.initialDelay(generalConfig.getSSBReconnectInitialDelay(), TimeUnit.SECONDS)
					.maxDelay(generalConfig.getSSBReconnectMaxDelay(), TimeUnit.SECONDS).applyAutomaticReconnect()
					.buildBlocking();

			logger.log(Level.INFO, "InternalBroker Connection Established......." + LocalDateTime.now());

		} catch (ConnectionFailedException e3) {
			logger.log(Level.ALL, " InternalBroker Connection Failed ...." + e3.getMessage() + LocalDateTime.now());
		}

		clientObj.connectWith().cleanStart(false).sessionExpiryInterval(10).send();
		return isConnected;

	}

	public static Mqtt5BlockingClient getClientObj() {
		return clientObj;
	}

}
