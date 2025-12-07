package com.project.demo.model;

import java.util.ArrayList;
import java.util.List;

public class ShiftModel {

	private Integer shiftId;

	private Integer company;

	private String shiftName;
	private List<ShiftTimeModel> shiftTimes = new ArrayList<>();

	public List<ShiftTimeModel> getShiftTimes() {
		return shiftTimes;
	}

	public void setShiftTimes(List<ShiftTimeModel> shiftTimes) {
		this.shiftTimes = shiftTimes;
	}

	public ShiftModel() {
	}

	public ShiftModel(Integer shiftId, Integer company, String shiftName) {
		this.shiftId = shiftId;
		this.company = company;
		this.shiftName = shiftName;
	}

	public Integer getShiftId() {

		return shiftId;
	}

	public void setShiftId(Integer shiftId) {

		this.shiftId = shiftId;

	}

	public Integer getCompany() {

		return company;

	}

	public void setCompany(Integer company) {

		this.company = company;

	}

	public String getShiftName() {

		return shiftName;

	}

	public void setShiftName(String shiftName) {

		this.shiftName = shiftName;

	}
}
