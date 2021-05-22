/**
 * @author selvakumarv
 *
 * 30-03-2021
 *
 *Mqtt5PublishCallBack.java
 */
package com.mm.mqconnection;

import java.nio.charset.StandardCharsets;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

/**
 * @author selvakumarv
 *
 */
public class Mqtt5PublishCallBack {

	private Mqtt5Publish mqtt5Publish;
	
	public Mqtt5PublishCallBack(Mqtt5Publish mqtt5Publish) {
		this.mqtt5Publish=mqtt5Publish;
		String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
		System.out.println("==>><>payload<><><"+payload+":topic:"+mqtt5Publish.getTopic().toString());
	}

	/**
	 * @return the mqtt5Publish
	 */
	public Mqtt5Publish getMqtt5Publish() {
		return mqtt5Publish;
	}

	/**
	 * @param mqtt5Publish the mqtt5Publish to set
	 */
	public void setMqtt5Publish(Mqtt5Publish mqtt5Publish) {
		this.mqtt5Publish = mqtt5Publish;
	}
	
	
}
