package com.ars.ODCC.Connection;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ODConnectionStatus {

	@SerializedName("od_connected")
	@Expose
	private Boolean odConnected;

	@SerializedName("statechangeFrom")
	@Expose
	private Integer statechangeFrom;

	public Boolean getOdConnected() {
		return odConnected;
	}

	public void setOdConnected(Boolean odConnected) {
		this.odConnected = odConnected;
	}

	public Integer getStatechangeFrom() {
		return statechangeFrom;
	}

	public void setStatechangeFrom(Integer statechangeFrom) {
		this.statechangeFrom = statechangeFrom;
	}

}
