/**
 * @author selvakumarv
 *
 * 24-03-2021
 *
 *StartComponentServiceTest.java
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
public class StartComponentServiceTest {
	
	private Configuration config;
	private MMConfig mMConfig;
	@Before
	public void setup() {
		config= Configuration.getInstance();
		mMConfig =config.mmConfig;
	}
	@Test
	public void startTest()  {
		StartComponentService start = new StartComponentService(mMConfig);
	start.getLogger(config);
	mMConfig.getLocalComponents(mMConfig.localComponentSubNode);
	  
	Boolean statu= start.startComponents();
	assertEquals(true, statu);
	}
}
