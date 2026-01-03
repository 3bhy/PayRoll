package com.project.demo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.EmployeeSalary;
public class EmployeeSalarySpec {
	 public static Specification<EmployeeSalary> hasEmployee(Integer employeeId) {
	        return (root, query, cb) -> employeeId == null ? null
	                : cb.equal(root.get("employeeId"), employeeId);
	    }

	    public static Specification<EmployeeSalary> hasYear(Integer year) {
	        return (root, query, cb) -> year == null ? null
	                : cb.equal(root.get("year"), year);
	    }

	    public static Specification<EmployeeSalary> hasMonth(Integer month) {
	        return (root, query, cb) -> month == null ? null
	                : cb.equal(root.get("month"), month);
	    }
	
}
