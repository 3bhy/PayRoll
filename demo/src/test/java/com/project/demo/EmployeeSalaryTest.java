package com.project.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeSalary;
import com.project.demo.entity.ShiftTimeAttendance;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.EmployeeSalaryRepo;
import com.project.demo.repo.SalesRepo;
import com.project.demo.repo.ShiftTimeRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.service.EmployeeSalaryService;
import com.project.demo.service.shiftTimeAttendanceService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeSalaryTest {

	@Mock
	private EmployeeSalaryRepo employeeSalaryRepo;
	@Mock
	private EmployeeRepo employeeRepo;
	@Mock
	private SalesRepo salesRepo;
	@Mock
	private ShiftTimeRepo shiftTimeRepo;
	@Mock
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;
	@Mock
	private shiftTimeAttendanceService shiftTimeAttendanceService;

	@InjectMocks
	private EmployeeSalaryService employeeSalaryService;

	// ==================== NULL VALUES TESTS ====================

	@Test
	void testCalculateEmployeeSalary_NullEmployeeId() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.calculateEmployeeSalary(null, 2024, 12));
	}

	@Test
	void testCalculateEmployeeSalary_NullYear() {
		assertThrows(IllegalArgumentException.class, () -> employeeSalaryService.calculateEmployeeSalary(1, null, 12));
	}

	@Test
	void testCalculateEmployeeSalary_NullMonth() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.calculateEmployeeSalary(1, 2024, null));
	}

	@Test
	void testUpdateSalaryOnAttendanceChange_NullDate() {
		employeeSalaryService.updateSalaryOnAttendanceChange(1, null);
		verify(employeeRepo, never()).findById(anyInt());
	}

	@Test
	void testAddSalaryDiscount_NullAmount() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.addSalaryDiscount(1, 2024, 12, null, "lose"));
	}

	@Test
	void testAddSalaryReward_NullAmount() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.addSalaryReward(1, 2024, 12, null, "reward"));
	}

	// ==================== WRONG DATA TESTS ====================

	@Test
	void testCalculateEmployeeSalary_MonthLessThan1() {
		assertThrows(IllegalArgumentException.class, () -> employeeSalaryService.calculateEmployeeSalary(1, 2024, 0));
	}

	@Test
	void testCalculateEmployeeSalary_MonthGreaterThan12() {
		assertThrows(IllegalArgumentException.class, () -> employeeSalaryService.calculateEmployeeSalary(1, 2024, 13));
	}

	@Test
	void testAddSalaryDiscount_NegativeAmount() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.addSalaryDiscount(1, 2024, 12, -500.0f, "lose"));
	}

	@Test
	void testAddSalaryReward_NegativeAmount() {
		assertThrows(IllegalArgumentException.class,
				() -> employeeSalaryService.addSalaryReward(1, 2024, 12, -1000.0f, "reward"));
	}

	// ==================== EMPTY/INVALID DATA TESTS ====================

	@Test
	void testCalculateEmployeeSalary_ZeroSalary() {

		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(0.0f);
		employee.setSalaryCycle("DAY");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));

		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);
		assertEquals(0.0f, result.getMainSalary());
		assertEquals(0.0f, result.getCalculatedSalary());
	}

	@Test
	void testCalculateEmployeeSalary_WithExistingSalary() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(5000.0f);
		employee.setSalaryCycle("DAY");

		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setMainSalary(5000.0f);
		existingSalary.setCalculatedSalary(25000.0f);
		existingSalary.setCalculatedIncentive(1000.0f);

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		lenient().when(shiftTimeAttendanceRepository.findAll(any(Specification.class)))
				.thenReturn(Collections.emptyList());
		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(50.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);
		assertEquals(5000.0f, result.getMainSalary());
		assertEquals(1550.0f, result.getCalculatedIncentive(), 0.01);
	}

	@Test
	void testAddSalaryDiscount_NullReason() {
		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setDiscount(0.0f);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.addSalaryDiscount(1, 2024, 12, 500.0f, null);

		assertEquals("", result.getDiscountReason());
		assertEquals(500.0f, result.getDiscount());
	}

	@Test
	void testAddSalaryDiscount_WithReason() {

		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setDiscount(100.0f);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.addSalaryDiscount(1, 2024, 12, 500.0f, "lose");

		assertEquals("lose", result.getDiscountReason());
		assertEquals(600.0f, result.getDiscount());
	}

	// ==================== PAY SALARY TESTS ====================

	@Test
	void testPaySalaryDirect_Overpayment() {
		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setFinalSalary(5000.0f);
		existingSalary.setSalaryAmountPaid(0.0f);
		existingSalary.setSalaryLocked(false);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.paySalaryDirect(1, 2024, 12, 10000.0f);

		assertEquals(10000.0f, result.getSalaryAmountPaid());
		verify(employeeSalaryRepo, times(1)).save(any(EmployeeSalary.class));
	}

	@Test
	void testPaySalaryDirect_MultiplePayments() {
		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setFinalSalary(10000.0f);
		existingSalary.setSalaryAmountPaid(3000.0f);
		existingSalary.setSalaryLocked(false);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.paySalaryDirect(1, 2024, 12, 4000.0f);

		assertEquals(7000.0f, result.getSalaryAmountPaid());
	}

	@Test
	void testPaySalaryDirect_SalaryLocked() {
		EmployeeSalary lockedSalary = new EmployeeSalary();
		lockedSalary.setEmployeeId(1);
		lockedSalary.setYear(2024);
		lockedSalary.setMonth(12);
		lockedSalary.setSalaryLocked(true);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(lockedSalary));

		assertThrows(RuntimeException.class, () -> employeeSalaryService.paySalaryDirect(1, 2024, 12, 1000.0f));

		verify(employeeSalaryRepo, never()).save(any(EmployeeSalary.class));
	}

	@Test
	void testPaySalaryDirect_SalaryNotFound() {
		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());

		assertThrows(jakarta.persistence.EntityNotFoundException.class,
				() -> employeeSalaryService.paySalaryDirect(1, 2024, 12, 1000.0f));
	}

	// ==================== ATTENDANCE CALCULATION TESTS ====================

	@Test
	void testCalculateEmployeeSalary_DailyCycleWithAttendances() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(1000.0f);
		employee.setSalaryCycle("DAY");

		List<ShiftTimeAttendance> mockAttendances = Collections.nCopies(5, new ShiftTimeAttendance());

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(shiftTimeAttendanceRepository.findAll(any(Specification.class))).thenReturn(mockAttendances);

		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(50.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);
		assertEquals(5000.0f, result.getCalculatedSalary());
		assertEquals(1550.0f, result.getCalculatedIncentive(), 0.01);
	}

	@Test
	void testCalculateEmployeeSalary_HourlyCycle() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(50.0f);
		employee.setSalaryCycle("HOUR");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(1, 2024, 12)).thenReturn("16:00:00");
		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(25.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);

		assertEquals(800.0f, result.getCalculatedSalary());
		assertEquals(775.0f, result.getCalculatedIncentive(), 0.01);
	}

	@Test
	void testCalculateEmployeeSalary_HourlyCycle_ValidTime() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(50.0f);
		employee.setSalaryCycle("HOUR");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(1, 2024, 12)).thenReturn("08:30:00");

		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(25.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);
		assertEquals(425.0f, result.getCalculatedSalary(), 0.01);
		assertEquals(775.0f, result.getCalculatedIncentive(), 0.01);
	}

	@Test
	void testCalculateEmployeeSalary_MonthlyCycle() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(15000.0f);
		employee.setSalaryCycle("MONTH");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(100.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);

		assertNotNull(result);
		assertEquals(15000.0f, result.getCalculatedSalary());
		assertEquals(3100.0f, result.getCalculatedIncentive(), 0.01);
	}

	@Test
	void testCalculateEmployeeSalary_InvalidSalaryCycle() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(5000.0f);
		employee.setSalaryCycle("WEEK");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));

		assertThrows(IllegalArgumentException.class, () -> employeeSalaryService.calculateEmployeeSalary(1, 2024, 12));
	}

	// ==================== INCENTIVE CALCULATION TESTS ====================

	@Test
	void testGetOrCreateEmployeeSalary_CreatesNewWhenNotFound() {
		Integer employeeId = 1;
		Integer year = 2024;
		Integer month = 12;

		Employee employee = new Employee();
		employee.setEmployeeId(employeeId);
		employee.setSalary(5000.0f);
		employee.setSalaryCycle("MONTH");

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(employeeId), any(LocalDate.class)))
				.thenReturn(50.0f);

		EmployeeSalary result = employeeSalaryService.addSalaryDiscount(employeeId, year, month, 500.0f, "test");

		assertNotNull(result);
		assertEquals(employeeId, result.getEmployee());
		assertEquals(year, result.getYear());
		assertEquals(month, result.getMonth());
	}

	@Test
	void testUpdateBaseSalary() {
		Integer employeeId = 1;
		Integer year = 2024;
		Integer month = 12;

		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(employeeId);
		existingSalary.setYear(year);
		existingSalary.setMonth(month);
		existingSalary.setMainSalary(5000.0f);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.updateBaseSalary(employeeId, year, month, 6000.0f);

		assertNotNull(result);
		assertEquals(6000.0f, result.getMainSalary());
	}

	@Test
	void testLockSalary() {
		Integer employeeId = 1;
		Integer year = 2024;
		Integer month = 12;

		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(employeeId);
		existingSalary.setYear(year);
		existingSalary.setMonth(month);
		existingSalary.setSalaryLocked(false);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.lockSalary(employeeId, year, month);

		assertNotNull(result);
		assertTrue(result.getSalaryLocked());
	}

	@Test
	void testCreateEmployeeSalary() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(10000.0f);

		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.createEmployeeSalary(employee);

		assertNotNull(result);
		assertEquals(1, result.getEmployee());
		assertEquals(10000.0f, result.getMainSalary());
		assertEquals(10000.0f, result.getCalculatedSalary());
	}

	@Test
	void testCalculateAttendanceDays() {
		Integer employeeId = 1;
		Integer year = 2024;
		Integer month = 12;

		List<ShiftTimeAttendance> attendances = Collections.nCopies(10, new ShiftTimeAttendance());

		when(shiftTimeAttendanceRepository.findAll(any(Specification.class))).thenReturn(attendances);

		Employee employee = new Employee();
		employee.setEmployeeId(employeeId);
		employee.setSalary(1000.0f);
		employee.setSalaryCycle("DAY");

		when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(employeeId), any(LocalDate.class)))
				.thenReturn(0.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(employeeId, year, month);

		assertNotNull(result);
		assertEquals(10000.0f, result.getCalculatedSalary());
	}

	@Test
	void testGetTotalActivityTime_NullOrEmpty() {
		Employee employee = new Employee();
		employee.setEmployeeId(1);
		employee.setSalary(50.0f);
		employee.setSalaryCycle("HOUR");

		when(employeeRepo.findById(1)).thenReturn(Optional.of(employee));
		lenient().when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.empty());
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(1, 2024, 12)).thenReturn(null);

		when(shiftTimeAttendanceService.calculateTotalIncentiveSales(eq(1), any(LocalDate.class))).thenReturn(0.0f);

		EmployeeSalary result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);
		assertEquals(0.0f, result.getCalculatedSalary());

		when(shiftTimeAttendanceRepository.findTotalActivityTimeByEmployeeAndMonth(1, 2024, 12)).thenReturn("");

		result = employeeSalaryService.calculateEmployeeSalary(1, 2024, 12);
		assertEquals(0.0f, result.getCalculatedSalary());
	}

	@Test
	void testAddSalaryIncentive() {
		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setIncentive(500.0f);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.addSalaryIncentive(1, 2024, 12, 300.0f, "reward");

		assertEquals(800.0f, result.getIncentive());
	}

	@Test
	void testAddSalaryReward() {
		EmployeeSalary existingSalary = new EmployeeSalary();
		existingSalary.setEmployeeId(1);
		existingSalary.setYear(2024);
		existingSalary.setMonth(12);
		existingSalary.setReward(200.0f);

		when(employeeSalaryRepo.findOne(any(Specification.class))).thenReturn(Optional.of(existingSalary));
		when(employeeSalaryRepo.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeSalary result = employeeSalaryService.addSalaryReward(1, 2024, 12, 500.0f, "reward");

		assertEquals(700.0f, result.getReward());
		assertEquals("reward", result.getRewardReason());
	}
}