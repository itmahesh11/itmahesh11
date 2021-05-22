/**
 * 
 */
package com.ars.ODCC.Configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Abhijithr
 * This class contains the methods for configuration parameters specific 
 * for the SSM module in the stop system.
 */
public class SSMConfig {
	private JsonNode moduleNode;
	private JsonNode topicsSubNode;
	private JsonNode subscribeTopicsSubNode;
	private JsonNode publishTopicsSubNode;

	public SSMConfig(JsonNode moduleNode) {
		this.moduleNode = moduleNode;
		setTopicsSubNode(moduleNode);
		setSubscribeTopicsSubNode(topicsSubNode);
		setPublishTopicsSubNode(topicsSubNode);
	}
	/*
	 * Topics - Subscribe Node getters
	 */
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>subscribe_trigger>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSubscribeTriggerTopic() {
		return subscribeTopicsSubNode.get("subscribe_trigger").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>subscribe_trigger>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSubscribeTriggerQos() {
		return subscribeTopicsSubNode.get("subscribe_trigger").get("qos").asInt();
	}
	/*
	 * Topics - publish Node getters
	 */
	/**
	 * Reads and returns the value at 
	 * ssm>topics>publish>subscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getPublishSubscribeTriggerTopic() {
		return publishTopicsSubNode.get("subscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>publish>subscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getPublishSubscribeTriggerQos() {
		return publishTopicsSubNode.get("subscribe").get("qos").asInt();
	}
	/*
	 * Setting nodes and sub nodes of topics - subscribe, publish
	 */
	/**
	 * sets the value for topicsSubNode from the root moduleNode.
	 * @return void
	 */
	private void setTopicsSubNode(JsonNode topicsSubNode) {
		this.topicsSubNode = moduleNode.get("topics");
	}
	/**
	 * sets the value for subscribeTopicsSubNode from the root subscribe.
	 * @return void
	 */
	private void setSubscribeTopicsSubNode(JsonNode subscribeTopicsSubNode) {
		this.subscribeTopicsSubNode = getNestedNode(subscribeTopicsSubNode, "subscribe");
	}
	/**
	 * sets the value for publishTopicsSubNode from the root publish.
	 * @return void
	 */
	private void setPublishTopicsSubNode(JsonNode publishTopicsSubNode) {
		this.publishTopicsSubNode = getNestedNode(publishTopicsSubNode, "publish");
	}
	/*
	 * JsonNode Array iteration logic
	 */
	/**
	 * This method iterates through the rootNode Array and returns the 
	 * JsonNode for the sub nodeName specified.
	 * @param JsonNode rootNode, String nodeName
	 * @return JsonNode moduleNode
	 */
	private JsonNode getNestedNode(JsonNode rootNode, String nodeName) {
		if (rootNode.isArray()) {
		    for (final JsonNode objNode : rootNode) {
		        if(objNode.get(nodeName) != null) {
		        	return objNode.get(nodeName);
		        }
		        
		    }
		}
		return null;
	}
	
}
