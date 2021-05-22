package com.ars.ODCC.Connection;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.ars.ODCC.message.ODConnectionStatus;
import com.ars.ODCC.message.ODMessage.ClientId;
import com.ars.ODCC.message.ODMessage.Container;
import com.ars.ODCC.message.ODMessage.InfoRequest;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse;
import com.ars.ODCC.message.ODMessage.StatusOverview;
import com.ars.ODCC.message.ODMessage.Subscribe;
import com.ars.ODCC.message.ODMessage.SubscriptionResponse;
import com.ars.ODCC.message.ODMessage.SystemInfo;
import com.ars.ODCC.message.ODMessage.SystemStatus;
import com.ars.ODCC.message.ODMessage.TravelInfo;
import com.ars.ODCC.message.ODMessage.Unsubscribe;
import com.ars.ODCC.message.ODMessageHandler;
import com.ars.ODCC.Configuration.Configuration;
import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.Utils.ARSLogger;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.internal.util.AsyncRuntimeException;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class OpenDrisBrokerService implements IOpenDrisBrokerService {
	/**
	 * StopSystemBrokerService for accessing internal broker / SSB
	 */
	private static StopSystemBrokerService stopSystemManager = new StopSystemBrokerService();
	/**
	 * Mqtt5BlockingClient for accessing internal broker / SSB client
	 */
	private static Mqtt5BlockingClient client = null;
	/**
	 * ODMessageHandler for accessing protobuf/json message
	 */
	private static ODMessageHandler oDMessageHandler = new ODMessageHandler();
	/**
	 * GeneralConfig to access settings.general
	 */
	private static GeneralConfig generalConfig = new GeneralConfig();
	/**
	 * Configuration to access settings.json file
	 */
	private static Configuration config = Configuration.getInstance();
	/**
	 * subscribeResponseTopic from settings.json file
	 */
	private static String subscribeResponseTopic = getTopic(config.odccConfig.getOpenDrisSubscriptionResponseTopic());
	/**
	 * traveInfoTopic from settings.json file
	 */
	private static String traveInformationTopic = getTopic(config.odccConfig.getOpenDrisTravelInformationTopic());
	/**
	 * subscribeTopic from settings.json file
	 */
	private static String subscribeTopic = getTopic(config.odccConfig.getOpenDrisSubscribeTopic());
	/**
	 * unSubscribeTopic from settings.json file
	 */
	private static String unSubscribeTopic = getTopic(config.odccConfig.getOpenDrisUnsubscribeTopic());

	private static String infoRequestTopic = getTopic(config.odccConfig.getOpenDrisInfoRequestTopic());
	private static String systemInfoTopic = getTopic(config.odccConfig.getOpenDrisSystemInfoTopic());
	private static String statusOverviewTopic = getTopic(config.odccConfig.getOpenDrisStatusOverviewTopic());
	private static String systemStatusTopic = getTopic(config.odccConfig.getOpenDrisSystemStatusTopic());
	private static String screenContentTopic = getTopic(config.odccConfig.getOpenDrisScreenContentTopic());
	private static String travelInfoTopic = getTopic(config.odccConfig.getOpenDrisTravelInfoTopic());
	private static String distributionSystemLastWillMessage = getDistributionSystemLastWillTopic(config.odccConfig.getOpenDrisDistributionSystemLastWillTopic());

	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = Application.getLogger();
	/**
	 * no of retries of central broker url
	 */
	private  Integer noOfRetries = 0;
	/**
	 * maximum no of retries for central broker url
	 */
	int maxNumberOfAttempts = generalConfig.getCentralBrokerMQTTMaxRandConnAttempt();
	/**
	 * minimum no of retries for central broker url
	 */
	int minNumberOfAttempts = generalConfig.getCentralBrokerMQTTMinRandConnAttempt();
	/**
	 * random no of attempts of retries to be done.
	 */
	Random random = new Random();
	int randomNumberOfAttempts = random.nextInt((maxNumberOfAttempts - minNumberOfAttempts)+1) + minNumberOfAttempts;
	private String primaryURL;
	int primaryPort ;
	boolean infinity = false;
	boolean alreadySent = false;
	boolean isFirstAttempt = true;




	/**
	 * Method to transfer message from central broker to stop system broker.
	 * 
	 * @throws InterruptedException
	 * @throws InvalidProtocolBufferException
	 */
	@Override
	public void subscribeToExternalBroker() throws InterruptedException, InvalidProtocolBufferException {

		Optional<Mqtt5Publish> receivedMessage;

		//Listens the message received from the Publisher
		Mqtt5BlockingClient.Mqtt5Publishes publish = client.publishes(MqttGlobalPublishFilter.ALL);

		client.subscribeWith()
		.addSubscription().topicFilter(subscribeResponseTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSubscriptionResponseQos())).
		applySubscription()
		.addSubscription().topicFilter(traveInformationTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisTravelInformationQos())).
		applySubscription()
		.addSubscription().topicFilter(infoRequestTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisInfoRequestQos())).
		applySubscription()
		.addSubscription().topicFilter(distributionSystemLastWillMessage).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisDistributionSystemLastWillQos())).
		applySubscription().send();


		logger.log(Level.INFO, "Listening to topics "+subscribeResponseTopic +" "+traveInformationTopic+" "+infoRequestTopic+" "+distributionSystemLastWillMessage );
		int nCounter = 0;

		String payload = null;
		while(true) {

			receivedMessage = publish.receive(1000,TimeUnit.MILLISECONDS);
			if(receivedMessage.isPresent()) {
				String topic = receivedMessage.get().getTopic().toString();
				logger.log(Level.SEVERE, "Received  topic ->"+topic );

				if(topic.equalsIgnoreCase(subscribeResponseTopic))  {
					//Converting protobuf to json
					SubscriptionResponse subscriptionResponse = SubscriptionResponse.parseFrom(receivedMessage.get().getPayloadAsBytes());
					payload = oDMessageHandler.parseSubscriptionRequestProtoBufMessage(subscriptionResponse);

					logger.log(Level.SEVERE, "Received the topic -> "+subscribeResponseTopic +" "+subscriptionResponse );
					stopSystemManager.publishSubscribeResponseMessage(payload);
				}
				else if(topic.equalsIgnoreCase(traveInformationTopic)){
					//Converting protobuf to json
					Container travelInfo = Container.parseFrom(receivedMessage.get().getPayloadAsBytes());
					payload = oDMessageHandler.parseTravelInformationProtoBufMessage(travelInfo);

					logger.log(Level.SEVERE, "Received the topic -> "+traveInformationTopic +" "+travelInfo);
					stopSystemManager.publishTravelInfoMessage(payload);
				}
				else if(topic.equalsIgnoreCase(infoRequestTopic)){
					//Converting protobuf to json
					InfoRequest infoRequest = InfoRequest.parseFrom(receivedMessage.get().getPayloadAsBytes());
					payload = oDMessageHandler.parseInfoRequestProtoBufMessage(infoRequest);
					logger.log(Level.SEVERE, "Received the topic -> "+infoRequestTopic +" "+infoRequest);
					stopSystemManager.publishInfoRequestMessage(payload);
				}
				else {
					String distributionTopic = distributionSystemLastWillMessage.replaceAll("#","");
					if(topic.contains(distributionTopic)) {
						logger.log(Level.SEVERE, "Received the topic -> "+distributionSystemLastWillMessage +" Distibution System is Offline");
						stopSystemManager.sendSubsriptionTrigger();
						sendConnectionStatusMessage(true);
					}
				}

				nCounter = 0;
			}
			else
			{
				nCounter++;
				if( nCounter == 20)
				{
					logger.log(Level.INFO, "Listening to topics CB" );
					nCounter = 0;
				}
			}
		}
	}



	/**
	 * Method to obtain connection from central broker.
	 * 
	 * @param brokerUrl
	 * @param port
	 * @throws AsyncRuntimeException
	 * @throws InvalidProtocolBufferException
	 * @throws InterruptedException
	 */
	@Override
	public void obtainExternalBrokerConnection(String brokerUrl, int port) throws AsyncRuntimeException, InvalidProtocolBufferException, InterruptedException {
		String clientId = generalConfig.getSystemInfoSubscriberOwnerCode()+"_"+generalConfig.getSystemInfoSubscriberType()+"_"+generalConfig.getSystemInfoSerialNumber();
		client =  Mqtt5Client.builder()
				.identifier(clientId)
				.serverHost(brokerUrl)
				.serverPort(port)
				.buildBlocking();

		try {
			client.connectWith()
			.simpleAuth() .username("username") .password("password".getBytes())
			.applySimpleAuth()
			.willPublish()
			.topic(unSubscribeTopic)
			.qos(MqttQos.AT_LEAST_ONCE)
			.payload(getLastWillMessage()).applyWillPublish()
			.keepAlive(60)
			.cleanStart(true)
			.send();


			logger.log(Level.SEVERE, "Connected to external broker..."+brokerUrl+" ");
			setRetryCount(brokerUrl);
			stopSystemManager.sendSubsriptionTrigger();
			sendConnectionStatusMessage(true);
			alreadySent = false;

			logger.log(Level.INFO, "subscribeToExternalBroker...start");
			subscribeToExternalBroker();
			logger.log(Level.INFO, "subscribeToExternalBroker...finish");
		}

		catch (AsyncRuntimeException e) {
			logger.log(Level.SEVERE, "Not able to connect external broker...Disconnected from external broker "+brokerUrl+" "+e.getMessage()+" ");
			if(!alreadySent) {
				sendConnectionStatusMessage(false);
				alreadySent = true;
			}

			throw e;
		}
	}


	/**
	 * Method to set retry count of central broker.
	 */
	@Override
	public void setRetryCount(String brokerURL) {
		noOfRetries = 0;
		infinity = false;
		primaryURL = brokerURL;
		isFirstAttempt = false;
	}

	/**
	 * Method to decide the url that broker needs to connect.
	 */
	@Override
	public  void handleConnectionToCB() {
		logger.log(Level.SEVERE, "Started Connecting to central broker connection....");
		String brokerURL1 = generalConfig.getCentralBrokerURL1();
		String brokerURL2 = generalConfig.getCentralBrokerURL2();
		int cbPort1 = generalConfig.getCentralBrokerPort1();
		int cbPort2 = generalConfig.getCentralBrokerPort2();

		long timeToWait = 0;

		logger.log(Level.INFO, "Random number of attempts "+randomNumberOfAttempts);

		long randomMilliSeconds = (long)(Math.random() * 1000);
		int maxBackOff = generalConfig.getCentralBrokerMqttMaxBackOffTime();

		primaryURL = brokerURL1;
		primaryPort= cbPort1;
		while(true)
		{
			try {
				logger.log(Level.SEVERE, "Connecting to  CB "+primaryURL+" .... Try....."+noOfRetries+" ");
				obtainExternalBrokerConnection(primaryURL,primaryPort);   //code to obtain  connection using Mqtt5Client
			}catch (InvalidProtocolBufferException | InterruptedException | AsyncRuntimeException e) {

				timeToWait = Math.min((long) ((Math.pow(2, noOfRetries)*1000) + randomMilliSeconds), maxBackOff);
				logger.log(Level.SEVERE, "Lost Connection to CB....Will retry after "+timeToWait+" milli seconds");
				try {
					Thread.sleep(timeToWait);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					Thread.currentThread().interrupt();
				}
				noOfRetries++;
				if(!infinity && noOfRetries == (randomNumberOfAttempts)) {
					noOfRetries = 0;
					if(primaryURL.equalsIgnoreCase(brokerURL1)) {
						primaryURL = brokerURL2;
						primaryPort = cbPort2;
						infinity = true;
					}else {
						primaryURL = brokerURL1;
						primaryPort = cbPort1;
						infinity = true;
					}
				}
			}
		}
	}

	/**
	 * Method to send connection status to internal broker
	 * @param status
	 */
	private void sendConnectionStatusMessage(Boolean status) {

		logger.log(Level.SEVERE, "Sending Connection status message "+status);
		ODConnectionStatus odMessage = new ODConnectionStatus();
		odMessage. setOdConnected(status);
		odMessage. setStatechangeFrom(getCurrentTimeAsInteger());
		Gson gson = new Gson();
		String jsonString = gson.toJson(odMessage);
		stopSystemManager.publishConnectionStatusMessage(jsonString);
	}

	/**
	 * Method to send subscribe message to central broker.
	 * @param subscribeMessage
	 */
	@Override
	public void publishSubscribeMessage(Subscribe subscribeMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.INFO, "Received message for the topic -> "+subscribeTopic+" -> "+subscribeMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+subscribeTopic);
			client.publishWith().topic(subscribeTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSubscribeQos())).payload(subscribeMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscribeTopic+" but unable to send to central broker.");
		}
	}

	/**
	 * Method to send unsubscribe message to central broker.
	 * @param unsubscribeMessage
	 */
	@Override
	public void publishUnsubscribeMessage(Unsubscribe unsubscribeMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.INFO, "Received message for the topic -> "+unSubscribeTopic+" -> "+unsubscribeMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+unSubscribeTopic +" ");
			client.publishWith().topic(unSubscribeTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisUnsubscribeQos())).payload(unsubscribeMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+unSubscribeTopic+" but unable to send to central broker.");
		}
	}

	/**
	 * Method to send systemInfoMessage to cb.
	 * @param systemInfoMessage
	 */
	@Override
	public void publishSystemInfoMessage(SystemInfo systemInfoMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+systemInfoTopic+" -> "+systemInfoMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+systemInfoTopic +" ");
			client.publishWith().topic(systemInfoTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSystemInfoQos())).payload(systemInfoMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+systemInfoTopic+" but unable to send to central broker.");
		}
	}

	/**
	 * Method to send statusOverviewMessage to cb.
	 * @param statusOverviewMessage
	 */
	@Override
	public void publishStatusOverviewMessage(StatusOverview statusOverviewMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+statusOverviewTopic+" -> "+statusOverviewMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+statusOverviewTopic +" ");
			client.publishWith().topic(statusOverviewTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisStatusOverviewQos())).payload(statusOverviewMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+statusOverviewTopic+" but unable to send to central broker.");
		}
	}


	/**
	 * Method to send publishSystemStatusMessage to cb.
	 * @param systemStatusMessage
	 */
	@Override
	public void publishSystemStatusMessage(SystemStatus systemStatusMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+systemStatusTopic+" -> "+systemStatusMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+systemStatusTopic +" ");
			client.publishWith().topic(systemStatusTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSystemStatusQos())).payload(systemStatusMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+systemStatusTopic+" but unable to send to central broker.");
		}
	}

	/**
	 * Method to send screenContentMessage to cb.
	 * @param screenContentMessage
	 */
	@Override
	public void publishScreenContentMessage(ScreenContentResponse screenContentMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+screenContentTopic+" -> "+screenContentMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+screenContentTopic +" ");
			client.publishWith().topic(screenContentTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisScreenContentQos())).payload(screenContentMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+screenContentTopic+" but unable to send to central broker.");
		}
	}

	@Override
	public void publishTravelInfoMessage(TravelInfo travelInfoMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+travelInfoTopic+" -> "+travelInfoMessage);
			logger.log(Level.SEVERE, "Publishing to topic -> "+travelInfoTopic +" ");
			client.publishWith().topic(travelInfoTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisTravelInfoQos())).payload(travelInfoMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+travelInfoTopic+" but unable to send to central broker.");
		}
	}


	/**
	 * Method to build last will message.
	 * @return
	 */
	private byte[] getLastWillMessage() {

		String subscriberOwnerCode = generalConfig.getSystemInfoSubscriberOwnerCode();
		int subscriberType = generalConfig.getSystemInfoSubscriberType();
		String serialNumber = generalConfig.getSystemInfoSerialNumber();

		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberOwnerCode(subscriberOwnerCode);
		clientId.setSubscriberTypeValue(subscriberType);
		clientId.setSerialNumber(serialNumber);
		ClientId id = clientId.build();

		Unsubscribe.Builder unSubscribe = Unsubscribe.newBuilder();
		unSubscribe.setClientId(id);
		unSubscribe.setIsPermanent(false);
		unSubscribe.setTimestamp(getCurrentTimeAsInteger());

		Unsubscribe unSubscribeMessage = unSubscribe.build();
		logger.log(Level.SEVERE, "Publishing last will message......"+unSubscribeMessage);;
		return unSubscribeMessage.toByteArray();
	}

	/**
	 * Method to get current time in integer.
	 * @return
	 */
	public Integer getCurrentTimeAsInteger() {
		Date now = new Date();      
		Long longTime = new Long(now.getTime()/1000);
		return longTime.intValue();
	}

	/**
	 * Method used to get topic from config file.
	 * @param topic
	 * @return topic
	 */
	private static String getTopic(String topic) {
		Integer subType = generalConfig.getSystemInfoSubscriberType();
		Integer version = generalConfig.getCentralBrokerOpenDrisVersion();

		String[] topicContent = {version.toString(),subType.toString(),generalConfig.getSystemInfoSubscriberOwnerCode(),generalConfig.getSystemInfoSerialNumber()};
		int i=0;
		int start= -1;
		for(int j=0; j<topic.length();j++) {
			if(topic.charAt(j) == '<') {
				start = j;
			}
			else if(topic.charAt(j) == '>') {
				topic = topic.substring(0, start) + topicContent[i] + topic.substring(j+1);
				i++;
				j -= (j-start);
			}
		}
		return topic;
	}

	
	/**
	 * Method used to get distribution system Last will topic from config file.
	 * @param topic
	 * @return topic
	 */
	private static String getDistributionSystemLastWillTopic(String topic) {
		Integer version = generalConfig.getCentralBrokerOpenDrisVersion();
		return topic.replaceAll("<Version>", version.toString());
	}

}
