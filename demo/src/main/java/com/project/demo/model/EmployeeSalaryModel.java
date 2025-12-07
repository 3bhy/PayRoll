package com.project.demo.model;

import java.util.Date;

public class EmployeeSalaryModel {

	private Integer salaryPaymentId;

	private Integer employeeId;

	private Integer year;

	private Integer month;

	private Date salaryDate;

	private Float calculatedSalary;

	private Float calculatedIncentive;

	private Float calculatedDiscount;

	private Float calculatedFinalSalary;

	private Float mainSalary;

	private Float incentive;

	private Float discount;

	private Float reward;

	private Float finalSalary;

	private Float salaryAmountPaid;

	private Float salaryDifference;

	private String discountReason;

	private String rewardReason;

	private Boolean salaryLocked;

	// Constructors
	public EmployeeSalaryModel() {
	}

	public EmployeeSalaryModel(Integer employeeId, Integer year, Integer month, Date salaryDate, Float mainSalary,
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

	public Integer getEmployeeId() {
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
