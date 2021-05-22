package com.mm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.entity.KeepAlive;
import com.mm.entity.MMEntity;
import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 *         22-03-2021
 *
 *         StartComponentService.java
 */
public class StartComponentService implements IModuleManagerProcess {

	private MMConfig mmConfig;
	private Properties properties;

	/**
	 * logger for logging the messages
	 */
	private ARSLogger logger;// = App.getLogger();

	public StartComponentService(MMConfig mmConfig) {
		this.mmConfig = mmConfig;
		this.properties = mmConfig.getProperties();
	}

	@Override
	public void getLogger(Configuration configuration) {
		logger = configuration.getLogger();

	}

	/**
	 * base on the config file(Setting.json) of startSequenceComponentsOrder
	 * 
	 * @return void
	 */
	@Override
	public Boolean startComponents() {
		Boolean statu=false;
		try {
			logger.log(Level.INFO, properties.getProperty("start_componet")+"  "+ LocalDateTime.now());
			List<MMEntity> mmEntityList = mmConfig.startSequenceComponentsOrder(mmConfig.getList());
			starComponent(mmEntityList);
			statu=true;
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage()+"  "+ LocalDateTime.now());
			statu=false;
		}
		return statu;
	}

	/**
	 * @param List<MMEntity> one by one execute jar
	 * @return void
	 */
	private void starComponent(List<MMEntity> mmEntityList) {
		try {
			if (mmEntityList != null && !mmEntityList.isEmpty()) {
				mmConfig.newMap();
				for (MMEntity mmEntity : mmEntityList) {
					executeJar(mmEntity, 0l);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param List<MMEntity> , long PID
	 *
	 *                       one by one execute jar after execute jar , Store in
	 *                       memory PID, mmEntity PID > old pid for restart /reboot
	 * @return Boolean true > successful execute jar false > fail execute jar
	 */
	@SuppressWarnings("deprecation")
	public Boolean executeJar(MMEntity mmEntity, long oldPID) {
		Boolean statu = false;
		final List<String> actualArgs = new ArrayList<String>();
		try {
			logger.log(Level.INFO,
					properties.getProperty("jar_load_meg")+"  "+ mmEntity.getComponentInstanceName()+"  " + LocalDateTime.now());
			String firstStr = properties.getProperty("jar_command_order_first");
			String secondStr = properties.getProperty("jar_command_order_second");
			String thirdStr = properties.getProperty("jar_command_order_thrid");
			if (firstStr != null && secondStr != null && thirdStr != null) {
				int firstnumber = Integer.parseInt(firstStr);
				int secondnumber = Integer.parseInt(secondStr);
				int thirdnumber = Integer.parseInt(thirdStr);
				actualArgs.add(firstnumber, properties.getProperty("java_command"));
				actualArgs.add(secondnumber, properties.getProperty("jar_command"));
				actualArgs.add(thirdnumber, mmEntity.getComponentExePath());

				Runtime re = Runtime.getRuntime();
				Process command = re.exec(actualArgs.toArray(new String[0]));
				Long pid = new Long(command.pid());
				statu = command.isAlive();

				if (oldPID > 0l) {
					mmConfig.removeDestoryPID(oldPID, pid, mmEntity);
				} else {
					mmEntity.setStartTimeStamp(mmConfig.getCurrentTimeStamp());
					mmConfig.putMap(pid, mmEntity);
					KeepAlive keepAlive = new KeepAlive();
					keepAlive.setModuleId(mmEntity.getModuleId());
					keepAlive.setUpdateTime(mmConfig.getCurrentTimeStamp());
					mmConfig.putKeepAlive(mmEntity.getModuleId(), keepAlive);
				}

				logger.log(Level.INFO, properties.getProperty("jar_load_done") + ":PID [" + String.valueOf(pid) + "] :"
						+mmEntity.getComponentInstanceName()+"  "+LocalDateTime.now());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statu;
	}
}
