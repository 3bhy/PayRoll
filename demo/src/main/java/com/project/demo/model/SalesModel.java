package com.project.demo.model;

import java.time.LocalDateTime;

import com.project.demo.entity.Employee;

public class SalesModel {
	private Integer salesId;

	private Employee employee;

	private LocalDateTime saleDate;

	private Float saleAmount;

	public SalesModel(Employee employee, LocalDateTime saleDate, Float saleAmount) {
		this.employee = employee;
		this.saleDate = saleDate;
		this.saleAmount = saleAmount;
	}

	// Getters and Setters
	public Integer getSalesId() {
		return salesId;
	}

	public void setSalesId(Integer salesId) {
		this.salesId = salesId;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public LocalDateTime getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(LocalDateTime saleDate) {
		this.saleDate = saleDate;
	}

	public Float getSaleAmount() {
		return saleAmount;
	}

	public void setSaleAmount(Float saleAmount) {
		this.saleAmount = saleAmount;
	}

}
