package com.project.demo.model;

import java.sql.Time;
import java.time.LocalDateTime;

public class LoginModel {

	private Integer loginId;

	private Integer employeeId;

	private Integer shiftTimeId;

	private Integer shiftTimeAttendanceId;

	private LocalDateTime loginDateTime;

	private LocalDateTime logoutDateTime;

	private Boolean logoutStatus = false;

	private Time activityTime;

	private Boolean locked = false;

	public LoginModel() {
	}

	public LoginModel(Integer loginId, Integer employeeId, Integer shiftTimeId, Integer shiftTimeAttendanceId,
			LocalDateTime loginDateTime, LocalDateTime logoutDateTime, Boolean logoutStatus, Time activityTime,
			Boolean locked) {
		this.loginId = loginId;
		this.employeeId = employeeId;
		this.shiftTimeId = shiftTimeId;
		this.shiftTimeAttendanceId = shiftTimeAttendanceId;
		this.loginDateTime = loginDateTime;
		this.logoutDateTime = logoutDateTime;
		this.logoutStatus = logoutStatus;
		this.activityTime = activityTime;
		this.locked = locked;
	}

	public Integer getShiftTimeAttendanceId() {
		return shiftTimeAttendanceId;
	}

	public void setShiftTimeAttendanceId(Integer shiftTimeAttendanceId) {
		this.shiftTimeAttendanceId = shiftTimeAttendanceId;
	}

	public Integer getLoginId() {
		return loginId;
	}

	public void setLoginId(Integer loginId) {
		this.loginId = loginId;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public LocalDateTime getLoginDateTime() {
		return loginDateTime;
	}

	public void setLoginDateTime(LocalDateTime loginDateTime) {
		this.loginDateTime = loginDateTime;
	}

	public LocalDateTime getLogoutDateTime() {
		return logoutDateTime;
	}

	public void setLogoutDateTime(LocalDateTime logoutDateTime) {
		this.logoutDateTime = logoutDateTime;
	}

	public Boolean getLogoutStatus() {
		return logoutStatus != null ? logoutStatus : false;
	}

	public void setLogoutStatus(Boolean logoutStatus) {
		this.logoutStatus = logoutStatus != null ? logoutStatus : false;
	}

	public Time getActivityTime() {
		return activityTime != null ? activityTime : Time.valueOf("00:00:00");
	}

	public void setActivityTime(Time activityTime) {
		this.activityTime = activityTime != null ? activityTime : Time.valueOf("00:00:00");
	}

	public Boolean getLocked() {
		return locked != null ? locked : false;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked != null ? locked : false;
	}

	public Integer getShiftTimeId() {
		return shiftTimeId;
	}

	public void setShiftTimeId(Integer shiftTimeId) {
		this.shiftTimeId = shiftTimeId;
	}

}
