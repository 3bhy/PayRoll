package com.project.demo.entity;

import java.sql.Time;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ShiftTimeAttendance")
public class ShiftTimeAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shiftTimeAttendanceId")
    private Integer shiftTimeAttendanceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnore 
    private Employee employee;
    
    @Column(name = "attendanceDate")
    private Date attendanceDate;
    
    @Column(name = "overTime")
    private Time overTime;
    
    @Column(name = "lessTime")
    private Time lessTime;
    
    @Column(name = "totalActiveTime")
    private Time totalActiveTime;
    
    @Column(name = "totalOverTime")
    private Time totalOverTime;
    
    @Column(name = "totalIncentiveSales")
    private Float totalIncentiveSales;
    
    // Relationships
    @OneToMany
    private List<Login> logins;
   
   
    public ShiftTimeAttendance() {}


	public ShiftTimeAttendance(Integer shiftTimeAttendanceId, Employee employee, Date attendanceDate,
			Time overTime, Time lessTime, Time totalActiveTime, Time totalOverTime,
			Float totalIncentiveSales) {
		
		this.shiftTimeAttendanceId = shiftTimeAttendanceId;
		this.employee = employee;
		this.attendanceDate = attendanceDate;
		this.overTime = overTime;
		this.lessTime = lessTime;
		this.totalActiveTime = totalActiveTime;
		this.totalOverTime = totalOverTime;
		this.totalIncentiveSales = totalIncentiveSales;
	}


	public Integer getShiftTimeAttendanceId() {
		return shiftTimeAttendanceId;
	}


	public void setShiftTimeAttendanceId(Integer shiftTimeAttendanceId) {
		this.shiftTimeAttendanceId = shiftTimeAttendanceId;
	}


	public Employee getEmployee() {
		return employee;
	}


	public void setEmployee(Employee employee) {
		this.employee = employee;
	}


	public Date getAttendanceDate() {
		return attendanceDate;
	}


	public void setAttendanceDate(Date attendanceDate) {
		this.attendanceDate = attendanceDate;
	}


	public Time getOverTime() {
		return overTime;
	}


	public void setOverTime(Time overTime) {
		this.overTime = overTime;
	}


	public Time getLessTime() {
		return lessTime;
	}


	public void setLessTime(Time lessTime) {
		this.lessTime = lessTime;
	}


	public Time getTotalActiveTime() {
		return totalActiveTime;
	}


	public void setTotalActiveTime(Time totalActiveTime) {
		this.totalActiveTime = totalActiveTime;
	}


	public Time getTotalOverTime() {
		return totalOverTime;
	}


	public void setTotalOverTime(Time totalOverTime) {
		this.totalOverTime = totalOverTime;
	}


	public Float getTotalIncentiveSales() {
		return totalIncentiveSales;
	}


	public void setTotalIncentiveSales(Float totalIncentiveSales) {
		this.totalIncentiveSales = totalIncentiveSales;
	}


	public List<Login> getLogins() {
		return logins;
	}


	public void setLogins(List<Login> logins) {
		this.logins = logins;
	}
    
    
}