package com.ars.ODCC.Connection;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import com.ars.ODCC.message.ODMessage.ScreenContentResponse;
import com.ars.ODCC.message.ODMessage.StatusOverview;
import com.ars.ODCC.message.ODMessage.Subscribe;
import com.ars.ODCC.message.ODMessage.SystemInfo;
import com.ars.ODCC.message.ODMessage.SystemStatus;
import com.ars.ODCC.message.ODMessage.TravelInfo;
import com.ars.ODCC.message.ODMessage.Unsubscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.internal.util.AsyncRuntimeException;

public interface IOpenDrisBrokerService {

	public void subscribeToExternalBroker() throws InterruptedException, InvalidProtocolBufferException;
	
	public void obtainExternalBrokerConnection(String brokerUrl, int port) throws AsyncRuntimeException, InvalidProtocolBufferException, InterruptedException;

	public void setRetryCount(String brokerURL);

	public  void handleConnectionToCB();
	
	public void publishSubscribeMessage(Subscribe subscribeMessage);
	
	public void publishUnsubscribeMessage(Unsubscribe unsubscribeMessage);
	
	public void publishSystemInfoMessage(SystemInfo systemInfoMessage);
	
	public void publishStatusOverviewMessage(StatusOverview statusOverviewMessage);
	
	public void publishSystemStatusMessage(SystemStatus systemStatusMessage);
	
	public void publishScreenContentMessage(ScreenContentResponse screenContentMessage);
	
	public void publishTravelInfoMessage(TravelInfo TravelInfoMessage);


}
