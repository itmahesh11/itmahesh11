/**
 * @author selvakumarv
 *
 *	2021-04-05
 *
 *MetricMessage.java	
 */
package com.mm.entity;

/**
 * @author selvakumarv
 *
 */
public class MetricMessage {
	
	private int clientId;
	private int componentIndex;
	private int component;
	private String property ;
	private String value ;
	private int unit;
	private long timestampBegin;
	private long timestampEnd;
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
	 * @return the componentIndex
	 */
	public int getComponentIndex() {
		return componentIndex;
	}
	/**
	 * @param componentIndex the componentIndex to set
	 */
	public void setComponentIndex(int componentIndex) {
		this.componentIndex = componentIndex;
	}
	/**
	 * @return the component
	 */
	public int getComponent() {
		return component;
	}
	/**
	 * @param component the component to set
	 */
	public void setComponent(int component) {
		this.component = component;
	}
	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the unit
	 */
	public int getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(int unit) {
		this.unit = unit;
	}
	/**
	 * @return the timestampBegin
	 */
	public long getTimestampBegin() {
		return timestampBegin;
	}
	/**
	 * @param timestampBegin the timestampBegin to set
	 */
	public void setTimestampBegin(long timestampBegin) {
		this.timestampBegin = timestampBegin;
	}
	/**
	 * @return the timestampEnd
	 */
	public long getTimestampEnd() {
		return timestampEnd;
	}
	/**
	 * @param timestampEnd the timestampEnd to set
	 */
	public void setTimestampEnd(long timestampEnd) {
		this.timestampEnd = timestampEnd;
	}
	
	
	

}
