package com.project.demo.model;

import java.sql.Date;

public class EmployeeShiftModel {

	private Integer employee_shift_id;

	private Integer employee_id;

	private Integer shift_id;

	private Boolean active;

	private Date startActiveDate;

	private Date endActiveDate;

	public EmployeeShiftModel() {
	}

	public EmployeeShiftModel(Integer employee_shift_id, Integer employee_id, Integer shift_id, Boolean active,
			Date startActiveDate, Date endActiveDate) {

		this.employee_shift_id = employee_shift_id;
		this.employee_id = employee_id;
		this.shift_id = shift_id;
		this.active = active;
		this.startActiveDate = startActiveDate;
		this.endActiveDate = endActiveDate;
	}

	public int getEmployee_shift_id() {
		return employee_shift_id;
	}

	public void setEmployee_shift_id(Integer employee_shift_id) {
		this.employee_shift_id = employee_shift_id;
	}

	public Integer getEmployeeId() {
		return employee_id;
	}

	public void setEmployeeId(Integer employee_id) {
		this.employee_id = employee_id;
	}

	public Integer getShiftId() {
		return shift_id;
	}

	public void setShiftId(Integer shift_id) {
		this.shift_id = shift_id;
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
