package com.project.demo.entity;

import java.time.LocalDate;

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
@Table(name = "employee_shift")
public class EmployeeShift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employee_shift_id")
	private Integer employeeShiftId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shift_id")
	private Shift shift;

	@Column(name = "active")
	private Boolean active;
    
	@Column(name = "start_active_date")
	private LocalDate startActiveDate;
	
	@Column(name = "end_active_date")
	private LocalDate endActiveDate;

	public EmployeeShift() {
	}

	public EmployeeShift(Integer employeeShiftId, Employee employee, Shift shift, Boolean active, LocalDate startActiveDate,
			LocalDate endActiveDate) {
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

	public LocalDate getStartActiveDate() {
		return startActiveDate;
	}

	public void setStartActiveDate(LocalDate startActiveDate) {
		this.startActiveDate = startActiveDate;
	}

	public LocalDate getEndActiveDate() {
		return endActiveDate;
	}

	public void setEndActiveDate(LocalDate endActiveDate) {
		this.endActiveDate = endActiveDate;
	}
}