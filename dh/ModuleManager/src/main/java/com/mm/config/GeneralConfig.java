/**
 * @author selvakumarv
 *
 * 25-03-2021
 *
 *GeneralConfig.java
 */
package com.mm.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author selvakumarv
 *
 */
public class GeneralConfig {

	private static String CONFIGURATION_FILE = "settings.json";
	
	public static final int LOG_FILE_SIZE = 5000000;
	public static final int LOG_FILE_NUMBER_OF_BACKUP = 10;
	private JsonNode rootNode;
	private final String ROOT_NODE_NAME = "sbc_settings";
	private File jsonConfigFile;
	private String moduleName = "general";

	private JsonNode moduleNode;
	private JsonNode centralBrokerSubNode;

	/**
	 * 
	 * Instantiate <code>GenaralConfig</code> and load configurations from json
	 * file. Call the initialize() method for module specific configuration loading.
	 */
	public GeneralConfig() {
		this.jsonConfigFile = getJsonConfigFile();
		rootNode = getRootNode();
		initialize();

	}

	public GeneralConfig(JsonNode moduleNode) {
		setModuleNode(moduleNode);
		setCentralBrokerSubNode(centralBrokerSubNode);

	}

	/**
	 * load json as File
	 */
	private File getJsonConfigFile() {
		return new File(CONFIGURATION_FILE);
	}

	/**
	 * Get the json file root node as JsonNode
	 * 
	 * @return JsonNode
	 */
	private JsonNode getRootNode() {
		try {
			return getObjectMapper().readTree(jsonConfigFile).get(ROOT_NODE_NAME);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get an instance of ObjectMapper
	 * 
	 * @return ObjectMapper
	 */
	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}

	/**
	 * Initialize the module specific configuration object
	 */
	public void initialize() {
		moduleNode = getNestedNode(rootNode, moduleName);
		setModuleNode(moduleNode);

	}

	/**
	 * This method iterates through the rootNode Array and returns the JsonNode for
	 * the sub nodeName specified.
	 * 
	 * @param JsonNode rootNode, String nodeName
	 * @return JsonNode moduleNode
	 */
	private JsonNode getNestedNode(JsonNode rootNode, String nodeName) {
		if (rootNode.isArray()) {
			for (final JsonNode objNode : rootNode) {
				if (objNode.get(nodeName) != null) {
					return objNode.get(nodeName);
				}

			}
		}
		return null;
	}

	public JsonNode getModuleNode() {
		return moduleNode;
	}

	public void setModuleNode(JsonNode moduleNode) {
		this.moduleNode = moduleNode;
	}

	public JsonNode getCentralBrokerSubNode() {
		return centralBrokerSubNode;
	}

	public void setCentralBrokerSubNode(JsonNode centralBrokerSubNode) {
		this.centralBrokerSubNode = moduleNode.get("centralbroker");
	}

	public int getSSBKeepAliveTime() {
		return moduleNode.get("ssb").get("KEEPALIVETIME").asInt();
	}

	public String getSSBURL() {
		return moduleNode.get("ssb").get("URL").asText();
	}

	public int getSSBPort() {
		return moduleNode.get("ssb").get("Port").asInt();
	}

	public int getSSBReconnectInitialDelay() {
		return moduleNode.get("ssb").get("reconnect_initialdelayInseconds").asInt();
	}

	public int getSSBReconnectMaxDelay() {
		return moduleNode.get("ssb").get("reconnect_maxdelayInseconds").asInt();
	}

	public int getSystemInfoSubscriberType() {
		return moduleNode.get("systeminfo").get("subscriber_type").asInt();
	}

	public String getSystemInfoSubscriberOwnerCode() {
		return moduleNode.get("systeminfo").get("SubscriberOwnerCode").asText();
	}

	public String getSystemInfoSerialNumber() {
		return moduleNode.get("systeminfo").get("SerialNumber").asText();
	}

	public String getKeepAliveTopic() {
		return moduleNode.get("keepalive").get("topic").asText();
	}

	public int getKeepAliveIntervalInSec() {
		return moduleNode.get("keepalive").get("intervalInSec").intValue();
	}

}
