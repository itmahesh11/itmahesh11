/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

package com.ars.ssd.configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Subramani
 * This class contains the getter methods for configuration parameters specific 
 * for the SSM module in the stop system.
 */
public class DHConfig {
	private JsonNode moduleNode;
	private JsonNode topicsSubNode;
	private JsonNode subscribeTopicsSubNode;
	private JsonNode publishTopicsSubNode;
	private JsonNode logMessageNode;
	private JsonNode tripAvailabilityNode;

	




	public JsonNode getTripAvailabilityNode() {
		return tripAvailabilityNode;
	}

	public void setTripAvailabilityNode(JsonNode tripAvailabilityNode) {
		this.tripAvailabilityNode = getNestedNode(tripAvailabilityNode, "tripAvailability");
	}

	public DHConfig(JsonNode moduleNode) {
		this.moduleNode = moduleNode;
		setTopicsSubNode();
		setSubscribeTopicsSubNode(topicsSubNode);
		setPublishTopicsSubNode(topicsSubNode);
		setLogMessageNode();
		setTripAvailabilityNode(logMessageNode);
		
	}
	/*
	 * Topics - Subscribe Node getters
	 */

	public JsonNode getLogMessageNode() {
		return logMessageNode;
	}
	public void setLogMessageNode() {
		this.logMessageNode = moduleNode.get("LogMessage");

	}

	/**
	 * sets the value for topicsSubNode from the root moduleNode.
	 * @return void
	 */
	private void setTopicsSubNode() {
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
	 * Reads and returns the value at 
	 * ssm>topics>subscribe>subscribe_trigger>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	
	
	public String getRendererRegistrationMessageTopic(String keyVal) {
		
		String topicObj ;
		topicObj = subscribeTopicsSubNode.get("renderer_registration_Message").get(keyVal).asText().trim();
		return topicObj;
	}
	/**
	 * odcc connection status
	 */
	public String getOdccConnectionStatus(String keyVal) {
		return subscribeTopicsSubNode.get("odcc_connection_status").get(keyVal).asText();
	}
	/**
	 * get module Id
	 * @return
	 */
	public int getDHModuleId() {
        return moduleNode.get("module_id").asInt();
    }
	public String getPassageFileWithPath() {
        return moduleNode.get("passage_file_with_path").asText().trim();
    }
	public String getFreeTextFileWithPath() {
        return moduleNode.get("freetext_file_with_path").asText().trim();
    }
 
	/**
	 * get Container Message topic
	 * @return
	 */
	public String getOdccContainerTopic(String keyVal) {
		String topicObj ;
		topicObj = subscribeTopicsSubNode.get("odcc-container").get(keyVal).asText().trim();
		return topicObj;
	}
	
	/**
	 * subscribe SSM request message under the topic
	 * @return
	 */
	public String getTopicForSubscribeSSMMessage(String keyVal) {
		return subscribeTopicsSubNode.get("ssm_travelinfo_request").get(keyVal).asText();
	}
	/**
	 * get topic used for publishing trip free text message to ssm
	 * @return
	 */
	public String getTopicForPublishTripToSSM(String keyVal) {
		String topicObj ;
		topicObj = publishTopicsSubNode.get("ssm_travelinfo_response").get(keyVal).asText().trim();
		return topicObj;
	}
	
	
	/**
	 * Publish system status message to RDR
	 */
	public String getTopicForPublishSystemStatusMessage( String keyVal) {
		String topicObj ;
		topicObj = publishTopicsSubNode.get("log_message").get(keyVal).asText().trim();
		return topicObj;
	}
	
	/**
	 * rdr trigger topic
	 * @param keyVal
	 * @return
	 */
	public String getTopicForTriggerRDRMessage( String keyVal) {
		String topicObj ;
		topicObj = publishTopicsSubNode.get("rdr_trigger").get(keyVal).asText().trim();
		return topicObj;
	}
	
	/**
	 * rdr trigger qos
	 * @return
	 */
	public int getRDRTriggerQos() {
		return publishTopicsSubNode.get("rdr_trigger").get("qos").asInt();
	}
	
	
	
	
	/**
	 * get trigger qos
	 * @return
	 */
	public int getOdccContainerTriggerQos() {
		return subscribeTopicsSubNode.get("odcc-container").get("qos").asInt();
	}
	public int getRendererRegistrationMessageTriggerQos() {
		return subscribeTopicsSubNode.get("renderer_registration_Message").get("qos").asInt();
	}
	public int getOdccConnectionStatusTriggerQos() {
		return subscribeTopicsSubNode.get("odcc_connection_status").get("qos").asInt();
	}
	public int getSsmSubscribeTriggerQos() {
		return subscribeTopicsSubNode.get("ssm_travelinfo_request").get("qos").asInt();
	}
	
	
	public int getSsmPublishTriggerQos() {
		return publishTopicsSubNode.get("ssm_travelinfo_response").get("qos").asInt();
	}
	public int getSystemStatusMessageTriggerQos() {
		return publishTopicsSubNode.get("log_message").get("qos").asInt();
	}
	
	public String getMessageOK() {
		return tripAvailabilityNode.get("message_OK").asText();
	}
	public int getCode() {
		return tripAvailabilityNode.get("code").asInt();
	}
	public String getMessageWRNING() {
		return tripAvailabilityNode.get("message_WRNING").asText();
	}
	public Boolean getLogMessageEnable() {
		return tripAvailabilityNode.get("enable").asBoolean();
	}
}

