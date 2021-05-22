package com.ars.ssm.connection;

import java.util.Arrays;
import java.util.Date;
import com.ars.ODCC.Connection.ODMessage.ClientId;
import com.ars.ODCC.Connection.ODMessage.ClientId.SubscriberType;
import com.ars.ODCC.Connection.ODMessage.InfoRequest;
import com.ars.ODCC.Connection.ODMessage.ScreenContentResponse.ScreenContent;
import com.ars.ODCC.Connection.ODMessage.StatusOverview;
import com.ars.ODCC.Connection.ODMessage.Subscribe;
import com.ars.ODCC.Connection.ODMessage.Subscribe.DisplayProperties;
import com.ars.ODCC.Connection.ODMessage.Subscribe.DisplayProperties.DestinationDetermination;
import com.ars.ODCC.Connection.ODMessage.Subscribe.FieldFilter;
import com.ars.ODCC.Connection.ODMessage.Subscribe.FieldFilter.Delivery;
import com.ars.ODCC.Connection.ODMessage.Subscribe.FilterParameters;
import com.ars.ODCC.Connection.ODMessage.SubscriptionResponse.Status;
import com.ars.ODCC.Connection.ODMessage.SystemInfo;
import com.ars.ODCC.Connection.ODMessage.SystemStatus;
import com.ars.ODCC.Connection.ODMessage.TravelInfo;
import com.ars.ODCC.Connection.ODMessage.Unsubscribe;
import com.ars.ssm.configuration.Configuration;

public class PublishMessageHandler {

	
	public static Subscribe getSubscriptionMessage() {
		Subscribe.Builder subscribe = Subscribe.newBuilder();
		subscribe.setClientId(getClientIdObject());
		InfoRequest.Builder s=InfoRequest.newBuilder();
		//subscribe.addAllStopCode(Arrays.asList(new String[]{"NL:Q:50000120","NL:Q:50000121","NL:Q:50000122"}));
		//String[] sQuayCode=getQuayCode(Configuration.getInstance().generalConfig.getSystemInfoQuayCodes().toString().split(","));
		subscribe.addAllStopCode(Arrays.asList(getQuayCode(Configuration.getInstance().generalConfig.getSystemInfoQuayCodes().toString().split(","))));
		subscribe.setDescription("");
		subscribe.setEmail("");
		subscribe.setDisplayProperties(getDisplayPropertiesObject());
		subscribe.setFilterParameters(getFilterParametersObject());
		subscribe.setFieldFilter(getFieldFilterObject());
		Subscribe objSubscribe = subscribe.build();
		return objSubscribe;
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
		clientId.setSubscriberOwnerCode(Configuration.getInstance().generalConfig.getSystemInfoSubscriberOwnerCode());
		clientId.setSerialNumber(Configuration.getInstance().generalConfig.getSystemInfoSerialNumber());
		return clientId.build();
	}
	public static DisplayProperties getDisplayPropertiesObject() {
		DisplayProperties.Builder displayProperties = DisplayProperties.newBuilder();
		displayProperties.setTextCharacters(5);
		displayProperties.setDestinationDetermination(DestinationDetermination.SELF_DETERMINING);
		displayProperties.setOverviewDisplay(true);
		DisplayProperties displayPropertiesObj = displayProperties.build();
		return displayPropertiesObj;
	}
	public static FilterParameters getFilterParametersObject() {
		FilterParameters.Builder filterParameters = FilterParameters.newBuilder();
		filterParameters.setFilterOn(true);
		filterParameters.setWaitingtimeLow(1);
		filterParameters.setWaitingtimeHigh(1);
		filterParameters.setPercentageLow(1);
		filterParameters.setPercentageHigh(1);
		FilterParameters filterParametersObj = filterParameters.build();
		return filterParametersObj;
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
	public static String[] getQuayCode(String[] sQCode) {
		for(int i=0;i<sQCode.length;i++) {
			sQCode[i]=sQCode[i].replace("\"","").replace("[","").replace("]","");
		}
		return sQCode;
	}
}
