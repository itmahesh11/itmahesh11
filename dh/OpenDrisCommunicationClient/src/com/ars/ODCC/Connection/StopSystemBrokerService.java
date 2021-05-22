package com.ars.ODCC.Connection;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.ars.ODCC.message.KeepAlive;
import com.ars.ODCC.message.ODMessage.ClientId;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse;
import com.ars.ODCC.message.ODMessage.StatusOverview;
import com.ars.ODCC.message.ODMessage.Subscribe;
import com.ars.ODCC.message.ODMessage.SystemInfo;
import com.ars.ODCC.message.ODMessage.SystemStatus;
import com.ars.ODCC.message.ODMessage.TravelInfo;
import com.ars.ODCC.message.ODMessage.Unsubscribe;
import com.ars.ODCC.Configuration.Configuration;
import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.Configuration.ODCCConfig;
import com.ars.ODCC.Utils.ARSLogger;
import com.ars.ODCC.message.ODMessageHandler;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class StopSystemBrokerService implements IStopSystemBrokerService{

	/**
	 * Configuration to access settings.json file
	 */
	private static Configuration config = Configuration.getInstance();
	/**
	 * GeneralConfig to access settings.general
	 */
	private static GeneralConfig generalConfig = new GeneralConfig();
	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = Application.getLogger();
	/**
	 * OpenDrisManager for accessing external broker
	 */
	private static OpenDrisBrokerService openDrisManager = new OpenDrisBrokerService();
	/**
	 * Mqtt5BlockingClient for accessing internal broker / SSB client
	 */
	private static Mqtt5BlockingClient client = null;
	/**
	 * ODMessageHandler for accessing protobuf/json message
	 */
	private static ODMessageHandler odMessageHandler = new ODMessageHandler();

	private static String subscribeTopic = config.odccConfig.getSSBSubscribeTopic();
	private static String unSubscribeTopic = config.odccConfig.getSSBUnsubscribeTopic();
	private static String clientIdTopic = "ssb/clientIdRequest";
	private static String systemInfoTopic = config.odccConfig.getSSBSystemInfoTopic();
	private static String statusOverviewTopic = config.odccConfig.getSSBStatusOverviewTopic();
	private static String systemStatusTopic = config.odccConfig.getSSBSystemStatusTopic();
	private static String screenContentTopic = config.odccConfig.getSSBScreenContentTopic();
	private static String travelInfoTopic = config.odccConfig.getSSBTravelInfoTopic();

	/**
	 * Method to transfer the message from stop system broker to central broker.
	 * @throws InterruptedException
	 */
	@Override
	public void subscribeToInternalBroker() throws InterruptedException {
		Optional<Mqtt5Publish> receivedMessage;

		Mqtt5BlockingClient.Mqtt5Publishes publish = client.publishes(MqttGlobalPublishFilter.ALL);

		client.subscribeWith()
		.addSubscription().topicFilter(subscribeTopic).qos(getQos(config.odccConfig.getSSBSubscribeQos())).
		applySubscription()
		.addSubscription().topicFilter(unSubscribeTopic).qos(getQos(config.odccConfig.getSSBUnsubscribeQos())).
		applySubscription()
		.addSubscription().topicFilter(systemInfoTopic).qos(getQos(config.odccConfig.getSSBSystemInfoQos())).
		applySubscription()
		.addSubscription().topicFilter(statusOverviewTopic).qos(getQos(config.odccConfig.getSSBStatusOverviewoQos())).
		applySubscription()
		.addSubscription().topicFilter(travelInfoTopic).qos(getQos(config.odccConfig.getSSBTraveInfoQos())).
		applySubscription()
		.addSubscription().topicFilter(screenContentTopic).qos(getQos(config.odccConfig.getSSBScreenContentQos())).
		applySubscription()
		.addSubscription().topicFilter(systemStatusTopic).qos(getQos(config.odccConfig.getSSBSystemStatusQos())).
		applySubscription().send();

		logger.log(Level.INFO, "Listening to topics "+subscribeTopic+" "+systemInfoTopic+" "+unSubscribeTopic+" "+statusOverviewTopic+" "+travelInfoTopic+" "+screenContentTopic+" "+systemStatusTopic);

		//Sends Keepalive after getting connected.
		int nCounter = 0;
		Integer nAliveSendTime = getCurrentTimeAsInteger();
		publishKeepAliveMessage(nAliveSendTime);

		//Listens the message received from the Publisher
		while(true) {

			receivedMessage = publish.receive(1000,TimeUnit.MILLISECONDS);

			if(receivedMessage.isPresent()) {

				String topic = receivedMessage.get().getTopic().toString();
				logger.log(Level.SEVERE, "Received  topic ->"+topic );

				if(topic.equalsIgnoreCase(subscribeTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+subscribeTopic);
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					Subscribe subscribe = odMessageHandler.parseSubscribeToJson(payload);
					if(subscribe != null) {
						openDrisManager.publishSubscribeMessage(subscribe);
					}
				}
				else if(topic.equalsIgnoreCase(unSubscribeTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+unSubscribeTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					Unsubscribe unsubscribe = odMessageHandler.parseUnsubscribeToJson(payload);
					if(unsubscribe != null) {
						openDrisManager.publishUnsubscribeMessage(unsubscribe);
					}

				}
				else if(topic.equalsIgnoreCase(systemInfoTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+systemInfoTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					SystemInfo systemInfo = odMessageHandler.parseSystemInfoToJson(payload);
					if(systemInfo != null) {
						openDrisManager.publishSystemInfoMessage(systemInfo);
					}
				}
				else if(topic.equalsIgnoreCase(statusOverviewTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+statusOverviewTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					StatusOverview statusOverview = odMessageHandler.parseStatusOverviewToJson(payload);
					if(statusOverview != null) {
						openDrisManager.publishStatusOverviewMessage(statusOverview);
					}
				}
				else if(topic.equalsIgnoreCase(travelInfoTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+travelInfoTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					TravelInfo travelInfo = odMessageHandler.parseTravelInfoToJson(payload);
					if(travelInfo != null) {
						openDrisManager.publishTravelInfoMessage(travelInfo);
					}
				}
				else if(topic.equalsIgnoreCase(screenContentTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+screenContentTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					ScreenContentResponse screenContent = odMessageHandler.parseScreenContentToJson(payload);
					if(screenContent != null) {
						openDrisManager.publishScreenContentMessage(screenContent);
					}
				}
				else if(topic.equalsIgnoreCase(systemStatusTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+systemStatusTopic+" ");
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					SystemStatus systemStatus = odMessageHandler.parseSystemStatusToJson(payload);
					if(systemStatus != null) {
						openDrisManager.publishSystemStatusMessage(systemStatus);
					}
				}

				nCounter=0;
			}
			else
			{
				nCounter++;
				if( nCounter == 20)
				{
					logger.log(Level.INFO, "Listening to topics SB" );
					nCounter = 0;
				}
			}
			Integer nCurrentTime = getCurrentTimeAsInteger();
			if ( nCurrentTime - nAliveSendTime > generalConfig.getIntervalInSec() )
			{
				nAliveSendTime = nCurrentTime;
				publishKeepAliveMessage(nAliveSendTime);						
			}
		}
	}

	/**
	 * Method to send subscription response to stop system broker.
	 * @param message
	 */
	@Override
	public void publishSubscribeResponseMessage(String message) {
		String subscriptionResponseTopic = config.odccConfig.getSSBSubscriptionResponseTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.INFO, "Received message for the topic -> "+subscriptionResponseTopic+" -> "+message.replace("/n", " "));
			logger.log(Level.SEVERE, "Pubishing to topic -> "+subscriptionResponseTopic+" ");
			client.publishWith().topic(subscriptionResponseTopic).qos(getQos(config.odccConfig.getSSBSubscriptionResponseQos())).payload((message).getBytes()).retain(true).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscriptionResponseTopic+" but unable to send to stop system broker.");
		}
	}

	/**
	 * Method to send travel information to stop system broker.
	 * @param message
	 */
	@Override
	public void publishTravelInfoMessage(String message) {
		String travelInfoTopic = config.odccConfig.getSSBTravelInformationTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.INFO, "Received message for the topic -> "+travelInfoTopic+" -> "+message.replace("/n", " "));
			logger.log(Level.SEVERE, "Publishing to topic -> "+travelInfoTopic+" ");
			client.publishWith().topic(travelInfoTopic).qos(getQos(config.odccConfig.getSSBTravelInformationQos())).payload((message).getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+travelInfoTopic+" but unable to send to stop system broker.");
		}
	}

	/**
	 * Method to send Info request message to SSB.
	 * @param message
	 */
	@Override
	public void publishInfoRequestMessage(String message) {
		String infoRequestTopic = config.odccConfig.getSSBInfoRequestTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.INFO, "Received message for the topic -> "+infoRequestTopic+" -> "+message.replace("/n", " "));
			logger.log(Level.SEVERE, "Publishing to topic -> "+infoRequestTopic+" ");
			client.publishWith().topic(infoRequestTopic).qos(getQos(config.odccConfig.getSSBInfoRequestQos())).payload((message).getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+infoRequestTopic+" but unable to send to stop system broker.");
		}
	}


	/**
	 * Method to send subscription trigger to stop system broker.
	 */
	@Override
	public void sendSubsriptionTrigger() {
		String subscribeTriggerTopic = config.odccConfig.getSSBSubscribeTriggerTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Publishing to topic -> "+subscribeTriggerTopic);
			client.publishWith().topic(subscribeTriggerTopic).qos(getQos(config.odccConfig.getSSBSubscribeTriggerQos())).payload(("").getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscribeTriggerTopic+" but unable to send to stop system broker.");
		}
	}

	/**
	 * Method to get the Qos.
	 * @param qos
	 * @return
	 */
	@Override
	public MqttQos getQos(int qos)
	{
		MqttQos mqttQos;
		if(qos == 0) {
			mqttQos = MqttQos.AT_MOST_ONCE;
		}
		if(qos == 1) {
			mqttQos = MqttQos.AT_LEAST_ONCE;
		}
		else {
			mqttQos = MqttQos.EXACTLY_ONCE;
		}
		return mqttQos;
	}

	/**
	 * Method to obtain internal mosquito connection.
	 * @return
	 * @throws InterruptedException 
	 */
	@Override
	public void obtainInternalBrokerConnection() throws InterruptedException {
		int clientId = config.odccConfig.getODCCModuleId();
		logger.log(Level.SEVERE, "Connecting to stop system broker ....");
		try {
			client =  Mqtt5Client.builder()
					.identifier(String.valueOf(clientId))
					.serverHost(generalConfig.getSSBURL())
					.serverPort(generalConfig.getSSBPort())
					.automaticReconnect()
					.initialDelay(generalConfig.getSSBReconnectInitialDelay(), TimeUnit.SECONDS)
					.maxDelay(generalConfig.getSSBReconnectMaxDelay(), TimeUnit.SECONDS)
					.applyAutomaticReconnect()
					.buildBlocking();

			client.connectWith().cleanStart(false).sessionExpiryInterval(120000).send();
			logger.log(Level.SEVERE, "Connected to stop system Broker..."+generalConfig.getSSBURL()+" ");
			sendClientID();
			subscribeToInternalBroker(); 
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Disconnected from stop system Broker...");
		}
	}

	/**
	 * Sends client Id to SSB whenever stopsyetem connects to SSB
	 */
	public void sendClientID() {
		if(client != null && client.getState().isConnected()) {
			String subscriberOwnerCode = generalConfig.getSystemInfoSubscriberOwnerCode();
			int subscriberType = generalConfig.getSystemInfoSubscriberType();
			String serialNumber = generalConfig.getSystemInfoSerialNumber();

			ClientId.Builder clientId = ClientId.newBuilder();
			clientId.setSubscriberOwnerCode(subscriberOwnerCode);
			clientId.setSubscriberTypeValue(subscriberType);
			clientId.setSerialNumber(serialNumber);
			JsonFormat.Printer jsonprinter = JsonFormat.printer();

			String payload = null;
			try {
				payload = jsonprinter.print(clientId);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			publishClientID(payload);
		}
		else {
			logger.log(Level.INFO, "ClientId is not sent");
		}
	}

	
	/**
	 * Method to send keepalive message to Stop System Broker.
	 * @param time
	 */
	@Override
	public void publishKeepAliveMessage(Integer time) {
		if(client != null && client.getState().isConnected()) {
			KeepAlive keepAliveMessage = new KeepAlive();
			keepAliveMessage.setModuleId(config.odccConfig.getODCCModuleId());
			keepAliveMessage.setUpdateTime(time);
			Gson gson = new Gson();
			String jsonString = gson.toJson(keepAliveMessage);
			try {
				logger.log(Level.SEVERE, "Sending KeepAlive message...."+jsonString);
				client.publishWith().topic(generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE).payload((jsonString).getBytes()).send();
			}catch (Exception e) {
				logger.log(Level.SEVERE, "Exception occured while sending KeepAlive message...."+e.getMessage());
			}
		}
		else {
			logger.log(Level.SEVERE, "Keep alive not sent Client State is "+client.getState());
		}
	}


	/**
	 * Method to send connection status to stop system broker.
	 * @param message
	 */
	@Override
	public void publishConnectionStatusMessage(String message) {
		String connectionStatusResponseTopic = config.odccConfig.getSSBConnectionStatusResponseTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Publishing to topic "+connectionStatusResponseTopic+" ->"+message.replace("/n", " "));
			client.publishWith().topic(connectionStatusResponseTopic).qos(MqttQos.AT_LEAST_ONCE).payload((message).getBytes()).retain(true).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+connectionStatusResponseTopic+" but unable to send to stop system broker.");
		}
	}

	/**
	 * Method to publish client Id to SSB.
	 * @param payload
	 */
	@Override
	public void publishClientID(String payload) {
		try {
			logger.log(Level.SEVERE, "Sending ClientId ...."+payload);
			client.publishWith().topic(clientIdTopic).qos(MqttQos.AT_LEAST_ONCE).payload((payload).getBytes()).retain(true).send();
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Exception occured while sending ClientID...."+e.getMessage());
		}
	}

	/**
	 *  Method to get current time in integer
	 * @return
	 */
	@Override
	public Integer getCurrentTimeAsInteger() {
		Date now = new Date();      
		Long longTime = new Long(now.getTime()/1000);
		return longTime.intValue();
	}
}
