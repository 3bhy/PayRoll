package com.project.demo.entity;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private LocalDate attendanceDate;
    
    @Column(name = "overTime")
    private LocalTime overTime;
    
    @Column(name = "lessTime")
    private LocalTime lessTime;
    
    @Column(name = "totalActiveTime")
    private LocalTime totalActiveTime;
    
    @Column(name = "totalOverTime")
    private LocalTime totalOverTime;
    
    @Column(name = "totalIncentiveSales")
    private Float totalIncentiveSales;
    
    // Relationships
    @OneToMany
    private List<Login> logins;
   
   
    public ShiftTimeAttendance() {}


	public ShiftTimeAttendance(Integer shiftTimeAttendanceId, Employee employee, LocalDate attendanceDate,
			LocalTime overTime, LocalTime lessTime, LocalTime totalActiveTime, LocalTime totalOverTime,
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


	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}


	public void setAttendanceDate(LocalDate loginDate) {
		this.attendanceDate = loginDate;
	}


	public LocalTime getOverTime() {
		return overTime;
	}


	public void setOverTime(LocalTime overTime) {
		this.overTime = overTime;
	}


	public LocalTime getLessTime() {
		return lessTime;
	}


	public void setLessTime(LocalTime lessTime) {
		this.lessTime = lessTime;
	}


	public LocalTime getTotalActiveTime() {
		return totalActiveTime;
	}


	public void setTotalActiveTime(LocalTime totalActiveTime) {
		this.totalActiveTime = totalActiveTime;
	}


	public LocalTime getTotalOverTime() {
		return totalOverTime;
	}


	public void setTotalOverTime(LocalTime totalOverTime) {
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