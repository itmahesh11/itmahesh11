/**
 * @author selvakumarv
 *
  */
package com.mm.config;

import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.ars.ODCC.Connection.ODMessage.InfoRequest;
import com.ars.ODCC.Connection.ODMessage.StatusOverview.StatusOverviewLine;
import com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage;
import com.ars.ODCC.Connection.ODMessage.SystemStatus.Metric;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mm.entity.KeepAlive;
import com.mm.entity.MMEntity;
import com.mm.entity.MetricMessage;
import com.mm.util.ARSLogger;
import com.mm.util.MMUtil;

/**
 * @author selvakumarv
 *
 *         22-03-2021
 *
 *         MMConfig.java
 */
public class MMConfig {

	public static final int MONITORING_INTERVAL = 3;

	private JsonNode moduleNode;
	public JsonNode localComponentSubNode;
	private Properties properties;
	private List<MMEntity> listJson;
	private  JsonNode topicsSubNode;
	private  JsonNode logMsgsubNode;
	private  JsonNode metricMsgsubNode;
	private  JsonNode componentStatusSubNode;
	private  JsonNode stopComponets;
	private  JsonNode publishSubNote; 
	/**
	 * In Memory Data
	 */
	private Map<Long, MMEntity> mapinmemory = new ConcurrentHashMap<>();
	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger;

	private Map<Integer, KeepAlive> keepAliveinmemory = new ConcurrentHashMap<Integer, KeepAlive>();
	private Map<String, MetricMessage> componentsStatuMemory = new ConcurrentHashMap<String, MetricMessage>();

	public MMConfig(JsonNode moduleNode) {
		properties = MMUtil.getInstance().getProperties();
		this.moduleNode = moduleNode;
		init();
	}
	private void init() {
		setLocalComponentsSubNode(moduleNode);
		setTopicsSubNode(moduleNode);
		setLogmsgSubscribe(topicsSubNode);
		setMetricmsgSubscribe(topicsSubNode);
		setComponentstatuse(topicsSubNode);
		setStopComponent(topicsSubNode);
		setpublishSubNote(topicsSubNode);
	}

	public void getLogger(Configuration configuration) {
		logger = configuration.getLogger();
		logger.startLogger(configuration.isLogBufferingDisabled());
		getLocalComponents(localComponentSubNode);
	}

	/**
	 * @param moduleNode2
	 * @return JsonNode for list JsonNodeArray
	 */
	private void setLocalComponentsSubNode(JsonNode moduleNode2) {

		localComponentSubNode = moduleNode.get("LocalComponentForMonitoring");
	}
	
	/**
	 * @param moduleNode2
	 * @return JsonNode for topics  JsonNodeArray
	 */
	private void setTopicsSubNode(JsonNode moduleNode2) {

		topicsSubNode = moduleNode.get("topics");
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>logmsg
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * 
	 */
	public void setLogmsgSubscribe(JsonNode topicsSubNode) {
		logMsgsubNode= getNestedNode(topicsSubNode, "logmessage");;
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>metricmsg
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * 
	 */
	public void setMetricmsgSubscribe(JsonNode topicsSubNode) {
		metricMsgsubNode= getNestedNode(topicsSubNode, "metricmessage");
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>componentstatus
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * 
	 */
	public void setComponentstatuse(JsonNode topicsSubNode) {
		componentStatusSubNode= getNestedNode(topicsSubNode, "componentstatus");
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>stopmm
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * 
	 */
	public void setStopComponent(JsonNode topicsSubNode) {
		stopComponets= getNestedNode(topicsSubNode, "stopMM");
	}
	
	
	
	
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>stopMM>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getStopMMMsgTopic() {
		
		return stopComponets.get("topic").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>stopMM>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getStopMMMsgQos() {
	return stopComponets.get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>logmsg>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getLogMsgTopic() {
		
		return logMsgsubNode.get("topic").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>logmsg>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getLogMsgQos() {
	return logMsgsubNode.get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>logmsg>filepath
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String  filepath in log file
	 */
	public String getLogMsgFilePath() {
	return logMsgsubNode.get("filepath").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>metricmsg>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getMeteriMsgTopic() {
		
		return metricMsgsubNode.get("topic").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>metericmsg>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getMeteriMsgQos() {
	return metricMsgsubNode.get("qos").asInt();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>metericmsg>filepath
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String filepath
	 */
	public String getMeteriMsgFilePath() {
	return metricMsgsubNode.get("filepath").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>componentStatus>topic
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getComponetMsgTopic() {
		
		return componentStatusSubNode.get("topic").asText().trim();
	}
	
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>componentStatus>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getComponetMsgQos() {
	return componentStatusSubNode.get("qos").asInt();
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>publish
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * 
	 */
	
	public void setpublishSubNote(JsonNode topicsSubNode) {
		publishSubNote= getNestedNode(topicsSubNode, "publish");
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>publish>systemstatus
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return String topic
	 */
	public String getPublishSystemstatusTopic() {
		
		return publishSubNote.get("systemstatus").get("topic").asText().trim();
	}
	/**
	 * Reads and returns the value at 
	 * moduleManager>topics>componentStatus>qos
	 * from json config file. If not specified in json file, 
	 * this will load the default value.
	 * @return int qos
	 */
	public int getPublishSystemstatusQos() {
	return publishSubNote.get("systemstatus").get("qos").asInt();
	}
	
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
	 * @param localComponents jsonArray converter to object
	 * @return List<MMEntity>
	 */
	@SuppressWarnings("unused")
	public void getLocalComponents(JsonNode localComponents) {
		try {
			logger.log(Level.INFO, properties.getProperty("start_json_converter_object") + LocalDateTime.now());
			ObjectMapper mapper = new ObjectMapper();
			listJson = StreamSupport.stream(localComponents.spliterator(), true)
					.map(sObj -> mapper.convertValue(sObj, MMEntity.class)).collect(Collectors.toList());
			logger.log(Level.INFO, properties.getProperty("end_json_converter_object") + LocalDateTime.now());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object jsonToObj(Class<?> aClass, String response) {
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
				.serializeNulls().create();
		Object obj = gson.fromJson(response, aClass);
		return obj;
	}
	public  String objectToJson(Object obj)
    {
        String jsonObject=null;

        try{
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create(); 
            jsonObject = gson.toJson(obj);

        } catch (Exception e){

        }
        return  jsonObject;
    }
	/*
	 * public void getJsonConveterTOObjec(JsonNode js) { try {
	 * logger.log(Level.INFO, properties.getProperty("start_json_converter_object")
	 * + LocalDateTime.now()); ObjectMapper mapper = new ObjectMapper(); listJson =
	 * StreamSupport.stream(localComponents.spliterator(), true) .map(sObj ->
	 * mapper.convertValue(sObj, MMEntity.class)).collect(Collectors.toList());
	 * logger.log(Level.INFO, properties.getProperty("end_json_converter_object") +
	 * LocalDateTime.now());
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 */

	/**
	 * get list objects
	 * 
	 * @return List<MMEntity>
	 */
	public List<MMEntity> getList() {
		return listJson;
	}

	/**
	 * get Properties for load config.properties file
	 * 
	 * @return List<MMEntity>
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param List<MMEntity> listJson base on configuration file(Setting.json) of
	 *                       start Sequence Order the Components
	 * @return List<MMEntity>
	 */
	@SuppressWarnings("unused")
	public List<MMEntity> startSequenceComponentsOrder(List<MMEntity> listJson) {

		if (listJson != null) {
			logger.log(Level.INFO, properties.getProperty("starting_sequence_order")+"  "+ LocalDateTime.now());
			listJson.sort((MMEntity mmentity, MMEntity mmEntity2) -> mmentity.getStartingSequence()
					- mmEntity2.getStartingSequence());
		}
		return listJson;
	}

	/**
	 * @param List<MMEntity> listJson base on configuration file(Setting.json) of
	 *                       Stop Sequence Order the Components
	 * @return List<MMEntity>
	 */
	@SuppressWarnings("unused")
	public List<MMEntity> stopSequenceComponentsOrder(List<MMEntity> listJson) {
		if (listJson != null) {
			logger.log(Level.INFO, properties.getProperty("stopping_sequence_order")+"  "+ LocalDateTime.now());
			listJson.sort((MMEntity mmentity, MMEntity mmEntity2) -> mmentity.getStoppingSequence()
					- mmEntity2.getStoppingSequence());
		}
		return listJson;
	}

	/**
	 * get In memory data for PID ,MMEntity
	 * 
	 * @return Map<Long, MMEntity>
	 */
	public Map<Long, MMEntity> getInMemoryPID() {
		return mapinmemory;
	}

	/**
	 * @param long PID->key , MMEntity > value PID --> get PID for Start Process at
	 *             time of per component store in memory of Map<Long,MMEntity>
	 * @return void
	 */
	public void putMap(Long pid, MMEntity mmEntity) {
		mapinmemory.put(pid, mmEntity);
	}

	public void clearMap() {
		mapinmemory.clear();
	}

	public void newMap() {
		mapinmemory = new ConcurrentHashMap<Long, MMEntity>();
	}

	/**
	 * @param long PID->key , MMEntity > value process destroy for remove old PID
	 *             and create process PID store in memory of Map<Long,MMEntity>
	 * @return void
	 */
	public void removeDestoryPID(Long oldpid, Long newpid, MMEntity mmEntity) {
		if (mapinmemory != null && !mapinmemory.isEmpty()) {
			mapinmemory.entrySet().removeIf(map -> map.getKey() == oldpid);
			mapinmemory.put(newpid, mmEntity);
		}

		/*
		 * if (mapinmemory.containsKey(oldpid)) { mapinmemory.remove(oldpid);
		 * mapinmemory.put(newpid, mmEntity); }
		 */
	}

	public Boolean removeMap(Long PID) {
		if (mapinmemory != null && !mapinmemory.isEmpty()) {

			mapinmemory.remove(PID);
			/*
			 * if(PID>0l) { return mapinmemory.entrySet().removeIf(map -> map.getKey()==
			 * PID);
			 * 
			 * }
			 */
		}
		return false;
	}
	
	

	@SuppressWarnings("deprecation")
	public long getCurrentTimeStamp() {
		
		Date date = new Date();
		Long currentSecond =new Long(date.getTime()/1000);
		return currentSecond;
		/*
		 * Timestamp timestamp = new Timestamp(System.currentTimeMillis()); return
		 * timestamp.getTime();
		 * 
		 */
		
	}

	public long plusSecondInTimeStamp(Timestamp tm, long sec) {
		Instant nextSec = tm.toInstant().plusSeconds(sec);
		Timestamp newTime = Timestamp.from(nextSec);
		return newTime.getTime();
	}

	/**
	 * Reads and returns the value of monitoringInterval from json config file. If
	 * not specified in json file, this will load the default value.
	 * 
	 * @return int MONITORING_INTERVAL
	 */
	public int getMonitoringInterval() {
		final int value = moduleNode.get("monitoringInterval").asInt();
		return value > 0 ? value : MONITORING_INTERVAL;
	}
	/**
	 * Reads and returns the value of monitoringInterval from json config file. If
	 * not specified in json file, this will load the default value.
	 * 
	 * @return int KeepaliveTimeBufferInSe
	 */
	public int getKeepaliveTimeBufferInSec() {
		final int value = moduleNode.get("keepalivetimebufferInSec").asInt();
		return value > 0 ? value : MONITORING_INTERVAL;
	}

	public int getMMModuleID() {
		return moduleNode.get("module_id").asInt();
	}

	public Map<Integer, KeepAlive> getKeepAliveMap() {
		return keepAliveinmemory;
	}

	public void putKeepAlive(Integer moduleId, KeepAlive keepAlive) {

		if (keepAliveinmemory != null && !keepAliveinmemory.isEmpty()) {
			// keepAliveinmemory.entrySet().removeIf(map -> map.getKey()==moduleId);
			if (keepAliveinmemory.containsKey(moduleId)) {
				keepAliveinmemory.replace(moduleId, keepAlive);
			} else {
				keepAliveinmemory.put(moduleId, keepAlive);
			}
		} else {
			keepAliveinmemory = new ConcurrentHashMap<Integer, KeepAlive>();
			keepAliveinmemory.put(moduleId, keepAlive);
		}
	}
	
	
	public void putComponenteStatusMsg(String component , MetricMessage message) {
		if(componentsStatuMemory!=null && !componentsStatuMemory.isEmpty()) {
			if(componentsStatuMemory.containsKey(component)) {
				componentsStatuMemory.remove(component, message);
			}else {
				componentsStatuMemory.put(component, message);
			}
		}else {
			componentsStatuMemory = new ConcurrentHashMap<String,MetricMessage>();
			componentsStatuMemory.put(component, message);
		}
	}
	/**
	 * returns the property level specified in the config file. 
	 * @return Level Space, Restart
	 */
	public  String  getPropertyMessage(int statusmsg) {
		switch (statusmsg) {
		case 1:
			return "Space";
		case 2:
			return "Restart";
		default : return "";
		}
	}
	/**
	 * returns the property level specified in the config file. 
	 * @return Level Disk, Software
	 */
	public  String  getComponentMessage(int statusmsg) {
		switch (statusmsg) {
		case 1:
			return "Disk";
		case 2:
			return "Software";
		default : return "";
		}
	}
	
	/**
	 * returns the property level specified in the config file. 
	 * @return Level OK, LOG
	 */
	public  String  getTypeMessage(int statusmsg) {
		switch (statusmsg) {
		case 1:
			return "Ok";
		case 2:
			return "LOG";
		default : return "";
		}
	}
	
	/**
	 * returns the property level specified in the config file. 
	 * @return Level 
	 */
	public  String  getTextMessage(int statusmsg) {
		switch (statusmsg) {
		/*
		 * case 1: return "Ok"; case 2: return "LOG";
		 */
		default : return "";
		}
	}
	
	public  LogMessage.Builder getStatusLogMsg(String payload, LogMessage.Builder  logMsg) {
	      
	       
	        try {
	        	
	               JsonFormat.parser().merge(payload,logMsg);
	        } catch (InvalidProtocolBufferException e1) {
	              
	               e1.printStackTrace();
	        }
	        return logMsg;
	       
	    }
	public  Metric.Builder getStatusMetricMsg(String payload, Metric.Builder  metricMsg) {
        try {
        	
               JsonFormat.parser().merge(payload,metricMsg);
        } catch (InvalidProtocolBufferException e1) {
              
               e1.printStackTrace();
        }
        return metricMsg;
       
    }
}
