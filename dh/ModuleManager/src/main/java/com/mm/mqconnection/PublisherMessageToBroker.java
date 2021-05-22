/**
 * @author selvakumarv
 *
 *	2021-04-01
 *
 *PublisherMessageBroker.java	
 */
package com.mm.mqconnection;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.mm.inf.IPublisherMessageToBroker;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 */
public class PublisherMessageToBroker implements IPublisherMessageToBroker {
	
	public  ARSLogger logger;
	public  Properties properties;
	
	public PublisherMessageToBroker() {
		
	}
	
	@Override
	public void setLogger(ARSLogger logger) {
		this.logger=logger;
	}
	@Override
	public void setProperties(Properties properties) {
		this.properties=properties;
	}
	
	@Override
	public void systemStatusMessage(String topic,int qos,String message) {
		Mqtt5BlockingClient brokerclient =StopSystemBrokerManager.getMqtt5BlockingClientConnectionl(); 
		if(brokerclient!=null) {
			logger.log(Level.INFO,properties.getProperty("start_to_publisher_system_status_overview")+"  "+LocalDateTime.now());
			brokerclient.publishWith().topic(topic).qos(StopSystemBrokerManager.getQos(qos)).payload(message.getBytes()).send();
		}else {
			logger.log(Level.SEVERE,"Disconnected from stop system Broker... " +LocalDateTime.now());
		}
	}
}
