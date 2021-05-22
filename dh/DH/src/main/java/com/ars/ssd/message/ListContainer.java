
/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/
package com.ars.ssd.message;

import com.ars.ODCC.connection.ODMessage.ClientId;
import com.ars.ODCC.connection.ODMessage.ClientId.SubscriberType;
import com.ars.ODCC.connection.ODMessage.Container;
import com.ars.ODCC.connection.ODMessage.ContainerOrBuilder;
import com.ars.ODCC.connection.ODMessage.GeneralMessage;
import com.ars.ODCC.connection.ODMessage.GeneralMessage.GeneralMessageType;
import com.ars.ODCC.connection.ODMessage.GeneralMessage.MessagePriority;
import com.ars.ODCC.connection.ODMessage.GeneralMessage.ShowOverviewDisplay;
import com.ars.ODCC.connection.ODMessage.GeneralMessageRemove;
import com.ars.ODCC.connection.ODMessage.PassingTimes;
//import com.ars.ssd.message.ODMessage.Container;
//import com.ars.ssd.message.ODMessage.GeneralMessage;
//import com.ars.ssd.message.ODMessage.GeneralMessageRemove;
//import com.ars.ssd.message.ODMessage.PassingTimes;
//import com.ars.ssd.message.ODMessage.PublicName;
//import com.ars.ssd.message.ODMessage.PassingTimes.Builder;
import com.ars.ODCC.connection.ODMessage.PassingTimes.Builder;
import com.ars.ODCC.connection.ODMessage.PassingTimes.Destination;
import com.ars.ODCC.connection.ODMessage.PassingTimes.ShowCancelledTrip;
import com.ars.ODCC.connection.ODMessage.PassingTimes.TransportType;
import com.ars.ODCC.connection.ODMessage.PassingTimes.TripStopStatus;
import com.ars.ODCC.connection.ODMessage.PublicName;
import com.ars.ODCC.connection.ODMessage.StatusType;
import com.ars.ODCC.connection.ODMessage.SystemStatus.LogMessage;
import com.ars.ODCC.connection.ODMessage.ValueType;
import com.ars.ssd.configuration.ARSLogger;
import com.ars.ssd.configuration.Configuration;
import com.ars.ssd.configuration.GeneralConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import ars.protobuf.DHMessage.FreeText;
import ars.protobuf.DHMessage.travelinfo;
import ars.protobuf.DHMessage.trip;

class ListContainer {

	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = null;
	private static HashMap<String, com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passTimeMapObj = null;
	private static HashMap<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> generalMessageMapObj = null;
	private static HashMap<String, com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder> generalMessageRemoveMapObj = null;
	private static HashMap<String, com.ars.ODCC.connection.ODMessage.PublicName.Builder> publicNameMapObj = null;
	private static SSBManager ssbManagerObj = null;
	private static Configuration config;
	private static GeneralConfig generalConfig = null;

	private static String current_state = "startup";
	private static int noTripTime = SSBManager.getCurrentTimeAsInteger();
	private static boolean stopCodeNotAvailable = true;
	private static boolean showBlankFlag = false;

	private static LocalDateTime dhCurrentDay = LocalDateTime.of(2000, 1, 1, 0, 0, 01);
	private static int lastBusTimeCurrentday = SSBManager.getCurrentTimeAsInteger();
	private static int firstBusTimeCurrentday = SSBManager.getCurrentTimeAsInteger();
	private static LocalDateTime currentDay;
	static boolean tripStopStatusFlag = false;

	/**
	 * Get List Container
	 * 
	 * @throws Exception
	 */
	public static Container.Builder getListConatiner(String jsonString) throws Exception {

		com.ars.ODCC.connection.ODMessage.Container.Builder containerMsg2 = null;

		try {
			// Json To ProtoBuf Message
			containerMsg2 = parseJsonToProtoBufMsg(jsonString);
			// Read Container Message
			readContainerProtoBufMsg(containerMsg2.build());

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Parsing Container Message Failed ...." + e.getMessage() + LocalDateTime.now());
		}
		return containerMsg2;

	}

	/**
	 * Iterates though all the proto buffer message in the Container and prints info
	 * 
	 * @param containerMessage
	 */
	private static void readContainerProtoBufMsg(com.ars.ODCC.connection.ODMessage.Container containerMessage) {

		try {

			if (containerMessage != null) {

				com.ars.ODCC.connection.ODMessage.PassingTimes passingTimesMsg = containerMessage.getPassingTimes();
				com.ars.ODCC.connection.ODMessage.GeneralMessage generalMessage = containerMessage.getGeneralMessages();
				com.ars.ODCC.connection.ODMessage.GeneralMessageRemove generalMessageRemove = containerMessage
						.getGeneralMessagesRemove();
				com.ars.ODCC.connection.ODMessage.PublicName publicName = containerMessage.getPublicNames();

				if (passingTimesMsg.getPassTimeHashList().size() > 0) {
					readPassingTimesProtoBufMsg(passingTimesMsg);
				}
				if (generalMessage.getMessageHashList().size() > 0) {
					readGeneralMessage(generalMessage);
				}
				int generalMessageRemoveObjSize = generalMessageRemove.getMessageHashList().size();
				if (generalMessageRemoveObjSize > 0) {
					readGeneralMessageRemove(generalMessageRemove);
				}

//				int publicNameSize = publicName.getStopCodeList().size();
				if (publicName != null) {
					readPublicNameProtoBufMsg(publicName);
					SSBManager.sendPublicNameMessageToRDR(publicName);
				}

				SSBManager.publishTripToAllRDR();// when we receive the update from ODCC immediatly DH send a trip
													// updates to RD
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL, "Exception in ListContainer: readContainerProtoBufMsg(): ...."
					+ e.getMessage() + LocalDateTime.now());
		}

	}

	/**
	 * initializing a passageTimes,generalMessage,generalMessageRemove map
	 */

	public static void initializeMap() {

		try {
			passTimeMapObj = new HashMap<String, com.ars.ODCC.connection.ODMessage.PassingTimes.Builder>();// Creating
			generalMessageMapObj = new HashMap<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder>();// Creating
			generalMessageRemoveMapObj = new HashMap<String, com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder>();
			publicNameMapObj = new HashMap<String, com.ars.ODCC.connection.ODMessage.PublicName.Builder>();

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Map Not Initialize  .... " + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * 
	 * prepare system status message for trip available
	 */
	public static String systemStatusMessageForTripAvailable() {

		try {

			LogMessage.Builder logMessage = LogMessage.newBuilder();

			boolean tripLogMessage = SSBManager.getConfig().getDhConfig().getLogMessageEnable();

			if (tripLogMessage) {
				if (passTimeMapObj.size() > 0) {

					if (current_state.equals("startup")) {

						// client value hardcode
						Random rand = new Random();
						String subscriber_owner_code = String.valueOf(rand.nextInt(1000));
						SubscriberType subscriber_type = SubscriberType.HALTESYSTEEM;
						String serial_number = "1000";

						ClientId.Builder clientId = ClientId.newBuilder();
						clientId.setSubscriberOwnerCode(subscriber_owner_code);
						clientId.setSubscriberType(subscriber_type);
						clientId.setSerialNumber(serial_number);

						logMessage.setType(StatusType.OK);
						logMessage.setClientId(clientId);
						logMessage.setCode(SSBManager.getConfig().getDhConfig().getCode());
						logMessage.setMessage(SSBManager.getConfig().getDhConfig().getMessageOK());
						logMessage.setDuration(0);
						logMessage.setTimestamp(SSBManager.getCurrentTimeAsInteger());

						current_state = "ride available";

						JsonFormat.Printer jsonprinter = JsonFormat.printer();
						String payload = jsonprinter.print(logMessage);

//						System.out.println( "logMessage_before : "+payload  );
						SSBManager.getLoggerObject().log(Level.ALL,
								"logMessage : " + payload.replace("\n", " ") + LocalDateTime.now());
//						System.out.println( "logMessage_after : "+payload  );

						SSBManager.publishSystemStatusMessage(payload);
//						System.out.println( "logMessage_payload : "+payload  );

					} else if (current_state == "no ride") {
						String subscriber_owner_code = "1";
						SubscriberType subscriber_type = SubscriberType.HALTESYSTEEM;
						String serial_number = "1111";

						ClientId.Builder clientId = ClientId.newBuilder();
						clientId.setSubscriberOwnerCode(subscriber_owner_code);
						clientId.setSubscriberType(subscriber_type);
						clientId.setSerialNumber(serial_number);

						logMessage.setType(StatusType.OK);
						logMessage.setClientId(clientId);
						logMessage.setCode(SSBManager.getConfig().getDhConfig().getCode());
						logMessage.setMessage(SSBManager.getConfig().getDhConfig().getMessageOK());
						logMessage.setTimestamp(SSBManager.getCurrentTimeAsInteger());

						int duration = logMessage.getTimestamp() - noTripTime;// SB_change_23_04_2021

						logMessage.setDuration(duration);
						current_state = "ride available";

						JsonFormat.Printer jsonprinter = JsonFormat.printer();
						String payload = jsonprinter.print(logMessage);
						SSBManager.publishSystemStatusMessage(payload);

					}
//					else if (current_state == "ride available")
//	                {
//						
//	                }
				} else {

					if ((current_state == "startup") || (current_state == "ride available"))

					{
						String subscriber_owner_code = "1";
						SubscriberType subscriber_type = SubscriberType.HALTESYSTEEM;
						String serial_number = "1111";

						ClientId.Builder clientId = ClientId.newBuilder();
						clientId.setSubscriberOwnerCode(subscriber_owner_code);
						clientId.setSubscriberType(subscriber_type);
						clientId.setSerialNumber(serial_number);

						noTripTime = SSBManager.getCurrentTimeAsInteger();
						logMessage.setType(StatusType.WARNING);
						logMessage.setClientId(clientId);
						logMessage.setCode(SSBManager.getConfig().getDhConfig().getCode());
						logMessage.setMessage(SSBManager.getConfig().getDhConfig().getMessageWRNING());
						logMessage.setDuration(0);
						logMessage.setTimestamp(SSBManager.getCurrentTimeAsInteger());
						current_state = "no ride";

						JsonFormat.Printer jsonprinter = JsonFormat.printer();
						String payload = jsonprinter.print(logMessage);
						SSBManager.publishSystemStatusMessage(payload);
					}

					else {

					}
				}
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.INFO,
					" Prepare System Status Message Failed ...." + e.getMessage() + LocalDateTime.now());
		}
		return current_state;
	}

	/**
	 * remove the all free text from memory
	 */

	public static int removeAllFreeTextFromMemory() {
		int generalMessageRemoved = -1;

		try {

			if (generalMessageMapObj.size() > 0 && generalMessageMapObj != null) {

				generalMessageMapObj.clear();
				SSBManager.getLoggerObject().log(Level.ALL,
						" Removed All The FreeText From Memory .... " + LocalDateTime.now());

				boolean fileMode = SSBManager.getConfig().getFileMode();
				if (fileMode == true) {
					dumpGeneralMsgToFreeTextFile();

				}
				generalMessageRemoved = generalMessageMapObj.size();

			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Remove All The FreeText From Memory Failed  ...." + e.getMessage() + LocalDateTime.now());
		}
		return generalMessageRemoved;

	}

	private static int getStatusType(String StatusType) {

		switch (StatusType) {
		case "ERROR ":
			return 0;
		case "WARNING ":
			return 1;
		case "OK ":
			return 2;
		case "LOG ":
			return 3;
		default:
			return 0;
		}
	}

	/**
	 * prepare travel info response message
	 */

	public static int[] prepareTravelInfoResponseSSM() {

		int tripMessageSize = -1;
		int tripCount = 0;
		int freeTextCount = 0;
		int travelInfoSize[] = new int[2];
		try {

			boolean passTimesPresent = false;
			boolean generalMessagePresent = false;

			Container.Builder containerObj = Container.newBuilder();
			PassingTimes.Builder passTimesObj = PassingTimes.newBuilder();

			Integer currentTime = SSBManager.getCurrentTimeAsInteger();

			if (passTimeMapObj.size() > 0) {

				for (Entry<String, Builder> entryObj : passTimeMapObj.entrySet()) {

					int expextedDepOrArrivelTime = -1;

					if (entryObj.getValue().getExpectedArrivalTimeList().size() > 0) {
						expextedDepOrArrivelTime = entryObj.getValue().getExpectedArrivalTime(0);
					} else if (entryObj.getValue().getExpectedDepartureTimeList().size() > 0) {
						expextedDepOrArrivelTime = entryObj.getValue().getExpectedDepartureTime(0);
					}

					if (expextedDepOrArrivelTime > currentTime) {

						if (entryObj.getValue().getPassTimeHashList().size() > 0) {
							passTimesObj.addPassTimeHash(entryObj.getValue().getPassTimeHash(0));
						}
						if (entryObj.getValue().getTargetArrivalTimeList().size() > 0) {
							passTimesObj.addTargetArrivalTime(entryObj.getValue().getTargetArrivalTime(0));
						}
						if (entryObj.getValue().getTargetDepartureTimeList().size() > 0) {
							passTimesObj.addTargetDepartureTime(entryObj.getValue().getTargetDepartureTime(0));
						}
						if (entryObj.getValue().getExpectedArrivalTimeList().size() > 0) {
							passTimesObj.addExpectedArrivalTime(entryObj.getValue().getExpectedArrivalTime(0));
						}
						if (entryObj.getValue().getExpectedDepartureTimeList().size() > 0) {
							passTimesObj.addExpectedDepartureTime(entryObj.getValue().getExpectedDepartureTime(0));
						}
						if (entryObj.getValue().getNumberOfCoachesList().size() > 0) {
							passTimesObj.addNumberOfCoaches(entryObj.getValue().getNumberOfCoaches(0));
						}
						if (entryObj.getValue().getTripStopStatusList().size() > 0) {
							passTimesObj.addTripStopStatus(entryObj.getValue().getTripStopStatus(0));
						}
						if (entryObj.getValue().getTransportTypeValueList().size() > 0) {
							passTimesObj.addTransportTypeValue(entryObj.getValue().getTransportTypeValue(0));
						}
						if (entryObj.getValue().getWheelchairAccessibleList().size() > 0) {
							passTimesObj.addWheelchairAccessible(entryObj.getValue().getWheelchairAccessible(0));

						}
						if (entryObj.getValue().getIsTimingStopList().size() > 0) {
							passTimesObj.addIsTimingStop(entryObj.getValue().getIsTimingStop(0));
						}
						if (entryObj.getValue().getStopCodeList().size() > 0) {
							passTimesObj.addStopCode(entryObj.getValue().getStopCode(0));
						}
						if (entryObj.getValue().getDestinationsList().size() > 0) {
							passTimesObj.addDestinations(entryObj.getValue().getDestinations(0));
						}
						if (entryObj.getValue().getShowCancelledTripList().size() > 0) {
							passTimesObj.addShowCancelledTrip(entryObj.getValue().getShowCancelledTrip(0));
						}
						if (entryObj.getValue().getBlockCodeList().size() > 0) {
							passTimesObj.addBlockCode(entryObj.getValue().getBlockCode(0));
						}
						if (entryObj.getValue().getOccupancyList().size() > 0) {
							passTimesObj.addOccupancy(entryObj.getValue().getOccupancy(0));
						}
						if (entryObj.getValue().getLinePublicNumberList().size() > 0) {
							passTimesObj.addLinePublicNumber(entryObj.getValue().getLinePublicNumber(0));
						}
						if (entryObj.getValue().getSideCodeList().size() > 0) {
							passTimesObj.addSideCode(entryObj.getValue().getSideCode(0));
						}
						if (entryObj.getValue().getLineDirectionList().size() > 0) {
							passTimesObj.addLineDirection(entryObj.getValue().getLineDirection(0));
						}
						if (entryObj.getValue().getLineColorList().size() > 0) {
							passTimesObj.addLineColor(entryObj.getValue().getLineColor(0));
						}
						if (entryObj.getValue().getLineTextColorList().size() > 0) {
							passTimesObj.addLineTextColor(entryObj.getValue().getLineTextColor(0));
						}
						if (entryObj.getValue().getLineIconList().size() > 0) {
							passTimesObj.addLineIcon(entryObj.getValue().getLineIcon(0));
						}
						if (entryObj.getValue().getDestinationColorList().size() > 0) {
							passTimesObj.addDestinationColor(entryObj.getValue().getDestinationColor(0));
						}
						if (entryObj.getValue().getDestinationIconList().size() > 0) {
							passTimesObj.addDestinationIcon(entryObj.getValue().getDestinationIcon(0));
						}
						if (entryObj.getValue().getGeneratedTimestampList().size() > 0) {
							passTimesObj.addGeneratedTimestamp(entryObj.getValue().getGeneratedTimestamp(0));
						}
						if (entryObj.getValue().getJourneyNumberList().size() > 0) {
							passTimesObj.addJourneyNumber(entryObj.getValue().getJourneyNumber(0));
						}

						passTimesPresent = true;
						tripCount++;

					}
				}
			}

			tripMessageSize = passTimeMapObj.size();

			GeneralMessage.Builder generalBuilderObj = GeneralMessage.newBuilder();
			if (generalMessageMapObj.size() > 0) {

				for (Entry<String, GeneralMessage.Builder> entryObj : generalMessageMapObj.entrySet()) {

					int messageStartTime = -1;
					int messageEndTime = -1;

					if (entryObj.getValue().getMessageStartTimeList().size() > 0) {
						messageStartTime = entryObj.getValue().getMessageStartTime(0);
					}
					if (entryObj.getValue().getMessageEndTimeList().size() > 0) {
						messageEndTime = entryObj.getValue().getMessageEndTime(0);
					}

					if (messageStartTime > currentTime || messageEndTime > currentTime) {

						if (entryObj.getValue().getMessageHashList().size() > 0) {
							generalBuilderObj.addMessageHash(entryObj.getValue().getMessageHash(0));
						}
						if (entryObj.getValue().getGeneralmessageTypeList().size() > 0) {
							generalBuilderObj.addGeneralmessageType(entryObj.getValue().getGeneralmessageType(0));
						}
						if (entryObj.getValue().getMessageContentList().size() > 0) {
							generalBuilderObj.addMessageContent(entryObj.getValue().getMessageContent(0));
						}
						if (entryObj.getValue().getMessageStartTimeList().size() > 0) {
							generalBuilderObj.addMessageStartTime(entryObj.getValue().getMessageStartTime(0));
						}
						if (entryObj.getValue().getMessageEndTimeList().size() > 0) {
							generalBuilderObj.addMessageEndTime(entryObj.getValue().getMessageEndTime(0));
						}
						if (entryObj.getValue().getGeneratedTimestampList().size() > 0) {
							generalBuilderObj.addGeneratedTimestamp(entryObj.getValue().getGeneratedTimestamp(0));
						}
						if (entryObj.getValue().getShowOverviewDisplayList().size() > 0) {
							generalBuilderObj.addShowOverviewDisplay(entryObj.getValue().getShowOverviewDisplay(0));
						}
						if (entryObj.getValue().getMessageTitleList().size() > 0) {
							generalBuilderObj.addMessageTitle(entryObj.getValue().getMessageTitle(0));
						}
						if (entryObj.getValue().getMessagePriorityList().size() > 0) {
							generalBuilderObj.addMessagePriority(entryObj.getValue().getMessagePriority(0));

						}
					}

					generalMessagePresent = true;
					freeTextCount++;

				}

			}

			travelInfoSize[0] = tripCount;
			travelInfoSize[1] = freeTextCount;

			if (passTimesPresent == true) {
				containerObj.setPassingTimes(passTimesObj);
			}
			if (generalMessagePresent == true) {
				containerObj.setGeneralMessages(generalBuilderObj);
			}

			JsonFormat.Printer jsonFormat = JsonFormat.printer();
			String jsonString = jsonFormat.print(containerObj);
			SSBManager.tripMessagePublishToSSM(jsonString);

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Prepare The Travel Information To SSM  Failed ...." + e.getMessage() + LocalDateTime.now());

		}
		return travelInfoSize;

	}

	@SuppressWarnings("unchecked")
	public static boolean prepareTripOnTimeWindow(DisplayRegistrationMessage displayRegMessage) {

		ars.protobuf.DHMessage.travelinfo.Builder travelInfo = travelinfo.newBuilder();
		boolean tripAvailableFlag = false;

		try {

			boolean tripInFutureAvailable = false;
			Integer currentTime = SSBManager.getCurrentTimeAsInteger();
			boolean sendFreeTextFlag = false;
			sendFreeTextFlag = displayRegMessage.getSendFreeText();

			if (passTimeMapObj.size() > 0) {

				ars.protobuf.DHMessage.trip.Builder tripObj = trip.newBuilder();

				String tripSelectionFlag = displayRegMessage.getTimeFieldUsedForTripSelection();
				int timeSelectionFlag = -1;
				if (tripSelectionFlag.equals("DEPARTURE"))
					timeSelectionFlag = 1;
				else if (tripSelectionFlag.equals("ARRIVAL"))
					timeSelectionFlag = 2;

				for (Entry<String, Builder> entry : passTimeMapObj.entrySet()) {

					boolean stopcodematches = false;
					for (String stopCode : displayRegMessage.getStopCodeList()) {

						if (stopCode.equals(entry.getValue().getStopCode(0))) {

							stopcodematches = true;
							if (stopcodematches) {

								int depOrArrivalTimelen = -1;
								if (timeSelectionFlag == 1) {
									depOrArrivalTimelen = entry.getValue().getExpectedDepartureTimeList().size();
								} else if (timeSelectionFlag == 2) {
									depOrArrivalTimelen = entry.getValue().getExpectedArrivalTimeList().size();
								}

								if (depOrArrivalTimelen > 0) {

									long depOrArrivelTime = 0;
									if (timeSelectionFlag == 1)
										depOrArrivelTime = entry.getValue().getExpectedDepartureTimeList().get(0);
									else if (timeSelectionFlag == 2)
										depOrArrivelTime = entry.getValue().getExpectedArrivalTimeList().get(0);

									long toonTIJDTimeInSec = displayRegMessage.getToonTijdInMin() * 60;
									long vertrekTimeOutInSec = SSBManager.getGeneralConfig().getVertrekTimeoutInSec();
//
									if ((depOrArrivelTime >= (currentTime - vertrekTimeOutInSec))
											&& ((depOrArrivelTime <= currentTime + toonTIJDTimeInSec))) {

										SSBManager.getLoggerObject().log(Level.ALL,
												entry.getKey() + " This TripHash Selected For "
														+ displayRegMessage.getDisplayName() + " "
														+ LocalDateTime.now());

										if (entry.getValue().getPassTimeHashList().size() > 0) {
											tripObj.setPassTimeHash(entry.getValue().getPassTimeHash(0));
										}

										if (entry.getValue().getTargetArrivalTimeList().size() > 0) {
											tripObj.setTargetArrivalTime(
													entry.getValue().getTargetArrivalTimeList().get(0));
										}
										if (entry.getValue().getTargetDepartureTimeList().size() > 0) {
											tripObj.setTargetDepartureTime(
													entry.getValue().getTargetDepartureTimeList().get(0));
										}
										if (entry.getValue().getExpectedArrivalTimeList().size() > 0) {
											tripObj.setExpectedArrivalTime(
													entry.getValue().getExpectedArrivalTimeList().get(0));
										}
										if (entry.getValue().getExpectedDepartureTimeList().size() > 0) {

											tripObj.setExpectedDepartureTime(
													entry.getValue().getExpectedDepartureTimeList().get(0));
										}

										if (entry.getValue().getNumberOfCoachesList().size() > 0) {

											tripObj.setNumberOfCoaches(
													entry.getValue().getNumberOfCoachesList().get(0));

										}
										if (entry.getValue().getTripStopStatusList().size() > 0) {

											TripStopStatus stateOfTrip = entry.getValue().getTripStopStatusList()
													.get(0);

											if (stateOfTrip == TripStopStatus.PLANNED) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.PLANNED);
											} else if (stateOfTrip == TripStopStatus.DRIVING) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.DRIVING);
											} else if (stateOfTrip == TripStopStatus.CANCELLED) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.CANCELLED);
											} else if (stateOfTrip == TripStopStatus.ARRIVED) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.ARRIVED);
											} else if (stateOfTrip == TripStopStatus.PASSED) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.PASSED);
											} else if (stateOfTrip == TripStopStatus.UNKNOWN) {
												tripObj.setTripStopStatus(
														ars.protobuf.DHMessage.trip.TripStopStatus.UNKNOWN);
											}

										}
										if (entry.getValue().getTransportTypeList().size() > 0) {

											TransportType stateTransportType = entry.getValue().getTransportTypeList()
													.get(0);

											if (stateTransportType == TransportType.BUS) {
												tripObj.setTransportType(ars.protobuf.DHMessage.trip.TransportType.BUS);
											} else if (stateTransportType == TransportType.TRAM) {
												tripObj.setTransportType(
														ars.protobuf.DHMessage.trip.TransportType.TRAM);
											} else if (stateTransportType == TransportType.METRO) {
												tripObj.setTransportType(
														ars.protobuf.DHMessage.trip.TransportType.METRO);
											} else if (stateTransportType == TransportType.TRAIN) {
												tripObj.setTransportType(
														ars.protobuf.DHMessage.trip.TransportType.TRAIN);
											} else if (stateTransportType == TransportType.BOAT) {
												tripObj.setTransportType(
														ars.protobuf.DHMessage.trip.TransportType.BOAT);
											}
										}

										if (entry.getValue().getWheelchairAccessibleList().size() > 0) {

											tripObj.setWheelchairAccessible(
													entry.getValue().getWheelchairAccessibleList().get(0));

										}
										if (entry.getValue().getIsTimingStopList().size() > 0) {
											tripObj.setIsTimingStop(entry.getValue().getIsTimingStopList().get(0));
										}
										if (entry.getValue().getStopCodeList().size() > 0
												&& (!entry.getValue().getStopCodeList().contains(""))) {
											tripObj.setStopCode(entry.getValue().getStopCodeList().get(0));
										}
										if (entry.getValue().getShowCancelledTripList().size() > 0) {

											ShowCancelledTrip stateShowCancelledTrip = entry.getValue()
													.getShowCancelledTripList().get(0);

											if (stateShowCancelledTrip == ShowCancelledTrip.TRUE) {
												tripObj.setShowCancelledTrip(trip.ShowCancelledTrip.TRUE);
											} else if (stateShowCancelledTrip == ShowCancelledTrip.FALSE) {
												tripObj.setShowCancelledTrip(
														ars.protobuf.DHMessage.trip.ShowCancelledTrip.FALSE);
											} else if (stateShowCancelledTrip == ShowCancelledTrip.MESSAGE) {
												tripObj.setShowCancelledTrip(
														ars.protobuf.DHMessage.trip.ShowCancelledTrip.MESSAGE);
											}

										}
										if (entry.getValue().getBlockCodeList().size() > 0
												&& (!entry.getValue().getBlockCodeList().contains(""))) {
											tripObj.setBlockCode(entry.getValue().getBlockCodeList().get(0));
										}
										if (entry.getValue().getOccupancyList().size() > 0) {
											tripObj.setOccupancy(entry.getValue().getOccupancyList().get(0));
										}
										if (entry.getValue().getLinePublicNumberList().size() > 0
												&& (!entry.getValue().getLinePublicNumberList().contains(""))) {
											tripObj.setLinePublicNumber(
													entry.getValue().getLinePublicNumberList().get(0));
										}
										if (entry.getValue().getSideCodeList().size() > 0
												&& (!entry.getValue().getSideCodeList().contains(""))) {
											tripObj.setSideCode(entry.getValue().getSideCodeList().get(0));
										}
										if (entry.getValue().getLineDirectionList().size() > 0) {
											tripObj.setLineDirection(entry.getValue().getLineDirectionList().get(0));
										}
										if (entry.getValue().getLineColorList().size() > 0
												&& (!entry.getValue().getLineColorList().contains(""))) {
											tripObj.setLineColor(entry.getValue().getLineColorList().get(0));
										}
										if (entry.getValue().getLineTextColorList().size() > 0
												&& (!entry.getValue().getLineTextColorList().contains(""))) {
											tripObj.setLineTextColor(entry.getValue().getLineTextColorList().get(0));
										}
										if (entry.getValue().getLineIconList().size() > 0
												&& (!entry.getValue().getLineIconList().contains(""))) {
											tripObj.setLineIcon(entry.getValue().getLineIconList().get(0));
										}
										if (entry.getValue().getDestinationColorList().size() > 0
												&& (!entry.getValue().getDestinationColorList().contains(""))) {
											tripObj.setDestinationColor(
													entry.getValue().getDestinationColorList().get(0));
										}
										if (entry.getValue().getDestinationTextColorList().size() > 0
												&& (!entry.getValue().getDestinationTextColorList().contains(""))) {
											tripObj.setDestinationTextColor(
													entry.getValue().getDestinationTextColorList().get(0));
										}
										if (entry.getValue().getDestinationIconList().size() > 0
												&& (!entry.getValue().getDestinationIconList().contains(""))) {
											tripObj.setDestinationIcon(
													entry.getValue().getDestinationIconList().get(0));
										}
										if (entry.getValue().getGeneratedTimestampList().size() > 0) {
											tripObj.setGeneratedTimestamp(
													entry.getValue().getGeneratedTimestampList().get(0));
										}
										if (entry.getValue().getJourneyNumberList().size() > 0) {
											tripObj.setJourneyNumber(entry.getValue().getJourneyNumberList().get(0));
										}

										ArrayList<String> destinationName = new ArrayList<String>();
										ArrayList<String> destinationDetails = new ArrayList<String>();

										ars.protobuf.DHMessage.trip.Destination.Builder destinationObj = ars.protobuf.DHMessage.trip.Destination
												.newBuilder();
										for (int k = 0; k < entry.getValue().getDestinations(0).getDestinationNameList()
												.size(); k++) {

											if (entry.getValue().getDestinations(0).getDestinationNameList()
													.size() > 0) {
												destinationName.add(entry.getValue().getDestinations(0)
														.getDestinationNameList().get(k));
											}
											if (entry.getValue().getDestinations(0).getDestinationDetailList()
													.size() > 0) {
												destinationDetails.add(entry.getValue().getDestinations(0)
														.getDestinationDetailList().get(k));
											}

										}

										// issue fix
										destinationObj.addAllDestinationName(destinationName);
										destinationObj.addAllDestinationDetail(destinationDetails);
										tripObj.setDestinations(destinationObj);

										travelInfo.addTrips(tripObj);
										tripAvailableFlag = true;// when the trip available then the data available flag
																	// set to

									} else if (depOrArrivelTime > currentTime + toonTIJDTimeInSec)

										tripInFutureAvailable = true;
								}
							}
						}
					}
				}
			}

			long currentTimeObj = SSBManager.getCurrentTimeAsInteger();
			if (currentTimeObj > lastBusTimeCurrentday) { // no trip
				if (currentTimeObj - lastBusTimeCurrentday > 3600) {
					showBlankFlag = true;
				} else {
					showBlankFlag = false;
				}
			} else if (firstBusTimeCurrentday > currentTimeObj) { // trip not yet started

				if (firstBusTimeCurrentday - currentTimeObj > 3600) {

					showBlankFlag = true;
				} else {
					showBlankFlag = false;
				}
			} else {
				showBlankFlag = false;
			}

			if (tripAvailableFlag == true) {

				travelInfo.setShowblank(showBlankFlag);
				travelInfo.setDataAvailabile(tripAvailableFlag);
				travelInfo.setDisplayText(" ");

			} else if (tripInFutureAvailable == true) {

				travelInfo.setShowblank(showBlankFlag);
				String textNoTripsInTimeWindow = SSBManager.getConfig().getDisplayTextForNoTrips("NoTripsInTimeWindow");
				travelInfo.setDataAvailabile(tripAvailableFlag);
				travelInfo.setDisplayText("" + textNoTripsInTimeWindow + "");

//				ars.protobuf.DHMessage.trip.Builder tripObj = trip.newBuilder();
//				travelInfo.addTrips(tripObj);
//				rootJsonObject.put("trips", travelInfo);

			} else {

				travelInfo.setShowblank(showBlankFlag);
				travelInfo.setDataAvailabile(tripAvailableFlag);
				boolean connectionStatus = SSBManager.getOdccConnectionStatus();
				if (connectionStatus == true) {

					String textNoTripsAndODConnected = SSBManager.getConfig()
							.getDisplayTextForNoTrips("NoTripsAndODConnected");
					travelInfo.setDisplayText("" + textNoTripsAndODConnected + "");
//					ars.protobuf.DHMessage.trip.Builder tripObj = trip.newBuilder();
//					travelInfo.addTrips(tripObj);

				} else {

					String textNoTripsAndODDisconnected = SSBManager.getConfig()
							.getDisplayTextForNoTrips("NoTripsAndODDisconnected");
					travelInfo.setDisplayText("" + textNoTripsAndODDisconnected + "");
//					ars.protobuf.DHMessage.trip.Builder tripObj = trip.newBuilder();
//					travelInfo.addTrips(tripObj);
				}
			}

			if (sendFreeTextFlag == true) {

				if (generalMessageMapObj.size() > 0) {

					ars.protobuf.DHMessage.FreeText.Builder freeTextObj = FreeText.newBuilder();

					for (Entry<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> entryObj : generalMessageMapObj
							.entrySet()) {

						if (entryObj.getValue().getMessageStartTimeList().size() > 0
								|| entryObj.getValue().getMessageEndTimeList().size() > 0) {

							int startTime = entryObj.getValue().getMessageStartTimeList().get(0);
							int endTime = entryObj.getValue().getMessageEndTimeList().get(0);

							if ((currentTime >= startTime) && (currentTime <= endTime)) {

								if (entryObj.getValue().getMessageHashList().size() > 0) {
									freeTextObj.setMessageHash(entryObj.getValue().getMessageHashList().get(0));
								}

								if (entryObj.getValue().getGeneralmessageTypeList().size() > 0) {

									GeneralMessageType stateGeneralmessageType = entryObj.getValue()
											.getGeneralmessageTypeList().get(0);

									if (stateGeneralmessageType == GeneralMessageType.GENERAL) {
										freeTextObj.setGeneralmessageType(
												ars.protobuf.DHMessage.FreeText.GeneralMessageType.GENERAL);
									} else if (stateGeneralmessageType == GeneralMessageType.OVERRULE) {
										freeTextObj.setGeneralmessageType(
												ars.protobuf.DHMessage.FreeText.GeneralMessageType.OVERRULE);
									} else if (stateGeneralmessageType == GeneralMessageType.BLANC) {
										freeTextObj.setGeneralmessageType(
												ars.protobuf.DHMessage.FreeText.GeneralMessageType.BLANC);
									}
								}

								if (entryObj.getValue().getMessageContentList().size() > 0) {
									freeTextObj.setMessageContent(entryObj.getValue().getMessageContentList().get(0));
								}
								if (entryObj.getValue().getMessageStartTimeList().size() > 0) {
									freeTextObj
											.setMessageStartTime(entryObj.getValue().getMessageStartTimeList().get(0));
								}
								if (entryObj.getValue().getMessageEndTimeList().size() > 0) {
									freeTextObj.setMessageEndTime(entryObj.getValue().getMessageEndTimeList().get(0));
								}
								if (entryObj.getValue().getGeneratedTimestampList().size() > 0) {
									freeTextObj.setGeneratedTimestamp(
											entryObj.getValue().getGeneratedTimestampList().get(0));
								}

								if (entryObj.getValue().getShowOverviewDisplayList().size() > 0) {

									ShowOverviewDisplay stateShowOverviewDisplay = entryObj.getValue()
											.getShowOverviewDisplayList().get(0);

									if (stateShowOverviewDisplay == ShowOverviewDisplay.TRUE) {
										freeTextObj.setShowOverviewDisplay(
												ars.protobuf.DHMessage.FreeText.ShowOverviewDisplay.TRUE);
									} else if (stateShowOverviewDisplay == ShowOverviewDisplay.FALSE) {
										freeTextObj.setShowOverviewDisplay(
												ars.protobuf.DHMessage.FreeText.ShowOverviewDisplay.FALSE);
									} else if (stateShowOverviewDisplay == ShowOverviewDisplay.ONLY) {
										freeTextObj.setShowOverviewDisplay(
												ars.protobuf.DHMessage.FreeText.ShowOverviewDisplay.ONLY);
									}
								}
								if (entryObj.getValue().getMessageTitleList().size() > 0) {
									freeTextObj.setMessageTitle(entryObj.getValue().getMessageTitleList().get(0));
								}

								if (entryObj.getValue().getMessagePriorityList().size() > 0) {

									MessagePriority stateMessagePriority = entryObj.getValue().getMessagePriorityList()
											.get(0);

									if (stateMessagePriority == MessagePriority.CALAMITY) {
										freeTextObj.setMessagePriority(
												ars.protobuf.DHMessage.FreeText.MessagePriority.CALAMITY);
									} else if (stateMessagePriority == MessagePriority.PTPROCESS) {
										freeTextObj.setMessagePriority(
												ars.protobuf.DHMessage.FreeText.MessagePriority.PTPROCESS);
									} else if (stateMessagePriority == MessagePriority.COMMERCIAL) {
										freeTextObj.setMessagePriority(
												ars.protobuf.DHMessage.FreeText.MessagePriority.COMMERCIAL);
									} else if (stateMessagePriority == MessagePriority.MISC) {
										freeTextObj.setMessagePriority(
												ars.protobuf.DHMessage.FreeText.MessagePriority.MISC);
									}
								}
								travelInfo.addFreeTexts(freeTextObj);
							}
						}
					}
				} else {
					SSBManager.getLoggerObject().log(Level.ALL, " There Is No FreeText ...." + " FreeText Size "
							+ generalMessageMapObj.size() + " " + LocalDateTime.now());
				}
			} else {

				SSBManager.getLoggerObject().log(Level.ALL, " RDR Not Interested In FreeText " + LocalDateTime.now());
			}

			JsonFormat.Printer jsonprinter = JsonFormat.printer();
			String payload = jsonprinter.print(travelInfo);

			// publish trip message
			SSBManager.publishTripMessage(payload, displayRegMessage);// sending JSONObject String
			SSBManager.getLoggerObject().log(Level.ALL, "  After Trip Publish   ...." + LocalDateTime.now());

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Prepare The Trip Information Failed  ...." + e.getMessage() + LocalDateTime.now());
		}
		return tripAvailableFlag;

	}

	@SuppressWarnings("unchecked")
	public static int getPublicNameDetails(DisplayRegistrationMessage displayRegMessage) {

		int publicNamesArraySize = -1;

		JSONObject jsonTop = new JSONObject();
		try {

			boolean IsDisplayHandlerKnowPublicName = false;
			JSONArray publicNameArray = new JSONArray();

			if ((displayRegMessage != null) && (publicNameMapObj.size() > 0)) {

				for (String stopCode : displayRegMessage.getStopCodeList()) {

					JSONObject jsonObj = new JSONObject();
					JSONArray stopCodeArray = new JSONArray();
					JSONArray publicNamePlaceArray = new JSONArray();
					JSONArray publicNameStopPlaceArray = new JSONArray();
					JSONArray publicNameQuayArray = new JSONArray();

					if (stopCodeNotAvailable == true) {

						IsDisplayHandlerKnowPublicName = true;
						for (Entry<String, com.ars.ODCC.connection.ODMessage.PublicName.Builder> publicNamedetails : publicNameMapObj
								.entrySet()) {

//								String stopCodeObj = publicNamedetails.getValue().getStopCodeList().get(0);
							String publicNamePlaceObj = null;
							String publicNameStopPlaceObj = null;
							String publicNameQuayObj = null;

							if (publicNamedetails.getValue().getPublicNamePlaceList().size() > 0) {
								publicNamePlaceObj = publicNamedetails.getValue().getPublicNamePlaceList().get(0);
							}
							if (publicNamedetails.getValue().getPublicNameStopPlaceList().size() > 0) {
								publicNameStopPlaceObj = publicNamedetails.getValue().getPublicNameStopPlaceList()
										.get(0);
							}
							if (publicNamedetails.getValue().getPublicNameQuayList().size() > 0) {
								publicNameQuayObj = publicNamedetails.getValue().getPublicNameQuayList().get(0);
							}

							stopCodeArray.add(stopCode);
							if (publicNamePlaceObj != null) {
								publicNamePlaceArray.add(publicNamePlaceObj);
							}
							if (publicNameStopPlaceObj != null) {
								publicNameStopPlaceArray.add(publicNameStopPlaceObj);
							}
							if (publicNameQuayObj != null) {
								publicNameQuayArray.add(publicNameQuayObj);
							}

							jsonObj.put("stopCode", stopCodeArray);
							jsonObj.put("publicNamePlace", publicNamePlaceArray);
							jsonObj.put("publicNameStopPlace", publicNameStopPlaceArray);
							jsonObj.put("publicNameQuay", publicNameQuayArray);

							publicNameArray.add(jsonObj);
//								stopCodeNotAvailable = false;

						}
					}

					else if (publicNameMapObj.containsKey(stopCode)) {

						IsDisplayHandlerKnowPublicName = true;
						for (Entry<String, com.ars.ODCC.connection.ODMessage.PublicName.Builder> publicNamedetails : publicNameMapObj
								.entrySet()) {

							if (stopCode.equals(publicNamedetails.getValue().getStopCodeList().get(0))) {

								String stopCodeObj = publicNamedetails.getValue().getStopCodeList().get(0);
								String publicNamePlaceObj = null;
								String publicNameStopPlaceObj = null;
								String publicNameQuayObj = null;

								stopCodeArray.add(stopCodeObj);

								if (publicNamedetails.getValue().getPublicNamePlaceList().size() > 0) {
									publicNamePlaceObj = publicNamedetails.getValue().getPublicNamePlaceList().get(0);
									publicNamePlaceArray.add(publicNamePlaceObj);
								}
								if (publicNamedetails.getValue().getPublicNameStopPlaceList().size() > 0) {
									publicNameStopPlaceObj = publicNamedetails.getValue().getPublicNameStopPlaceList()
											.get(0);
									publicNameStopPlaceArray.add(publicNameStopPlaceObj);
								}
								if (publicNamedetails.getValue().getPublicNameQuayList().size() > 0) {
									publicNameQuayObj = publicNamedetails.getValue().getPublicNameQuayList().get(0);
									publicNameQuayArray.add(publicNameQuayObj);
								}

								jsonObj.put("stopCode", stopCodeArray);
								jsonObj.put("publicNamePlace", publicNamePlaceArray);
								jsonObj.put("publicNameStopPlace", publicNameStopPlaceArray);
								jsonObj.put("publicNameQuay", publicNameQuayArray);

								publicNameArray.add(jsonObj);

							}
						}
						publicNamesArraySize = publicNameArray.size();

					}
				}
				if (IsDisplayHandlerKnowPublicName == true) { // contain public info

					jsonTop.put("publicNames", publicNameArray);
					SSBManager.getLoggerObject().log(Level.ALL, " RDR PublicName Sending Through Registration Message "
							+ jsonTop.toString() + LocalDateTime.now());

					ObjectMapper mapper = new ObjectMapper();
					String jsonString = mapper.writeValueAsString(jsonTop);
					SSBManager.publishPublicName(jsonString, displayRegMessage);

				} else if (IsDisplayHandlerKnowPublicName == false) {// don't have public name

					jsonTop.put("publicNames", publicNameArray);
					ObjectMapper mapper = new ObjectMapper();
					String jsonString = mapper.writeValueAsString(jsonTop);
					SSBManager.publishPublicName(jsonString, displayRegMessage);
				}
			} else if (publicNameMapObj.size() <= 0) {

				jsonTop.put("publicNames", publicNameArray);
				ObjectMapper mapper = new ObjectMapper();
				String jsonString = mapper.writeValueAsString(jsonTop);
				SSBManager.publishPublicName(jsonString, displayRegMessage);
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					"  Get The PublicName Information Failed  ...." + e.getMessage() + LocalDateTime.now());

		}
		return publicNamesArraySize;

	}

	public static ArrayList<com.ars.ODCC.connection.ODMessage.PublicName.Builder> readPublicNameProtoBufMsg(
			PublicName publicName) {
		ArrayList<com.ars.ODCC.connection.ODMessage.PublicName.Builder> publicNameList = new ArrayList<com.ars.ODCC.connection.ODMessage.PublicName.Builder>();

		try {
			if (publicName != null) {

				int publicNameSize = publicName.getStopCodeList().size();

				for (int i = 0; i < publicNameSize; i++) {

					stopCodeNotAvailable = false;

					com.ars.ODCC.connection.ODMessage.PublicName.Builder publicNameObj = com.ars.ODCC.connection.ODMessage.PublicName
							.newBuilder();

					if (publicName.getStopCodeList().size() > 0) {
						publicNameObj.addStopCode(publicName.getStopCode(i));
					}

					if (publicName.getPublicNamePlaceList().size() > 0) {
						publicNameObj.addPublicNamePlace(publicName.getPublicNamePlace(i));
					}

					if (publicName.getPublicNameStopPlaceList().size() > 0) {
						publicNameObj.addPublicNameStopPlace(publicName.getPublicNameStopPlace(i));
					}
					if (publicName.getPublicNameQuayList().size() > 0) {
						publicNameObj.addPublicNameQuay(publicName.getPublicNameQuay(i));
					}
					publicNameList.add(publicNameObj);

				}
				if (publicNameList.size() <= 0) {

					if (publicName.getStopCodeList().size() <= 0
							&& publicName.getPublicNameStopPlaceList().size() > 0) {

						stopCodeNotAvailable = true;
						com.ars.ODCC.connection.ODMessage.PublicName.Builder publicNameObj = com.ars.ODCC.connection.ODMessage.PublicName
								.newBuilder();

						publicNameObj.addStopCode("DUMMY");
						if (publicName.getPublicNamePlaceList().size() > 0) {
							publicNameObj.addPublicNamePlace(publicName.getPublicNamePlace(0));
						}

						if (publicName.getPublicNameStopPlaceList().size() > 0) {
							publicNameObj.addPublicNameStopPlace(publicName.getPublicNameStopPlace(0));
						}
						if (publicName.getPublicNameQuayList().size() > 0) {
							publicNameObj.addPublicNameQuay(publicName.getPublicNameQuay(0));
						}
						publicNameList.add(publicNameObj);
						SSBManager.getLoggerObject().log(Level.ALL,
								" Received Public Name Without Stop Code  ...." + LocalDateTime.now());

					}

				}
			}

			if (publicNameList.size() > 0) {
				addPublicNameToMap(publicNameList);

			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL, "Exception In Public Name : readPublicNameProtoBufMsg(): ...."
					+ e.getMessage() + LocalDateTime.now());
		}
		return publicNameList;

	}

	public static int addPublicNameToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.PublicName.Builder> publicNameList) {
		int publicNameSize = -1;

		try {
			if (publicNameList != null) {

				for (int i = 0; i < publicNameList.size(); i++) {
//					System.out.println(" i "+publicNameList.get(i));
					publicNameMapObj.put(publicNameList.get(i).getStopCode(0), publicNameList.get(i));
				}

				publicNameSize = publicNameMapObj.size();
				SSBManager.getLoggerObject().log(Level.ALL, " PublicName Added To Map ...." + LocalDateTime.now());
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Insert The public Name Into Map Failed ()  ...." + e.getMessage() + LocalDateTime.now());
		}
		return publicNameSize;
	}

	public static int[] removeOldTrips(Integer currentTime) {

		int removeTripSize = -1;
		
		int busTime[] = new int[3] ;
		try {

			long currentTimeLong = Long.valueOf(currentTime);
			currentDay = LocalDateTime.ofInstant(Instant.ofEpochSecond(currentTimeLong), ZoneId.systemDefault());

			if (passTimeMapObj.size() > 0 && ((dhCurrentDay.getYear() == 2000)
					|| (dhCurrentDay.getDayOfMonth() != currentDay.getDayOfMonth()))) {

				firstBusTimeCurrentday = currentTime + 86400;
				dhCurrentDay = currentDay;
//				SSBManager.getLoggerObject().log(Level.ALL, " First Bus Departure Time  "
//						+ firstBusTimeCurrentday + "  " + LocalDateTime.now());

				for (Entry<String, Builder> entryObj : passTimeMapObj.entrySet()) {

					int epochDepartureTime;
					if (entryObj.getValue().getExpectedDepartureTimeList().size() > 0) {

						epochDepartureTime =  new Integer(entryObj.getValue().getExpectedDepartureTime(0));
					} else {
						epochDepartureTime = new Integer(entryObj.getValue().getExpectedArrivalTime(0));// first way.
					}

					Long epochDepTime = Long.valueOf(epochDepartureTime);
					LocalDateTime departureTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochDepTime),
							ZoneId.systemDefault());

					int departureTimeDay = departureTime.getDayOfMonth();
					if (dhCurrentDay.getDayOfMonth() == departureTimeDay) {

//						SSBManager.getLoggerObject().log(Level.ALL, " 2 ==> " + dhCurrentDay.getDayOfMonth() + " <==> "
//								+ departureTime.getDayOfMonth() + "  " + LocalDateTime.now());

						if (epochDepartureTime > lastBusTimeCurrentday) { // get last trip
							lastBusTimeCurrentday = epochDepartureTime;// make UTC
						}
						if (epochDepartureTime < firstBusTimeCurrentday) { // get last trip
							firstBusTimeCurrentday = epochDepartureTime;// make UTC
						}
					}
				}
			}
			
			SSBManager.getLoggerObject().log(Level.ALL, " Last Bus Departure Time : " + lastBusTimeCurrentday
					+ "  " + " First Bus Departure Time : " + firstBusTimeCurrentday + "  " + LocalDateTime.now());

			if (passTimeMapObj.size() > 0) {

				ArrayList<String> oldTripList = new ArrayList<String>();
				int timeInterval = SSBManager.getConfig().cleanupSettings("removeTripOlderthanInSec");

				for (Entry<String, Builder> entryObj : passTimeMapObj.entrySet()) {

					Integer expectedDepTime = 0;

					if (entryObj.getValue().getExpectedDepartureTimeList().size() > 0) {

						expectedDepTime = entryObj.getValue().getExpectedDepartureTimeList().get(0);

					} else {

						expectedDepTime = entryObj.getValue().getExpectedArrivalTimeList().get(0);
					}

					int timeObj = currentTime - expectedDepTime;

					if (timeObj > timeInterval) {

						String passtimeHash = entryObj.getKey();

						if (passtimeHash.length() > 0) {

							oldTripList.add(passtimeHash);
						}
					}
				}

				if (oldTripList.size() > 0) {

					for (int i = 0; i < oldTripList.size(); i++) {

						passTimeMapObj.remove(oldTripList.get(i));
					}
					SSBManager.getLoggerObject().log(Level.ALL,
							" Old Trip Removed And Map Updated  ...." + LocalDateTime.now());
					dumpPassageTimesMsgToPassageFile();
					SSBManager.getLoggerObject().log(Level.ALL,
							" Old Passage File Updated  ...." + LocalDateTime.now());
					SSBManager.getLoggerObject().log(Level.ALL, " Old Trip Removed  ...." + LocalDateTime.now());
				}
				removeTripSize = oldTripList.size();

			}
			
			busTime[0] = removeTripSize;
			busTime[1] = lastBusTimeCurrentday;
			busTime[2] = firstBusTimeCurrentday;

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Remove The Old Trip From Map Failed ()  ...." + e.getMessage() + LocalDateTime.now());
		}
		return busTime;

	}

	/**
	 * remove old freetext
	 * 
	 * @param currentTime
	 */

	public static int removeOldFreeText(Integer currentTime) {
		int removeGeneralMessageSize = -1;

		try {
			if (generalMessageMapObj.size() > 0) {

				ArrayList<String> oldFreeTextList = new ArrayList<String>();
				int timeInterval = SSBManager.getConfig().cleanupSettings("removeFreeTextOlderthanInSec");

				for (Entry<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> entryObj : generalMessageMapObj
						.entrySet()) {

					int endTimelen = entryObj.getValue().getMessageEndTimeList().size();

					if (endTimelen > 0) {

						int endTime = entryObj.getValue().getMessageEndTimeList().get(0);

						int timeObj = currentTime - endTime;

						if (timeObj > timeInterval) {

							String messageHash = entryObj.getKey();

							if (messageHash.length() > 0) {

								oldFreeTextList.add(messageHash);
//							
							}
						}
					}
				}
				if (oldFreeTextList.size() > 0) {

					for (int j = 0; j < oldFreeTextList.size(); j++) {

						generalMessageMapObj.remove(oldFreeTextList.get(j));
					}

					boolean fileMode = SSBManager.getConfig().getFileMode();
					if (fileMode == true) {
						dumpGeneralMsgToFreeTextFile();

					}
					SSBManager.getLoggerObject().log(Level.ALL, " Old FreeText Removed  ...." + LocalDateTime.now());
				}
				removeGeneralMessageSize = oldFreeTextList.size();
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Remove The Old FreeText From Map Failed ()  ...." + e.getMessage() + LocalDateTime.now());
		}
		return removeGeneralMessageSize;

	}

	public static int loadGeneralMessageToMap() {

		int onRestartReadFreeTextFileFlag = -1;

		ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> generalMsgList = new ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder>();
		FileReader fileReaderObj = null;
		try {

			String freeTextPath = SSBManager.getBaseFilePath(SSBManager.getConfig().getDhConfig().getFreeTextFileWithPath());
			File isFileAvailableObj = new File(freeTextPath);

			if (isFileAvailableObj.exists()) {

				fileReaderObj = new FileReader(freeTextPath);
				int rowcnt = 0;
				Scanner scannerObj = new Scanner(fileReaderObj);

				while (scannerObj.hasNextLine()) {
					String generalInfoLine = scannerObj.nextLine();
					com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder generalMsgObj = com.ars.ODCC.connection.ODMessage.GeneralMessage
							.newBuilder();

					if (rowcnt > 0) {
						String generalInfo[] = generalInfoLine.split(",");

						if (generalInfo.length > 0) {

							int generalmessageTypeNum = -1;
							String messageContent = null;
							int messagestartTime = -1;
							int messageEndTime = -1;
							int generatedtimestamp = -1;
							String messageHash = generalInfo[0];
							int showOverviewDisplayNum = -1;
							String messageTitle = null;
							int messagePriorityNum = -1;

							if (generalInfo[1].length() > 0) {
								String generalmessageType = generalInfo[1];
								generalmessageTypeNum = getGeneralMsgType(generalmessageType);
							}
							if (generalInfo[2].length() > 0) {
								messageContent = generalInfo[2];
							}
							if (generalInfo[3].length() > 0) {
								messagestartTime = Integer.parseInt(generalInfo[3]);
							}
							if (generalInfo[4].length() > 0) {
								messageEndTime = Integer.parseInt(generalInfo[4]);
							}
							if (generalInfo[5].length() > 0) {
								generatedtimestamp = Integer.parseInt(generalInfo[5]);

							}
							if (generalInfo[6].length() > 0) {
								String showOverviewDisplay = generalInfo[6];
								showOverviewDisplayNum = getShowOverviewDisplay(showOverviewDisplay);

							}
							if (generalInfo[7].length() > 0) {
								messageTitle = generalInfo[7];
							}
							if (generalInfo[8].length() > 0) {
								String messagePriority = generalInfo[8];
								messagePriorityNum = getMessagePriority(messagePriority);
							}

							if (messageHash != null) {
								generalMsgObj.addMessageHash(messageHash);
							}
							if (generalmessageTypeNum != -1) {
								generalMsgObj.addGeneralmessageTypeValue(generalmessageTypeNum);
							}
							if (messageContent != null) {
								generalMsgObj.addMessageContent(messageContent);
							}
							if (messagestartTime != -1) {
								generalMsgObj.addMessageStartTime(messagestartTime);

							}
							if (messageEndTime != -1) {
								generalMsgObj.addMessageEndTime(messageEndTime);
							}
							if (generatedtimestamp != -1) {
								generalMsgObj.addGeneratedTimestamp(generatedtimestamp);
							}
							if (showOverviewDisplayNum != -1) {
								generalMsgObj.addShowOverviewDisplayValue(showOverviewDisplayNum);
							}
							if (messageTitle != null) {
								generalMsgObj.addMessageTitle(messageTitle);
							}
							if (messagePriorityNum != -1) {
								generalMsgObj.addMessagePriorityValue(messagePriorityNum);
							}

							generalMsgList.add(generalMsgObj);
						}

					} else {
						rowcnt++;
					}
				}
				// reading the list and add to map
				addGeneralMsgToMap(generalMsgList);
			} else {
				SSBManager.getLoggerObject().log(Level.ALL,
						" FreeText File Does Not Exists  ...." + LocalDateTime.now());
			}
		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					"Exception in loadGeneralMessageToMap ()  ...." + e.getMessage() + LocalDateTime.now());

		} finally {
			try {
				if (fileReaderObj != null) {
					fileReaderObj.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SSBManager.getLoggerObject().log(Level.ALL,
						" Close FreeText File Connection Failed  ...." + e.getMessage() + LocalDateTime.now());
			}
		}
		onRestartReadFreeTextFileFlag = generalMsgList.size();
		return onRestartReadFreeTextFileFlag;
	}

	/**
	 * add general message to map
	 */
	private static void addGeneralMsgToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> generaMsgList) {
		try {
			if (generaMsgList != null) {

				for (int i = 0; i < generaMsgList.size(); i++) {
					generalMessageMapObj.put(generaMsgList.get(i).getMessageHash(0), generaMsgList.get(i));
				}

				SSBManager.getLoggerObject().log(Level.ALL, " FreeText Info BackUp Done .... " + LocalDateTime.now());
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Insert The General Message To Map Failed  ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	private static int getGeneralMsgType(String generalMsgType) {

		switch (generalMsgType) {
		case "GENERAL":
			return 0;
		case "OVERRULE":
			return 1;
		case "BLANC":
			return 2;
		default:
			return 0;
		}
	}

	private static int getShowOverviewDisplay(String showOverviewDisplayValue) {

		switch (showOverviewDisplayValue) {
		case "TRUE ":
			return 0;
		case "FALSE":
			return 1;
		case "ONLY":
			return 2;
		default:
			return 0;
		}
	}

	private static int getMessagePriority(String messagePriority) {

		switch (messagePriority) {
		case "CALAMITY":
			return 0;
		case "PTPROCESS":
			return 1;
		case "COMMERCIAL":
			return 2;
		case "MISC":
			return 3;
		default:
			return 0;
		}
	}

	/**
	 * Load PassTimes object To Map
	 */
	@SuppressWarnings("null")
	public static int loadPassTimesToMap() {

		int onRestartReadPassageFileFlag = -1;

		ArrayList<com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passingTimeList = new ArrayList<Builder>();
		FileReader fileReaderObj = null;
		try {

			String passageFilePath = SSBManager.getBaseFilePath(SSBManager.getConfig().getDhConfig().getPassageFileWithPath());
			File isFileAvailableObj = new File(passageFilePath);

			if (isFileAvailableObj.exists()) {

				fileReaderObj = new FileReader(passageFilePath);
				int rowcnt = 0;

				Scanner scannerObj = new Scanner(fileReaderObj);
				while (scannerObj.hasNextLine()) {
					String travelinfo = scannerObj.nextLine();
					com.ars.ODCC.connection.ODMessage.PassingTimes.Builder passingTimesMsgObj = com.ars.ODCC.connection.ODMessage.PassingTimes
							.newBuilder();

					if (rowcnt > 0) {

						StringBuilder sb = new StringBuilder();
						sb.append(travelinfo);
						String travelInfo[] = travelinfo.split(",");
						int len = travelinfo.length();
						char lastCharacter = travelinfo.charAt(len - 1);
						if (lastCharacter == ',') {

							sb.replace(len, len, " ,");
							String info[] = sb.toString().split(",");
							travelInfo = info;
						}

						if (travelInfo.length > 0) {

							String passTimeHash = travelInfo[0];

							int targetArrivalTime = -1;
							int targetDepartureTime = -1;
							int expectedArrivalTime = -1;
							int expectedDepartureTime = -1;
							int numberOfCoaches = -1;
							int tripStopStatusNum = -1;
							int transportTypeNum = -1;
							String wheelchairAccessible = null;
							String isTimingStop = null;
							String stopCode = null;
							int showCancelledTripNum = -1;
							String blockCode = null;
							int occupancy = -1;
							String linePublicNumber = null;
							String sideCode = null;
							int lineDirection = -1;
							String lineColor = null;
							String lineTextColor = null;
							String lineIcon = null;
							String destinationColor = null;
							String destinationTextColor = null;
							String destinationIcon = null;
							int generatedTimestamp = -1;
							int journeyNumber = -1;

							if (travelInfo[1].length() > 0) {
								targetArrivalTime = Integer.parseInt(travelInfo[1].trim());
							}
							if (travelInfo[2].length() > 0) {
								targetDepartureTime = Integer.parseInt(travelInfo[2].trim());
							}
							if (travelInfo[3].length() > 0) {
								expectedArrivalTime = Integer.parseInt(travelInfo[3].trim());
							}
							if (travelInfo[4].length() > 0) {
								expectedDepartureTime = Integer.parseInt(travelInfo[4].trim());
							}
							if (travelInfo[5].length() > 0) {
								numberOfCoaches = Integer.parseInt(travelInfo[5].trim());
							}
							if (travelInfo[6].length() > 0) {
								String tripStopStatus = travelInfo[6];
								tripStopStatusNum = getTripStopStatus(tripStopStatus);
							}
							if (travelInfo[7].length() > 0) {
								String transportType = travelInfo[7];
								transportTypeNum = getTransportType(transportType);
							}

							if (travelInfo[8].length() > 0) {
//								wheelchairAccessible = Boolean.parseBoolean(travelInfo[8]);
								wheelchairAccessible = travelInfo[8];
							}
							if (travelInfo[9].length() > 0) {
//								isTimingStop = Boolean.parseBoolean(travelInfo[9]);
								isTimingStop = travelInfo[9];
							}
							if (travelInfo[10].length() > 0) {
								stopCode = travelInfo[10];

							}
							if (travelInfo[11].length() > 0) {
								String showCancelledTrip = travelInfo[11];
								showCancelledTripNum = getShowCancelledTrip(showCancelledTrip);
							}
							if (travelInfo[12].length() > 0) {
								blockCode = travelInfo[12];
							}
							if (travelInfo[13].length() > 0) {
								occupancy = Integer.parseInt(travelInfo[13].trim());
							}

							if (travelInfo[14].length() > 0) {
								linePublicNumber = travelInfo[14];
							}
							if (travelInfo[15].length() > 0) {
								sideCode = travelInfo[15];
							}
							if (travelInfo[16].length() > 0) {
								lineDirection = Integer.parseInt(travelInfo[16].trim());
							}

							if (travelInfo[17].length() > 0) {
								lineColor = travelInfo[17];
							}
							if (travelInfo[18].length() > 0) {
								lineTextColor = travelInfo[18];
							}
							if (travelInfo[19].length() > 0) {
								lineIcon = travelInfo[19];
							}
							if (travelInfo[20].length() > 0) {
								destinationColor = travelInfo[20];
							}
							if (travelInfo[21].length() > 0) {
								destinationTextColor = travelInfo[21];
							}
							if (travelInfo[22].length() > 0) {
								destinationIcon = travelInfo[22];
							}
							if (travelInfo[23].length() > 0) {
								generatedTimestamp = Integer.parseInt(travelInfo[23].trim());

							}
							if (travelInfo[24].length() > 0) {
								journeyNumber = Integer.parseInt(travelInfo[24].trim());

							}

							ArrayList<String> desNameObj = new ArrayList<String>();
							ArrayList<String> desDetailsObj = new ArrayList<String>();
							int k = 0;
							for (int i = 25; i < travelInfo.length; i++) {

								if (i % 2 == 0) {
									if (travelInfo[i].equals("")) {
										desDetailsObj.add(" ");
									} else {
										desDetailsObj.add(travelInfo[i]);

									}
									k++;

								} else {
									if (travelInfo[i].equals("")) {
										desNameObj.add(" ");
									} else {
										desNameObj.add(travelInfo[i]);

									}
								}
							}

							Destination.Builder destinationObj = Destination.newBuilder();

							for (int j = 0; desNameObj.size() > j; j++) {

								if (desNameObj.get(j).length() > 0) {
									destinationObj.addDestinationName(desNameObj.get(j));
								}

								if (desDetailsObj.get(j).length() > 0) {
									destinationObj.addDestinationDetail(desDetailsObj.get(j));
								}
							}

							passingTimesMsgObj.addDestinations(destinationObj);

							passingTimesMsgObj.addPassTimeHash(passTimeHash);
							if (targetArrivalTime != -1) {
								passingTimesMsgObj.addTargetArrivalTime(targetArrivalTime);

							}
							if (targetDepartureTime != -1) {
								passingTimesMsgObj.addTargetDepartureTime(targetDepartureTime);
							}
							if (expectedArrivalTime != -1) {
								passingTimesMsgObj.addExpectedArrivalTime(expectedArrivalTime);
							}
							if (expectedDepartureTime != -1) {
								passingTimesMsgObj.addExpectedDepartureTime(expectedDepartureTime);

							}
							if (numberOfCoaches != -1) {
								passingTimesMsgObj.addNumberOfCoaches(numberOfCoaches);
							}

							if (tripStopStatusNum != -1) {
								passingTimesMsgObj.addTripStopStatusValue(tripStopStatusNum);
							}
							if (transportTypeNum != -1) {
								passingTimesMsgObj.addTransportTypeValue(transportTypeNum);

							}
							if (wheelchairAccessible != null) {

								passingTimesMsgObj.addWheelchairAccessible(Boolean.parseBoolean(wheelchairAccessible));
							}
							if (isTimingStop != null) {
								passingTimesMsgObj.addIsTimingStop(Boolean.parseBoolean(isTimingStop));
							}
							if (stopCode != null) {
								passingTimesMsgObj.addStopCode(stopCode);

							}
							if (showCancelledTripNum != -1) {
								passingTimesMsgObj.addShowCancelledTripValue(showCancelledTripNum);
							}
							if (blockCode != null) {
								passingTimesMsgObj.addBlockCode(blockCode);

							}
							if (occupancy != -1) {
								passingTimesMsgObj.addOccupancy(occupancy);
							}
							if (linePublicNumber != null) {
								passingTimesMsgObj.addLinePublicNumber(linePublicNumber);
							}
							if (sideCode != null) {
								passingTimesMsgObj.addSideCode(sideCode);
							}
							if (lineDirection != -1) {
								passingTimesMsgObj.addLineDirection(lineDirection);
							}
							if (lineColor != null) {
								passingTimesMsgObj.addLineColor(lineColor);

							}
							if (lineTextColor != null) {
								passingTimesMsgObj.addLineTextColor(lineTextColor);
							}
							if (lineIcon != null) {
								passingTimesMsgObj.addLineIcon(lineIcon);
							}
							if (destinationColor != null) {
								passingTimesMsgObj.addDestinationColor(destinationColor);
							}
							if (destinationTextColor != null) {
								passingTimesMsgObj.addDestinationTextColor(destinationTextColor);

							}
							if (destinationIcon != null) {
								passingTimesMsgObj.addDestinationIcon(destinationIcon);

							}
							if (generatedTimestamp != -1) {
								passingTimesMsgObj.addGeneratedTimestamp(generatedTimestamp);

							}
							if (journeyNumber != -1) {
								passingTimesMsgObj.addJourneyNumber(journeyNumber);

							}
							passingTimeList.add(passingTimesMsgObj);
						}

					} else {
						rowcnt++;
					}
				}
				// reading the list and add map
				addPassTimesMessageToMap(passingTimeList);
			}

			else {
				SSBManager.getLoggerObject().log(Level.ALL, "Passage File Does Not Exists  ...." + LocalDateTime.now());
			}

		} catch (Exception e)

		{
			SSBManager.getLoggerObject().log(Level.ALL,
					"Insert The PassTimes Into Map Failed  ...." + e.getMessage() + LocalDateTime.now());

		} finally {
			try {
				if (fileReaderObj != null) {
					fileReaderObj.close();

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SSBManager.getLoggerObject().log(Level.ALL,
						" Close File Connection Failed  ...." + e.getMessage() + LocalDateTime.now());
			}
		}
		onRestartReadPassageFileFlag = passingTimeList.size();
		return onRestartReadPassageFileFlag;
	}

	/**
	 * Adding the passtimes list to map
	 * 
	 * @param passingTimeList
	 */
	private static void addPassTimesMessageToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passingTimeList) {
		try {
			if (passingTimeList != null) {

				for (int i = 0; i < passingTimeList.size(); i++) {
					passTimeMapObj.put(passingTimeList.get(i).getPassTimeHash(0), passingTimeList.get(i));
				}
				SSBManager.getLoggerObject().log(Level.ALL, " Trip Info BackUp Done ...." + LocalDateTime.now());
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Add The PassTimes Into Map Failed  .... " + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * get the tripStopStatus
	 * 
	 * @param tripStopStatus
	 * @return
	 */
	private static int getTripStopStatus(String tripStopStatus) {

		switch (tripStopStatus) {
		case "PLANNED":
			return 0;
		case "DRIVING":
			return 1;
		case "CANCELLED":
			return 2;
		case "ARRIVED":
			return 3;
		case "PASSED":
			return 4;
		case "UNKNOWN":
			return 5;
		default:
			return 0;
		}
	}

	private static TripStopStatus getTripStopStatus(int tripStopStatus) {

		switch (tripStopStatus) {
		case 0:
			return TripStopStatus.PLANNED;
		case 1:
			return TripStopStatus.DRIVING;
		case 2:
			return TripStopStatus.CANCELLED;
		case 3:
			return TripStopStatus.ARRIVED;
		case 4:
			return TripStopStatus.PASSED;
		case 5:
			return TripStopStatus.UNKNOWN;
		default:
			return TripStopStatus.PLANNED;
		}
	}

	/**
	 * get the TransportType
	 * 
	 * @param transportType
	 * @return
	 */
	private static int getTransportType(String transportType) {

		switch (transportType) {
		case "BUS":
			return 0;
		case "TRAM":
			return 1;
		case "METRO":
			return 2;
		case "TRAIN":
			return 4;
		case "BOAT":
			return 5;
		default:
			return 0;
		}
	}

	/**
	 * get ShowCancelledTrip
	 * 
	 * @param showCancelledTrip
	 * @return
	 */
	private static int getShowCancelledTrip(String showCancelledTrip) {

		switch (showCancelledTrip) {
		case "TRUE":
			return 0;
		case "FALSE":
			return 1;
		case "MESSAGE":
			return 2;
		default:
			return 0;
		}
	}

	/**
	 * Reading GeneralMessageRemove
	 * 
	 * @param generalMessageRemove
	 */
	public static ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder> readGeneralMessageRemove(
			GeneralMessageRemove generalMessageRemove) {
		ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder> generaMessageRemoveList = new ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder>();

		try {
			if (generalMessageRemove != null) {
				int generalMsgRemoveSize = generalMessageRemove.getMessageHashList().size();

				for (int j = 0; j < generalMsgRemoveSize; j++) {
					com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder generalMessageRemoveObj = com.ars.ODCC.connection.ODMessage.GeneralMessageRemove
							.newBuilder();

					generalMessageRemoveObj.addMessageHash(generalMessageRemove.getMessageHashList().get(j));
					if (generalMessageRemove.getGeneratedTimestampList().size() > 0) {
						generalMessageRemoveObj.addGeneratedTimestamp(generalMessageRemove.getGeneratedTimestamp(j));
					}
					generaMessageRemoveList.add(generalMessageRemoveObj);
				}
				addGeneralMessageRemoveToMap(generaMessageRemoveList);
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Exception in ListContainer: readGeneralMessageRemove() : .... " + e.getMessage()
							+ LocalDateTime.now());
		}
		return generaMessageRemoveList; // new

	}

	/**
	 * Add the GeneralMessageRemove To Map
	 * 
	 * @param generaMessageRemoveList
	 */
	public static int addGeneralMessageRemoveToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder> generaMessageRemoveList) {

		int generalMessageRemove = -1;
		try {
			if (generaMessageRemoveList != null) {

				for (int i = 0; i < generaMessageRemoveList.size(); i++) {

					generalMessageRemoveMapObj.put(generaMessageRemoveList.get(i).getMessageHash(0),
							generaMessageRemoveList.get(i));
				}
				SSBManager.getLoggerObject().log(Level.ALL,
						" GeneralMessageRemove Added To Map ...." + LocalDateTime.now());
				generalMessageRemove = generalMessageRemoveMapObj.size();
				removeGeneralMessageFromMap();
			}
		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Add The addGeneralMessageRemoveToMap To Map Failed ...." + e.getMessage() + LocalDateTime.now());
		}
		return generalMessageRemove;

	}

	/**
	 * Remove the GeneralMessage From Map
	 */
	public static void removeGeneralMessageFromMap() {

		try {
			if (generalMessageRemoveMapObj != null) {
				for (Entry<String, com.ars.ODCC.connection.ODMessage.GeneralMessageRemove.Builder> entryobj : generalMessageRemoveMapObj
						.entrySet()) {

					String messagehashKey = entryobj.getValue().getMessageHashList().get(0);
					removeGeneralMessage(messagehashKey);
				}
				boolean fileMode = SSBManager.getConfig().getFileMode();
				if (fileMode == true) {
					// after remove the message from generalmessagemap again creating freetex file.
					dumpGeneralMsgToFreeTextFile();

				}

			}
		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Remove The General Message To Map Failed ...." + e.getMessage() + LocalDateTime.now());
		}
	}

	/**
	 * Remove GeneralMessage From Map Based On The MessageHashKey
	 * 
	 * @param messagehashKey
	 */
	public static void removeGeneralMessage(String messagehashKey) {
		try {

			if (generalMessageMapObj != null) {
				for (Entry<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> entryobj : generalMessageMapObj
						.entrySet()) {

					if (messagehashKey.equals(entryobj.getKey())) {

						// Remove this entry from HashMap
						generalMessageMapObj.remove(messagehashKey);
						SSBManager.getLoggerObject().log(Level.ALL,
								" FreeText Message Removed From FreeText File And FreeText File Updated ...."
										+ LocalDateTime.now());
						break;
					}
				}
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Remove The General Message From Map Failed ...." + e.getMessage() + LocalDateTime.now());

		}
	}

	/**
	 * Reading The Travel Info From GeneralMessage
	 * 
	 * @param generalMessage
	 */
	public static ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> readGeneralMessage(
			GeneralMessage generalMessage) {
		ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> generaMessageList = new ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder>();

		try {

			if (generalMessage != null) {

				int generalMessageSize = generalMessage.getMessageHashList().size();

				for (int i = 0; i < generalMessageSize; i++) {

					com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder generalMessageObj = com.ars.ODCC.connection.ODMessage.GeneralMessage
							.newBuilder();

					generalMessageObj.addMessageHash(generalMessage.getMessageHash(i));
					if (generalMessage.getGeneralmessageTypeList().size() > 0) {
						generalMessageObj.addGeneralmessageType(generalMessage.getGeneralmessageType(i));
					}
//					if (generalMessage.getMessageContentList().size() > 0) {
//						generalMessageObj.addMessageContent(generalMessage.getMessageContent(i));
//					}
					if (generalMessage.getMessageContentList().size() > 0
							&& (!generalMessage.getMessageContentList().contains(""))) {
						generalMessageObj.addMessageContent(generalMessage.getMessageContent(i));
					}
					if (generalMessage.getMessageStartTimeList().size() > 0) {
						generalMessageObj.addMessageStartTime(generalMessage.getMessageStartTime(i));
					}
					if (generalMessage.getMessageEndTimeList().size() > 0) {
						generalMessageObj.addMessageEndTime(generalMessage.getMessageEndTime(i));
					}
					if (generalMessage.getGeneratedTimestampList().size() > 0) {
						generalMessageObj.addGeneratedTimestamp(generalMessage.getGeneratedTimestamp(i));
					}
					if (generalMessage.getShowOverviewDisplayList().size() > 0) {
						generalMessageObj.addShowOverviewDisplay(generalMessage.getShowOverviewDisplay(i));
					}
//					if (generalMessage.getMessageTitleList().size() > 0) {
//						generalMessageObj.addMessageTitle(generalMessage.getMessageTitle(i));
//					}
					if (generalMessage.getMessageTitleList().size() > 0
							&& (!generalMessage.getMessageTitleList().contains(""))) {
						generalMessageObj.addMessageTitle(generalMessage.getMessageTitle(i));
					}
					if (generalMessage.getMessagePriorityList().size() > 0) {
						generalMessageObj.addMessagePriority(generalMessage.getMessagePriority(i));
					}

					generaMessageList.add(generalMessageObj);

				}
				addGeneralMessageToMap(generaMessageList);

			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					"Exception in GeneralMessage: readGeneralMessage(): ...." + e.getMessage() + LocalDateTime.now());
		}
		return generaMessageList;

	}

	/**
	 * Reading The Travel Info From PassTimes
	 * 
	 * @param passingTimesMsg
	 */
	public static ArrayList<Builder> readPassingTimesProtoBufMsg(
			com.ars.ODCC.connection.ODMessage.PassingTimes passingTimesMsg) {

		ArrayList<com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passingTimeList = new ArrayList<Builder>();
		try {

			if (passingTimesMsg != null) {

				int tripLengthObj = passingTimesMsg.getPassTimeHashList().size();// passingTimesMsg. Size();

				for (int i = 0; i < tripLengthObj; i++) {

					com.ars.ODCC.connection.ODMessage.PassingTimes.Builder passTimeObj = com.ars.ODCC.connection.ODMessage.PassingTimes
							.newBuilder();

					passTimeObj.addPassTimeHash(passingTimesMsg.getPassTimeHash(i));

					if (passingTimesMsg.getTargetArrivalTimeList().size() > 0) {
						passTimeObj.addTargetArrivalTime(passingTimesMsg.getTargetArrivalTime(i));
					}
					if (passingTimesMsg.getTargetDepartureTimeList().size() > 0) {
						passTimeObj.addTargetDepartureTime(passingTimesMsg.getTargetDepartureTime(i));
					}
					if (passingTimesMsg.getExpectedArrivalTimeList().size() > 0) {
						passTimeObj.addExpectedArrivalTime(passingTimesMsg.getExpectedArrivalTime(i));

					}
					if (passingTimesMsg.getExpectedDepartureTimeList().size() > 0) {
						passTimeObj.addExpectedDepartureTime(passingTimesMsg.getExpectedDepartureTime(i));

					}

					if (passingTimesMsg.getNumberOfCoachesList().size() > 0) {
						passTimeObj.addNumberOfCoaches(passingTimesMsg.getNumberOfCoaches(i));

					}
					if (passingTimesMsg.getTripStopStatusList().size() > 0) {
						passTimeObj.addTripStopStatusValue(passingTimesMsg.getTripStopStatusValue(i));
						
						
						int tripStatus =passingTimesMsg.getTripStopStatusValue(i);
						TripStopStatus stateOfTrip = getTripStopStatus(tripStatus);					
						
						
						if( stateOfTrip == TripStopStatus.CANCELLED) {
							tripStopStatusFlag = true;
						}

					}
					if (passingTimesMsg.getTransportTypeList().size() > 0) {
						passTimeObj.addTransportTypeValue(passingTimesMsg.getTransportTypeValue(i));

					}
					if (passingTimesMsg.getWheelchairAccessibleList().size() > 0) {
						passTimeObj.addWheelchairAccessible(passingTimesMsg.getWheelchairAccessible(i));

					}
					if (passingTimesMsg.getIsTimingStopList().size() > 0) {
						passTimeObj.addIsTimingStop(passingTimesMsg.getIsTimingStop(i));

					}
//					if (passingTimesMsg.getStopCodeList().size() > 0) {
//						passTimeObj.addStopCode(passingTimesMsg.getStopCode(i));
//
//					}
					if (passingTimesMsg.getStopCodeList().size() > 0
							&& (!passingTimesMsg.getStopCodeList().contains(""))) {
						passTimeObj.addStopCode(passingTimesMsg.getStopCode(i));

					}
					if (passingTimesMsg.getDestinationsList().size() > 0) {
						passTimeObj.addDestinations(passingTimesMsg.getDestinations(i));
					}
					if (passingTimesMsg.getShowCancelledTripList().size() > 0) {
						passTimeObj.addShowCancelledTrip(passingTimesMsg.getShowCancelledTrip(i));
					}
//					if (passingTimesMsg.getBlockCodeList().size() > 0) {
//						passTimeObj.addBlockCode(passingTimesMsg.getBlockCode(i));
//					}
//					System.out.println(passingTimesMsg.getBlockCodeList().contains(""));
					if (passingTimesMsg.getBlockCodeList().size() > 0
							&& (!passingTimesMsg.getBlockCodeList().contains(""))) {
						passTimeObj.addBlockCode(passingTimesMsg.getBlockCode(i));
					}
					if (passingTimesMsg.getOccupancyList().size() > 0) {
						passTimeObj.addOccupancy(passingTimesMsg.getOccupancy(i));
					}
//					if (passingTimesMsg.getLinePublicNumberList().size() > 0) {
//						passTimeObj.addLinePublicNumber(passingTimesMsg.getLinePublicNumber(i));
//					}
					if (passingTimesMsg.getLinePublicNumberList().size() > 0
							&& (!passingTimesMsg.getLinePublicNumberList().contains(""))) {
						passTimeObj.addLinePublicNumber(passingTimesMsg.getLinePublicNumber(i));
					}
//					if (passingTimesMsg.getSideCodeList().size() > 0) {
//						passTimeObj.addSideCode(passingTimesMsg.getSideCode(i));	
//					}
					if (passingTimesMsg.getSideCodeList().size() > 0
							&& (!passingTimesMsg.getSideCodeList().contains(""))) {
						passTimeObj.addSideCode(passingTimesMsg.getSideCode(i));
					}
					if (passingTimesMsg.getLineDirectionList().size() > 0) {
						passTimeObj.addLineDirection(passingTimesMsg.getLineDirection(i));
					}
//					if (passingTimesMsg.getLineColorList().size() > 0) {
//						passTimeObj.addLineColor(passingTimesMsg.getLineColor(i));
//					}
					if ((passingTimesMsg.getLineColorList().size()) > 0
							&& (!passingTimesMsg.getLineColorList().contains(""))) {
						passTimeObj.addLineColor(passingTimesMsg.getLineColor(i));
					}
//					if (passingTimesMsg.getLineTextColorList().size() > 0) {
//						passTimeObj.addLineTextColor(passingTimesMsg.getLineTextColor(i));
//					}
					if (passingTimesMsg.getLineTextColorList().size() > 0
							&& (!passingTimesMsg.getLineTextColorList().contains(""))) {
						passTimeObj.addLineTextColor(passingTimesMsg.getLineTextColor(i));
					}
//					if (passingTimesMsg.getLineIconList().size() > 0) {
//						passTimeObj.addLineIcon(passingTimesMsg.getLineIcon(i));
//					}
					if (passingTimesMsg.getLineIconList().size() > 0
							&& (!passingTimesMsg.getLineIconList().contains(""))) {
						passTimeObj.addLineIcon(passingTimesMsg.getLineIcon(i));
					}
//					if (passingTimesMsg.getDestinationColorList().size() > 0) {
//						passTimeObj.addDestinationColor(passingTimesMsg.getDestinationColor(i));
//					}
					if (passingTimesMsg.getDestinationColorList().size() > 0
							&& (!passingTimesMsg.getDestinationColorList().contains(""))) {
						passTimeObj.addDestinationColor(passingTimesMsg.getDestinationColor(i));
					}
//					if (passingTimesMsg.getDestinationTextColorList().size() > 0) {
//						passTimeObj.addDestinationTextColor(passingTimesMsg.getDestinationTextColor(i));
//					}
					if (passingTimesMsg.getDestinationTextColorList().size() > 0
							&& (!passingTimesMsg.getDestinationTextColorList().contains(""))) {
						passTimeObj.addDestinationTextColor(passingTimesMsg.getDestinationTextColor(i));
					}
//					if (passingTimesMsg.getDestinationIconList().size() > 0) {
//						passTimeObj.addDestinationIcon(passingTimesMsg.getDestinationIcon(i));
//					}
					if (passingTimesMsg.getDestinationIconList().size() > 0
							&& (!passingTimesMsg.getDestinationIconList().contains(""))) {
						passTimeObj.addDestinationIcon(passingTimesMsg.getDestinationIcon(i));
					}
					if (passingTimesMsg.getGeneratedTimestampList().size() > 0) {
						passTimeObj.addGeneratedTimestamp(passingTimesMsg.getGeneratedTimestamp(i));
					}
					if (passingTimesMsg.getJourneyNumberList().size() > 0) {
						passTimeObj.addJourneyNumber(passingTimesMsg.getJourneyNumber(i));
					}

					passingTimeList.add(passTimeObj);
				}
				// read from list and add into Map
				addPassingTimesToMap(passingTimeList);
				// test today date and trip date are equal
//				prepareRDRScreenProcess();
			}

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL, "Exception in PassingTimes: readPassingTimesProtoBufMsg(): ...."
					+ e.getMessage() + LocalDateTime.now());
		}
		return passingTimeList; // new

	}

	/**
	 * Add GeneralMessage To Map
	 * 
	 * @param generaMsgList
	 */
	public static int addGeneralMessageToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> generaMsgList) {
		int generalMessageSize = -1;

		try {
			if (generaMsgList != null) {

				for (int i = 0; i < generaMsgList.size(); i++) {
					generalMessageMapObj.put(generaMsgList.get(i).getMessageHash(0), generaMsgList.get(i));
				}
				/**
				 * write the travel information into passagefile
				 */
				generalMessageSize = generalMessageMapObj.size();

				boolean fileMode = SSBManager.getConfig().getFileMode();
				if (fileMode == true) {
					dumpGeneralMsgToFreeTextFile();

				}
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Insert The General Message To Map Failed   ...." + e.getMessage() + LocalDateTime.now());
		}
		return generalMessageSize;

	}

	/**
	 * Add Or Update The PassTimes into Map
	 * 
	 * @param passingTimeList
	 */
	public static int addPassingTimesToMap(
			ArrayList<com.ars.ODCC.connection.ODMessage.PassingTimes.Builder> passingTimeList) {
		int passTimesMapSize = -1;

		try {

			if (passingTimeList != null) {

				for (int i = 0; i < passingTimeList.size(); i++) {

					passTimeMapObj.put(passingTimeList.get(i).getPassTimeHash(0), passingTimeList.get(i));
				}
				passTimesMapSize = passTimeMapObj.size();

				if( tripStopStatusFlag == true ) {
					boolean fileMode = SSBManager.getConfig().getFileMode();
					if (fileMode == true) {
						dumpPassageTimesMsgToPassageFile( );
						tripStopStatusFlag = false;
					}
				}
				
			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Insert The Pass Times To Map Failed  ...." + e.getMessage() + LocalDateTime.now());
		}
		return passTimesMapSize;

	}

	public static boolean dumpFreeTextMsgToFreeTextFile(FileWriter freeTextFileWriter) {

		boolean freetextFileUpdated = false;
		try {
			for (Entry<String, com.ars.ODCC.connection.ODMessage.GeneralMessage.Builder> entryobj : generalMessageMapObj
					.entrySet()) {

				if (entryobj.getValue().getMessageHashList().size() > 0) {

					freeTextFileWriter.write(entryobj.getValue().getMessageHashList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getGeneralmessageTypeList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getGeneralmessageTypeList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getMessageContentList().size() > 0
						&& (!entryobj.getValue().getMessageContentList().contains(""))) {
					freeTextFileWriter.write(entryobj.getValue().getMessageContentList().get(0));
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getMessageStartTimeList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getMessageStartTimeList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getMessageEndTimeList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getMessageEndTimeList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getGeneratedTimestampList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getGeneratedTimestampList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getShowOverviewDisplayList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getShowOverviewDisplayList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getMessageTitleList().size() > 0
						&& (!entryobj.getValue().getMessageTitleList().contains(""))) {
					freeTextFileWriter.write(entryobj.getValue().getMessageTitleList().get(0) + "");
				}
				freeTextFileWriter.write(",");

				if (entryobj.getValue().getMessagePriorityList().size() > 0) {
					freeTextFileWriter.write(entryobj.getValue().getMessagePriorityList().get(0) + "");
				}
				freeTextFileWriter.write("\n");
				freetextFileUpdated = true;

//				freeTextFileWriter.write(entryobj.getValue().getMessageHashList().get(0) + ","
//						+ entryobj.getValue().getGeneralmessageTypeList().get(0) + ","
//						+ entryobj.getValue().getMessageContentList().get(0) + ","
//						+ entryobj.getValue().getMessageStartTimeList().get(0) + ","
//						+ entryobj.getValue().getMessageEndTimeList().get(0) + ","
//						+ entryobj.getValue().getGeneratedTimestampList().get(0) + ","
//						+ entryobj.getValue().getShowOverviewDisplayList().get(0) + ","
//						+ entryobj.getValue().getMessageTitleList().get(0) + ","
//						+ entryobj.getValue().getMessagePriorityList().get(0) + "\n");
			}

		}

		catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Dump The FreeText Message Into FreeText File Failed ...." + e.getMessage() + LocalDateTime.now());

		}
		return freetextFileUpdated;

	}

	/**
	 * add the travel information into passage file
	 * 
	 * @param fw
	 */
	public static boolean writeTravelInfoIntoPassageFile(FileWriter passageFileWriter) {

		boolean fileUpdated = false;

		try {

			for (Entry<String, Builder> entry : passTimeMapObj.entrySet()) {

				if (entry.getValue().getPassTimeHashList().size() > 0) {
					passageFileWriter.write(entry.getValue().getPassTimeHashList().get(0));
				}
				passageFileWriter.write(",");

				if (entry.getValue().getTargetArrivalTimeList().size() > 0) {
					passageFileWriter.write(entry.getValue().getTargetArrivalTimeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getTargetDepartureTimeList().size() > 0) {
					passageFileWriter.write(entry.getValue().getTargetDepartureTimeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getExpectedArrivalTimeList().size() > 0) {
					passageFileWriter.write(entry.getValue().getExpectedArrivalTimeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getExpectedDepartureTimeList().size() > 0) {
					passageFileWriter.write(entry.getValue().getExpectedDepartureTimeList().get(0) + "");

				}
				passageFileWriter.write(",");

				if (entry.getValue().getNumberOfCoachesList().size() > 0) {
					passageFileWriter.write(entry.getValue().getNumberOfCoachesList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getTripStopStatusList().size() > 0) {
					passageFileWriter.write(entry.getValue().getTripStopStatusList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getTransportTypeList().size() > 0) {
					passageFileWriter.write(entry.getValue().getTransportTypeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getWheelchairAccessibleList().size() > 0) {
					passageFileWriter.write(entry.getValue().getWheelchairAccessibleList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getIsTimingStopList().size() > 0) {
					passageFileWriter.write(entry.getValue().getIsTimingStopList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getStopCodeList().size() > 0
						&& (!entry.getValue().getStopCodeList().contains(""))) {
					passageFileWriter.write(entry.getValue().getStopCodeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getShowCancelledTripList().size() > 0) {
					passageFileWriter.write(entry.getValue().getShowCancelledTripList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getBlockCodeList().size() > 0
						&& (!entry.getValue().getBlockCodeList().contains(""))) {
					passageFileWriter.write(entry.getValue().getBlockCodeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getOccupancyList().size() > 0) {
					passageFileWriter.write(entry.getValue().getOccupancyList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getLinePublicNumberList().size() > 0
						&& (!entry.getValue().getLinePublicNumberList().contains(""))) {
					passageFileWriter.write(entry.getValue().getLinePublicNumberList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getSideCodeList().size() > 0
						&& (!entry.getValue().getSideCodeList().contains(""))) {
					passageFileWriter.write(entry.getValue().getSideCodeList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getLineDirectionList().size() > 0) {
					passageFileWriter.write(entry.getValue().getLineDirectionList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getLineColorList().size() > 0
						&& (!entry.getValue().getLineColorList().contains(""))) {
					passageFileWriter.write(entry.getValue().getLineColorList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getLineTextColorList().size() > 0
						&& (!entry.getValue().getLineTextColorList().contains(""))) {
					passageFileWriter.write(entry.getValue().getLineTextColorList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getLineIconList().size() > 0
						&& (!entry.getValue().getLineIconList().contains(""))) {
					passageFileWriter.write(entry.getValue().getLineIconList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getDestinationColorList().size() > 0
						&& (!entry.getValue().getDestinationColorList().contains(""))) {
					passageFileWriter.write(entry.getValue().getDestinationColorList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getDestinationTextColorList().size() > 0
						&& (!entry.getValue().getDestinationTextColorList().contains(""))) {
					passageFileWriter.write(entry.getValue().getDestinationTextColorList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getDestinationIconList().size() > 0
						&& (!entry.getValue().getDestinationIconList().contains(""))) {
					passageFileWriter.write(entry.getValue().getDestinationIconList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getGeneratedTimestampList().size() > 0) {
					passageFileWriter.write(entry.getValue().getGeneratedTimestampList().get(0) + "");
				}
				passageFileWriter.write(",");

				if (entry.getValue().getJourneyNumberList().size() > 0) {
					passageFileWriter.write(entry.getValue().getJourneyNumberList().get(0) + "");
				}

				for (int k = 0; k < entry.getValue().getDestinations(0).getDestinationNameList().size(); k++) {

					passageFileWriter.write(",");

					if (entry.getValue().getDestinations(0).getDestinationNameList().size() > 0) {

						passageFileWriter.write(entry.getValue().getDestinations(0).getDestinationNameList().get(k));
					}
					passageFileWriter.write(",");

					if (entry.getValue().getDestinations(0).getDestinationDetailList().size() > 0) {
						passageFileWriter.write(entry.getValue().getDestinations(0).getDestinationDetailList().get(k));
					}
				}
				passageFileWriter.write("\n");
				fileUpdated = true;

			}

		} catch (Exception e) {

			SSBManager.getLoggerObject().log(Level.ALL,
					" Write The Travel Info Into Passage File Failed  ...." + e.getMessage() + LocalDateTime.now());

		}
		return fileUpdated;

	}

	/**
	 * Creating a FreeText File
	 */

	public static boolean dumpGeneralMsgToFreeTextFile() {

		boolean generalFileUpdated = false;
		FileWriter fwObjFreetext = null;
		try {

			String freeTextFilePath = SSBManager.getBaseFilePath(SSBManager.getConfig().getDhConfig().getFreeTextFileWithPath());
			fwObjFreetext = new FileWriter(freeTextFilePath, false);
			fwObjFreetext.write(
					"messageHash,geneCralmessageType,messageContent,messageStartTime,messageEndTime,generatedTimestamp,showOverviewDisplay,messageTitle,messagePriority\n");
			generalFileUpdated = dumpFreeTextMsgToFreeTextFile(fwObjFreetext);
			SSBManager.getLoggerObject().log(Level.ALL,
					" FreeText Message Dump To FreeText File And FreeText File Updated  dumpGeneralMsgToFreeTextFile ()  ...."
							+ LocalDateTime.now());

		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Write The FreeText Message To FreeText File Failed  ...." + e.getMessage() + LocalDateTime.now());
		} finally {
			try {
				if (fwObjFreetext != null) {
					fwObjFreetext.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SSBManager.getLoggerObject().log(Level.ALL,
						" Close File Connection Failed  ...." + e.getMessage() + LocalDateTime.now());
			}
		}
		return generalFileUpdated;

	}

	/**
	 * Creating a Passage File
	 */
	public static boolean dumpPassageTimesMsgToPassageFile() {

		boolean isFileUpdated = false;

		FileWriter fwObjPassage = null;
		try {
				
			String passageFilePath = SSBManager.getBaseFilePath(SSBManager.getConfig().getDhConfig().getPassageFileWithPath());
			fwObjPassage = new FileWriter(passageFilePath, false);
			fwObjPassage.write(
					"pass_time_hash,target_arrival_time,target_departure_time,expected_arrival_time,expectedDepartureTime,number_of_coaches,trip_stop_status,transport_type,wheelchair_accessible,is_timing_stop,stop_code,show_cancelled_trip,block_code,occupancy,line_public_number,side_code,line_direction,line_color,line_text_color,line_icon,destination_color,destination_text_color,destination_icon,generated_timestamp,journey_number,DestinationName50,DestinationDetail50,DestinationName30,DestinationDetail30,DestinationName24,DestinationDetail24,DestinationName19,DestinationDetail19,DestinationName16,DestinationDetail16\n");
			isFileUpdated = writeTravelInfoIntoPassageFile(fwObjPassage);
			SSBManager.getLoggerObject().log(Level.ALL,
					" Trip Info Dump To Passage File And Passage File Updated  dumpPassageTimesMsgToPassageFile ()  ...."
							+ LocalDateTime.now());
		} catch (Exception e) {
			SSBManager.getLoggerObject().log(Level.ALL,
					" Dump Trip Info Into Passage File Failed  ...." + e.getMessage() + LocalDateTime.now());
		} finally {
			try {
				if (fwObjPassage != null) {
					fwObjPassage.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SSBManager.getLoggerObject().log(Level.ALL,
						" Close Passage File Connection Failed  ...." + e.getMessage() + LocalDateTime.now());
			}
		}
		return isFileUpdated;

	}

	/**
	 * Parse Json To ProtoBuf Message
	 * 
	 * @param jsonString
	 * @return
	 */
	private static com.ars.ODCC.connection.ODMessage.Container.Builder parseJsonToProtoBufMsg(String jsonString) {

		com.ars.ODCC.connection.ODMessage.Container.Builder objContainer = com.ars.ODCC.connection.ODMessage.Container
				.newBuilder();

		try {

			JsonFormat.parser().merge(jsonString, objContainer);

//			JsonFormat.Parser jsonParser =JsonFormat.parser(); 
//			jsonParser.usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(jsonString, objContainer); 

		} catch (InvalidProtocolBufferException e1) {

			// TODO Auto-generated catch block
			SSBManager.getLoggerObject().log(Level.ALL,
					"Error Deserializing Json To Protobuf ...." + e1.getMessage() + LocalDateTime.now());
		}

		return objContainer;
	}

	public static void getValidTripInfo() {

		ars.protobuf.DHMessage.travelinfo.Builder travelInfo = travelinfo.newBuilder();

		try {

			travelInfo = ListContainer.createTravelInfoMessage();
			JsonFormat.Printer jsonprinter = JsonFormat.printer();
			String payload = null;
			try {
				payload = jsonprinter.print(travelInfo);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}

		} catch (Exception e1) {

			// TODO Auto-generated catch block
			SSBManager.getLoggerObject().log(Level.ALL,
					" Prepare Travel Info Failed ...." + e1.getMessage() + LocalDateTime.now());
		}

	}

	public static ars.protobuf.DHMessage.travelinfo.Builder createTravelInfoMessage() {

		ars.protobuf.DHMessage.travelinfo.Builder travelInfo = travelinfo.newBuilder();
		ars.protobuf.DHMessage.trip.Destination.Builder destinationObj = ars.protobuf.DHMessage.trip.Destination
				.newBuilder();
		ars.protobuf.DHMessage.trip.Builder tripObj = trip.newBuilder();
		ars.protobuf.DHMessage.FreeText.Builder freeTextObj = FreeText.newBuilder();

		ArrayList<String> destinationName = new ArrayList<String>();
		ArrayList<String> destinationDetails = new ArrayList<String>();
		destinationName.add("des1");
		destinationDetails.add("destination1");
		destinationName.add("des1");
		destinationDetails.add("destination1");
		destinationObj.addAllDestinationName(destinationName);
		destinationObj.addAllDestinationDetail(destinationDetails);

		tripObj.setPassTimeHash("one");
		tripObj.setTargetArrivalTime(11);
		tripObj.setTargetDepartureTime(12);
		tripObj.setExpectedArrivalTime(13);
		tripObj.setExpectedDepartureTime(14);
		tripObj.setNumberOfCoaches(2);
		tripObj.setTripStopStatus(ars.protobuf.DHMessage.trip.TripStopStatus.PLANNED);
		tripObj.setTransportType(ars.protobuf.DHMessage.trip.TransportType.BUS);
		tripObj.setWheelchairAccessible(true);
		tripObj.setIsTimingStop(true);
		tripObj.setStopCode("stp1");
		tripObj.setDestinations(destinationObj);
		tripObj.setShowCancelledTrip(ars.protobuf.DHMessage.trip.ShowCancelledTrip.TRUE);
		tripObj.setBlockCode("block1");
		tripObj.setOccupancy(1);
		tripObj.setLinePublicNumber("L01");
		tripObj.setSideCode("side1");
		tripObj.setLineDirection(0);
		tripObj.setLineColor("blue");
		tripObj.setLineTextColor("black");
		tripObj.setLineIcon("icon1");
		tripObj.setDestinationColor("blue");
		tripObj.setDestinationTextColor("black");
		tripObj.setDestinationIcon("Destin2");
		tripObj.setGeneratedTimestamp(41346327);
		tripObj.setJourneyNumber(123);

//        tripObj.setPassTimeHash("two");
//        tripObj.setTargetArrivalTime( 21);
//        tripObj.setTargetDepartureTime( 22);
//        tripObj.setExpectedArrivalTime( 23);
//        tripObj.setExpectedDepartureTime( 24);
//        tripObj.setNumberOfCoaches( 3);
//        tripObj.setTripStopStatus( ars.protobuf.DHMessage.trip.TripStopStatus.PLANNED);
//        tripObj.setTransportType( ars.protobuf.DHMessage.trip.TransportType.BUS);
//        tripObj.setWheelchairAccessible(true);
//        tripObj.setIsTimingStop(true);
//        tripObj.setStopCode("stp1");
//        tripObj.setDestinations(destinationObj);
//        tripObj.setShowCancelledTrip(ars.protobuf.DHMessage.trip.ShowCancelledTrip.TRUE);
//        tripObj.setBlockCode("block1");
//        tripObj.setOccupancy(1);
//        tripObj.setLinePublicNumber("L01");
//        tripObj.setSideCode("side1");
//        tripObj.setLineDirection(0);
//        tripObj.setLineColor("blue");
//        tripObj.setLineTextColor("black");
//        tripObj.setLineIcon("icon1");
//        tripObj.setDestinationColor("blue");
//        tripObj.setDestinationTextColor("black");
//        tripObj.setDestinationIcon("Destin2");
//        tripObj.setGeneratedTimestamp(41346328);
//        tripObj.setJourneyNumber(124);

//		freeTextObj.setMessageHash("GM1");
//		freeTextObj.setGeneralmessageType(
//				ars.protobuf.DHMessage.FreeText.GeneralMessageType.GENERAL);
//		freeTextObj.setMessageContent("First general message");
//		freeTextObj.setMessageStartTime(247184);
//        freeTextObj.setMessageEndTime(1619347);
//        freeTextObj.setGeneratedTimestamp(2384928);
//        freeTextObj.setShowOverviewDisplay(ars.protobuf.DHMessage.FreeText.ShowOverviewDisplay.FALSE);
//        freeTextObj.setMessageTitle("GM title1");
//        freeTextObj.setMessagePriority(ars.protobuf.DHMessage.FreeText.MessagePriority.PTPROCESS );

//        freeTextObj.setMessageHash("GM2");
//        freeTextObj.setMessageContent("second general message");
//        freeTextObj.setMessageStartTime(247184);
//        freeTextObj.setMessageEndTime(1619347);
//        freeTextObj.setGeneratedTimestamp(2384928);
//        freeTextObj.setShowOverviewDisplay(ars.protobuf.DHMessage.FreeText.ShowOverviewDisplay.TRUE);
//        freeTextObj.setMessageTitle("GM title2");
//        freeTextObj.setMessagePriority(ars.protobuf.DHMessage.FreeText.MessagePriority.COMMERCIAL );

		if (tripObj.getPassTimeHash().length() > 0) {
			travelInfo.addTrips(tripObj);
			travelInfo.setDataAvailabile(true);
			travelInfo.setDisplayText("");
		} else {

			boolean odccConneced = true;
			boolean odccnotConnected = false;
			boolean tripInFeature = false;

			travelInfo.setDataAvailabile(false);

			if (tripInFeature == true) {
				travelInfo.setDisplayText("No departures in the coming time");

			} else if (odccConneced == true) {

				travelInfo.setDisplayText("No departures in the coming time");

			} else if (odccnotConnected == true) {

				travelInfo.setDisplayText("No travel information available");
			}

		}
		if (freeTextObj.getMessageHash().length() > 0) {
			travelInfo.addFreeTexts(freeTextObj);

		}

		return travelInfo;

	}

}
