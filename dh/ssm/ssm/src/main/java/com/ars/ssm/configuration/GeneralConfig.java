package com.ars.ssm.configuration;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Dharani Vijayakumar
 * This class contains the getter methods for configuration parameters specific 
 * for the General module in the stop system.
 */
public class GeneralConfig {
	
	private static String CONFIGURATION_FILE = "settings.json";
	private static final String LOG_FILE_NAME = "general";
	public static final int LOG_FILE_SIZE = 5000000;
	public static final int LOG_FILE_NUMBER_OF_BACKUP = 10;
	private static final String LOG_LEVEL = "ALL";
	private static String LOG_FILE_PATH = "D:\\general\\Logs";

	private JsonNode rootNode;
	private final String ROOT_NODE_NAME = "sbc_settings";
	private File jsonConfigFile;
	private String moduleName = "general";
	GeneralConfig generalConfig;

	private JsonNode moduleNode;
	private JsonNode centralBrokerSubNode;
	private JsonNode ssbSubNode;
	private JsonNode logSubNode;
	private JsonNode systemInfoSubNode;
	
	/**
	 * 
	 * Instantiate <code>GenaralConfig</code> and load configurations from
	 * json file. Call the initialize() method for module specific configuration
	 * loading.
	 */
	public GeneralConfig(){
		this.
		jsonConfigFile = getJsonConfigFile();
		rootNode = getRootNode();
		initialize();
		
	}

	public GeneralConfig(JsonNode moduleNode) {
		setModuleNode(moduleNode);
		setCentralBrokerSubNode(centralBrokerSubNode);
		setSsbSubNode(ssbSubNode);
		setLogSubNode(logSubNode);
		setSystemInfoSubNode(systemInfoSubNode);
	}
	
	
	
	
	/**
	 * load json as File
	 */
	private File getJsonConfigFile() {
		return new File(CONFIGURATION_FILE);
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
	 * Get an instance of ObjectMapper
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
		generalConfig = new GeneralConfig(moduleNode);
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
	public JsonNode getSsbSubNode() {
		return ssbSubNode;
	}
	public void setSsbSubNode(JsonNode ssbSubNode) {
		this.ssbSubNode = moduleNode.get("ssb");
	}
	public JsonNode getLogSubNode() {
		return logSubNode;
	}
	public void setLogSubNode(JsonNode logSubNode) {
		this.logSubNode = moduleNode.get("log");
	}
	public JsonNode getSystemInfoSubNode() {
		return systemInfoSubNode;
	}
	public void setSystemInfoSubNode(JsonNode systemInfoSubNode) {
		this.systemInfoSubNode = moduleNode.get("systeminfo");
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

		
      public String getSystemInfoQuayCodes() {
    	  return moduleNode.get("systeminfo").get("QuayCodes").toString();
    	  }
		 
      public int getSystemInfoNumberOfLinesTravelInfo() {
    	  return moduleNode.get("systeminfo").get("NumberofLinesofTravelInformation").asInt();
      }
      public String getSystemInfoTypesOfHousing() {
    	  return moduleNode.get("systeminfo").get("TypeofHousing").asText();
      }
      public int getSystemInfoNumberOfScreens() {
    	  return moduleNode.get("systeminfo").get("NumberofScreens").asInt();
      }
      public String getSystemInfoTypeOfScreens() {
    	  return moduleNode.get("systeminfo").get("TypeofScreens").asText();
      }
      
    

	public int getCentralBrokerOpenDrisVersion() {
  	  return moduleNode.get("centralbroker").get("OpenDRISVersion").asInt();
    }
    public int getCentralBrokerKeepAliveTime() {
    	  return moduleNode.get("centralbroker").get("KEEPALIVETIME").asInt();
      }

    public int getCentralBrokerMqttMaxBackOffTime() {
    	  return moduleNode.get("centralbroker").get("MQTTMaxBackOff").asInt();
      }

    public int getCentralBrokerMQTTMinRandConnAttempt() {
    	  return moduleNode.get("centralbroker").get("MQTTMinRandConnAttempt").asInt();
      }

    public int getCentralBrokerMQTTMaxRandConnAttempt() {
    	  return moduleNode.get("centralbroker").get("MQTTMaxRandConnAttempt").asInt();
      }
    public String getCentralBrokerURL() {
    	return moduleNode.get("centralbroker").get("brokerdetails").get(0).get("URL").asText();
    }
    public int getCentralBrokerPort() {
    	return moduleNode.get("centralbroker").get("brokerdetails").get(0).get("Port").asInt();
    }
    public String getCentralBrokerSubscriberOwnerCode() {
    	return moduleNode.get("centralbroker").get("brokerdetails").get(0).get("SubscriberOwnerCode").asText();
    }
    public String getCentralBrokerSerialNumber() {
    	return moduleNode.get("centralbroker").get("brokerdetails").get(0).get("SerialNumber").asText();
    }

    public JsonNode getCentralBrokerDetails() {
    	  return moduleNode.get("centralbroker").get("brokerdetails");
      }
    
    public String getKeepAliveTopic() {
        return moduleNode.get("keepalive").get("topic").asText();
      }
    public int getIntervalInSec() {
        return moduleNode.get("keepalive").get("intervalInSec").intValue();
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
