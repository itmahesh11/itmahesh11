/**
 * @author selvakumarv
 *
 
 */
package com.mm.service;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;

import com.ars.ODCC.Connection.ODMessage.ClientId;
import com.ars.ODCC.Connection.ODMessage.ClientId.SubscriberType;
import com.ars.ODCC.Connection.ODMessage.StatusType;
import com.ars.ODCC.Connection.ODMessage.SystemStatus;
import com.ars.ODCC.Connection.ODMessage.SystemStatus.Metric;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.entity.LogMessage;
import com.mm.inf.IPublisherMessageToBroker;
import com.mm.mqconnection.PublisherMessageToBroker;
import com.mm.util.ARSLogger;
import com.mm.util.MessageWriteToFile;

/**
 * @author selvakumarv
 *
 *	2021-04-01
 *
 *StatusOverviewService.java
 */
public class StatusOverviewService {

	private Configuration configuration;
	private MMConfig mmConfig;
	private MessageWriteToFile messageWriteToFile;
	private IPublisherMessageToBroker publisherMessageToBroker;
	private ARSLogger logger;
	private Properties properties;
	
	public StatusOverviewService(Configuration configuration, MMConfig mmConfig) {
		this.configuration=configuration;
		this.mmConfig=mmConfig;
		properties=mmConfig.getProperties();
		
		init();
	}
	private void init() {
		publisherMessageToBroker= new PublisherMessageToBroker();
		messageWriteToFile = new MessageWriteToFile();
		logger= configuration.getLogger();
		messageWriteToFile.setLogger(logger);
		messageWriteToFile.setProperties(properties);
		
		publisherMessageToBroker.setLogger(logger);
		publisherMessageToBroker.setProperties(properties);
	}
	
	public void receivedLogMessage(String value) {
		try {
			//LogMessage logMessage =(LogMessage) mmConfig.jsonToObj(LogMessage.class, value);
			com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.Builder loggMessage =mmConfig.getStatusLogMsg(value, com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.newBuilder());
			if(loggMessage!=null) {
				
				String filePath=mmConfig.getLogMsgFilePath();
				if(filePath!=null) {
					Boolean statu =messageWriteToFile.writeMessage(filePath, value);
					if(statu) {
						String json = getSystemStatusLogMsg(loggMessage.build());//mmConfig.objectToJson(logMessage);
						System.out.println("==>ee444>>>json<><>:"+json+":::"+mmConfig.getPublishSystemstatusTopic());
						
						publisherMessageToBroker.systemStatusMessage(mmConfig.getPublishSystemstatusTopic(),mmConfig.getPublishSystemstatusQos(),json);
					}
				}else {
					
				}
			}
		} catch (Exception e) {
			System.out.println("===="+e.getMessage());
		}
		
		
	
		
	}
	
	private void setValueTooLogMsg(LogMessage mLogMessage) {
		com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.Builder logMessage = com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.newBuilder();
		//logMessage.setClientId(mLogMessage.getClientId());
		logMessage.setCode(mLogMessage.getCode());
		logMessage.setDuration(0);
		logMessage.setType(null);
		logMessage.setMessage(null);
		logMessage.setTimestamp(0);
		//logMessage.set
		
		
	}
	
	public static String getSystemStatusLogMsg(com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage odccLogMsg) {
        ClientId.Builder clientId = ClientId.newBuilder();
        clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
        clientId.setSubscriberOwnerCode("MM");
        clientId.setSerialNumber("ss34134134");
 
        SystemStatus.Builder systemStatus= SystemStatus.newBuilder();
		/*
		 * Metric.Builder metric = Metric.newBuilder(); metric.setClientId(clientId);
		 * metric.setComponent("2"); metric.setComponentIndex("3");
		 * metric.setProperty("4"); metric.setValue(5); metric.setUnit("6");
		 * metric.setTimestampBegin(7); metric.setTimestampEnd(8);
		 */
        
        com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.Builder logMessage = com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.newBuilder();
        logMessage.setClientId(clientId);
        logMessage.setType(StatusType.LOG);
        logMessage.setMessage(odccLogMsg.getMessage());
        logMessage.setDuration(odccLogMsg.getDuration());
        logMessage.setTimestamp(odccLogMsg.getTimestamp());
      //  systemStatus.addMetrics(metric);
        systemStatus.addLogs(logMessage);
        SystemStatus systemStatuss = systemStatus.build();
 
        JsonFormat.Printer jsonprinter = JsonFormat.printer();
 
        String payload = null;
        try {
            payload = jsonprinter.print(systemStatuss);
        } catch (InvalidProtocolBufferException e) {
           
            e.printStackTrace();
        }
 
        System.out.println("System Status");
        System.out.println(payload);
        return payload;
 
    }
	public static String getSystemStatusMetricMsg(com.ars.ODCC.Connection.ODMessage.SystemStatus.Metric odccMetricMsg) {
        ClientId.Builder clientId = ClientId.newBuilder();
        clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
        clientId.setSubscriberOwnerCode("MM");
        clientId.setSerialNumber("ss34134134");
 
        SystemStatus.Builder systemStatus= SystemStatus.newBuilder();
		
		  Metric.Builder metric = Metric.newBuilder();
		  metric.setClientId(clientId);
		  metric.setComponent(odccMetricMsg.getComponent());
		  metric.setComponentIndex(odccMetricMsg.getComponentIndex());
		  metric.setProperty(odccMetricMsg.getProperty());
		  metric.setValue(odccMetricMsg.getValue());
		  metric.setUnit(odccMetricMsg.getUnit());
		  metric.setTimestampBegin(odccMetricMsg.getTimestampBegin()); 
		  metric.setTimestampEnd(odccMetricMsg.getTimestampEnd());
		 
 
        
        
        
        
       
        
        
		/*
		 * com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.Builder logMessage
		 * = com.ars.ODCC.Connection.ODMessage.SystemStatus.LogMessage.newBuilder();
		 * logMessage.setClientId(clientId); logMessage.setType(StatusType.LOG);
		 * logMessage.setMessage(odccLogMsg.getMessage());
		 * logMessage.setDuration(odccLogMsg.getDuration());
		 * logMessage.setTimestamp(odccLogMsg.getTimestamp()); //
		 *  systemStatus.addLogs(logMessage);
		 */
        systemStatus.addMetrics(metric);
        SystemStatus systemStatuss = systemStatus.build();
 
        JsonFormat.Printer jsonprinter = JsonFormat.printer();
 
        String payload = null;
        try {
            payload = jsonprinter.print(systemStatuss);
        } catch (InvalidProtocolBufferException e) {
           
            e.printStackTrace();
        }
 
        System.out.println("System Status");
        System.out.println(payload);
        return payload;
 
    }
	public void receivedMetricMessage(String value) {
		Boolean status =storeMeasurementFile(value);
		if(status) {
			
		}
	}
public void receivedComponetStatuscMessage(String jsonMsg) {
	Boolean status =storeMeasurementFile(jsonMsg);
	if(status) {
		
	}
}

private Boolean storeMeasurementFile(String value) {
	String filePath=mmConfig.getMeteriMsgFilePath();
	Boolean status=false;
	if(filePath!=null) {
		logger.log(Level.INFO, properties.getProperty("start_write_file")+" "+filePath+" "+LocalDateTime.now());
		status =messageWriteToFile.writeMessage(filePath, value);
	}
	return status;
}
public Boolean fetchDeletedOldContentsLogFile() {
	Boolean status=false;
	try {
		String filepath =mmConfig.getLogMsgFilePath();
		if(filepath!=null && !filepath.isEmpty()) {
			status=messageWriteToFile.fetchDeletedOldContents(filepath);

		}
	} catch (Exception e) {
		
	}
	return status;
}
public Boolean fetchDeletedOldContentsMeasurmentFile() {
	Boolean status=false;
	try {
		String filepath =mmConfig.getMeteriMsgFilePath();
		if(filepath!=null && !filepath.isEmpty()) {
			status=messageWriteToFile.fetchDeletedOldContents(filepath);

		}
	} catch (Exception e) {
		
	}
	return status;
}
	
}
