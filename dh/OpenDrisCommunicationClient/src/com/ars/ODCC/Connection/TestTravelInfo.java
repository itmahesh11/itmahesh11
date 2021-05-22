package com.ars.ODCC.Connection;

import com.ars.ODCC.Connection.ODMessage.Container;
import com.ars.ODCC.Connection.ODMessage.GeneralMessage;
import com.ars.ODCC.Connection.ODMessage.GeneralMessageRemove;
import com.ars.ODCC.Connection.ODMessage.PassingTimes;
import com.ars.ODCC.Connection.ODMessage.PublicName;
import com.ars.ODCC.Connection.ODMessage.GeneralMessage.GeneralMessageType;
import com.ars.ODCC.Connection.ODMessage.GeneralMessage.MessagePriority;
import com.ars.ODCC.Connection.ODMessage.GeneralMessage.ShowOverviewDisplay;
import com.ars.ODCC.Connection.ODMessage.PassingTimes.Destination;
import com.ars.ODCC.Connection.ODMessage.PassingTimes.ShowCancelledTrip;
import com.ars.ODCC.Connection.ODMessage.PassingTimes.TransportType;
import com.ars.ODCC.Connection.ODMessage.PassingTimes.TripStopStatus;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class TestTravelInfo {

	public static void main(String[] args) {

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
		
		//Creation of client
				final Mqtt5BlockingClient client = Mqtt5Client.builder()
						.identifier("1992444")
						.serverHost("localhost")
						.buildBlocking();

				//Connecting the client to server with clean start as it retains the mesaage when the server is disconnected

				client.connect();

				client.publishWith().topic("travel_information/2/2/SUR/ss34134134").qos(MqttQos.AT_LEAST_ONCE).payload((objRespone.toByteArray())).send();

	
	}

}
