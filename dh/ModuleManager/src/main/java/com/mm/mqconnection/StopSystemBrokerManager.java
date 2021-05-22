/**
 * @author selvakumarv
 *
 * 25-03-2021
 *
 *StopSystemBrokerManager.java
 */
package com.mm.mqconnection;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.mm.config.Configuration;
import com.mm.config.GeneralConfig;
import com.mm.config.MMConfig;
import com.mm.entity.KeepAlive;
import com.mm.inf.ISubscribeToInternalBroker;
import com.mm.service.StopComponentService;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 */
public class StopSystemBrokerManager {

	/**
	 * Mqtt5BlockingClient for accessing internal broker / SSB client
	 */
	private @Nonnull Mqtt5BlockingClient client;
	private @Nonnull Configuration config;
	private ARSLogger logger;
	private @Nonnull MMConfig mmConfig;
	public @Nonnull GeneralConfig generalConfig;
	public ISubscribeToInternalBroker subscribeinternalBroker;
	private Mqtt5BlockingClient.Mqtt5Publishes publish;
	private Optional<Mqtt5Publish> receivedMessage;
	private StopComponentService stopComponentService;
	public static  Mqtt5BlockingClient mqtt5BlockingClientConnectionl;
	
	
	public StopSystemBrokerManager(Configuration configuration,MMConfig mmConfig) {
		this.config = configuration;
		logger = configuration.getLogger();
		this.mmConfig = mmConfig;
		generalConfig = new GeneralConfig();
		subscribeinternalBroker = new SubscribeToInternalBroker(configuration,mmConfig,generalConfig);
		stopComponentService = new StopComponentService(mmConfig);
		stopComponentService.getLogger(configuration);
	}

	/**
	 * Method to obtain internal mosquito connection.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public Boolean obtainInternalBrokerConnection() throws InterruptedException {
		Boolean isconnected=false;
		
		int clientId = mmConfig.getMMModuleID();
		logger.log(Level.INFO, "Connecting to stop system broker ...."+" " + LocalDateTime.now());
		try {
			/*
			 * client =
			 * Mqtt5Client.builder().identifier(String.valueOf(clientId)).serverHost(
			 * generalConfig.getSSBURL())
			 * .serverPort(generalConfig.getSSBPort()).automaticReconnect()
			 * .initialDelay(generalConfig.getSSBReconnectInitialDelay(), TimeUnit.SECONDS)
			 * .maxDelay(generalConfig.getSSBReconnectMaxDelay(),
			 * TimeUnit.SECONDS).applyAutomaticReconnect() .buildBlocking();
			 * 
			 * client.connectWith().cleanStart(false).sessionExpiryInterval(120000).send();
			 */
			client =brokerConnection(String.valueOf(clientId),generalConfig.getSSBURL(),generalConfig.getSSBPort(),generalConfig.getSSBReconnectInitialDelay(),generalConfig.getSSBReconnectMaxDelay());
			if(client!=null) {
				isconnected =client.getState().isConnected();
				logger.log(Level.INFO,
						"Connected to stop system Broker..." + generalConfig.getSSBURL() + " " + LocalDateTime.now());
			}else {
				logger.log(Level.SEVERE, "Disconnected from stop system Broker..." +" "+ LocalDateTime.now());
			}
			
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Disconnected from stop system Broker..." +" "+ LocalDateTime.now());
		}
		return isconnected;
	}

	/**
	 * Method to transfer the message from stop system broker to central broker.
	 * 
	 * @throws InterruptedException
	 */
	public  void subscribeToInternalBroker() throws InterruptedException {
		try {

			String keepAlivetopic = generalConfig.getKeepAliveTopic();
			String logmsgtopic =mmConfig.getLogMsgTopic();
			int logmsqos=mmConfig.getLogMsgQos();
			String metricmsgtopic =mmConfig.getMeteriMsgTopic();
			int metricmsgQos=mmConfig.getMeteriMsgQos();
			String componetmsgtopic =mmConfig.getComponetMsgTopic();
			int componentsqos= mmConfig.getComponetMsgQos();
			String stopcomponetmsgtopic =mmConfig.getStopMMMsgTopic();
			int stopcomponentsqos= mmConfig.getStopMMMsgQos();
			 
			publish = client.publishes(MqttGlobalPublishFilter.ALL);
			

			client.subscribeWith().addSubscription().topicFilter(keepAlivetopic).qos(getQos(2)).applySubscription()
			.addSubscription().topicFilter(stopcomponetmsgtopic).qos(getQos(stopcomponentsqos)).applySubscription()
			.addSubscription().topicFilter(logmsgtopic).qos(getQos(logmsqos)).applySubscription()
			.addSubscription().topicFilter(metricmsgtopic).qos(getQos(metricmsgQos)).applySubscription()
			.addSubscription().topicFilter(componetmsgtopic).qos(getQos(componentsqos)).applySubscription()
					.send();
			logger.log(Level.INFO, "Listening to topics " + keepAlivetopic + " " + LocalDateTime.now());

			long lastKeepAliveCheckTime = mmConfig.getCurrentTimeStamp();
			// Listens the message received from the Publisher
			while (true) {
				receivedMessage = publish.receive(150,TimeUnit.MILLISECONDS);//(1000, TimeUnit.MILLISECONDS);

				if (receivedMessage.isPresent()) {
					//client.
					//.
					String topic = receivedMessage.get().getTopic().toString();
					String payload = new String(receivedMessage.get().getPayloadAsBytes(), StandardCharsets.UTF_8);
					logger.log(Level.INFO, "Received  topic ->" + topic +" "+ payload+" "+LocalDateTime.now());

					if (topic.equalsIgnoreCase(keepAlivetopic)) {
												
						logger.log(Level.INFO, "Updated currentTime " + lastKeepAliveCheckTime +" "+LocalDateTime.now());
						subscribeinternalBroker.keepAliveMessage(payload);
						
					}else if(topic.equalsIgnoreCase(stopcomponetmsgtopic)) {
						stopComponentService.stopComponents();
					}else if(topic.equalsIgnoreCase(logmsgtopic)) {
						subscribeinternalBroker.logMessage(payload);
					}else if(topic.equalsIgnoreCase(metricmsgtopic)) {
						subscribeinternalBroker.metericMessage(payload);
					}else if(topic.equalsIgnoreCase(componetmsgtopic)) {
						subscribeinternalBroker.componentStatusMessage(payload);
					}
				}
				
				
				
				long currenTime = mmConfig.getCurrentTimeStamp();
				//long keepalivetime =mmConfig.getKeepaliveTimeBufferInSec();
				long currentsubtractlastKeep =(currenTime-lastKeepAliveCheckTime);
				//logger.log(Level.INFO, "currentTime " + currenTime +" "+ "currentTime subtract lastKeep "+" "+currentsubtractlastKeep+" "+" config file keepalive second "+" "+generalConfig.getKeepAliveIntervalInSec()+" "+LocalDateTime.now());
				if( currentsubtractlastKeep >=mmConfig.getMonitoringInterval()  )
				{
				// Check for the keep alive message time.
					
					lastKeepAliveCheckTime = currenTime;
				
					Map<Integer, KeepAlive> mapList = mmConfig.getKeepAliveMap();
					if (mapList != null && !mapList.isEmpty()) {
						System.out.println("keepAliveMonitoring.mapList." + mapList.size());
						mapList.entrySet().stream().forEach(map -> {
							logger.log(Level.SEVERE,
									"keepAlive Monitoring. " + " moduleID [ " + map.getKey()+" ] "+"  " + LocalDateTime.now());
							if(subscribeinternalBroker instanceof SubscribeToInternalBroker) {
								((SubscribeToInternalBroker) subscribeinternalBroker).checkKeepAliveModule(map.getKey(), map.getValue());
							}
							
						});

					}
					//
					
				}
				
				
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage()+" " + LocalDateTime.now());
		}

	}

	
	public static Mqtt5BlockingClient brokerConnection(String clientId,String ssbUrl,int ssbport,int ssDelay, int maxdelay) {
		Mqtt5BlockingClient clientConnection=null;
		try {
			clientConnection = Mqtt5Client.builder().identifier(clientId).serverHost(ssbUrl)
					.serverPort(ssbport).automaticReconnect()
					.initialDelay(ssDelay, TimeUnit.SECONDS)
					.maxDelay(maxdelay, TimeUnit.SECONDS).applyAutomaticReconnect()
					.buildBlocking();

			clientConnection.connectWith().cleanStart(false).sessionExpiryInterval(120000).send();
			setMqtt5BlockingClientConnectionl(clientConnection);
		} catch (Exception e) {
			
		}
		return clientConnection;
	}
	
	
	/**
	 * @return the mqtt5BlockingClientConnectionl
	 */
	public static Mqtt5BlockingClient getMqtt5BlockingClientConnectionl() {
		
		return mqtt5BlockingClientConnectionl;
	}

	/**
	 * @param mqtt5BlockingClientConnectionl the mqtt5BlockingClientConnectionl to set
	 */
	public static void setMqtt5BlockingClientConnectionl(Mqtt5BlockingClient mqtt5BlockingClientConnectionl) {
		StopSystemBrokerManager.mqtt5BlockingClientConnectionl = mqtt5BlockingClientConnectionl;
	}

	/**
	 * Method to get the Qos.
	 * 
	 * @param qos
	 * @return
	 */
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
}
