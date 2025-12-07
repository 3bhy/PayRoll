package com.project.demo.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employeeSalary")
public class EmployeeSalary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "salaryPaymentId")
	private Integer salaryPaymentId;

	@Column(name = "employeeId")
	private Integer employeeId;

	@Column(name = "year")
	private Integer year;

	@Column(name = "month")
	private Integer month;

	@Column(name = "salaryDate")
	private Date salaryDate;

	@Column(name = "calculatedSalary")
	private Float calculatedSalary;

	@Column(name = "calculatedIncentive")
	private Float calculatedIncentive;

	@Column(name = "calculatedDiscount")
	private Float calculatedDiscount;

	@Column(name = "calculatedFinalSalary")
	private Float calculatedFinalSalary;

	@Column(name = "mainSalary")
	private Float mainSalary;

	@Column(name = "incentive")
	private Float incentive;

	@Column(name = "discount")
	private Float discount;

	@Column(name = "reward")
	private Float reward;

	@Column(name = "finalSalary")
	private Float finalSalary;

	@Column(name = "salaryAmountPaid")
	private Float salaryAmountPaid;

	@Column(name = "salaryDifference")
	private Float salaryDifference;

	@Column(name = "discountReason")
	private String discountReason;

	@Column(name = "rewardReason")
	private String rewardReason;

	@Column(name = "salaryLocked")
	private Boolean salaryLocked;

	// Constructors
	public EmployeeSalary() {
	}

	public EmployeeSalary(Integer employeeId, Integer year, Integer month, Date salaryDate, Float mainSalary,
			Float incentive, Float discount, Float reward) {
		this.employeeId = employeeId;
		this.year = year;
		this.month = month;
		this.salaryDate = salaryDate;
		this.mainSalary = mainSalary;
		this.incentive = incentive;
		this.discount = discount;
		this.reward = reward;
	}

	// Getters and Setters
	public Integer getSalaryPaymentId() {
		return salaryPaymentId;
	}

	public void setSalaryPaymentId(Integer salaryPaymentId) {
		this.salaryPaymentId = salaryPaymentId;
	}

	public Integer getEmployee() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Date getSalaryDate() {
		return salaryDate;
	}

	public void setSalaryDate(Date salaryDate) {
		this.salaryDate = salaryDate;
	}

	public Float getCalculatedSalary() {
		return calculatedSalary;
	}

	public void setCalculatedSalary(Float calculatedSalary) {
		this.calculatedSalary = calculatedSalary;
	}

	public Float getCalculatedIncentive() {
		return calculatedIncentive;
	}

	public void setCalculatedIncentive(Float calculatedIncentive) {
		this.calculatedIncentive = calculatedIncentive;
	}

	public Float getCalculatedDiscount() {
		return calculatedDiscount;
	}

	public void setCalculatedDiscount(Float calculatedDiscount) {
		this.calculatedDiscount = calculatedDiscount;
	}

	public Float getCalculatedFinalSalary() {
		return calculatedFinalSalary;
	}

	public void setCalculatedFinalSalary(Float calculatedFinalSalary) {
		this.calculatedFinalSalary = calculatedFinalSalary;
	}

	public Float getMainSalary() {
		return mainSalary;
	}

	public void setMainSalary(Float mainSalary) {
		this.mainSalary = mainSalary;
	}

	public Float getIncentive() {
		return incentive;
	}

	public void setIncentive(Float incentive) {
		this.incentive = incentive;
	}

	public Float getDiscount() {
		return discount;
	}

	public void setDiscount(Float discount) {
		this.discount = discount;
	}

	public Float getReward() {
		return reward;
	}

	public void setReward(Float reward) {
		this.reward = reward;
	}

	public Float getFinalSalary() {
		return finalSalary;
	}

	public void setFinalSalary(Float finalSalary) {
		this.finalSalary = finalSalary;
	}

	public Float getSalaryAmountPaid() {
		return salaryAmountPaid;
	}

	public void setSalaryAmountPaid(Float salaryAmountPaid) {
		this.salaryAmountPaid = salaryAmountPaid;
	}

	public Float getSalaryDifference() {
		return salaryDifference;
	}

	public void setSalaryDifference(Float salaryDifference) {
		this.salaryDifference = salaryDifference;
	}

	public String getDiscountReason() {
		return discountReason;
	}

	public void setDiscountReason(String discountReason) {
		this.discountReason = discountReason;
	}

	public String getRewardReason() {
		return rewardReason;
	}

	public void setRewardReason(String rewardReason) {
		this.rewardReason = rewardReason;
	}

	public Boolean getSalaryLocked() {
		return salaryLocked;
	}

	public void setSalaryLocked(Boolean salaryLocked) {
		this.salaryLocked = salaryLocked;
	}

}