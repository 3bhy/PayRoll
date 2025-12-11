package com.project.demo.entity;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "shift_time")
public class ShiftTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shiftTimeId")
	private Integer shiftTimeId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shiftId")
	@JsonIgnore
	private Shift shiftId;

	@Column(name = "dayIndex")
	
	private Integer dayIndex;

	@Column(name = "fromTime")
	private Time fromTime;

	@Column(name = "toTime")
	private Time toTime;
	@Column(name = "totalTime")
	private Time totalTime;

	public ShiftTime() {
	}

	public ShiftTime(Integer shiftTimeId, Shift shiftId, Integer dayIndex, Time fromTime, Time toTime, Time totalTime) {
		this.shiftTimeId = shiftTimeId;
		this.shiftId = shiftId;
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

	public int getShiftTimeId() {
		return shiftTimeId;
	}

	public void setShiftTimeId(Integer shiftTimeId) {
		this.shiftTimeId = shiftTimeId;
	}

	public Shift getShiftId() {
		return shiftId;
	}

	public void setShiftId(Shift shiftId) {
		this.shiftId = shiftId;
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
