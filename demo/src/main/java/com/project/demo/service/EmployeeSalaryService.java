package com.project.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeSalary;
import com.project.demo.entity.ShiftTime;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.EmployeeSalaryRepo;
import com.project.demo.repo.SalesRepo;
import com.project.demo.repo.ShiftTimeRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.specification.ShiftTimeSpec;

import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmployeeSalaryService {

	@Autowired
	private EmployeeSalaryRepo employeeSalaryRepo;

	@Autowired
	private EmployeeRepo employeeRepo;

	@Autowired
	private SalesRepo salesRepo;
	@Autowired
	private ShiftTimeRepo shiftTimeRepo;
	@Autowired
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;

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

		// ADD YEAR VALIDATION HERE
		int currentYear = Year.now().getValue();
		if (year < 2000 || year > currentYear + 1) {
			throw new IllegalArgumentException("Year must be between 2000 and " + (currentYear + 1));
		}
		Employee employee = employeeRepo.findById(employeeId)
				.orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

		Float mainSalary = employee.getSalary() != null ? employee.getSalary() : 0.0f;

		Float calculatedSalary = calculateBaseSalary(employee, year, month);

		Float calculatedIncentive = calculateIncentive(employee, year, month);

		return createOrUpdateSalary(employee, year, month, mainSalary, calculatedSalary, calculatedIncentive);
	}

	public void updateSalaryOnAttendanceChange(Integer employeeId, Date attendanceDate) {
		try {

			if (attendanceDate == null) {
				System.err.println("Attendance date is null, skipping salary update");
				return;
			}
			java.time.LocalDate localDate;
			if (attendanceDate instanceof java.sql.Date) {
				localDate = java.time.LocalDate.parse(attendanceDate.toString());
			} else {
				localDate = attendanceDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
			}

			Integer year = localDate.getYear();
			Integer month = localDate.getMonthValue();

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
	private Float calculateBaseSalary(Employee employee, Integer year, Integer month) {
		String salaryCycle = employee.getSalaryCycle();
		Float baseSalaryRate = employee.getSalary() != null ? employee.getSalary() : 0.0f;

		Integer workingDays = calculateUniqueWorkingDays(employee.getEmployee());

		if ("DAY".equals(salaryCycle)) {
			// FIXME this calculate the salary according to the shift time regardless his
			// attendance !!
			// Salary should be calculated according to the attendance
			// FIXME-DONE
			Integer workingDaysPerMonth = workingDays;
			return baseSalaryRate * workingDaysPerMonth;

		} else if ("HOUR".equals(salaryCycle)) {
			Float totalActivityTime = getTotalActivityTime(employee.getEmployee(), year, month);

			if (totalActivityTime > 0) {
				return baseSalaryRate * totalActivityTime;
			} else {
				// FIXME here if the employee didn't work through the month,you will give him a
				// salary equals 1 hour!!
				// He didn't work this hour!!
				// FIXME-DONE

				return 0f;
			}
		} else if (("MONTH".equals(salaryCycle))) {
			// FIXME there is still a third valid case which is the monthly salary
			// FIXME-DONE
			return baseSalaryRate;
		} else {
			// Invalid salary cycle
			throw new IllegalArgumentException(
					"Invalid salary cycle '" + salaryCycle + "' for employee " + employee.getEmployee());
		}
	}

	private Float getTotalActivityTime(Integer employeeId, Integer year, Integer month) {
		return shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(employeeId, year, month);
	}

	private Integer calculateUniqueWorkingDays(Integer employeeId) {
		
			List<Integer> distinctDays = shiftTimeRepo.findDistinctDaysWithAttendance(employeeId);
			// FIXME if distinct days are null, the employee has no active shift. So return
			// should be zero
			// FIXME-DONE
			return distinctDays != null ? distinctDays.size() : 0;
		
	}

	// FIXME incentive sales
	// FIXME-DONE

	private Float calculateIncentive(Employee employee, Integer year, Integer month) {
		Float incentivePercent = employee.getSalesIncentivePercent() != null ? employee.getSalesIncentivePercent()
				: 0.0f;
		if (incentivePercent == 0.0f)
			return 0.0f;

		Float totalSales = 0.0f;

		if (Boolean.TRUE.equals(employee.getIncentiveOnAllSales())) {
			List<ShiftTime> shifts = getShiftTimesFromDatabase(employee.getEmployee());
			if (shifts.isEmpty())
				return 0.0f;

			List<Date> attendanceDates = shiftTimeAttendanceRepository.findAttendanceDates(employee.getEmployee(), year,
					month);
			if (attendanceDates.isEmpty())
				return 0.0f;

			for (Date date : attendanceDates) {
				for (ShiftTime shift : shifts) {
					LocalTime shiftStart = shift.getFromTime();
					LocalTime shiftEnd = shift.getToTime();

					if (shiftStart != null && shiftEnd != null) {
						totalSales += salesRepo.calculateSalesForEmployeeOnDate(employee.getEmployee(), date,
								shiftStart, shiftEnd);
					}
				}
			}
			// FIXME this returns all sales between the defined times all days. Does the
			// employee work every day at the same time with no weekends?
			// FIXME-DONE
		} else {
			totalSales = salesRepo.calculateEmployeeSalesByMonth(employee.getEmployee(), year, month);
		}

		return totalSales * (incentivePercent / 100);
	}

// Get shift times from database
	private List<ShiftTime> getShiftTimesFromDatabase(Integer employeeId) {
		try {
			Specification<ShiftTime> spec = Specification.where(ShiftTimeSpec.hasEmployee(employeeId))
					.and(ShiftTimeSpec.isActive());

			List<ShiftTime> shifts = shiftTimeRepo.findAll(spec);

			return shifts.stream().filter(shift -> shift.getFromTime() != null && shift.getToTime() != null)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("Error getting shift times for employee " + employeeId, e);
		}
	}

	private EmployeeSalary createOrUpdateSalary(Employee employee, Integer year, Integer month, Float mainSalary,
			Float calculatedSalary, Float calculatedIncentive) {
		Optional<EmployeeSalary> existingSalary = employeeSalaryRepo
				.findByEmployeeIdAndYearAndMonth(employee.getEmployee(), year, month);

		EmployeeSalary salary;
		if (existingSalary.isPresent()) {
			salary = existingSalary.get();
			salary.setMainSalary(mainSalary);
			salary.setCalculatedSalary(calculatedSalary);
			salary.setCalculatedIncentive(calculatedIncentive);
			salary.setCalculatedDiscount(0f);
		} else {
			salary = new EmployeeSalary();
			salary.setEmployeeId(employee.getEmployee());
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

	public EmployeeSalary addFinalSalary(Integer employeeId, Integer year, Integer month, Float finalSalary) {
		EmployeeSalary employeeSalary = employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
				.orElseThrow(() -> new EntityNotFoundException("Salary record not found for employee: " + employeeId
						+ ", year: " + year + ", month: " + month));

		employeeSalary.setFinalSalary(finalSalary);
		calculateFinalSalary(employeeSalary);

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

		Float calculatedFinalSalary = calculatedSalary + calculatedIncentive - calculatedDiscount;
		employeeSalary.setCalculatedFinalSalary(calculatedFinalSalary);

		Float finalSalary = employeeSalary.getFinalSalary();
		if (finalSalary != null) {
			Float incentive = employeeSalary.getIncentive() != null ? employeeSalary.getIncentive() : 0f;
			Float discount = employeeSalary.getDiscount() != null ? employeeSalary.getDiscount() : 0f;
			Float Reward = employeeSalary.getReward() != null ? employeeSalary.getReward() : 0f;

			// FinalSalary: from user + incentive - discount + reward
			Float adjustedFinalSalary = finalSalary + incentive - discount + Reward;
			employeeSalary.setFinalSalary(adjustedFinalSalary);

		}

		if (employeeSalary.getSalaryAmountPaid() != null && employeeSalary.getFinalSalary() != null) {
			Float paymentDifference = employeeSalary.getFinalSalary() - employeeSalary.getSalaryAmountPaid();
			employeeSalary.setSalaryDifference(paymentDifference);
		}
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
		employeeSalary.setDiscountReason(reason);

		calculateFinalSalary(employeeSalary);
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

		calculateFinalSalary(employeeSalary);

		return employeeSalaryRepo.save(employeeSalary);
	}

	// get employee salary
	private EmployeeSalary getOrCreateEmployeeSalary(Integer employeeId, Integer year, Integer month) {
		Optional<EmployeeSalary> existingSalary = employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year,
				month);

		if (existingSalary.isPresent()) {
			return existingSalary.get();
		}

		Employee employee = employeeRepo.findById(employeeId)
				.orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

		Float mainSalary = getMainSalary(employee);
		Float calculatedSalary = calculateBaseSalary(employee, year, month);
		Float calculatedIncentive = calculateIncentive(employee, year, month);

		return createOrUpdateSalary(employee, year, month, mainSalary, calculatedSalary, calculatedIncentive);
	}

	// pay salary
	public EmployeeSalary paySalaryDirect(Integer employeeId, Integer year, Integer month, Float amountPaid) {
		EmployeeSalary salary = employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
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
		return employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year, month);
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

		EmployeeSalary employeeSalary = employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
				.orElseGet(() -> {
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
		EmployeeSalary employeeSalary = employeeSalaryRepo.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
				.orElseThrow(() -> new RuntimeException("Salary not found"));

		employeeSalary.setSalaryLocked(true);
		return employeeSalaryRepo.save(employeeSalary);
	}

	public EmployeeSalary createEmployeeSalary(Employee employee) {
		EmployeeSalary employeeSalary = new EmployeeSalary();
		employeeSalary.setEmployeeId(employee.getEmployee());
		employeeSalary.setMainSalary(employee.getSalary());
		employeeSalary.setCalculatedSalary(employee.getSalary());
		employeeSalary.setCalculatedIncentive(0f);
		employeeSalary.setCalculatedDiscount(0f);

		return employeeSalaryRepo.save(employeeSalary);
	}
}