package com.project.demo.entity;

import java.sql.Time;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "login")
public class Login {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "loginId")
	private Integer loginId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employeeId")
	private Employee employee;

	@ManyToOne
	@JoinColumn(name = "shiftTimeId")
	private ShiftTime shiftTimeId;

	@ManyToOne
	@JoinColumn(name = "shiftTimeAttendanceId")
	private ShiftTimeAttendance shiftTimeAttendance;

	@Column(name = "loginDateTime")
	private LocalDateTime loginDateTime;

	@Column(name = "logoutDateTime")
	private LocalDateTime logoutDateTime;

	@Column(name = "logoutStatus", columnDefinition = "boolean default false")
	private Boolean logoutStatus = false;

	@Column(name = "activityTime")
	private Time activityTime;

	@Column(name = "locked", columnDefinition = "boolean default false")
	private Boolean locked = false;

	public Login() {
	}

	@PrePersist
	public void setDefaultValues() {
		if (this.logoutStatus == null) {
			this.logoutStatus = false;
		}
		if (this.locked == null) {
			this.locked = false;
		}
		if (this.activityTime == null) {
			this.activityTime = Time.valueOf("00:00:00");
		}
	}

	public Login(Integer loginId, Employee employee, ShiftTime shiftTimeId, ShiftTimeAttendance shiftTimeAttendance,
			LocalDateTime loginDateTime, LocalDateTime logoutDateTime, Boolean logoutStatus, Time activityTime,
			Boolean locked) {
		this.loginId = loginId;
		this.employee = employee;
		this.shiftTimeId = shiftTimeId;
		this.shiftTimeAttendance = shiftTimeAttendance;
		this.loginDateTime = loginDateTime;
		this.logoutDateTime = logoutDateTime;
		this.logoutStatus = logoutStatus != null ? logoutStatus : false;
		this.activityTime = activityTime != null ? activityTime : Time.valueOf("00:00:00");
		this.locked = locked != null ? locked : false;
	}

	public ShiftTimeAttendance getShiftTimeAttendanceId() {
		return shiftTimeAttendance;
	}

	public void setShiftTimeAttendanceId(ShiftTimeAttendance shiftTimeAttendance) {
		this.shiftTimeAttendance = shiftTimeAttendance;
	}

	public Integer getLoginId() {
		return loginId;
	}

	public void setLoginId(Integer loginId) {
		this.loginId = loginId;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
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

	public ShiftTime getShiftTimeId() {
		return shiftTimeId;
	}

	public void setShiftTimeId(ShiftTime shiftTimeId) {
		this.shiftTimeId = shiftTimeId;
	}

}