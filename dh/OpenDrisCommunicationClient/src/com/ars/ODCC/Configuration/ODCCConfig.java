package com.ars.ODCC.Configuration;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Dharani Vijayakumar
 * This class contains the getter methods for configuration parameters specific 
 * for the ODCC module in the stop system.
 */
public class ODCCConfig {
	private JsonNode moduleNode;
	private JsonNode topicsSubNode;
	private JsonNode openDrisTopicsSubNode;
	private JsonNode ssbTopicsSubNode;

	public ODCCConfig(JsonNode moduleNode) {
		this.moduleNode = moduleNode;
		setTopicsSubNode();
		setOpenDrisTopicsSubNode(topicsSubNode);
		setSSBTopicsSubNode(topicsSubNode);
	}
	
	
	/*
	 * Setting nodes and sub nodes of topics - OpenDris, ssb
	 */
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
	private void setOpenDrisTopicsSubNode(JsonNode subscribeTopicsSubNode) {
		this.openDrisTopicsSubNode = getNestedNode(subscribeTopicsSubNode, "opendris");
	}
	/**
	 * sets the value for publishTopicsSubNode from the root publish.
	 * @return void
	 */
	private void setSSBTopicsSubNode(JsonNode publishTopicsSubNode) {
		this.ssbTopicsSubNode = getNestedNode(publishTopicsSubNode, "ssb");
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
	/*
	 * Topics - OpenDris Node getters
	 */
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>subscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisSubscribeTopic() {
		return openDrisTopicsSubNode.get("subscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>subscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisSubscribeQos() {
		return openDrisTopicsSubNode.get("subscribe").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>unsubscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisUnsubscribeTopic() {
		return openDrisTopicsSubNode.get("unsubscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>unsubscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisUnsubscribeQos() {
		return openDrisTopicsSubNode.get("unsubscribe").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>travel_information>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisTravelInformationTopic() {
		return openDrisTopicsSubNode.get("travel_information").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>travel_information>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisTravelInformationQos() {
		return openDrisTopicsSubNode.get("travel_information").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>subscription_response>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisSubscriptionResponseTopic() {
		return openDrisTopicsSubNode.get("subscription_response").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>subscription_response>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisSubscriptionResponseQos() {
		return openDrisTopicsSubNode.get("subscription_response").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>systemstatus>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisSystemStatusTopic() {
		return openDrisTopicsSubNode.get("systemstatus").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>systemstatus>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisSystemStatusQos() {
		return openDrisTopicsSubNode.get("systemstatus").get("qos").asInt();
	}
    
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>inforequest>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisInfoRequestTopic() {
		return openDrisTopicsSubNode.get("inforequest").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>inforequest>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisInfoRequestQos() {
		return openDrisTopicsSubNode.get("inforequest").get("qos").asInt();
	}
    
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>Systeminfo>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisSystemInfoTopic() {
		return openDrisTopicsSubNode.get("Systeminfo").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>Systeminfo>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisSystemInfoQos() {
		return openDrisTopicsSubNode.get("Systeminfo").get("qos").asInt();
	}
    
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>Statusoverview>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisStatusOverviewTopic() {
		return openDrisTopicsSubNode.get("Statusoverview").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>Statusoverview>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisStatusOverviewQos() {
		return openDrisTopicsSubNode.get("Statusoverview").get("qos").asInt();
	}
     
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>travelinfo>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisTravelInfoTopic() {
		return openDrisTopicsSubNode.get("travelinfo").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>travelinfo>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisTravelInfoQos() {
		return openDrisTopicsSubNode.get("travelinfo").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>fileavailable>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisFileAvailableTopic() {
		return openDrisTopicsSubNode.get("fileavailable").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>fileavailable>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisFileAvailableQos() {
		return openDrisTopicsSubNode.get("fileavailable").get("qos").asInt();
	}

	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>screencontent>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisScreenContentTopic() {
		return openDrisTopicsSubNode.get("screencontent").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>screencontent>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisScreenContentQos() {
		return openDrisTopicsSubNode.get("screencontent").get("qos").asInt();
	}

	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>lastWillOfDistributionSystem>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getOpenDrisDistributionSystemLastWillTopic() {
		return openDrisTopicsSubNode.get("lastWillOfDistributionSystem").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>OpenDris>lastWillOfDistributionSystem>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getOpenDrisDistributionSystemLastWillQos() {
		return openDrisTopicsSubNode.get("lastWillOfDistributionSystem").get("qos").asInt();
	}
     
     
	/*
	 * Topics - ssb Node getters
	 */
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBSubscribeTopic() {
		return ssbTopicsSubNode.get("subscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBSubscribeQos() {
		return ssbTopicsSubNode.get("subscribe").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscribe_trigger>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBSubscribeTriggerTopic() {
		return ssbTopicsSubNode.get("subscribe_trigger").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscribe_trigger>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBSubscribeTriggerQos() {
		return ssbTopicsSubNode.get("subscribe_trigger").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>unsubscribe>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBUnsubscribeTopic() {
		return ssbTopicsSubNode.get("unsubscribe").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>unsubscribe>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBUnsubscribeQos() {
		return ssbTopicsSubNode.get("unsubscribe").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>unsubscribe_trigger>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBUnsubscribeTriggerTopic() {
		return ssbTopicsSubNode.get("unsubscribe_trigger").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>unsubscribe_trigger>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBUnsubscribeTriggerQos() {
		return ssbTopicsSubNode.get("unsubscribe_trigger").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssbDris>travel_information>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBTravelInformationTopic() {
		return ssbTopicsSubNode.get("travel_information").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>travel_information>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBTravelInformationQos() {
		return ssbTopicsSubNode.get("travel_information").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscription_response>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBSubscriptionResponseTopic() {
		return ssbTopicsSubNode.get("subscription_response").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>subscription_response>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBSubscriptionResponseQos() {
		return ssbTopicsSubNode.get("subscription_response").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>systemstatus>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBSystemStatusTopic() {
		return ssbTopicsSubNode.get("systemstatus").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>systemstatus>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBSystemStatusQos() {
		return ssbTopicsSubNode.get("systemstatus").get("qos").asInt();
	}
    
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>inforequest>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBInfoRequestTopic() {
		return ssbTopicsSubNode.get("inforequest").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>inforequest>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBInfoRequestQos() {
		return ssbTopicsSubNode.get("inforequest").get("qos").asInt();
	}
    
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>Systeminfo>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBSystemInfoTopic() {
		return ssbTopicsSubNode.get("Systeminfo").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>Systeminfo>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBSystemInfoQos() {
		return ssbTopicsSubNode.get("Systeminfo").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>Statusoverview>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBStatusOverviewTopic() {
		return ssbTopicsSubNode.get("Statusoverview").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>Statusoverview>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBStatusOverviewoQos() {
		return ssbTopicsSubNode.get("Statusoverview").get("qos").asInt();
	}

	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>travelinfo>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBTravelInfoTopic() {
		return ssbTopicsSubNode.get("travelinfo").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>travelinfo>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBTraveInfoQos() {
		return ssbTopicsSubNode.get("travelinfo").get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>fileavailable>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBFileAvailableTopic() {
		return ssbTopicsSubNode.get("fileavailable").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>fileavailable>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBFileAvailableQos() {
		return ssbTopicsSubNode.get("fileavailable").get("qos").asInt();
	}

	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>screencontent>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBScreenContentTopic() {
		return ssbTopicsSubNode.get("screencontent").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>SSB>screencontent>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBScreenContentQos() {
		return ssbTopicsSubNode.get("screencontent").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>screencontent>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBConnectionStatusQueryTopic() {
		return ssbTopicsSubNode.get("connectionstatus_query").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>SSB>screencontent>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBConnectionStatusQueryQos() {
		return ssbTopicsSubNode.get("connectionstatus_query").get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>ssb>connectionstatus_response>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getSSBConnectionStatusResponseTopic() {
		return ssbTopicsSubNode.get("connectionstatus_response").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * odcc>topics>SSB>connectionstatus_response>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getSSBConnectionStatusResponseQos() {
		return ssbTopicsSubNode.get("connectionstatus_response").get("qos").asInt();
	}
	public int getODCCModuleId() {
		return moduleNode.get("module_id").asInt();
	}
	
}
