/**
 * @author selvakumarv
 *
 * 22-03-2021
 *
 *MMEntity.java
 */
package com.mm.entity;

/**
 * @author selvakumarv
 *
 */
public class MMEntity {

	private String componentInstanceName;
	private int moduleId;
	private String componentServiceName;
	private String componentExePath;
	private int startingSequence;
	private int stoppingSequence;
	private long startTimeStamp;

	public MMEntity() {
	}

	/**
	 * @param componentInstanceName
	 * @param moduleId
	 * @param componentServiceName
	 * @param componentExePath
	 * @param startingSequence
	 * @param stoppingSequence
	 */
	public MMEntity(String componentInstanceName, int moduleId, String componentServiceName, String componentExePath,
			int startingSequence, int stoppingSequence) {
		super();
		this.componentInstanceName = componentInstanceName;
		this.moduleId = moduleId;
		this.componentServiceName = componentServiceName;
		this.componentExePath = componentExePath;
		this.startingSequence = startingSequence;
		this.stoppingSequence = stoppingSequence;
	}

	/**
	 * @return the componentInstanceName
	 */
	public String getComponentInstanceName() {
		return componentInstanceName;
	}

	/**
	 * @param componentInstanceName the componentInstanceName to set
	 */
	public void setComponentInstanceName(String componentInstanceName) {
		this.componentInstanceName = componentInstanceName;
	}

	/**
	 * @return the moduleId
	 */
	public int getModuleId() {
		return moduleId;
	}

	/**
	 * @param moduleId the moduleId to set
	 */
	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * @return the componentServiceName
	 */
	public String getComponentServiceName() {
		return componentServiceName;
	}

	/**
	 * @param componentServiceName the componentServiceName to set
	 */
	public void setComponentServiceName(String componentServiceName) {
		this.componentServiceName = componentServiceName;
	}

	/**
	 * @return the componentExePath
	 */
	public String getComponentExePath() {
		return componentExePath;
	}

	/**
	 * @param componentExePath the componentExePath to set
	 */
	public void setComponentExePath(String componentExePath) {
		this.componentExePath = componentExePath;
	}

	/**
	 * @return the startingSequence
	 */
	public int getStartingSequence() {
		return startingSequence;
	}

	/**
	 * @param startingSequence the startingSequence to set
	 */
	public void setStartingSequence(int startingSequence) {
		this.startingSequence = startingSequence;
	}

	/**
	 * @return the stoppingSequence
	 */
	public int getStoppingSequence() {
		return stoppingSequence;
	}

	/**
	 * @param stoppingSequence the stoppingSequence to set
	 */
	public void setStoppingSequence(int stoppingSequence) {
		this.stoppingSequence = stoppingSequence;
	}

	/**
	 * @return the startTimeStamp
	 */
	public long getStartTimeStamp() {
		return startTimeStamp;
	}

	/**
	 * @param startTimeStamp the startTimeStamp to set
	 */
	public void setStartTimeStamp(long startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

}
