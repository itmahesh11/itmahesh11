package com.mm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.entity.KeepAlive;
import com.mm.mqconnection.StopSystemBrokerManager;
import com.mm.mqconnection.SubscribeToInternalBroker;
import com.mm.service.MonitoringComponentService;
import com.mm.service.StartComponentService;
import com.mm.service.StatusOverviewService;
import com.mm.service.StopComponentService;
import com.mm.util.ARSLogger;

/**
 * Hello world!
 *
 */
public class App {
	/**
	 * logger for logging the messages
	 */
	private static ARSLogger logger = null;
	public static Configuration config = null;
	private static MMConfig mmconf = null;
	private static StopSystemBrokerManager stopSystemBrokerManager;
	public static SubscribeToInternalBroker subscribeToInternalBroker;
	private final static String timeStampFormat = "[yyyy/MM/dd,HH:mm:ss.SSS]";
	private static StatusOverviewService statusOverviewService;
	public App() {

	}

	public static void main(String[] args) {

		try {

			
			config = Configuration.getInstance();
			mmconf = config.mmConfig;
			mmconf.getLogger(config);
			
			getLogger();
			
			
			 start();
			  
			  stopSystemBrokerManager = new StopSystemBrokerManager(config,
			  mmconf); subscribeToInternalBroker = new SubscribeToInternalBroker(config,
			  mmconf, stopSystemBrokerManager.generalConfig);
			  startMQServer();
			 
			  
			  // keepAliveMonitoring(); 
			  StopComponentService stopComponentService = new StopComponentService(mmconf); 
			  stopComponentService.getLogger(config);
			 
				
			  Scanner in = new Scanner(System.in);  
	          System.out.println("Need to Stop Module Manager: exit");  
	          String name = in.nextLine();  
	          //System.out.println("==>>"+name);    
	           if(name!=null) {
	        	   if(!name.isEmpty()) {
	        		  if(name.equalsIgnoreCase("exit")) {
	        			 stopComponentService.stopComponents();
	        		  }
	        	   }
	           }
			  
				/*
				 * LogMessage logManager = new LogMessage(); logManager.setClientId(1);
				 * logManager.setCode(12); logManager.setDuration(30);
				 * logManager.setMessage("start module");
				 * logManager.setTimestamp(mmconf.getCurrentTimeStamp()); logManager.setType(1);
				 * String json = mmconf.objectToJson(logManager);
				 * System.out.println("==>><><>>"+json);
				 */
	           
	           
		} catch (Exception e) {
			System.out.println("==>><>eee<>>"+e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Method to start the MM module
	 * 
	 * @throws InterruptedException
	 */
	public static void startMQServer() throws InterruptedException {

		// Thread that handles the functions related to stop system broker.
		new Thread(() -> {
			try {
				if(stopSystemBrokerManager.obtainInternalBrokerConnection()) {
					stopSystemBrokerManager.subscribeToInternalBroker();
				}
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Unable to connect to stop system broker...." + LocalDateTime.now());
			}
		}).start();

	}

	public static void keepAliveMonitoring() {
		int mointoringInterval = mmconf.getMonitoringInterval();

		logger.log(Level.SEVERE, "Check keep Alive Interval. " + mointoringInterval + LocalDateTime.now());
		final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				System.out.println("keepAliveMonitoring.." + LocalDateTime.now());

				Map<Integer, KeepAlive> mapList = mmconf.getKeepAliveMap();
				if (mapList != null && !mapList.isEmpty()) {
					System.out.println("keepAliveMonitoring.mapList." + mapList.size());
					mapList.entrySet().stream().forEach(map -> {
						logger.log(Level.SEVERE,
								"keepAlive Monitoring. " + " moduleID [ " + map.getKey()+" ] "+"  " + LocalDateTime.now());
						subscribeToInternalBroker.checkKeepAliveModule(map.getKey(), map.getValue());
					});

				}

			}
		}, 0,mointoringInterval, TimeUnit.SECONDS);
	}

	public static void start() {

		StartComponentService service = new StartComponentService(mmconf);
		service.getLogger(config);
		service.startComponents();
		mmconf.getLocalComponents(mmconf.localComponentSubNode);
	}

	public static void monitor() {

		final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {

				MonitoringComponentService monitor = new MonitoringComponentService(mmconf);
				monitor.getLogger(config);
				monitor.isAliveAllComponents();
			}
		}, 0, mmconf.getMonitoringInterval(), TimeUnit.SECONDS);
	}

	public static byte[] objToByte(KeepAlive tcpPacket) throws IOException {
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
	    objStream.writeObject(tcpPacket);

	    return byteStream.toByteArray();
	}
	public static Object byteToObj(byte[] bytes) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
	    ObjectInputStream objStream = new ObjectInputStream(byteStream);

	    return objStream.readObject();
	}

	public static String prettyBinary(String binary, int blockSize, String separator) {

        java.util.List<String> result = new ArrayList();
        int index = 0;
        while (index < binary.length()) {
            result.add(binary.substring(index, Math.min(index + blockSize, binary.length())));
            index += blockSize;
        }

        return result.stream().collect(Collectors.joining(separator));
    }
	public static String convertStringToBinary(String input) {

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))   // char -> int, auto-cast
                            .replaceAll(" ", "0")                         // zero pads
            );
        }
        return result.toString();

    }

public static void conveterToString(String input) {
	//String input = "01001000 01100101 01101100 01101100 01101111";

    // Java 8 makes life easier
    String raw = Arrays.stream(input.split(" "))
            .map(binary -> Integer.parseInt(binary, 2))
            .map(Character::toString)
            .collect(Collectors.joining()); // cut the space

    System.out.println(raw);

}

	/**
	 * Gets the application logger
	 * 
	 * @return ARSLogger
	 */
	public static ARSLogger getLogger() {
		if (logger == null && config != null) {
			logger = new ARSLogger(config.getLogFileName(), config.getLogFilePath(), config.getLogFileSize(),
					config.getLogFileNoOfBackup(), false);
			logger.setLevel(getLevel());
		}
		return logger;
	}

	/**
	 * returns the log level specified in the config file.
	 * 
	 * @return Level OFF, SEVERE, WARNING, INFO, ALL
	 */
	private static Level getLevel() {
		switch (config.getLogLevel()) {
		case "OFF":
			return Level.OFF;
		case "SEVERE":
			return Level.SEVERE;
		case "WARNING":
			return Level.WARNING;
		case "INFO":
			return Level.INFO;
		default:
			return Level.ALL;
		}
	}
}
