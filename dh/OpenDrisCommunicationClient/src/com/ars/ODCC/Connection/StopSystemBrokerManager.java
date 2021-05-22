package com.ars.ODCC.Connection;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.ars.ODCC.Configuration.Configuration;
import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.Configuration.ODCCConfig;
import com.ars.ODCC.Connection.ODMessage.Subscribe;
import com.ars.ODCC.Connection.ODMessage.Unsubscribe;
import com.ars.ODCC.Utils.ARSLogger;
import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class StopSystemBrokerManager{
	
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
	private static OpenDrisManger openDrisManager = new OpenDrisManger();
	/**
	 * Mqtt5BlockingClient for accessing internal broker / SSB client
	 */
	private static Mqtt5BlockingClient client = null;
	/**
	 * ODMessageHandler for accessing protobuf/json message
	 */
	private static ODMessageHandler odMessageHandler = new ODMessageHandler();



	/**
	 * Method to transfer the message from stop system broker to central broker.
	 * @throws InterruptedException
	 */
	public void subscribeToInternalBroker() throws InterruptedException {
		Optional<Mqtt5Publish> receivedMessage;
		String subscribeTopic = config.odccConfig.getSSBSubscribeTopic();
		String unSubscribeTopic = config.odccConfig.getSSBUnsubscribeTopic();
		Mqtt5BlockingClient.Mqtt5Publishes publish = client.publishes(MqttGlobalPublishFilter.ALL);

		client.subscribeWith()
		.addSubscription().topicFilter(subscribeTopic).qos(getQos(config.odccConfig.getSSBSubscribeQos())).
		applySubscription()
		.addSubscription().topicFilter(unSubscribeTopic).qos(getQos(config.odccConfig.getSSBUnsubscribeQos())).
		applySubscription().send();

		logger.log(Level.SEVERE, "Listening to topics "+subscribeTopic+" "+unSubscribeTopic+" "+LocalDateTime.now());

		//Sends Keepalive after getting connected.
		int nCounter = 0;
		Integer nAliveSendTime = getCurrentTimeAsInteger();
		publishKeepAliveMessage(nAliveSendTime);
		
		//Listens the message received from the Publisher
		while(true) {
			
			receivedMessage = publish.receive(1000,TimeUnit.MILLISECONDS);

			if(receivedMessage.isPresent()) {

				String topic = receivedMessage.get().getTopic().toString();
				logger.log(Level.SEVERE, "Received  topic ->"+topic +LocalDateTime.now());

				if(topic.equalsIgnoreCase(subscribeTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+subscribeTopic+" "+LocalDateTime.now());
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					Subscribe subscribe = odMessageHandler.parseSubscribeJson(payload);
					openDrisManager.publishSubscribeMessage(subscribe);
				}
				else if(topic.equalsIgnoreCase(unSubscribeTopic)) {
					logger.log(Level.SEVERE, "Received the topic -> "+unSubscribeTopic+" "+LocalDateTime.now());
					//convert Json to protobuf
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					Unsubscribe unsubscribe = odMessageHandler.parseUnsubscribeJson(payload);
					openDrisManager.publishUnsubscribeMessage(unsubscribe);
				}
				nCounter=0;
			}
			else
			{
				nCounter++;
				if( nCounter == 20)
				{
					logger.log(Level.SEVERE, "Listening to topics SB"+LocalDateTime.now() );
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
	public void publishSubscribeResponseMessage(String message) {
		String subscriptionResponseTopic = config.odccConfig.getSSBSubscriptionResponseTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscriptionResponseTopic+" -> "+message+" "+LocalDateTime.now());
			logger.log(Level.SEVERE, "Pubishing to topic -> "+subscriptionResponseTopic+" "+LocalDateTime.now());
			client.publishWith().topic(subscriptionResponseTopic).qos(getQos(config.odccConfig.getSSBSubscriptionResponseQos())).payload((message).getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscriptionResponseTopic+" but unable to send to stop system broker."+LocalDateTime.now());
		}
	}
	
	/**
	 * Method to send travel information to stop system broker.
	 * @param message
	 */
	public void publishTravelInfoMessage(String message) {
		String travelInfoTopic = config.odccConfig.getSSBTravelInfoTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+travelInfoTopic+" -> "+message+" "+LocalDateTime.now());
			logger.log(Level.SEVERE, "Publishing to topic -> "+travelInfoTopic+" "+LocalDateTime.now());
			client.publishWith().topic(travelInfoTopic).qos(getQos(config.odccConfig.getSSBTraveInfoQos())).payload((message).getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+travelInfoTopic+" but unable to send to stop system broker."+LocalDateTime.now());
		}
	}

	/**
	 * Method to send subscription trigger to stop system broker.
	 */
	public void sendSubsriptionTrigger() {
		String subscribeTriggerTopic = config.odccConfig.getSSBSubscribeTriggerTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Publishing to topic -> "+subscribeTriggerTopic+LocalDateTime.now());
			client.publishWith().topic(subscribeTriggerTopic).qos(getQos(config.odccConfig.getSSBSubscribeTriggerQos())).payload(("").getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscribeTriggerTopic+" but unable to send to stop system broker."+LocalDateTime.now());
		}
	}
	
	/**
	 * Method to get the Qos.
	 * @param qos
	 * @return
	 */
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
	public void obtainInternalBrokerConnection() throws InterruptedException {
		int clientId = config.odccConfig.getODCCModuleId();
		logger.log(Level.SEVERE, "Connecting to stop system broker ...."+LocalDateTime.now());
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
			logger.log(Level.SEVERE, "Connected to stop system Broker..."+generalConfig.getSSBURL()+" "+LocalDateTime.now());
			subscribeToInternalBroker(); 
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Disconnected from stop system Broker..."+LocalDateTime.now());
		}
	}
	

	/**
	 * Method used to send the keep alive messages to the SSB
	 */
	/*
	 * public void sendKeepAliveMessage() { if(client != null &&
	 * client.getState().isConnected()) { TimerTask task = new TimerTask() {
	 * 
	 * @Override public void run() { // provide a time delay for the task to
	 * complete try{ Thread.sleep(1000L); } catch(InterruptedException e){
	 * e.printStackTrace(); } Date now = new Date(); Long longTime = new
	 * Long(now.getTime()/1000); KeepAlive keepAliveMessage = new KeepAlive();
	 * keepAliveMessage.setModuleId(config.odccConfig.getODCCModuleId());
	 * keepAliveMessage.setUpdateTime(longTime.intValue()); Gson gson = new Gson();
	 * String jsonString = gson.toJson(keepAliveMessage); try {
	 * publishKeepAliveMessage(jsonString); }catch (Exception e) {
	 * System.out.println("Exception occured while sending KeepAlive message...."+e.
	 * getMessage()+" "+LocalDateTime.now()); //logger.log(Level.SEVERE,
	 * "Exception occured while sending KeepAlive message...."+e.getMessage()+" "
	 * +LocalDateTime.now()); } } }; Timer timer = new Timer("ODCC Timer"); Long
	 * scheduleTime =(long) (generalConfig.getIntervalInSec()*1000);
	 * timer.schedule(task, new Date(), scheduleTime); } }
	 */
	
	/**
	 * Method to send keepalive message to Stop System Broker.
	 * @param time
	 */
	public void publishKeepAliveMessage(Integer time) {
		if(client != null && client.getState().isConnected()) {
			KeepAlive keepAliveMessage = new KeepAlive();
			keepAliveMessage.setModuleId(config.odccConfig.getODCCModuleId());
			keepAliveMessage.setUpdateTime(time);
			Gson gson = new Gson();
			String jsonString = gson.toJson(keepAliveMessage);
			try {
				logger.log(Level.SEVERE, "Sending KeepAlive message...."+jsonString+" "+LocalDateTime.now());
				client.publishWith().topic(generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE).payload((jsonString).getBytes()).send();
			}catch (Exception e) {
				logger.log(Level.SEVERE, "Exception occured while sending KeepAlive message...."+e.getMessage()+" "+LocalDateTime.now());
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
	public void publishODMessage(String message) {
		String connectionStatusResponseTopic = config.odccConfig.getSSBConnectionStatusResponseTopic();
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Publishing to topic "+connectionStatusResponseTopic+" "+LocalDateTime.now());
			client.publishWith().topic(connectionStatusResponseTopic).qos(MqttQos.AT_LEAST_ONCE).payload((message).getBytes()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+connectionStatusResponseTopic+" but unable to send to stop system broker."+LocalDateTime.now());
		}
	}
	
	/**
	 *  Method to get current time in integer
	 * @return
	 */
	public Integer getCurrentTimeAsInteger() {
		Date now = new Date();      
		Long longTime = new Long(now.getTime()/1000);
		return longTime.intValue();
	}

}
