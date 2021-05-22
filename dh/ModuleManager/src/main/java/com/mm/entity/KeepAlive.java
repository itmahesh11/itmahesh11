/**
 * @author selvakumarv
 *
 * 25-03-2021
 *
 *KeepAlive.java
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
public class KeepAlive  implements Serializable{

	@SerializedName("module_id")
	@Expose
	private Integer moduleId;

	@SerializedName("updateTime")
	@Expose
	private Long updateTime;

	/**
	 * @return the moduleId
	 */
	public Integer getModuleId() {
		return moduleId;
	}

	/**
	 * @param moduleId the moduleId to set
	 */
	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * @return the updateTime
	 */
	public Long getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param updateTime the updateTime to set
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	
}
