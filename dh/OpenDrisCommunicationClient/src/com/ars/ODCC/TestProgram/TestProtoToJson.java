package com.ars.ODCC.TestProgram;
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import java.util.Arrays;
import java.util.Date;

import com.ars.ODCC.message.ODMessage.ClientId;
import com.ars.ODCC.message.ODMessage.ClientId.SubscriberType;
import com.ars.ODCC.message.ODMessage.Container;
import com.ars.ODCC.message.ODMessage.GeneralMessage;
import com.ars.ODCC.message.ODMessage.GeneralMessage.GeneralMessageType;
import com.ars.ODCC.message.ODMessage.GeneralMessage.MessagePriority;
import com.ars.ODCC.message.ODMessage.GeneralMessage.ShowOverviewDisplay;
import com.ars.ODCC.message.ODMessage.GeneralMessageRemove;
import com.ars.ODCC.message.ODMessage.PassingTimes;
import com.ars.ODCC.message.ODMessage.PassingTimes.Destination;
import com.ars.ODCC.message.ODMessage.PassingTimes.ShowCancelledTrip;
import com.ars.ODCC.message.ODMessage.PassingTimes.TransportType;
import com.ars.ODCC.message.ODMessage.PassingTimes.TripStopStatus;
import com.ars.ODCC.message.ODMessage.PublicName;
import com.ars.ODCC.message.ODMessage.Subscribe;
import com.ars.ODCC.message.ODMessage.Subscribe.DisplayProperties;
import com.ars.ODCC.message.ODMessage.Subscribe.DisplayProperties.Builder;
import com.ars.ODCC.message.ODMessage.Subscribe.DisplayProperties.DestinationDetermination;
import com.ars.ODCC.message.ODMessage.Subscribe.FieldFilter;
import com.ars.ODCC.message.ODMessage.Subscribe.FieldFilter.Delivery;
import com.ars.ODCC.message.ODMessage.Subscribe.FilterParameters;
import com.ars.ODCC.message.ODMessage.SubscriptionResponse;
import com.ars.ODCC.message.ODMessage.SubscriptionResponse.Status;
import com.ars.ODCC.message.ODMessage.Unsubscribe;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class TestProtoToJson {

	public static void main(String[] args) {
		
		  SubscriptionResponse.Builder subresponse = SubscriptionResponse.newBuilder();
		  
		  subresponse.setSuccess(true);
		  
		  subresponse.setStatus(Status.DATA_CHANGED);
		  
		  subresponse.setTimestamp(12345);
		  
		  
		  
		  SubscriptionResponse objRespone = subresponse.build();
		 

		//getSubscriptionMessage();
		//Creation of client
		final Mqtt5BlockingClient client = Mqtt5Client.builder()
				.identifier("SUR_2_ss34134dfdf")
				.serverHost("localhost")
				.buildBlocking();

		//Connecting the client to server with clean start as it retains the message when the server is disconnected

		client.connect();

		client.publishWith().topic("subscription_response/2/2/SUR/34134134").qos(MqttQos.AT_LEAST_ONCE).payload(objRespone.toByteArray()).send();
		client.publishWith().topic("travel_information/1/2/SUR/34134134").qos(MqttQos.AT_LEAST_ONCE).payload(createContainerMessage().toByteArray()).send();
		client.publishWith().topic("SS/out/subscribe").qos(MqttQos.AT_LEAST_ONCE).payload(getSubscriptionMessage().toByteArray()).send();

		//getSubscriptionMessage();
		//createContainerMessage();
	}
	public static Container createContainerMessage()

	{
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
		Container objRespone = objContainer.build();
		return objRespone;

	}
	public static Subscribe getSubscriptionMessage() {
		Subscribe.Builder subscribe = Subscribe.newBuilder();
		
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		
		DisplayProperties.Builder displayProperties = DisplayProperties.newBuilder();
		displayProperties.setTextCharacters(5);
		displayProperties.setDestinationDetermination(DestinationDetermination.MAX_CHARACTERS);
		displayProperties.setOverviewDisplay(true);
		
		FilterParameters.Builder filterParameters = FilterParameters.newBuilder();
		filterParameters.setFilterOn(true);
		filterParameters.setWaitingtimeLow(10);
		filterParameters.setWaitingtimeHigh(20);
		filterParameters.setPercentageLow(30);
		filterParameters.setPercentageHigh(40);
		
		FieldFilter.Builder fieldFilter = FieldFilter.newBuilder();
		fieldFilter.setTargetArrivalTime(Delivery.ALWAYS);
		fieldFilter.setTargetDepartureTime(Delivery.ALWAYS);
		fieldFilter.setExpectedArrivalTime(Delivery.ALWAYS);
		fieldFilter.setExpectedDepartureTime(Delivery.ALWAYS);
		fieldFilter.setNumberOfCoaches(Delivery.ALWAYS);
		fieldFilter.setTripStopStatus(Delivery.ALWAYS);
		fieldFilter.setWheelchairAccessible(Delivery.ALWAYS);
		fieldFilter.setTransportType(Delivery.ALWAYS);
		fieldFilter.setIsTimingStop(Delivery.ALWAYS);
		fieldFilter.setStopCode(Delivery.ALWAYS);
		fieldFilter.setDestinations(Delivery.ALWAYS);
		fieldFilter.setShowCancelledTrip(Delivery.ALWAYS);
		fieldFilter.setBlockCode(Delivery.ALWAYS);
		fieldFilter.setOccupancy(Delivery.ALWAYS);
		fieldFilter.setLinePublicNumber(Delivery.ALWAYS);
		fieldFilter.setSideCode(Delivery.ALWAYS);
		fieldFilter.setLineDirection(Delivery.ALWAYS);
		fieldFilter.setLineColor(Delivery.ALWAYS);
		fieldFilter.setLineTextColor(Delivery.ALWAYS);
		fieldFilter.setLineIcon(Delivery.ALWAYS);
		fieldFilter.setDestinationColor(Delivery.ALWAYS);
		fieldFilter.setDestinationTextColor(Delivery.ALWAYS);
		fieldFilter.setDestinationIcon(Delivery.ALWAYS);
		fieldFilter.setGeneratedTimestamp(Delivery.ALWAYS);
		fieldFilter.setJourneyNumber(Delivery.ALWAYS);
		
		
		subscribe.setClientId(clientId);
		subscribe.addAllStopCode(Arrays.asList(new String[]{"NL:Q:50000120","NL:Q:50000121","NL:Q:50000122"}));
		subscribe.setDescription("");
		subscribe.setEmail("");
		subscribe.setDisplayProperties(displayProperties);
		subscribe.setFilterParameters(filterParameters);
		subscribe.setFieldFilter(fieldFilter);
		Subscribe objSubscribe = subscribe.build();
		
		/*
		 * JsonFormat.Printer jsonFormat = JsonFormat.printer(); String jsonString =
		 * null; try { jsonString = jsonFormat.print(objSubscribe); } catch
		 * (InvalidProtocolBufferException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } System.out.println(jsonString);
		 */		return objSubscribe;
	}


	public static Unsubscribe getUnsubscriptionMessage() {
		Unsubscribe.Builder unsubscribe = Unsubscribe.newBuilder();
		unsubscribe.setClientId(getClientIdObject());
		unsubscribe.setIsPermanent(true);
		unsubscribe.setTimestamp(getCurrentTimeStamp());
		Unsubscribe unsubscribeObj = unsubscribe.build();
		return unsubscribeObj;
	}
	public static  int getCurrentTimeStamp() {
		return (int) (new Date().getTime()/1000);
	}
	public static ClientId getClientIdObject() {
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberTypeValue(SubscriberType.HALTESYSTEEM_VALUE);
		clientId.setSubscriberOwnerCode("SUR");
		clientId.setSerialNumber("ss34134134");
		return clientId.build();
	}
	public static Builder getDisplayPropertiesObject() {
		DisplayProperties.Builder displayProperties = DisplayProperties.newBuilder();
		displayProperties.setTextCharacters(0);
		displayProperties.setDestinationDetermination(DestinationDetermination.MAX_CHARACTERS);
		displayProperties.setOverviewDisplay(false);
		//DisplayProperties displayPropertiesObj = displayProperties.build();
		//System.out.println("obj disp -> "+displayPropertiesObj);
		return displayProperties;
	}
	public static com.ars.ODCC.message.ODMessage.Subscribe.FilterParameters.Builder getFilterParametersObject() {
		FilterParameters.Builder filterParameters = FilterParameters.newBuilder();
		filterParameters.setFilterOn(false);
		filterParameters.setWaitingtimeLow(0);
		filterParameters.setWaitingtimeHigh(0);
		filterParameters.setPercentageLow(0);
		filterParameters.setPercentageHigh(0);
		//FilterParameters filterParametersObj = filterParameters.build();
		return filterParameters;
	}
	public static FieldFilter getFieldFilterObject() {
		FieldFilter.Builder fieldFilter = FieldFilter.newBuilder();
		fieldFilter.setTargetArrivalTime(Delivery.ALWAYS);
		fieldFilter.setTargetDepartureTime(Delivery.ALWAYS);
		fieldFilter.setExpectedArrivalTime(Delivery.ALWAYS);
		fieldFilter.setExpectedDepartureTime(Delivery.ALWAYS);
		fieldFilter.setNumberOfCoaches(Delivery.ALWAYS);
		fieldFilter.setTripStopStatus(Delivery.ALWAYS);
		fieldFilter.setWheelchairAccessible(Delivery.ALWAYS);
		fieldFilter.setTransportType(Delivery.ALWAYS);
		fieldFilter.setIsTimingStop(Delivery.ALWAYS);
		fieldFilter.setStopCode(Delivery.ALWAYS);
		fieldFilter.setDestinations(Delivery.ALWAYS);
		fieldFilter.setShowCancelledTrip(Delivery.ALWAYS);
		fieldFilter.setBlockCode(Delivery.ALWAYS);
		fieldFilter.setOccupancy(Delivery.ALWAYS);
		fieldFilter.setLinePublicNumber(Delivery.ALWAYS);
		fieldFilter.setSideCode(Delivery.ALWAYS);
		fieldFilter.setLineDirection(Delivery.ALWAYS);
		fieldFilter.setLineColor(Delivery.ALWAYS);
		fieldFilter.setLineTextColor(Delivery.ALWAYS);
		fieldFilter.setLineIcon(Delivery.ALWAYS);
		fieldFilter.setDestinationColor(Delivery.ALWAYS);
		fieldFilter.setDestinationTextColor(Delivery.ALWAYS);
		fieldFilter.setDestinationIcon(Delivery.ALWAYS);
		fieldFilter.setGeneratedTimestamp(Delivery.ALWAYS);
		fieldFilter.setJourneyNumber(Delivery.ALWAYS);
		FieldFilter fieldFilterObj = fieldFilter.build();
		return fieldFilterObj;
	}




}
