package com.ars.ODCC.message;

/*************************************

Copyright © 1998-2021 ARS T&TT.

**************************************/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeepAlive {

	@SerializedName("module_id")
	@Expose
	private Integer moduleId;

	@SerializedName("updateTime")
	@Expose
	private Integer updateTime;

	public Integer getModuleId() {
		return moduleId;
	}

	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}

	public Integer getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Integer updateTime) {
		this.updateTime = updateTime;
	}
}
