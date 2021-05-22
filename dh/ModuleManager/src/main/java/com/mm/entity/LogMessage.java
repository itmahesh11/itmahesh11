/**
 * @author selvakumarv
 *
 *	2021-04-05
 *
 *LogMessage.java	
 */
package com.mm.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author selvakumarv
 *
 */
@SuppressWarnings("serial")
public class LogMessage implements Serializable{

	@SerializedName("clientId")
	@Expose
	private int clientId;
	@SerializedName("type")
	@Expose
	private int type;
	@SerializedName("code")
	@Expose
	private int code;
	@SerializedName("message")
	@Expose
	private String message;
	@SerializedName("duration")
	@Expose
	private long duration;
	@SerializedName("timestamp")
	@Expose
	private long timestamp;
	/**
	 * @return the clientId
	 */
	public int getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
	
	
	
	
}
