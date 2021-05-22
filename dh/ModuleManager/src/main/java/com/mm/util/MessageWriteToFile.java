/**
 * @author selvakumarv
 *
 *	2021-04-01
 *
 *MessageWriteToFile.java	
 */
package com.mm.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import com.mm.entity.KeepAlive;

/**
 * @author selvakumarv
 *
 */
public class MessageWriteToFile {

	public static int measurmenSignlLineChar = 10;
	public static int perhoursLine = 2;
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd,HH:mm:ss.SSS");
	private static ARSLogger logger;
	public static Properties properties;

	/*
	 * public MessageWriteToFile(Configuration configuration,MMConfig mmConfig) {
	 * this.configuration=configuration; this.mmConfig=mmConfig; }
	 */

	public void setLogger(ARSLogger logger) {
		this.logger=logger;
	}
	public void setProperties(Properties properties) {
		this.properties=properties;
	}
	public void writeLogFile() {

	}

	public void writeMetricFile() {

	}

	public static Boolean writeMessage(String filepath, Object message) {
		Boolean statu = false;
		LocalDateTime ldt = LocalDateTime.now();
		FileWriter fileWriter=null;
		try {
			Path path = Paths.get(filepath);// "D:\\output.txt");
			File file = path.toFile();

			StringBuffer sb = new StringBuffer();

			byte[] bs = objToByte(message);
			sb.append("[");
			sb.append(ldt.format(dateTimeFormatter));
			sb.append("]");
			sb.append(" ");
			sb.append("[");
			sb.append(bs);
			sb.append(" ");
			sb.append("]");
			
			/*
			 * sb.append("["); sb.append(byteToObj(bs)); sb.append("]");
			 */

			if (!file.exists()) {
				file.createNewFile();
			} else {
				double kilobytes = (file.length() / 1024);

				System.out.println("==><><size>><" + kilobytes + " MB");
			}
			fileWriter = new FileWriter(file, true);

			String newLine = System.getProperty("line.separator");

			int numberchar = 100;

			int countch=0;
			String printmsg =sb.toString();
			for (int i=0; i <=printmsg.length(); i++) {
				char value=printmsg.charAt(i);
				if (countch == numberchar) {
					fileWriter.write(value + newLine);
					countch = 0;
				} else {
					
					if(countch==0) {
						fileWriter.write(value);
					}else if(i==(printmsg.length()-1)){
						fileWriter.write(value+ newLine);
					}else {
					fileWriter.write(value);
					}
				}
				countch++;
			}
			
			statu = true;
		} catch (Exception e) {
			statu = false;
			logger.log(Level.SEVERE, e.getMessage()+LocalDateTime.now());
		}
		finally {
			try {
				if(fileWriter!=null) {
					fileWriter.flush();
				fileWriter.close();
				logger.log(Level.INFO, properties.getProperty("end_writefile")+" [ "+filepath+" ] "+LocalDateTime.now());
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage()+LocalDateTime.now());
			}
		}
		return statu;
	}

	public Boolean fetchDeletedOldContents(String filepath) {
		Boolean status = false;
		PrintWriter writer = null;
		try {
			Path path = Paths.get(filepath);
			File file = path.toFile();
			if (file != null && file.exists()) {
				writer = new PrintWriter(file);
				writer.print("");
				writer.close();
				if (file.length()==0) {
					status = true;
				}

			}

		} catch (Exception e) {

		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		return status;
	}

	public static void time() {
		LocalDateTime ldt = LocalDateTime.now();

		System.out.println("==>>b>>" + ldt.format(dateTimeFormatter));
		ldt = ldt.plusHours(1);
		System.out.println("==>>a>>" + ldt.format(dateTimeFormatter));
		ldt = ldt.plusSeconds(30);
		System.out.println("==>>ss>>" + ldt.format(dateTimeFormatter));
	}

	public static void main(String arg[]) throws Exception {
		LocalDateTime ldt = LocalDateTime.now();
		time();
		fetchSixtyDaysinFile("D:\\output.txt");

		StringBuffer sb = new StringBuffer();
		KeepAlive ka = new KeepAlive();
		ka.setModuleId(29);
		ka.setUpdateTime(122290999L);
		byte[] bs = objToByte(ka);
		sb.append(ldt.format(dateTimeFormatter));
		sb.append(" ");
		sb.append("[");
		sb.append(bs);
		sb.append("]");
		sb.append("[");
		sb.append(byteToObj(bs));
		sb.append("]");

		writeMessage("", sb.toString());
	}

	public static void fetchSixtyDaysinFile(String filepath) {
		List<String> list = new ArrayList<>();

		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(filepath));
			String txt = br.readLine();
			// [2021/04/02
			if (txt != null) {
				System.out.println("==>>>" + txt);
				String datz = "2021/04/02,12:26:09.145";
				LocalDateTime ldt = LocalDateTime.parse(txt, dateTimeFormatter);
				System.out.println("==1>>>" + ldt.format(dateTimeFormatter));
				ldt = ldt.plusDays(60);
				System.out.println("==>>>" + ldt.format(dateTimeFormatter));
				// String val= txt.substring(1, 10);
				// String val.split("/");

			}
			// br returns as stream and convert it into a List
			// list = br.lines().findFirst();;//(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static byte[] objToByte(Object tcpPacket) throws IOException {
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

}
