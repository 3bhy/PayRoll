package com.project.demo.scheduler;

import com.project.demo.entity.Employee;
import com.project.demo.repo.EmployeeSalaryRepo;
import com.project.demo.service.EmployeeSalaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class SalaryCalculationService {

	@Autowired
	private EmployeeSalaryRepo employeeSalaryRepo;

	@Autowired
	private EmployeeSalaryService employeeSalaryService;

	@Scheduled(cron = "0 0 12 27 * ?")
	public void calculateMonthlySalaries() {
		YearMonth currentYearMonth = YearMonth.now();

		List<Employee> employeesWithoutSalary = employeeSalaryRepo
				.findEmployeesWithoutSalary(currentYearMonth.getYear(), currentYearMonth.getMonthValue());

		for (Employee employee : employeesWithoutSalary) {
			try {
				employeeSalaryService.calculateEmployeeSalary(employee.getEmployee(), currentYearMonth.getYear(),
						currentYearMonth.getMonthValue());
				employeeSalaryService.lockSalary(employee.getEmployee(), currentYearMonth.getYear(),
						currentYearMonth.getMonthValue());
			} catch (Exception e) {
				System.err.println("Error calculating salary for employee ID: " + employee.getEmployee());
			}
		}
	}
}