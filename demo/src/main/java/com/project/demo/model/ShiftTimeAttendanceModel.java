package com.project.demo.model;

import java.sql.Time;
import java.util.Date;

import com.project.demo.entity.Employee;

public class ShiftTimeAttendanceModel {
	
	    private Integer shiftTimeAttendanceId;
	    
	    private Employee employee;
	    
		private Date attendanceDate;
	    
	    private Time overTime;
	    
	    private Time lessTime;
	    
	    private Time totalActiveTime;
	    
	    private Time totalOverTime;
	    
	    private Float totalIncentiveSales;
	    
	  
	    
	   
	    public ShiftTimeAttendanceModel() {}


		public ShiftTimeAttendanceModel(Integer shiftTimeAttendanceId, Employee employee, Date attendanceDate,
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


	

}
