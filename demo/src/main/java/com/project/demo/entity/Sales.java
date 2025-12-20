package com.project.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sales")
public class Sales {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sales_id")
	private Integer salesId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employeeId")
	@JsonIgnore
	private Employee employee;

	@Column(name = "saleDate")
	private LocalDateTime saleDate;

	public Boolean getSeller() {
		return seller;
	}

	public void setSeller(Boolean seller) {
		this.seller = seller;
	}

	@Column(name = "seller")
	private Boolean seller;

	@Column(name = "is_return")
	private Boolean isReturn;

	public Boolean getIsReturn() {
		return isReturn;
	}

	public void setIsReturn(Boolean isReturn) {
		this.isReturn = isReturn;
	}

	@Column(name = "saleAmount")
	private Float saleAmount;

	public Sales() {
	}

	public Sales(Employee employee, LocalDateTime saleDate, Float saleAmount, Boolean seller, Boolean isReturn) {
		this.employee = employee;
		this.saleDate = saleDate;
		this.saleAmount = saleAmount;
		this.seller = seller;
		this.isReturn = isReturn;
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
