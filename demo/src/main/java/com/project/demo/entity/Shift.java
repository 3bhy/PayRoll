package com.project.demo.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
@Table(name = "shift")
public class Shift {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shiftId")
	private Integer shiftId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "companyId")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Company company;

	@Column(name = "shiftName")
	private String shiftName;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<ShiftTime> shiftTimes = new ArrayList<>();

	@OneToMany
	@JsonIgnore
	private List<EmployeeShift> employeeShifts = new ArrayList<>();

	public Shift() {
	}

	public Shift(Integer shiftId, Company company, String shiftName) {
		this.shiftId = shiftId;
		this.company = company;
		this.shiftName = shiftName;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public void addShiftTime(ShiftTime shiftTime) {
		shiftTimes.add(shiftTime);
		shiftTime.setShiftId(this);
	}

	public void removeShiftTime(ShiftTime shiftTime) {
		shiftTimes.remove(shiftTime);
		shiftTime.setShiftId(null);
	}

	public List<ShiftTime> getShiftTimes() {
		return shiftTimes;
	}

	public void setShiftTimes(List<ShiftTime> shiftTimes) {
		this.shiftTimes = shiftTimes;
	}

	public int getShiftId() {
		return shiftId;
	}

	public List<EmployeeShift> getEmployeeShifts() {
		return employeeShifts;
	}

	public void setEmployeeShifts(List<EmployeeShift> employeeShifts) {
		this.employeeShifts = employeeShifts;
	}

	public void setShiftId(Integer shiftId) {
		this.shiftId = shiftId;
	}

	public String getShiftName() {
		return shiftName;
	}

	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}
}