package com.project.demo.model;

import java.sql.Time;

public class ShiftTimeModel {

	private Integer shiftTimeId;

	private Integer shift;

	private Integer dayIndex;

	private Time fromTime;

	private Time toTime;
	private Time totalTime;

	public ShiftTimeModel() {
	}

	public ShiftTimeModel(Integer shiftTimeId, Integer shift, Integer dayIndex, Time fromTime, Time toTime,
			Time totalTime) {
		this.shiftTimeId = shiftTimeId;
		this.shift = shift;
		this.dayIndex = dayIndex;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.totalTime = totalTime;
	}

	public Time getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Time totalTime) {
		this.totalTime = totalTime;
	}

	public Integer getShiftTimeId() {
		return shiftTimeId;
	}

	public void setShiftTimeId(Integer shiftTimeId) {
		this.shiftTimeId = shiftTimeId;
	}

	public Integer getShift() {
		return shift;
	}

	public void setShift(Integer shift) {
		this.shift = shift;
	}

	public Integer getDayIndex() {
		return dayIndex;
	}

	public void setDayIndex(Integer dayIndex) {
		this.dayIndex = dayIndex;
	}

	public Time getFromTime() {
		return fromTime;
	}

	public void setFromTime(Time fromTime) {
		this.fromTime = fromTime;
	}

	public Time getToTime() {
		return toTime;
	}

	public void setToTime(Time toTime) {
		this.toTime = toTime;
	}

}
