/**
 * @author selvakumarv
 *
  */
package com.mm.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.entity.MMEntity;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv 23-03-2021
 *
 *         MonitoringComponentService.java
 */
public class MonitoringComponentService implements IModuleManagerProcess {

	private MMConfig mmConfig;
	private Properties properties;

	/**
	 * logger for logging the messages
	 */
	private ARSLogger logger;

	private StartComponentService startComponentService;

	public MonitoringComponentService(MMConfig mmConfig) {
		this.mmConfig = mmConfig;
		this.properties = mmConfig.getProperties();
		startComponentService = new StartComponentService(mmConfig);

	}

	@Override
	public void getLogger(Configuration configuration) {
		logger = configuration.getLogger();
		startComponentService.getLogger(configuration);
	}

	@Override
	public Boolean isAliveAllComponents() {
		Boolean status = false;
		try {
			Map<Long, MMEntity> pidMap = mmConfig.getInMemoryPID();
			logger.log(Level.INFO, properties.getProperty("monitoring_component")+"  "+LocalDateTime.now());
			if (pidMap != null && !pidMap.isEmpty()) {
				pidMap.forEach((k, v) -> isAliveOneComponent(k, v));
			} else {
				logger.log(Level.INFO, properties.getProperty("stop_all_components")+ "  " +LocalDateTime.now());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage() + " " +LocalDateTime.now());
		}
		return status;
	}

	@Override
	public Boolean isAliveOneComponent(long pid, MMEntity mmEntity) {
		Boolean isAlive = false;
		try {
			Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
			if (!processHandle.isEmpty()) {
				isAlive = processHandle.get().isAlive();
				if (isAlive) {
					if (mmEntity != null) {
						logger.log(Level.INFO, properties.getProperty("alive")+"  "+ " PID [ " + pid + " ] :"
								+ mmEntity.getComponentInstanceName()+"  "+LocalDateTime.now());
					}
				} else {

					if (mmEntity != null) {
						// restart component
						logger.log(Level.INFO, properties.getProperty("restart_component")+"  "+" PID [ " + pid + " ] "
								+ mmEntity.getComponentInstanceName()+"  "+ LocalDateTime.now());
						isAlive = startComponentService.executeJar(mmEntity, pid);
					}
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage() + " " + LocalDateTime.now());
		}
		return isAlive;
	}

	@Override
	public Boolean restartComponent(long pid, MMEntity mmEntity) {
		Boolean restart = false;
		try {
			if (pid > 0l && mmEntity != null) {
				Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
				if (!processHandle.isEmpty()) {

					processHandle.get().destroyForcibly();
					mmConfig.removeMap(pid);
					logger.log(Level.INFO, properties.getProperty("stop_component")+"  "+ " PID [ " + pid + " ] "
							+ mmEntity.getComponentInstanceName()+"  "+LocalDateTime.now());
				}
				logger.log(Level.INFO, properties.getProperty("restart_component")+"  "+ " PID [ " + pid + " ] "
						+ mmEntity.getComponentInstanceName()+"  "+ LocalDateTime.now());
				restart = startComponentService.executeJar(mmEntity, pid);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage() + " " + LocalDateTime.now());
		}
		return restart;
	}
}
