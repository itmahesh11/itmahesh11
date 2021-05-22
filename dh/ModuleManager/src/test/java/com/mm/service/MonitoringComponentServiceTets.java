/**
 * @author selvakumarv
 *
 * 24-03-2021
 *
 *MonitoringComponentServiceTets.java
 */
package com.mm.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;

/**
 * @author selvakumarv
 *
 */
public class MonitoringComponentServiceTets {

	private Configuration config;
	private MMConfig mMConfig;
	IModuleManagerProcess monitor;

	@Before
	public void setup() {
		config = Configuration.getInstance();
		mMConfig = config.mmConfig;
	}

	@Test
	public void monitoringTest() {
		monitor = new MonitoringComponentService(mMConfig);
		monitor.getLogger(config);
		monitor.isAliveAllComponents();
	}

	@Test
	public void monitoringOneComponetsTest() {
		if (monitor == null) {
			monitor = new MonitoringComponentService(mMConfig);
		}
		monitor.getLogger(config);
		//passing PID
		Boolean monitoring = monitor.isAliveOneComponent(5436, null);
		assertEquals(true, monitoring);
	}
}
