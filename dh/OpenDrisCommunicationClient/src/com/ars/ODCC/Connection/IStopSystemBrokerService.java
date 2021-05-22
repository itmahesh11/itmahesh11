package com.ars.ODCC.Connection;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import com.hivemq.client.mqtt.datatypes.MqttQos;

/**
 * @author Dharanikumar
 *
 */
public interface IStopSystemBrokerService {
	
	public void subscribeToInternalBroker() throws InterruptedException;
	
	public void publishSubscribeResponseMessage(String message);
	
	public void publishTravelInfoMessage(String message);
	
	public void sendSubsriptionTrigger();
	
	public MqttQos getQos(int qos);
	
	public void obtainInternalBrokerConnection() throws InterruptedException ;
	
	public void publishKeepAliveMessage(Integer time);
	
	public Integer getCurrentTimeAsInteger();
	
	public void publishInfoRequestMessage(String message);
	
	public void sendClientID();

	public void publishConnectionStatusMessage(String message);
	
	public void publishClientID(String payload);

}
