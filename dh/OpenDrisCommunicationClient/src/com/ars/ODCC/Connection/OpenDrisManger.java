package com.ars.ODCC.Connection;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


import com.ars.ODCC.Configuration.Configuration;
import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.Connection.ODMessage.ClientId;
import com.ars.ODCC.Connection.ODMessage.Container;
import com.ars.ODCC.Connection.ODMessage.Subscribe;
import com.ars.ODCC.Connection.ODMessage.SubscriptionResponse;
import com.ars.ODCC.Connection.ODMessage.Unsubscribe;
import com.ars.ODCC.Utils.ARSLogger;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.internal.util.AsyncRuntimeException;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class OpenDrisManger {
	/**
	 * StopSystemBrokerManager for accessing internal broker / SSB
	 */
	private static StopSystemBrokerManager stopSystemManager = new StopSystemBrokerManager();
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
	private static final String subscribeResponseTopic = getTopic(config.odccConfig.getOpenDrisSubscriptionResponseTopic());
	/**
	 * traveInfoTopic from settings.json file
	 */
	private static final String traveInformationTopic = getTopic(config.odccConfig.getOpenDrisTravelInformationTopic());
	/**
	 * subscribeTopic from settings.json file
	 */
	private static String subscribeTopic = getTopic(config.odccConfig.getOpenDrisSubscribeTopic());
	/**
	 * unSubscribeTopic from settings.json file
	 */
	private static String unSubscribeTopic = getTopic(config.odccConfig.getOpenDrisUnsubscribeTopic());
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
	double maxNumberOfAttempts = generalConfig.getCentralBrokerMQTTMaxRandConnAttempt();
	/**
	 * minimum no of retries for central broker url
	 */
	double minNumberOfAttempts = generalConfig.getCentralBrokerMQTTMinRandConnAttempt();
	/**
	 * average no of attempts of retries to be done.
	 */
	double avgNumberOfAttempts = Math.round((maxNumberOfAttempts+ minNumberOfAttempts)/2);
	private String primaryURL;
	int primaryPort ;
	boolean start = true;
	boolean infinity = false;




	/**
	 * Method to transfer message from central broker to stop system broker.
	 * 
	 * @throws InterruptedException
	 * @throws InvalidProtocolBufferException
	 */
	public static void subscribeToExternalBroker() throws InterruptedException, InvalidProtocolBufferException {

		Optional<Mqtt5Publish> receivedMessage;

		//Listens the message received from the Publisher
		Mqtt5BlockingClient.Mqtt5Publishes publish = client.publishes(MqttGlobalPublishFilter.ALL);

		client.subscribeWith()
		.addSubscription().topicFilter(subscribeResponseTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSubscriptionResponseQos())).
		applySubscription()
		.addSubscription().topicFilter(traveInformationTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisTravelInfoQos())).
		applySubscription().send();


		logger.log(Level.SEVERE, "Listening to topics "+subscribeResponseTopic +" "+traveInformationTopic +LocalDateTime.now());
		int nCounter = 0;

		String payload = null;
		while(true) {

			receivedMessage = publish.receive(1000,TimeUnit.MILLISECONDS);
			if(receivedMessage.isPresent()) {
				String topic = receivedMessage.get().getTopic().toString();
				logger.log(Level.SEVERE, "Received  topic ->"+topic +LocalDateTime.now());

				if(topic.equalsIgnoreCase(subscribeResponseTopic))  {
					//Converting protobuf to json
					SubscriptionResponse subscriptionResponse = SubscriptionResponse.parseFrom(receivedMessage.get().getPayloadAsBytes());
					payload = oDMessageHandler.parseSubscriptionRequestProtoBufMessage(subscriptionResponse);

					logger.log(Level.SEVERE, "Received the topic -> "+subscribeResponseTopic +" "+subscriptionResponse +LocalDateTime.now());
					stopSystemManager.publishSubscribeResponseMessage(payload);
				}
				if(topic.equalsIgnoreCase(traveInformationTopic)){
					//Converting protobuf to json
					Container travelInfo = Container.parseFrom(receivedMessage.get().getPayloadAsBytes());
					payload = oDMessageHandler.parseTravelInformationProtoBufMessage(travelInfo);

					logger.log(Level.SEVERE, "Received the topic -> "+traveInformationTopic +" "+travelInfo+LocalDateTime.now());
					stopSystemManager.publishTravelInfoMessage(payload);
					System.out.println(payload);
				}
				nCounter = 0;
			}
			else
			{
				nCounter++;
				if( nCounter == 20)
				{
					logger.log(Level.SEVERE, "Listening to topics CB"+LocalDateTime.now() );
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


			logger.log(Level.SEVERE, "Connected to external broker..."+brokerUrl+" "+LocalDateTime.now());
			setRetryCount(brokerUrl);
			stopSystemManager.sendSubsriptionTrigger();
			sendConnectionStatusMessage(true);

			logger.log(Level.SEVERE, "subscribeToExternalBroker...start"+LocalDateTime.now());
			subscribeToExternalBroker();
			logger.log(Level.SEVERE, "subscribeToExternalBroker...finish"+LocalDateTime.now());
		}

		catch (AsyncRuntimeException e) {

			logger.log(Level.SEVERE, "Not able to connect external broker...Disconnected from external broker "+brokerUrl+" "+e.getMessage()+" "+LocalDateTime.now());
			sendConnectionStatusMessage(false);
			throw e;
		}
	}

	/**
	 * Method to set retry count of central broker.
	 */
	public void setRetryCount(String brokerURL) {
		noOfRetries = 0;
		infinity = false;
		primaryURL = brokerURL;
	}

	/**
	 * Method to decide the url that broker needs to connect.
	 */
	public  void handleConnectionToCB() {
		logger.log(Level.SEVERE, "Started Connecting to central broker connection...."+LocalDateTime.now());
		String brokerURL1 = generalConfig.getCentralBrokerURL1();
		String brokerURL2 = generalConfig.getCentralBrokerURL2();
		int cbPort1 = generalConfig.getCentralBrokerPort1();
		int cbPort2 = generalConfig.getCentralBrokerPort2();

		long timeToWait = 0;


		logger.log(Level.SEVERE, "Average number of attempts "+avgNumberOfAttempts);

		long randomMilliSeconds = 1000L;
		int maxBackOff = generalConfig.getCentralBrokerMqttMaxBackOffTime();

		if(start){
			primaryURL = brokerURL1;
			primaryPort= cbPort1;
		}
		while(true)
		{

			logger.log(Level.SEVERE, "Connecting CB.... Try....."+LocalDateTime.now());

			try {
				logger.log(Level.SEVERE, "Connecting to  CB "+primaryURL+" .... Try....."+noOfRetries+" "+LocalDateTime.now());
				obtainExternalBrokerConnection(primaryURL,primaryPort);   //code to obtain  connection using Mqtt5Client

			}catch (InvalidProtocolBufferException | InterruptedException | AsyncRuntimeException e) {

				timeToWait = Math.min((long) ((Math.pow(2, noOfRetries)*1000) + randomMilliSeconds), maxBackOff);
				logger.log(Level.SEVERE, "Lost Connection to CB....Will retry after "+timeToWait+" "+LocalDateTime.now());
				try {
					Thread.sleep(timeToWait);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(!infinity && noOfRetries == (avgNumberOfAttempts-1)) {
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
				noOfRetries++;
			}
		}
	}

	/**
	 * Method to send connection status to internal broker
	 * @param status
	 */
	private void sendConnectionStatusMessage(Boolean status) {

		logger.log(Level.SEVERE, "Sending OD MESSAGE "+status+LocalDateTime.now());
		ODConnectionStatus odMessage = new ODConnectionStatus();
		odMessage. setOdConnected(status);
		odMessage. setStatechangeFrom(getCurrentTimeAsInteger());
		Gson gson = new Gson();
		String jsonString = gson.toJson(odMessage);
		stopSystemManager.publishODMessage(jsonString);
		return;
	}

	/**
	 * Method to send subscribe message to central broker.
	 * @param subscribeMessage
	 */
	public void publishSubscribeMessage(Subscribe subscribeMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscribeTopic+" -> "+subscribeMessage+" "+LocalDateTime.now());
			logger.log(Level.SEVERE, "Publishing to topic -> "+subscribeTopic +" "+ subscribeMessage+LocalDateTime.now());
			client.publishWith().topic(subscribeTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisSubscribeQos())).payload(subscribeMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+subscribeTopic+" but unable to send to central broker."+LocalDateTime.now());
		}
	}

	/**
	 * Method to send unsubscribe message to central broker.
	 * @param unsubscribeMessage
	 */
	public void publishUnsubscribeMessage(Unsubscribe unsubscribeMessage) {
		if(client != null && client.getState().isConnected()) {
			logger.log(Level.SEVERE, "Received message for the topic -> "+unSubscribeTopic+" -> "+unsubscribeMessage+" "+LocalDateTime.now());
			logger.log(Level.SEVERE, "Publishing to topic -> "+unSubscribeTopic +" "+LocalDateTime.now());
			client.publishWith().topic(unSubscribeTopic).qos(stopSystemManager.getQos(config.odccConfig.getOpenDrisUnsubscribeQos())).payload(unsubscribeMessage.toByteArray()).send();
		}
		else {
			logger.log(Level.SEVERE, "Received message for the topic -> "+unSubscribeTopic+" but unable to send to central broker."+LocalDateTime.now());
		}
	}

	/**
	 * Method to build last will message.
	 * @return
	 */
	private byte[] getLastWillMessage() {

		logger.log(Level.SEVERE, "Publishing last will message......"+LocalDateTime.now());
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
		System.out.println(unSubscribeMessage);
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
		int i=0,start= -1;
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

}
