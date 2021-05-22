package com.ars.ODCC.TestProgram;
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;


import com.ars.ODCC.Configuration.GeneralConfig;
import com.ars.ODCC.message.ODMessage.ClientId;
import com.ars.ODCC.message.ODMessage.InfoRequest;
import com.ars.ODCC.message.ODMessage.InfoRequest.RequestType;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;


public class InfoRequestTest {


	/**
	 * GeneralConfig to access settings.general
	 */
	private static GeneralConfig generalConfig = new GeneralConfig();

	public static void main(String[] args) {

		InfoRequest.Builder infoRequest = InfoRequest.newBuilder();
		String subscriberOwnerCode = generalConfig.getSystemInfoSubscriberOwnerCode();
		int subscriberType = generalConfig.getSystemInfoSubscriberType();
		String serialNumber = generalConfig.getSystemInfoSerialNumber();
		ClientId.Builder clientId = ClientId.newBuilder();
		clientId.setSubscriberOwnerCode(subscriberOwnerCode);
		clientId.setSubscriberTypeValue(subscriberType);
		clientId.setSerialNumber(serialNumber);
		infoRequest.setClientId(clientId);
		infoRequest.setRequestType(RequestType.SCREEN_CONTENT);
		InfoRequest objRespone = infoRequest.build();
		
		JsonFormat.Printer jsonprinter = JsonFormat.printer();

		String payload = null;
		try {
			payload = jsonprinter.print(objRespone);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Info Request JSON");
		System.out.println(payload);


		//getSubscriptionMessage();
		//Creation of client
		final Mqtt5BlockingClient client = Mqtt5Client.builder()
				.identifier("SUR_2_ss34134dfdfs")
				.serverHost("192.168.201.156")
				.buildBlocking();

		//Connecting the client to server with clean start as it retains the message when the server is disconnected

		client.connect();

		client.publishWith().topic("inforequest/1/2/SUR/34134134").qos(MqttQos.AT_LEAST_ONCE).payload(objRespone.toByteArray()).send();

		//getSubscriptionMessage();
		//createContainerMessage();
	}
}
