package com.project.demo.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeSalary;
import com.project.demo.entity.ShiftTimeAttendance;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.EmployeeSalaryRepo;
import com.project.demo.repo.SalesRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.specification.EmployeeSalarySpec;
import com.project.demo.specification.ShiftTimeAttendanceSpec;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmployeeSalaryService {

	@Autowired
	private EmployeeSalaryRepo employeeSalaryRepo;
	@Autowired
	private SalesRepo employeeSaleRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;
	@Lazy
	@Autowired
	private shiftTimeAttendanceService shiftTimeAttendance;

	// Calculate Employee Salary
	public EmployeeSalary calculateEmployeeSalary(Integer employeeId, Integer year, Integer month) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Employee ID cannot be null");
		}
		if (year == null) {
			throw new IllegalArgumentException("Year cannot be null");
		}
		if (month == null) {
			throw new IllegalArgumentException("Month cannot be null");
		}
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("Month must be between 1 and 12");
		}

		int currentYear = Year.now().getValue();
		if (year < 2000 || year > currentYear + 1) {
			throw new IllegalArgumentException("Year must be between 2000 and " + (currentYear + 1));
		}
		Employee employee = employeeRepo.findById(employeeId)
				.orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

		Float mainSalary = employee.getSalary() != null ? employee.getSalary() : 0.0f;

		Float calculatedSalary = calculateBaseSalary(employee, year, month);

		Float calculatedIncentive = calculateMonthlyIncentive(employee.getEmployeeId(), year, month);
		return createOrUpdateSalary(employee, year, month, mainSalary, calculatedSalary, calculatedIncentive);

	}

	public void updateSalaryOnAttendanceChange(Integer employeeId, LocalDate attendanceDate) {
		try {

			if (attendanceDate == null) {
				System.err.println("Attendance date is null, skipping salary update");
				return;
			}
			Integer year = attendanceDate.getYear();
			Integer month = attendanceDate.getMonthValue();

			calculateEmployeeSalary(employeeId, year, month);
		} catch (Exception e) {
			System.err.println("Error updating salary on attendance change: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// main salary
	private Float getMainSalary(Employee employee) {
		return employee.getSalary() != null ? employee.getSalary() : 0.0f;
	}

	// calculated salary
	// calculated salary
	private Float calculateBaseSalary(Employee employee, Integer year, Integer month) {
		String salaryCycle = employee.getSalaryCycle();
		Float baseSalaryRate = employee.getSalary() != null ? employee.getSalary() : 0.0f;

		if (baseSalaryRate <= 0) {
			return 0.0f;
		}

		if ("DAY".equals(salaryCycle)) {
			// FIXME this calculate the salary according to the shift time regardless his
			// attendance !!
			// Salary should be calculated according to the attendance
			// FIXME DONE logic still needs to be revised, what are those workingDays?
			return calculateDailySalary(employee, year, month, baseSalaryRate);

		} else if ("HOUR".equals(salaryCycle)) {
			return calculateHourlySalary(employee, year, month, baseSalaryRate);

		} else if (("MONTH".equals(salaryCycle))) {
			return calculateMonthlySalary(employee, year, month, baseSalaryRate);
		} else {
			// Invalid salary cycle
			throw new IllegalArgumentException(
					"Invalid salary cycle '" + salaryCycle + "' for employee " + employee.getEmployeeId());
		}
	}

	private Float calculateDailySalary(Employee employee, Integer year, Integer month, Float dailyRate) {
		try {
			Integer employeeId = employee.getEmployeeId();

			Integer actualAttendanceDays = calculateAttendanceDays(employeeId, year, month);

			return actualAttendanceDays * dailyRate;

		} catch (Exception e) {
			System.err.println("Error calculating daily salary: " + e.getMessage());
			return 0.0f;
		}
	}

	private Integer calculateAttendanceDays(Integer employeeId, Integer year, Integer month) {
		try {
			List<ShiftTimeAttendance> monthlyAttendances = shiftTimeAttendanceRepository
					.findAll(ShiftTimeAttendanceSpec.byEmployeeAndMonth(employeeId, year, month));

			if (monthlyAttendances == null || monthlyAttendances.isEmpty()) {
				return 0;
			}

			return monthlyAttendances.size();

		} catch (Exception e) {
			System.err.println("Error calculating attendance days: " + e.getMessage());
			return 0;
		}
	}

	private Float calculateHourlySalary(Employee employee, Integer year, Integer month, Float hourlyRate) {
		try {
			Integer employeeId = employee.getEmployeeId();

			Float totalActualHours = getTotalActivityTime(employeeId, year, month);

			return totalActualHours * hourlyRate;

		} catch (Exception e) {
			System.err.println("Error calculating hourly salary: " + e.getMessage());
			return 0.0f;
		}
	}

	private Float calculateMonthlySalary(Employee employee, Integer year, Integer month, Float monthlySalary) {
		return employee.getSalary() != null ? employee.getSalary() : 0.0f;
	}

	private Float getTotalActivityTime(Integer employeeId, Integer year, Integer month) {
		String totalTimeStr = shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(employeeId, year,
				month);

		if (totalTimeStr == null || totalTimeStr.isEmpty()) {
			return 0f;
		}

		LocalTime time = LocalTime.parse(totalTimeStr);
		Duration duration = Duration.ofSeconds(time.toSecondOfDay());

		return duration.toMinutes() / 60f;
	}

	private EmployeeSalary createOrUpdateSalary(Employee employee, Integer year, Integer month, Float mainSalary,
			Float calculatedSalary, Float calculatedIncentive) {
		Optional<EmployeeSalary> existingSalary = employeeSalaryRepo
				.findOne(EmployeeSalarySpec.hasEmployee(employee.getEmployeeId()).and(EmployeeSalarySpec.hasYear(year))
						.and(EmployeeSalarySpec.hasMonth(month)));

		EmployeeSalary salary;
		if (existingSalary.isPresent()) {
			salary = existingSalary.get();
			salary.setMainSalary(mainSalary);
			salary.setCalculatedSalary(calculatedSalary);
			salary.setCalculatedIncentive(calculatedIncentive);
			salary.setCalculatedDiscount(0f);
		} else {
			salary = new EmployeeSalary();
			salary.setEmployeeId(employee.getEmployeeId());
			salary.setYear(year);
			salary.setMonth(month);
			salary.setSalaryDate(LocalDate.now());
			salary.setMainSalary(mainSalary);
			salary.setCalculatedSalary(calculatedSalary);
			salary.setCalculatedIncentive(calculatedIncentive);
			salary.setCalculatedDiscount(0f);
			salary.setDiscount(0f);
			salary.setReward(0f);
			salary.setIncentive(0f);
			salary.setSalaryLocked(false);
		}

		calculateFinalSalary(salary);
		return employeeSalaryRepo.save(salary);
	}

	public EmployeeSalary calculateAndStoreFinalSalary(EmployeeSalary employeeSalary) {

		if (employeeSalary == null)
			throw new IllegalArgumentException("Salary cannot be null");

		Float mainSalary = employeeSalary.getMainSalary() != null ? employeeSalary.getMainSalary() : 0f;
		Float reward = employeeSalary.getReward() != null ? employeeSalary.getReward() : 0f;
		Float discount = employeeSalary.getDiscount() != null ? employeeSalary.getDiscount() : 0f;
		Float incentive = employeeSalary.getIncentive() != null ? employeeSalary.getIncentive() : 0f;

		Float finalAmount = mainSalary + reward + incentive - discount;
		employeeSalary.setFinalSalary(finalAmount);

		if (employeeSalary.getSalaryAmountPaid() != null && employeeSalary.getFinalSalary() != null) {
			Float paymentDifference = employeeSalary.getFinalSalary() - employeeSalary.getSalaryAmountPaid();
			employeeSalary.setSalaryDifference(paymentDifference);
		}
		return employeeSalaryRepo.save(employeeSalary);
	}

	private void calculateFinalSalary(EmployeeSalary employeeSalary) {
		Float calculatedSalary = employeeSalary.getCalculatedSalary() != null ? employeeSalary.getCalculatedSalary()
				: 0f;
		Float calculatedIncentive = employeeSalary.getCalculatedIncentive() != null
				? employeeSalary.getCalculatedIncentive()
				: 0f;
		Float calculatedDiscount = employeeSalary.getCalculatedDiscount() != null
				? employeeSalary.getCalculatedDiscount()
				: 0f;
		// new update
		Float credit = employeeSaleRepo.calculateEmployeeCredit(employeeSalary.getEmployee());
		if (credit == null)
			credit = 0f;

		Float calculatedFinalSalary = calculatedSalary + calculatedIncentive - calculatedDiscount - credit;
		employeeSalary.setCalculatedFinalSalary(calculatedFinalSalary);

	}

	// add discount from user
	public EmployeeSalary addSalaryDiscount(Integer employeeId, Integer year, Integer month, Float amount,
			String reason) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Employee ID cannot be null");
		}
		if (amount == null) {
			throw new IllegalArgumentException("Amount cannot be null");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount cannot be negative");
		}
		EmployeeSalary employeeSalary = getOrCreateEmployeeSalary(employeeId, year, month);

		Float newDiscount = (employeeSalary.getDiscount() != null ? employeeSalary.getDiscount() : 0f) + amount;
		employeeSalary.setDiscount(newDiscount);
		employeeSalary.setDiscountReason(reason != null ? reason : "");

		calculateFinalSalary(employeeSalary);
		calculateAndStoreFinalSalary(employeeSalary);

		return employeeSalaryRepo.save(employeeSalary);
	}

	// add reward from user
	public EmployeeSalary addSalaryReward(Integer employeeId, Integer year, Integer month, Float amount,
			String reason) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Employee ID cannot be null");
		}
		if (amount == null) {
			throw new IllegalArgumentException("Amount cannot be null");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount cannot be negative");
		}
		EmployeeSalary employeeSalary = getOrCreateEmployeeSalary(employeeId, year, month);

		Float newReward = (employeeSalary.getReward() != null ? employeeSalary.getReward() : 0f) + amount;
		employeeSalary.setReward(newReward);
		employeeSalary.setRewardReason(reason);

		calculateFinalSalary(employeeSalary);
		calculateAndStoreFinalSalary(employeeSalary);

		return employeeSalaryRepo.save(employeeSalary);
	}

	// add incentive from user
	public EmployeeSalary addSalaryIncentive(Integer employeeId, Integer year, Integer month, Float amount,
			String reason) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Employee ID cannot be null");
		}
		if (amount == null) {
			throw new IllegalArgumentException("Amount cannot be null");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount cannot be negative");
		}
		EmployeeSalary employeeSalary = getOrCreateEmployeeSalary(employeeId, year, month);

		Float newIncentive = (employeeSalary.getIncentive() != null ? employeeSalary.getIncentive() : 0f) + amount;
		employeeSalary.setIncentive(newIncentive);

		calculateAndStoreFinalSalary(employeeSalary);

		return employeeSalaryRepo.save(employeeSalary);
	}

	// get employee salary
	private EmployeeSalary getOrCreateEmployeeSalary(Integer employeeId, Integer year, Integer month) {
		Optional<EmployeeSalary> existingSalary = employeeSalaryRepo.findOne(EmployeeSalarySpec.hasEmployee(employeeId)
				.and(EmployeeSalarySpec.hasYear(year)).and(EmployeeSalarySpec.hasMonth(month)));

		if (existingSalary.isPresent()) {
			return existingSalary.get();
		}

		Employee employee = employeeRepo.findById(employeeId)
				.orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

		Float mainSalary = getMainSalary(employee);
		Float calculatedSalary = calculateBaseSalary(employee, year, month);

		Float calculatedIncentive = calculateMonthlyIncentive(employeeId, year, month);

		return createOrUpdateSalary(employee, year, month, mainSalary, calculatedSalary, calculatedIncentive);
	}

//Calculate Incentive instesd of shift and attendance and incentive on all sales
	private Float calculateMonthlyIncentive(Integer employeeId, Integer year, Integer month) {
		Float totalMonthlyIncentive = 0.0f;

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			Float dailyIncentive = shiftTimeAttendance.calculateTotalIncentiveSales(employeeId, date);
			totalMonthlyIncentive += dailyIncentive != null ? dailyIncentive : 0.0f;
		}

		return totalMonthlyIncentive;
	}

	// pay salary
	public EmployeeSalary paySalaryDirect(Integer employeeId, Integer year, Integer month, Float amountPaid) {
		EmployeeSalary salary = employeeSalaryRepo
				.findOne(EmployeeSalarySpec.hasEmployee(employeeId).and(EmployeeSalarySpec.hasYear(year))
						.and(EmployeeSalarySpec.hasMonth(month)))
				.orElseThrow(() -> new EntityNotFoundException("Salary record not found for employee: " + employeeId
						+ ", year: " + year + ", month: " + month));

		if (Boolean.TRUE.equals(salary.getSalaryLocked())) {
			throw new RuntimeException("Salary is locked and cannot be modified");
		}

		Float currentAmountPaid = salary.getSalaryAmountPaid() != null ? salary.getSalaryAmountPaid() : 0.0f;
		Float totalAmountPaid = currentAmountPaid + amountPaid;
		salary.setSalaryAmountPaid(totalAmountPaid);

		calculateFinalSalary(salary);

		return employeeSalaryRepo.save(salary);
	}

	public Optional<EmployeeSalary> getEmployeeSalary(Integer employeeId, Integer year, Integer month) {
		return employeeSalaryRepo.findOne(EmployeeSalarySpec.hasEmployee(employeeId)
				.and(EmployeeSalarySpec.hasYear(year)).and(EmployeeSalarySpec.hasMonth(month)));
	}

	// update main salary
	public EmployeeSalary updateBaseSalary(Integer employeeId, Integer year, Integer month, Float newBaseSalary) {
		if (employeeId == null) {
			throw new IllegalArgumentException("Employee ID cannot be null");
		}
		if (newBaseSalary == null) {
			throw new IllegalArgumentException("Base salary cannot be null");
		}
		if (newBaseSalary < 0) {
			throw new IllegalArgumentException("Base salary cannot be negative");
		}

		EmployeeSalary employeeSalary = employeeSalaryRepo.findOne(EmployeeSalarySpec.hasEmployee(employeeId)
				.and(EmployeeSalarySpec.hasYear(year)).and(EmployeeSalarySpec.hasMonth(month))).orElseGet(() -> {
					EmployeeSalary newSalary = new EmployeeSalary();
					newSalary.setEmployeeId(employeeId);
					newSalary.setYear(year);
					newSalary.setMonth(month);
					newSalary.setSalaryDate(LocalDate.now());
					return newSalary;
				});

		employeeSalary.setMainSalary(newBaseSalary);
		calculateFinalSalary(employeeSalary);

		return employeeSalaryRepo.save(employeeSalary);
	}

	// lock salary
	public EmployeeSalary lockSalary(Integer employeeId, Integer year, Integer month) {
		EmployeeSalary employeeSalary = employeeSalaryRepo
				.findOne(EmployeeSalarySpec.hasEmployee(employeeId).and(EmployeeSalarySpec.hasYear(year))
						.and(EmployeeSalarySpec.hasMonth(month)))
				.orElseThrow(() -> new RuntimeException("Salary not found"));

		employeeSalary.setSalaryLocked(true);
		return employeeSalaryRepo.save(employeeSalary);
	}

	public EmployeeSalary createEmployeeSalary(Employee employee) {
		EmployeeSalary employeeSalary = new EmployeeSalary();
		employeeSalary.setEmployeeId(employee.getEmployeeId());
		employeeSalary.setMainSalary(employee.getSalary());
		employeeSalary.setCalculatedSalary(employee.getSalary());
		employeeSalary.setCalculatedIncentive(0f);
		employeeSalary.setCalculatedDiscount(0f);

		return employeeSalaryRepo.save(employeeSalary);
	}
}