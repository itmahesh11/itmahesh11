package com.ars.ssm.configuration;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 
 * @author Abhijith Ravindran
 * This class is a singleton class which loads , parses and provides access to
 * configuration json file parameters.
 */
public class Configuration {
	/*
	 * Default values
	 */
	private static String CONFIGURATION_FILE = "settings.json";
	private static final String LOG_FILE_NAME = "ssm";
	public static final int LOG_FILE_SIZE = 5000000;
	public static final int LOG_FILE_NUMBER_OF_BACKUP = 10;
	private static final String LOG_LEVEL = "ALL";
	
	private static Configuration configuration;
	/** current version of SSM. */
//	private static final String CURRENT_VERSION = "0.0.1";
	/** default log file path */
	//private static String LOG_FILE_PATH = "D:\\SSM\\Logs";
	private static String LOG_FILE_PATH;
	/** default module state file path */
//	private static String MODULE_STATE_FILE_PATH = "D:\\SSM\\ModuleState";
	
	/** rootnode of json */
	private JsonNode rootNode;
	/** rootnode name of json */
	private final String ROOT_NODE_NAME = "sbc_settings";
	/** configuration json as File */
	private File jsonConfigFile;
	/** The current module name */
	private String moduleName = "SSM";
	/** The current module node */
	private JsonNode moduleNode;
	/** config class specific for ssm  parameters */
	public SSMConfig ssmConfig;
	/** The current module name */
	private String generalModuleName = "general";
	/** The current module node */
	private JsonNode generalModuleNode;
	/** config class for general parameters**/
	public GeneralConfig generalConfig;

	/**
	 * 
	 * Instantiate <code>Configuration</code> and load configurations from
	 * json file. Call the initialize() method for module specific configuration
	 * loading.
	 */
	private Configuration() throws JsonProcessingException, IOException {
		jsonConfigFile = getJsonConfigFile();
		rootNode = getRootNode();
		initialize();
	}

	/**
	 * Gets an unique instance of <code>Configuration</code>
	 * 
	 * @return unique instance of <code>Configuration</code>
	 */
	synchronized public static Configuration getInstance() {
		if (configuration == null) {
			try {
				configuration = new Configuration();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return configuration;
	}
	/**
	 * Initialize the module specific configuration object
	 */
	public void initialize() {
		moduleNode = getNestedNode(rootNode, moduleName);
		generalModuleNode = getNestedNode(rootNode, generalModuleName);
		ssmConfig = new SSMConfig(moduleNode);
		generalConfig = new GeneralConfig(generalModuleNode);
	}
	/**
	 * load json as File
	 */
	private File getJsonConfigFile() {
		return new File(CONFIGURATION_FILE);
	}
	/**
	 * Get an instance of ObjectMapper
	 * @return ObjectMapper
	 */
	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}
	/**
	 * Get the json file root node as JsonNode
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
	 * Get the log file name.
	 * @return String
	 */
	public String getLogFileName() {
		return LOG_FILE_NAME;
	}
	/**
	 * Reads and returns the value of LOG_FILE_PATH from json config file. If not 
	 * specified in json file, this will load the default value.
	 * @return String LOG_FILE_PATH
	 */
	public String getLogFilePath() {
		final String value = moduleNode.get("log").get("LOG_FILE_PATH").asText().trim();
		return value.length() > 0 ? value : LOG_FILE_PATH;
	}
	/**
	 * Reads and returns the value of LOG_FILE_SIZE from json config file. If not 
	 * specified in json file, this will load the default value.
	 * @return int LOG_FILE_SIZE
	 */
	public int getLogFileSize() {
		Integer value = moduleNode.get("log").get("LOG_FILE_SIZE").asInt();
		return value != null ? value : LOG_FILE_SIZE;
	}
	/**
	 * Reads and returns the value of LOG_FILE_NUMBER_OF_BACKUP from json config file. If not 
	 * specified in json file, this will load the default value.
	 * @return int LOG_FILE_NUMBER_OF_BACKUP
	 */
	public int getLogFileNoOfBackup() {
		Integer value = moduleNode.get("log").get("LOG_FILE_NUMBER_OF_BACKUP").asInt();
		return value != null ? value : LOG_FILE_NUMBER_OF_BACKUP;
	}
	/**
	 * Reads and returns the value of LOG_LEVEL from json config file. If not 
	 * specified in json file, this will load the default value.
	 * @return int LOG_LEVEL
	 */
	public String getLogLevel() {
		final String value = moduleNode.get("log").get("LOG_LEVEL").asText().trim();
		return value.length() > 0 ? value : LOG_LEVEL;
	}
	
	/**
	 * Reads the value of LOG_BUFFERING_ENABLED from json config file 
	 * and returns a boolean value 'true' for '0' and 'false' for '1'. If not 
	 * specified in json file, this will load the default value.
	 * @return boolean isBufferingDisabled
	 */
	public boolean isLogBufferingDisabled() {
		boolean isBufferingDisabled = true;
		Integer bufferingEnabled = moduleNode.get("log").get("LOG_BUFFERING_ENABLED").asInt();
		if(bufferingEnabled != null) {
			if(bufferingEnabled == 1) {
				isBufferingDisabled = false;
			}
		}
		return isBufferingDisabled;
	}
	/**
	 * This method iterates through the rootNode Array and returns the 
	 * JsonNode for the sub nodeName specified.
	 * @param JsonNode rootNode, String nodeName
	 * @return JsonNode moduleNode
	 */
	public JsonNode getNestedNode(JsonNode rootNode, String nodeName) {
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
