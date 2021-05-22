package com.ars.ODCC.message;
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/
import com.ars.ODCC.message.ODMessage.Container;
import com.ars.ODCC.message.ODMessage.InfoRequest;
import com.ars.ODCC.message.ODMessage.ScreenContentResponse;
import com.ars.ODCC.message.ODMessage.StatusOverview;
import com.ars.ODCC.message.ODMessage.Subscribe;
import com.ars.ODCC.message.ODMessage.SubscriptionResponse;
import com.ars.ODCC.message.ODMessage.SystemInfo;
import com.ars.ODCC.message.ODMessage.SystemStatus;
import com.ars.ODCC.message.ODMessage.TravelInfo;
import com.ars.ODCC.message.ODMessage.Unsubscribe;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

public class ODMessageHandler {
	
	
	
	public  Subscribe parseSubscribeToJson(String jsonString) { 
		Subscribe.Builder
		messageBuilder = null; try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = Subscribe.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	public  Unsubscribe parseUnsubscribeToJson(String jsonString) { 
		Unsubscribe.Builder
		messageBuilder = null; try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = Unsubscribe.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	public  SystemInfo parseSystemInfoToJson(String jsonString) { 
		SystemInfo.Builder	messageBuilder = null; 
		try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = SystemInfo.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	public  StatusOverview parseStatusOverviewToJson(String jsonString) { 
		StatusOverview.Builder
		messageBuilder = null; try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = StatusOverview.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	public  TravelInfo parseTravelInfoToJson(String jsonString) { 
		TravelInfo.Builder
		messageBuilder = null;
		try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = TravelInfo.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} 
		return messageBuilder.build();
	}
	
	public  ScreenContentResponse parseScreenContentToJson(String jsonString) { 
		ScreenContentResponse.Builder
		messageBuilder = null; try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = ScreenContentResponse.newBuilder();

			jsonParser.merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	public  SystemStatus parseSystemStatusToJson(String jsonString) { 
		SystemStatus.Builder
		messageBuilder = null; try {
			JsonFormat.Parser jsonParser=JsonFormat.parser();
			messageBuilder = SystemStatus.newBuilder();

			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(
					jsonString, messageBuilder); 
		} catch(Exception e ){
			System.out.println("Exception e"+ e.toString()); 
		} return messageBuilder.build();
	}
	
	
	
	 

	public  String parseSubscriptionRequestProtoBufMessage( SubscriptionResponse subscriptionResponse ) {
		String jsonString = null; 
		if (subscriptionResponse == null) {
			System.out.println("Protobuf message was null");
		} else {
			try {
				JsonFormat.Printer  jsonFormat = JsonFormat.printer();
				jsonString = jsonFormat.print(subscriptionResponse);
			} catch (Exception e) {
				throw new RuntimeException("Error deserializing protobuf to json", e);
			}
		}

		return jsonString;
	}
	public String parseTravelInformationProtoBufMessage(Container travelInformation) {
		String jsonString = null; 
		if (travelInformation == null) {
			System.out.println("Protobuf message was null");
		} else {
			try {
				JsonFormat.Printer  jsonFormat = JsonFormat.printer();
				jsonString = jsonFormat.print(travelInformation);
			} catch (Exception e) {
				throw new RuntimeException("Error deserializing protobuf to json", e);
			}
		}

		return jsonString;
	}
	public String parseInfoRequestProtoBufMessage(InfoRequest infoRequest) {
		String jsonString = null; 
		if (infoRequest == null) {
			System.out.println("Protobuf message was null");
		} else {
			try {
				JsonFormat.Printer  jsonFormat = JsonFormat.printer();
				jsonString = jsonFormat.print(infoRequest);
			} catch (Exception e) {
				throw new RuntimeException("Error deserializing protobuf to json", e);
			}
		}

		return jsonString;
	}


}
