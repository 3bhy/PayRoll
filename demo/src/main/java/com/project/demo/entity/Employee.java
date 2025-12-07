package com.project.demo.entity;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "employee")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employeeId")
	private Integer employeeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	@JsonIgnore
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "personId")
	@JsonIgnore
	private Person person;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profileId")
	@JsonIgnore

	private Profile profile;

	@Column(name = "managerId")
	private Integer managerId;

	@Column(name = "salary")
	private Float salary;

	@Column(name = "salaryCycle")
	private String salaryCycle;

	@Column(name = "salesIncentivePercent")
	private Float salesIncentivePercent;

	@Column(name = "incentiveOnAllSales")
	private Boolean incentiveOnAllSales;

	@OneToMany
	private List<EmployeeShift> employeeShifts;

	@OneToMany
	private List<ShiftTimeAttendance> shiftTimeAttendances;

	@OneToMany
	private List<Login> logins;

	@OneToMany
	private List<EmployeeSalary> employeeSalaries;

	public Employee(Integer employeeId, Company company, Person person, Profile profile, Integer managerId,
			Float salary, String salaryCycle, Float salesIncentivePercent, Boolean incentiveOnAllSales) {
		super();
		this.employeeId = employeeId;
		this.company = company;
		this.person = person;
		this.profile = profile;
		this.managerId = managerId;
		this.salary = salary;
		this.salaryCycle = salaryCycle;
		this.salesIncentivePercent = salesIncentivePercent;
		this.incentiveOnAllSales = incentiveOnAllSales;
	}

	// Constructors
	public Employee() {
	}

	// Getters and Setters
	public Integer getEmployee() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
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