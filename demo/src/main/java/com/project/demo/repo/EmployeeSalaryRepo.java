package com.project.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeSalary;

@Repository
public interface EmployeeSalaryRepo extends JpaRepository<EmployeeSalary, Integer> {

	
	@Query("SELECT es FROM EmployeeSalary es WHERE es.employeeId = :employeeId AND es.year = :year AND es.month = :month")
	Optional<EmployeeSalary> findByEmployeeIdAndYearAndMonth(@Param("employeeId") Integer employeeId,
			@Param("year") Integer year, @Param("month") Integer month);

	@Query(value = """
		    SELECT e.* 
		    FROM employee e
		    LEFT JOIN employee_salary es
		        ON e.employee_id = es.employee_id
		        AND es.year = :year
		        AND es.month = :month
		    WHERE es.salary_payment_id IS NULL
		""", nativeQuery = true)
		List<Employee> findEmployeesWithoutSalary(@Param("year") int year,
		                                          @Param("month") int month);

}
