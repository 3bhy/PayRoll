package com.project.demo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.Employee;

public class EmployeeSpec {

	public static Specification<Employee> hasCompany(Integer companyId) {
		 System.out.println("EmployeeSpec.hasCompany called with: " + companyId);
		return (root, query, cb) -> companyId == null ? null
				: cb.equal(root.get("company").get("companyId"), companyId);
		
	}

	public static Specification<Employee> hasPerson(Integer personId) {
		 System.out.println("EmployeeSpec.hasCompany called with: " + personId);
		return (root, query, cb) -> personId == null ? null : cb.equal(root.get("person").get("personId"), personId);
	}

	public static Specification<Employee> hasManager(Integer managerId) {
		 System.out.println("EmployeeSpec.hasCompany called with: " + managerId);
		return (root, query, cb) -> managerId == null ? null
				: cb.equal(root.get("manager").get("employeeId"), managerId);
	}
}
