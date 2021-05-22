/**
 * @author selvakumarv
 *
 * 25-03-2021
 *
 *SubscribeToInternalBroker.java
 */
package com.mm.mqconnection;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mm.config.Configuration;
import com.mm.config.GeneralConfig;
import com.mm.config.MMConfig;
import com.mm.entity.KeepAlive;
import com.mm.entity.MMEntity;
import com.mm.inf.ISubscribeToInternalBroker;
import com.mm.service.MonitoringComponentService;
import com.mm.service.StatusOverviewService;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 */

public class SubscribeToInternalBroker implements ISubscribeToInternalBroker {

	private Configuration config;
	private MMConfig mmConfig;
	private GeneralConfig generalConfig;
	private MonitoringComponentService monitoringComponentService;
	private ARSLogger logger;
	private Properties properties;
	private StatusOverviewService statusOverviewService;
	

	public SubscribeToInternalBroker(Configuration configuration,MMConfig mmConfig,GeneralConfig generalConfig) {
		this.config = configuration;
		this.mmConfig = mmConfig;
		this.logger = config.getLogger();
		this.generalConfig = generalConfig;
		
		init();
	}

	private void init() {
		this.monitoringComponentService = new MonitoringComponentService(mmConfig);
		monitoringComponentService.getLogger(config);
		properties = mmConfig.getProperties();
		statusOverviewService = new StatusOverviewService(config,mmConfig);
		logger=config.getLogger();
	}
	@Override
	public void keepAliveMessage(String receivedKeepAlivemsg) {
		KeepAlive keep = (KeepAlive) mmConfig.jsonToObj(KeepAlive.class, receivedKeepAlivemsg);
		if (keep != null) {
			mmConfig.putKeepAlive(keep.getModuleId(), keep);
		}
	}

	@Override
	public void logMessage(String receivedLogmsg) {
		statusOverviewService.receivedLogMessage(receivedLogmsg);
		
	}

	@Override
	public void metericMessage(String receivedMetricmsg) {
		statusOverviewService.receivedMetricMessage(receivedMetricmsg);
		
		
	}

	@Override
	public void componentStatusMessage(String receivedcomponentmsg) {
		statusOverviewService.receivedComponetStatuscMessage(receivedcomponentmsg);
		
	}
	
	public void checkKeepAliveModule(Integer moduleId, KeepAlive keepAlive) {

		if (moduleId>0 && keepAlive!=null) {
						//
		long keepaliveInterval=(keepAlive.getUpdateTime()+mmConfig.getKeepaliveTimeBufferInSec());// (keepAlive.getUpdateTime()+generalConfig.getKeepAliveIntervalInSec());
		long curretntime=mmConfig.getCurrentTimeStamp();
		logger.log(Level.INFO, "curretntime: " +curretntime+" "+" keepalive Interval  "+ keepaliveInterval+"  "+ LocalDateTime.now());
			if ( curretntime > keepaliveInterval) {
				
				
				logger.log(Level.INFO, properties.getProperty("base_on_config_not_receive_msg")+" " + "  moduleId [ "
						+ moduleId+" ] "+ " "+ LocalDateTime.now());
				fetchPIDAndRestart(moduleId);
				
			} else {
				logger.log(Level.SEVERE, properties.getProperty("base_on_config_receive_msg") + "  moduleId [ "
						+ moduleId+" ] "+"  "+ LocalDateTime.now());
			}
		}
	}

	private void fetchPIDAndRestart(Integer moduleId) {
		try {
			MMEntity entity = null;
			Map<Long, MMEntity> mapEntity = mmConfig.getInMemoryPID();
			
			if (mapEntity != null && !mapEntity.isEmpty()) {

				Map<Long, MMEntity> mapList = mapEntity.entrySet().stream()
						.filter(map -> map.getValue().getModuleId() == moduleId)
						.collect(Collectors.toMap(mapz -> mapz.getKey(), map -> map.getValue()));

				if (mapList != null && !mapList.isEmpty()) {

					entity = mapList.entrySet().stream().findFirst().get().getValue();
					Long PID = mapList.entrySet().stream().findFirst().get().getKey();
					
					monitoringComponentService.restartComponent(PID, entity);
				}

			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage() 
					+ moduleId+" " + LocalDateTime.now());
		}

	}

	

	

	
}
