/**
 * @author selvakumarv
 *
 * 24-03-2021
 *
 *StopComponentServiceTest.java
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
public class StopComponentServiceTest {

	private Configuration config;
	private MMConfig mMConfig;
	@Before
	public void setup() {
		config= Configuration.getInstance();
		mMConfig =config.mmConfig;
	}
	
	
	@Test
	public void stopOneComponentTest() {
		StopComponentService stopService = new StopComponentService(mMConfig);
		stopService.getLogger(config);
		
	Boolean statu=stopService.startComponents();
		assertEquals(true, statu);
		//passing PID
		//stopService.stopComponent(5467L);

	}
}
