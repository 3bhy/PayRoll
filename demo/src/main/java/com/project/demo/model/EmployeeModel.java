package com.project.demo.model;

public class EmployeeModel {

	private Integer employeeId;
	private Integer companyId;
	private Integer personId;
	private Integer profileId;
	private Integer managerId;
	private Float salary;
	private String salaryCycle;
	private Float salesIncentivePercent;
	private Boolean incentiveOnAllSales;

	public EmployeeModel(Integer employeeId, Integer companyId, Integer personId, Integer profileId, Integer managerId,
			Float salary, String salaryCycle, Float salesIncentivePercent, Boolean incentiveOnAllSales) {

		this.employeeId = employeeId;
		this.companyId = companyId;
		this.personId = personId;
		this.profileId = profileId;
		this.managerId = managerId;
		this.salary = salary;
		this.salaryCycle = salaryCycle;
		this.salesIncentivePercent = salesIncentivePercent;
		this.incentiveOnAllSales = incentiveOnAllSales;
	}
	public EmployeeModel() {}
	// Getters and Setters
	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
		this.personId = personId;
	}

	public Integer getProfileId() {
		return profileId;
	}

	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}

	public Float getSalary() {
		return salary;
	}

	public void setSalary(Float salary) {
		this.salary = salary;
	}

	public String getSalaryCycle() {
		return salaryCycle;
	}

	public void setSalaryCycle(String salaryCycle) {
		this.salaryCycle = salaryCycle;
	}

	public Float getSalesIncentivePercent() {
		return salesIncentivePercent;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public void setSalesIncentivePercent(Float salesIncentivePercent) {
		this.salesIncentivePercent = salesIncentivePercent;
	}

	public Boolean getIncentiveOnAllSales() {
		return incentiveOnAllSales;
	}

	public void setIncentiveOnAllSales(Boolean incentiveOnAllSales) {
		this.incentiveOnAllSales = incentiveOnAllSales;
	}

}
