package com.project.demo.model;

import java.time.LocalTime;

public class ShiftTimeModel {

	private Integer shiftTimeId;

	private Integer shift;

	private Integer dayIndex;

	private LocalTime fromTime;

	private LocalTime toTime;
	private LocalTime totalTime;

	public ShiftTimeModel() {
	}

	public ShiftTimeModel(Integer shiftTimeId, Integer shift, Integer dayIndex, LocalTime fromTime, LocalTime toTime,
			LocalTime totalTime) {
		this.shiftTimeId = shiftTimeId;
		this.shift = shift;
		this.dayIndex = dayIndex;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.totalTime = totalTime;
	}

	public LocalTime getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(LocalTime totalTime) {
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

	public LocalTime getFromTime() {
		return fromTime;
	}

	public void setFromTime(LocalTime fromTime) {
		this.fromTime = fromTime;
	}

	public LocalTime getToTime() {
		return toTime;
	}

	public void setToTime(LocalTime toTime) {
		this.toTime = toTime;
	}

}
