package com.ars.ssm.connection;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.ars.ODCC.Connection.ODMessage.Subscribe;
import com.ars.ODCC.Connection.ODMessage.Unsubscribe;
import com.ars.ssm.Application;
import com.ars.ssm.configuration.Configuration;
import com.ars.ssm.configuration.GeneralConfig;
import com.ars.ssm.configuration.SSMConfig;
import com.ars.ssm.utils.ARSLogger;
import com.ars.ssm.utils.KeepAlive;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public class StopSystemBrokerManager {
	public static Mqtt5BlockingClient client = null;
	private static ARSLogger logger = Application.getLogger();
	private static ARSLogger subscriptionLogger = Application.getSubscriptionLogger();
	private static Configuration config = Configuration.getInstance();
	private static ObjectMapper mapper = new ObjectMapper();
	private static KeepAlive keepAlive = new KeepAlive();
	private static String clientID;
	/**
	 * Method to obtain mosquito connection and if successful call the subscribeToBroker() method.
	 * @return void
	 * @throws ConnectionFailedExceptionS
	 * @throws InterruptedException 
	 */
	
	public static void startBrokerConnection(String brokerUrl, int brokerPort ) throws Exception {
		//String clientId = "SSM_ClientID";
		String clientId=String.valueOf(config.ssmConfig.getModuleId());
		logger.log(Level.INFO, "Connecting to broker with :\nurl: "+brokerUrl+"\nclientId: "+clientId+"\nport: "+brokerPort);
        try {
            client =  Mqtt5Client.builder()
            		         .identifier(clientId)
            		         .serverHost(brokerUrl)
            		         .serverPort(brokerPort)
            		         .automaticReconnect()
            		         .initialDelay(config.generalConfig.getSSBReconnectInitialDelay(), TimeUnit.SECONDS)
            		         .maxDelay(config.generalConfig.getSSBReconnectMaxDelay(), TimeUnit.SECONDS)
            		         .applyAutomaticReconnect()
            		         .buildBlocking();
        } catch (Exception e) {
            logger.log(Level.INFO, "Unable to create client : "+e.getMessage());
        }
        
        logger.log(Level.INFO, "Connecting to the Broker with: brokerUrl:"+brokerUrl+" & clientId:"+clientId);
		client.connectWith().cleanStart(false).sessionExpiryInterval(120000).send();	
		subscribeToBroker();  
	}
	/**
	 * This method subscribes to the mentioned topics with the MQTT Broker
	 * and actively listens to the topics to perform corresponding tasks.
	 * 
	 * @throws InterruptedException
	 */
	public static void subscribeToBroker() throws InterruptedException {
		logger.log(Level.INFO,"connected......");
		Optional<Mqtt5Publish> receivedMessage;

		//Listens the message received from the Publisher
		Mqtt5BlockingClient.Mqtt5Publishes publish = client.publishes(MqttGlobalPublishFilter.ALL);
		final String subscription_trigger_topic = config.ssmConfig.getSubscribeTriggerTopic();
		String unsubscribe_topic = config.ssmConfig.getUnsubscribeTopic();
		String subscription_reponse_topic = config.ssmConfig.getSubscriptionResponseTopic();
		String connection_response_topic = config.ssmConfig.getConnectionStatusResponseTopic();
		String sClientID="client_id";
		logger.log(Level.INFO, "Subscribing to topics: \n"
		+subscription_trigger_topic+"\n"+unsubscribe_topic
		+"\n"+subscription_reponse_topic+"\n"+connection_response_topic);
		client.subscribeWith()
		.addSubscription().topicFilter(subscription_trigger_topic).qos(MqttQos.AT_LEAST_ONCE).
		applySubscription()
		.addSubscription().topicFilter(unsubscribe_topic).qos(MqttQos.AT_LEAST_ONCE).
		applySubscription()
		.addSubscription().topicFilter(subscription_reponse_topic).qos(MqttQos.AT_LEAST_ONCE).
		applySubscription()
		.addSubscription().topicFilter(connection_response_topic).qos(MqttQos.AT_LEAST_ONCE).
		applySubscription()
		.addSubscription().topicFilter(sClientID).qos(MqttQos.AT_LEAST_ONCE).
		applySubscription()
		.send();
		logger.log(Level.INFO, "subscribed to all configured topics...");

		//Sends Keepalive after getting connected.
		int nCounter = 0;
		Integer nAliveSendTime = getCurrentTimeAsInteger();
		publishKeepAliveMessage(nAliveSendTime);

		while(true) {
			receivedMessage = publish.receive(1000,TimeUnit.MILLISECONDS);
		    if(receivedMessage.isPresent()) {
			 	String topic = receivedMessage.get().getTopic().toString();
			 	logger.log(Level.INFO, "topic..."+topic);
			 	if(topic.equals(subscription_trigger_topic)) {
				String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
				logger.log(Level.INFO, "Received:>subscription Topic : " + topic + " MSG : " + payload);
				subscriptionLogger.log(Level.INFO, "SUBSCRIPTION TRIGGER RECEIVED "+payload);
				subscriptionLogger.log(Level.INFO,"SUBSCRIPTION REQ STARTS");
				Subscribe subscriptionMessage = PublishMessageHandler.getSubscriptionMessage();
				//need to do subscriptionMessage
				publishSubscribeMessage(subscriptionMessage);
			 	}
			if(topic.equals(unsubscribe_topic)) {
				String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
				logger.log(Level.INFO, "Received:>Unsubcriber Topic : " + topic + " MSG : " + payload);
				Unsubscribe unsubscribeMessage = PublishMessageHandler.getUnsubscriptionMessage();
				publishUnsubscribeMessage(unsubscribeMessage);
			}
			if(topic.equals(subscription_reponse_topic)) {
				String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
				logger.log(Level.INFO, "Received:>subscription reponse Topic : " + topic + " MSG : " + payload);
				subscriptionLogger.log(Level.INFO,"SUBSCRIPTION RES STARTS");
				setStatusFromSubscriptionResponse(payload);
			}
			if(topic.equals(connection_response_topic)) {//need to do
				String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
				logger.log(Level.INFO, "Received:>connection response Topic : " + topic + " MSG : " + payload);
				setConnectionStatus(payload);
			}
		    }else
			{
				nCounter++;
				if( nCounter == 20)
				{
					logger.log(Level.SEVERE, "Listening to topics SB"+LocalDateTime.now() );
					nCounter = 0;
				}
			}
			Integer nCurrentTime = getCurrentTimeAsInteger();
			if ( nCurrentTime - nAliveSendTime > Configuration.getInstance().generalConfig.getIntervalInSec() )
			{
				nAliveSendTime = nCurrentTime;
				publishKeepAliveMessage(nAliveSendTime);						
			}
		}
			
	}
	/**
	 * Method to send keepalive message to Stop System Broker.
	 * @param time
	 */
	public static void publishKeepAliveMessage(Integer time) {
		if(client != null && client.getState().isConnected()) {
			KeepAlive keepAliveMessage = new KeepAlive();
			keepAliveMessage.setModuleId(config.ssmConfig.getModuleId());
			keepAliveMessage.setUpdateTime(time);
			Gson gson = new Gson();
			String jsonString = gson.toJson(keepAliveMessage);
			try {
				logger.log(Level.INFO, "Sending KeepAlive message...."+jsonString+" "+LocalDateTime.now());
				client.publishWith().topic(Configuration.getInstance().generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE).payload((jsonString).getBytes()).send();
			}catch (Exception e) {
				logger.log(Level.SEVERE, "Exception occured while sending KeepAlive message...."+e.getMessage()+" "+LocalDateTime.now());
			}
		}
		else {
			logger.log(Level.SEVERE, "Keep alive not sent Client State is "+client.getState());
		}
	}

	/**
	 *  Method to get current time in integer
	 * @return
	 */
	public static Integer getCurrentTimeAsInteger() {
		Date now = new Date();      
		Long longTime = new Long(now.getTime()/1000);
		return longTime.intValue();
	}
	/*
	 * State Management mthods
	 */
	/**
	 * This method handles the state management regarding the subscription response.
	 * For each response received, the respective state is changed. If invalid, the current 
	 * state is maintained.
	 * @param payload
	 * @return void
	 */
	private static void setStatusFromSubscriptionResponse(String payload) {
		subscriptionLogger.log(Level.INFO, "SUBSCRIPTION RESPONSE RECIEVED:\n "+payload);
		String status = getStatus(payload);
		if(status != null && !status.isEmpty()) {
			Application.STATE = switch(status) {
				case "DATA_CHANGED", "STOP_INVALID", "REQUEST_INVALID" -> "RE_CONFIGURATION_NEEDED";
				case "AUTHORISATION_REQUIRED" -> "AUTHORISATION_REQUIRED";
				case "AUTHORISATION_VALIDATED" -> "AUTHORISATION_VALIDATED";
				case "PLANNING_SENT" -> "PLANNING_RECEIVED";
				case "NO_PLANNING" -> "NO_PLANNING";
			default -> Application.STATE;
			};
			logger.log(Level.INFO, "\nAPPLICATION STATE: "+Application.STATE);
			subscriptionLogger.log(Level.SEVERE, "APPLICATION STATE:\n "+Application.STATE);
			subscriptionLogger.log(Level.FINE,"SUBSCRIPTION RES ENDS");
		}else {
			logger.log(Level.INFO, "\nSUBSCRIPTION RESPONSE is Empty");
		}
	}
	/**
	 * This method parses the json string and checks
	 * for the status string value and returns it.
	 * @param payload
	 * @return String status
	 */
	private static String getStatus(String payload) {
		try {
			JsonNode node = mapper.readTree(payload);
			return node.get("status").asText().trim().toUpperCase();
		} catch (JsonProcessingException e) {
			logger.log(Level.SEVERE, "Invalid connection response from SSB: "+e);
		}
		return null;
	}
	/**
	 * This method sets the STATE value as CONNECTED or DISCONNECTED in accordance 
	 * to the value received from the payload.
	 * @param payload
	 * @return void
	 */
	private static void setConnectionStatus(String payload) {
		if(isConnected(payload)) {
			Application.STATE = "CONNECTED";
		}else {
			Application.STATE = "DISCONNECTED";
		}
		logger.log(Level.INFO, "\nAPPLICATION STATE: "+Application.STATE);
	}
	/**
	 * This method parses the json string and checks
	 * for the od_connected boolean value and returns it.
	 * @param payload
	 * @return boolean isConnected
	 */
	private static boolean isConnected(String payload) {
		try {
				JsonNode node = mapper.readTree(payload);
				return node.get("od_connected").asBoolean();
			} catch (JsonProcessingException e) {
				logger.log(Level.SEVERE, "Invalid connection response from SSB: "+e);
			}
		return false;
	}
	/**
	 * This method parses the json string and checks
	 * for the clientid String value and returns it.
	 * @param payload
	 * @return String clientID
	 */
	private static String getClientID(String payload) {
		try {
				JsonNode node = mapper.readTree(payload);
				return node.get("client_id").toString();
			} catch (JsonProcessingException e) {
				logger.log(Level.SEVERE, "Invalid connection response from SSB: "+e);
			}
		return "";
	}
	/*
	 * Publish methods
	 */
	/**
	 * This method publishes the subscription message and sets the Application STATE as SUBSCRIPTION_SENT
	 */
	public static void publishSubscribeMessage(Subscribe subscriptionMessage) {
		String subscribeTopic = config.ssmConfig.getPublishSubscribeTopic();
		client.publishWith().topic(subscribeTopic).qos(MqttQos.AT_LEAST_ONCE).payload(getMessageAsJsonString(subscriptionMessage,true).getBytes()).send();
		Application.STATE = "SUBSCRIPTION_SENT";
		logger.log(Level.INFO, "Subscribe message published to "+subscribeTopic+" topic.");
		subscriptionLogger.log(Level.INFO, "SUBSCRIPTION_SENT");
		subscriptionLogger.log(Level.INFO,"SUBSCRIPTION REQ ENDS");
		logger.log(Level.INFO, "\nAPPLICATION STATE: "+Application.STATE);	
	}
	/**
	 * This method publishes the unsubscribe message.
	 * @param unsubscribeMessage 
	 */
	public static void publishUnsubscribeMessage(Unsubscribe unsubscribeMessage) {
		String unsubscribeTopic = config.ssmConfig.getPublishUnsubscribeTopic();
		client.publishWith().topic(unsubscribeTopic).qos(MqttQos.AT_LEAST_ONCE).payload(getMessageAsJsonString(unsubscribeMessage,false).getBytes()).send();
		Application.STATE = "UNSUBSCRIPTION_SENT";
		logger.log(Level.INFO, "Unsubscribe message published to "+unsubscribeTopic+" topic.");
		//subscriptionLogger.log(Level.INFO, "UNSUBSCRIPTION SENT");
		logger.log(Level.INFO, "\nAPPLICATION STATE: "+Application.STATE);
	}
	/**
	 * This method publishes the keep Alive message.
	 * 
	 */
	public static void publishKeepAliveMessage() {
		if (client != null  && client.getState().isConnected() ) {
			keepAlive.setModuleId(config.ssmConfig.getModuleId());
			keepAlive.setUpdateTime((int) (new Date().getTime()/1000));
			Gson gson = new Gson();
			String jsonString = gson.toJson(keepAlive);
			try {
				StopSystemBrokerManager.client.publishWith().topic(config.generalConfig.getKeepAliveTopic()).qos(MqttQos.AT_LEAST_ONCE).payload(jsonString.getBytes()).send();
				logger.log(Level.INFO, "KEEP ALIVE SENT "+LocalDateTime.now());
			}catch(Exception e) {logger.log(Level.INFO, "Issue in sending Keep Alive: "+e);}
		}
	}
	/**
	 * 
	 */
	public static String getMessageAsJsonString(MessageOrBuilder messageObj,boolean flag) {
//		Gson gson = new Gson();
		JsonFormat.Printer jsonprinter = JsonFormat.printer();
		String jsonStr = null;
		try {
		jsonStr = jsonprinter.print(messageObj);
		if(flag) {
			subscriptionLogger.log(Level.INFO, jsonStr);
			subscriptionLogger.log(Level.INFO, "System Time :"+(new Date().getTime()));
		}
		}catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to parse the message object as json string: "+e);
			if(flag)subscriptionLogger.log(Level.INFO, "Message parse error: "+e.getMessage());
		}
        return jsonStr;
	}
}
