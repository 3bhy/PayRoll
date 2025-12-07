package com.project.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "companyRole")
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "companyRoleId")
	private Integer companyRoleId;

	public Role() {
	}

	public Role(Integer companyRoleId) {
		this.companyRoleId = companyRoleId;
	}

	public int getCompanyRoleId() {
		return companyRoleId;
	}

	public void setCompanyRoleId(Integer companyRoleId) {
		this.companyRoleId = companyRoleId;
	}

}
