/**
 * @author selvakumarv
 *
 * 29-03-2021
 *
 *StopSystemBrokerManagerTest.java
 */
package com.mm.service.connect;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.mqconnection.StopSystemBrokerManager;

/**
 * @author selvakumarv
 *
 */
public class StopSystemBrokerManagerTest {
	
	
	private @NotNull StopSystemBrokerManager stopSystemBrokerManager;
	private @NotNull Configuration config;
	private @NotNull MMConfig mMConfig;
	@Before
	public void setup() throws JsonProcessingException, IOException {
		config = new Configuration();
		mMConfig =config.mmConfig;
		mMConfig.getLogger(config);
		stopSystemBrokerManager = new StopSystemBrokerManager(config, mMConfig);
		//subscribeToInternalBroker = new SubscribeToInternalBroker(config, mMConfig, stopSystemBrokerManager.generalConfig);
	}
	@Test
	public void testConnection() throws InterruptedException {
		Boolean isconnected=stopSystemBrokerManager.obtainInternalBrokerConnection();
		assertEquals(true,isconnected);
		stopSystemBrokerManager.subscribeToInternalBroker();
		//subscribeToInternalBroker.keepAliveMessage(null);
		
	}
	/*
	 * @After public void doneTest() { stopSystemBrokerManager. }
	 */

}
