package com.ars.ODCC.TestProgram;
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/
import java.util.ArrayList;
import java.util.List;

import com.ars.ODCC.message.ODMessage.ClientId;
import com.ars.ODCC.message.ODMessage.Container;
import com.ars.ODCC.message.ODMessage.GeneralMessage;
import com.ars.ODCC.message.ODMessage.GeneralMessageRemove;
import com.ars.ODCC.message.ODMessage.InfoRequest;
import com.ars.ODCC.message.ODMessage.PassingTimes;
import com.ars.ODCC.message.ODMessage.PublicName;
import com.ars.ODCC.message.ODMessage.InfoRequest.RequestType;
import com.ars.ODCC.message.ODMessage.PassingTimes.Destination;
import com.ars.ODCC.message.ODMessage.PassingTimes.ShowCancelledTrip;
import com.ars.ODCC.message.ODMessage.PassingTimes.TransportType;
import com.ars.ODCC.message.ODMessage.PassingTimes.TripStopStatus;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse.ContentType;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse.ScreenContent;
import com.ars.ODCC.message.ODMessage.StatusOverview;
import com.ars.ODCC.message.ODMessage.StatusOverview.StatusOverviewLine;
import com.ars.ODCC.message.ODMessage.StatusType;
import com.ars.ODCC.message.ODMessage.SystemInfo;
import com.ars.ODCC.message.ODMessage.SystemInfo.SystemInfoLine;
import com.ars.ODCC.message.ODMessage.SystemStatus;
import com.ars.ODCC.message.ODMessage.SystemStatus.LogMessage;
import com.ars.ODCC.message.ODMessage.SystemStatus.Metric;
import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.message.ODMessage.TravelInfo;
import com.ars.ODCC.message.ODMessage.ValueType;
import com.ars.ODCC.message.ODMessage.ClientId.SubscriberType;
import com.ars.ODCC.message.ODMessage.GeneralMessage.GeneralMessageType;
import com.ars.ODCC.message.ODMessage.GeneralMessage.MessagePriority;
import com.ars.ODCC.message.ODMessage.GeneralMessage.ShowOverviewDisplay;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class InfoValues {
	/**
	 * GeneralConfig to access settings.general
	 */
	private static GeneralConfig generalConfig = new GeneralConfig();

	public static void main(String[] args) {

		


		//getSubscriptionMessage();
		//Creation of client
		final Mqtt5BlockingClient client = Mqtt5Client.builder()
				.identifier("SUR_2_ss34134dfdfx44cs")
				.serverHost("localhost")
				.buildBlocking();

		//Connecting the client to server with clean start as it retains the message when the server is disconnected

		client.connect();

		//client.publishWith().topic("SS/out/screencontent").qos(MqttQos.AT_LEAST_ONCE).payload(getScreenContent().getBytes()).send();
		//client.publishWith().topic("SS/out/travelinfo").qos(MqttQos.AT_LEAST_ONCE).payload(getTravelInfo().getBytes()).send();
		client.publishWith().topic("SS/out/Statusoverview").qos(MqttQos.AT_LEAST_ONCE).payload(getStatusOverview().getBytes()).send();
		//client.publishWith().topic("SS/out/Systeminfo").qos(MqttQos.AT_LEAST_ONCE).payload(getSystemInfo().getBytes()).send();
		//client.publishWith().topic("SS/out/systemstatus").qos(MqttQos.AT_LEAST_ONCE).payload(getSystemStatus().getBytes()).send();


		



		//getSubscriptionMessage();
		//createContainerMessage();
	}
	
	public static String getScreenContent() {
		
		ScreenContentResponse.Builder scr = ScreenContentResponse.newBuilder();
		
		ScreenContent.Builder screenContent = ScreenContent.newBuilder();
		screenContent.setContent("3");
		screenContent.setContentType(ContentType.IMAGE_GIF);
		screenContent.setScreenIndex(1);
		screenContent.setTimestamp(123446);
		screenContent.setContent("4");
		screenContent.setContentType(ContentType.IMAGE_GIF);
		screenContent.setScreenIndex(2);
		screenContent.setTimestamp(12344634);
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		scr.setClientId(clientId);
		scr.addScreenContents(screenContent);
		ScreenContentResponse screenRs = scr.build();

		

            JsonFormat.Printer jsonprinter = JsonFormat.printer();

            String payload = null;
			try {
				payload = jsonprinter.print(screenRs);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("ScreenContent Response");

            System.out.println(payload);
		return payload;
	}
	public static String getSystemStatus() {
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");

		SystemStatus.Builder systemStatus= SystemStatus.newBuilder();
		Metric.Builder metric = Metric.newBuilder();
		metric.setClientId(clientId);
		metric.setComponent("2");
		metric.setComponentIndex("3");
		metric.setProperty("4");
		metric.setValue(5);
		metric.setUnit("6");
		metric.setTimestampBegin(7);
		metric.setTimestampEnd(8);

		LogMessage.Builder logMessage = LogMessage.newBuilder();
		logMessage.setClientId(clientId);
		logMessage.setType(StatusType.LOG);
		logMessage.setMessage("4");
		logMessage.setDuration(5);
		logMessage.setTimestamp(123456);
		systemStatus.addMetrics(metric);
		systemStatus.addLogs(logMessage);
		SystemStatus systemStatuss = systemStatus.build();

		JsonFormat.Printer jsonprinter = JsonFormat.printer();

		String payload = null;
		try {
			payload = jsonprinter.print(systemStatuss);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("System Status");
		System.out.println(payload);
		return payload;

	}
	
	
	public static String getTravelInfo() {
		TravelInfo.Builder travelInfo = TravelInfo.newBuilder();
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		travelInfo.setClientId(clientId);

		Container.Builder  objContainer =  Container.newBuilder();

        Destination.Builder objDestination = Destination.newBuilder();
        objDestination.addDestinationName("des1");
        objDestination.addDestinationDetail("destination1");
        objDestination.addDestinationName("des2");
        objDestination.addDestinationDetail("destination2");
       
        PassingTimes.Builder objPassingTimes = PassingTimes.newBuilder();
        objPassingTimes.addPassTimeHash("one");
        objPassingTimes.addTargetArrivalTime( 11);
        objPassingTimes.addTargetDepartureTime( 12);
        objPassingTimes.addExpectedArrivalTime( 13);
        objPassingTimes.addExpectedDepartureTime( 14);
        objPassingTimes.addNumberOfCoaches( 2);
        objPassingTimes.addTripStopStatus( TripStopStatus.PLANNED);
        objPassingTimes.addTransportType( TransportType.BUS);
        objPassingTimes.addWheelchairAccessible(true);
        objPassingTimes.addIsTimingStop(true);
        objPassingTimes.addStopCode("stp1");
        objPassingTimes.addDestinations(objDestination);
        objPassingTimes.addShowCancelledTrip(ShowCancelledTrip.TRUE);
        objPassingTimes.addBlockCode("block1");
        objPassingTimes.addOccupancy(1);
        objPassingTimes.addLinePublicNumber("L01");
        objPassingTimes.addSideCode("side1");
        objPassingTimes.addLineDirection(0);
        objPassingTimes.addLineColor("blue");
        objPassingTimes.addLineTextColor("black");
        objPassingTimes.addLineIcon("icon1");
        objPassingTimes.addDestinationColor("blue");
        objPassingTimes.addDestinationTextColor("black");
        objPassingTimes.addDestinationIcon("Destin2");
        objPassingTimes.addGeneratedTimestamp(41346327);
        objPassingTimes.addJourneyNumber(123);
        objPassingTimes.addPassTimeHash("two");
        objPassingTimes.addTargetArrivalTime( 21);
        objPassingTimes.addTargetDepartureTime( 22);
        objPassingTimes.addExpectedArrivalTime( 23);
        objPassingTimes.addExpectedDepartureTime( 24);
        objPassingTimes.addNumberOfCoaches( 3);
        objPassingTimes.addTripStopStatus( TripStopStatus.PLANNED);
        objPassingTimes.addTransportType( TransportType.BUS);
        objPassingTimes.addWheelchairAccessible(true);
        objPassingTimes.addIsTimingStop(true);
        objPassingTimes.addStopCode("stp1");
        objPassingTimes.addDestinations(objDestination);
        objPassingTimes.addShowCancelledTrip(ShowCancelledTrip.TRUE);
        objPassingTimes.addBlockCode("block1");
        objPassingTimes.addOccupancy(1);
        objPassingTimes.addLinePublicNumber("L01");
        objPassingTimes.addSideCode("side1");
        objPassingTimes.addLineDirection(0);
        objPassingTimes.addLineColor("blue");
        objPassingTimes.addLineTextColor("black");
        objPassingTimes.addLineIcon("icon1");
        objPassingTimes.addDestinationColor("blue");
        objPassingTimes.addDestinationTextColor("black");
        objPassingTimes.addDestinationIcon("Destin2");
        objPassingTimes.addGeneratedTimestamp(41346328);
        objPassingTimes.addJourneyNumber(124);
       

        PublicName.Builder objPublicName = PublicName.newBuilder();
        objPublicName.addStopCode("S1");
        objPublicName.addPublicNamePlace("stop name");
        objPublicName.addPublicNameQuay("Quay1");
        objPublicName.addPublicNameStopPlace("pubstopname");

        GeneralMessage.Builder objGM = GeneralMessage.newBuilder();
        objGM.addMessageHash("GM1");
        objGM.addGeneralmessageType(GeneralMessageType.GENERAL);
        objGM.addMessageContent("First general message");
        objGM.addMessageStartTime(247184);
        objGM.addMessageEndTime(1619347);
        objGM.addGeneratedTimestamp(2384928);
        objGM.addShowOverviewDisplay(ShowOverviewDisplay.ONLY);
        objGM.addMessageTitle("GM title1");
        objGM.addMessagePriority(MessagePriority.COMMERCIAL);
        objGM.addMessageHash("GM2");
        objGM.addGeneralmessageType(GeneralMessageType.GENERAL);
        objGM.addMessageContent("second general message");
        objGM.addMessageStartTime(247184);
        objGM.addMessageEndTime(1619347);
        objGM.addGeneratedTimestamp(2384928);
        objGM.addShowOverviewDisplay(ShowOverviewDisplay.TRUE);
        objGM.addMessageTitle("GM title2");
        objGM.addMessagePriority(MessagePriority.CALAMITY);

        GeneralMessageRemove.Builder objGMR = GeneralMessageRemove.newBuilder();
        objGMR.addMessageHash("fourGM");
        objGMR.addGeneratedTimestamp(34384);
        objContainer.setPassingTimes(objPassingTimes);
        objContainer.setGeneralMessages(objGM);
        objContainer.setPublicNames(objPublicName);
        objContainer.setGeneralMessagesRemove(objGMR);

	
		travelInfo.setTravelInfoContent(objContainer);
		
		 JsonFormat.Printer jsonprinter = JsonFormat.printer();

         String payload = null;
			try {
				payload = jsonprinter.print(travelInfo);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            System.out.println("TravelInfo");

         System.out.println(payload);
		return payload;
		
	}
	public static String getStatusOverview() {
		
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		
		StatusOverview.Builder sts = StatusOverview.newBuilder();
		StatusOverviewLine.Builder line = StatusOverviewLine.newBuilder();
			
		line.setComponent("1");
		line.setProperty("2");
		line.setValueType(ValueType.BYTES_TYPE)	;
		line.setStringValue("4");
		line.setIntValue(5);
		line.setDoubleValue(6);
		line.setBoolType(false);
		line.setBytesValue(ByteString.EMPTY);
		line.setStatus(StatusType.WARNING);

		sts.setClientId(clientId);
		sts.addStatusOverviewLines(line);
		 JsonFormat.Printer jsonprinter = JsonFormat.printer();

		 String payload = null;
			try {
				payload = jsonprinter.print(sts);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

      System.out.println("StatusOverview "+payload);
		return payload;
	}
	
	public static String getSystemInfo() {
	
		SystemInfo.Builder systemInfo = SystemInfo.newBuilder();
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		systemInfo.setClientId(clientId);
		SystemInfoLine.Builder sysinfo = SystemInfoLine.newBuilder();
		sysinfo.setName("1");
		sysinfo.setValueType(ValueType.DOUBLE_TYPE);
		sysinfo.setStringValue("3");
		sysinfo.setIntValue(4);
		sysinfo.setDoubleValue(5);
		sysinfo.setBoolType(false);
		sysinfo.setBytesValue(ByteString.EMPTY);
		systemInfo.addSystemInfoLines(sysinfo);
		 JsonFormat.Printer jsonprinter = JsonFormat.printer();

		 String payload = null;
			try {
				payload = jsonprinter.print(systemInfo);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

      System.out.println("SSystemInfo ");
      System.out.println(payload);
		return payload;
	}
}
