/**
 * 
 */
package com.ars.ssm.configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Abhijith Ravindran
 * This class contains the getter methods for configuration parameters specific 
 * for the SSM module in the stop system.
 */
public class SSMConfig {
	private JsonNode moduleNode;
	private JsonNode topicsSubNode;
	private JsonNode subscribeTopicsSubNode;
	private JsonNode publishTopicsSubNode;
	private String subscriptionLogFileName = "subscriptions";
	
	public SSMConfig(JsonNode moduleNode) {
		this.moduleNode = moduleNode;
		setTopicsSubNode(moduleNode);
		setSubscribeTopicsSubNode(topicsSubNode);
		setPublishTopicsSubNode(topicsSubNode);
	}
	/*
	 * Module ID
	 */
	/**
	 * Reads and returns the value at 
	 * ssm>module_id
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int module_id
	 */
	public int getModuleId() {
		return moduleNode.get("module_id").asInt();
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
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>subscriptionresponse>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSubscriptionResponseTopic() {
		return subscribeTopicsSubNode.get("subscriptionresponse").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>subscriptionresponse>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSubscriptionResponseQos() {
		return subscribeTopicsSubNode.get("subscriptionresponse").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>unsubscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getUnsubscribeTopic() {
		return subscribeTopicsSubNode.get("unsubscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>unsubscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getUnsubscribeQos() {
		return subscribeTopicsSubNode.get("unsubscribe").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>connectionstatus_response>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getConnectionStatusResponseTopic() {
		return subscribeTopicsSubNode.get("connectionstatus_response").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>connectionstatus_response>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getConnectionStatusResponseQos() {
		return subscribeTopicsSubNode.get("connectionstatus_response").get("qos").asInt();
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
	public String getPublishSubscribeTopic() {
		return publishTopicsSubNode.get("subscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>publish>subscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getPublishSubscribeQos() {
		return publishTopicsSubNode.get("subscribe").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * ssm>topics>publish>unsubscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getPublishUnsubscribeTopic() {
		return publishTopicsSubNode.get("unsubscribe").get("topic").asText().trim();
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
	/**
	 * Subscription Logger methods
	 */

	public String getSubscriptionLogFileName() {
		return subscriptionLogFileName;
	}
}
