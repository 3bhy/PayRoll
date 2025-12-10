package com.project.demo.entity;

import java.sql.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "employeeShift")
public class EmployeeShift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employeeShiftId")
	private Integer employeeShiftId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employeeId")
	private Employee employee;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shiftId")
	private Shift shift;

	@Column(name = "active")
	private Boolean active;
    
	@Column(name = "startActiveDate")
	private Date startActiveDate;
	
	@Column(name = "endActiveDate")
	private Date endActiveDate;

	public EmployeeShift() {
	}

	public EmployeeShift(Integer employeeShiftId, Employee employee, Shift shift, Boolean active, Date startActiveDate,
			Date endActiveDate) {
		this.employeeShiftId = employeeShiftId;
		this.employee = employee;
		this.shift = shift;
		this.active = active;
		this.startActiveDate = startActiveDate;
		this.endActiveDate = endActiveDate;
	}

	public Integer getEmployeeShiftId() {
		return employeeShiftId;
	}

	public void setEmployeeShiftId(Integer employeeShiftId) {
		this.employeeShiftId = employeeShiftId;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getStartActiveDate() {
		return startActiveDate;
	}

	public void setStartActiveDate(Date startActiveDate) {
		this.startActiveDate = startActiveDate;
	}

	public Date getEndActiveDate() {
		return endActiveDate;
	}

	public void setEndActiveDate(Date endActiveDate) {
		this.endActiveDate = endActiveDate;
	}
}