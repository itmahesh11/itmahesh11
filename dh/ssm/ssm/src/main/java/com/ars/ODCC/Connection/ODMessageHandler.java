package com.ars.ODCC.Connection;

import com.ars.ODCC.Connection.ODMessage.Subscribe;
import com.ars.ODCC.Connection.ODMessage.SubscriptionResponse;
import com.ars.ODCC.Connection.ODMessage.TravelInfo;
import com.ars.ODCC.Connection.ODMessage.Unsubscribe;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

public class ODMessageHandler {
	
	
	
	public  Subscribe parseSubscribeJson(String jsonString) { 
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
	
	public  Unsubscribe parseUnsubscribeJson(String jsonString) { 
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
	public String parseTravelInfoProtoBufMessage( TravelInfo travelInfo ) {
		String jsonString = null; 
		if (travelInfo == null) {
			System.out.println("Protobuf message was null");
		} else {
			try {
				JsonFormat.Printer  jsonFormat = JsonFormat.printer();
				jsonString = jsonFormat.print(travelInfo);
			} catch (Exception e) {
				throw new RuntimeException("Error deserializing protobuf to json", e);
			}
		}

		return jsonString;
	}

}
