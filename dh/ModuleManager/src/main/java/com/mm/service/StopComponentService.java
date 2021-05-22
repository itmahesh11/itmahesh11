/**
 * @author selvakumarv
 *
  */
package com.mm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.entity.MMEntity;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 *         24-03-2021 StopComponentService.java
 */
public class StopComponentService implements IModuleManagerProcess {

	private MMConfig mmConfig;
	private MonitoringComponentService monitoringComponentService;
	private Properties properties;
	/**
	 * logger for logging the messages
	 */
	private ARSLogger logger;

	public StopComponentService(MMConfig mmConfig) {
		this.mmConfig = mmConfig;
		this.properties = mmConfig.getProperties();
		monitoringComponentService = new MonitoringComponentService(mmConfig);

	}

	@Override
	public void getLogger(Configuration configuration) {
		logger = configuration.getLogger();
		monitoringComponentService.getLogger(configuration);

	}

	/**
	 * base on the config file(Setting.json) for json to list data Sequence order
	 * for list data
	 * 
	 * @return void
	 */
	@Override
	public Boolean stopComponents() {
		Boolean status=false;
		try {
			if (mmConfig != null) {
				List<MMEntity> listMM = mmConfig.getList();

				if (listMM != null && !listMM.isEmpty()) {
					logger.log(Level.INFO, properties.getProperty("start_stop_component")+"  " + LocalDateTime.now());
					List<MMEntity> listMMOrder = mmConfig.stopSequenceComponentsOrder(listMM);
					if (listMMOrder != null && !listMMOrder.isEmpty()) {
						listMMOrder.forEach((V) -> {
							fetchPID(V);
						});
						status=true;
					}

				}
			}
		} catch (Exception e) {
			status=false;
			logger.log(Level.SEVERE, e.getMessage() + LocalDateTime.now());
		} finally {
			getModuleManagerExit();
		}
		return status;
	}

	/**
	 * @param MMEntity InMomery map for PID, MMEntity it check instance Name for
	 *                 inMemory map
	 * @return void
	 */
	private void fetchPID(MMEntity mmEntity) {

		if (mmConfig != null) {
			Map<Long, MMEntity> mapList = mmConfig.getInMemoryPID();
			if (mapList != null && !mapList.isEmpty()) {
				mapList.forEach((K, V) -> {
					if (V.getComponentInstanceName().equalsIgnoreCase(mmEntity.getComponentInstanceName())) {

						stopComponent(K);
						return;
					};
				});
			}
		}

	}

	// checkAliveProcess
	/**
	 * @param PID check alive in the PID -process true the process is destroy false
	 *            the process already destroy
	 * @return void
	 */
	public Boolean stopComponent(Long PID) {
		Boolean statu=false;
		try {
			Boolean status = monitoringComponentService.isAliveOneComponent(PID.longValue(), null);
			if (status) {
				Optional<ProcessHandle> processHandle = ProcessHandle.of(PID);
				if (!processHandle.isEmpty()) {
					processHandle.get().destroyForcibly();
					mmConfig.removeMap(PID);
					logger.log(Level.INFO,  properties.getProperty("stop_component")+" "+" PID [ " + PID +" ] "
							+" "+ LocalDateTime.now());
					statu=true;
				}
			}
		} catch (Exception e) {
			statu=false;
			logger.log(Level.SEVERE, e.getMessage() +" "+ LocalDateTime.now());
		}
		return statu;
	}

	public void getModuleManagerExit() {
		logger.log(Level.INFO, properties.getProperty("module_manager_exit")+" "+ LocalDateTime.now());
		System.exit(0);
	}
}
