package com.project.demo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeSalary;

import jakarta.persistence.criteria.Subquery;

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
	
	
	
	 public static Specification<Employee> withoutSalaryForYearAndMonth(
	            Integer year, Integer month) {

	        return (root, query, cb) -> {

	            Subquery<Integer> subquery = query.subquery(Integer.class);
	            var salaryRoot = subquery.from(EmployeeSalary.class);

	            subquery.select(salaryRoot.get("employeeId"))
	                    .where(
	                        cb.equal(salaryRoot.get("employeeId"), root.get("employeeId")),
	                        cb.equal(salaryRoot.get("year"), year),
	                        cb.equal(salaryRoot.get("month"), month)
	                    );

	            return cb.not(cb.exists(subquery));
	        };
	    }
}
