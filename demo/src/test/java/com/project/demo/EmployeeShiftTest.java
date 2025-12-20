package com.project.demo;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeShift;
import com.project.demo.entity.Shift;
import com.project.demo.model.EmployeeShiftModel;
import com.project.demo.repo.EmployeeShiftRepo;
import com.project.demo.service.EmployeeShiftService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeShiftTest {

	@Mock
	private EmployeeShiftRepo employeeShiftRepository;

	@InjectMocks
	private EmployeeShiftService employeeShiftService;

	private EmployeeShift employeeShift;
	private EmployeeShiftModel employeeShiftModel;
	private Employee employee;
	private Shift shift;
	private LocalDate today;
	private LocalDate tomorrow;
	private LocalDate yesterday;
	private LocalDate nextWeek;

	@BeforeEach
	void setUp() {
		today = LocalDate.now();
		tomorrow = LocalDate.now().plusDays(1);
		yesterday = LocalDate.now().minusDays(1);
		nextWeek = LocalDate.now().plusDays(7);

		employeeShiftModel = new EmployeeShiftModel();

		employee = new Employee();
		employee.setEmployeeId(1);

		shift = new Shift();
		shift.setShiftId(1);

		employeeShift = new EmployeeShift();
		employeeShift.setEmployeeShiftId(1);
		employeeShift.setEmployee(employee);
		employeeShift.setShift(shift);
		employeeShift.setActive(true);
		employeeShift.setStartActiveDate(today);
		employeeShift.setEndActiveDate(nextWeek);

		employeeShiftModel.setEmployeeId(1);
		employeeShiftModel.setShiftId(1);
		employeeShiftModel.setActive(true);
		employeeShiftModel.setStartActiveDate(today);
		employeeShiftModel.setEndActiveDate(tomorrow);
	}

	// ==================== swapShift Tests ====================

	@Test
	void testSwapShift_NewShiftStartsBeforeToday_TemporaryFalse() {
		employeeShiftModel.setStartActiveDate(yesterday);
		employeeShiftModel.setEndActiveDate(tomorrow);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class))).thenAnswer(invocation -> {
			EmployeeShift saved = invocation.getArgument(0);
			if (saved.getEmployeeShiftId() == null) {
				saved.setEmployeeShiftId(100);
			}
			return saved;
		});

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsBeforeToday_TemporaryTrue() {
		employeeShiftModel.setStartActiveDate(yesterday);
		employeeShiftModel.setEndActiveDate(today);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, true);

		assertNotNull(result);

		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsBeforeCurrentEnd_TemporaryFalse() {
		employeeShiftModel.setStartActiveDate(tomorrow);
		employeeShiftModel.setEndActiveDate(today.plusDays(3));

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsBeforeCurrentEnd_TemporaryTrue() {
		employeeShift.setEndActiveDate(today.plusDays(10));
		employeeShiftModel.setStartActiveDate(tomorrow);
		employeeShiftModel.setEndActiveDate(today.plusDays(3));

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, true);

		assertNotNull(result);
		verify(employeeShiftRepository, times(3)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsAfterCurrentEnd_TemporaryFalse() {
		employeeShiftModel.setStartActiveDate(nextWeek.plusDays(1));
		employeeShiftModel.setEndActiveDate(nextWeek.plusDays(7));

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(1)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsAfterCurrentEnd_TemporaryTrue() {
		employeeShift.setEndActiveDate(nextWeek.plusDays(10));
		employeeShiftModel.setStartActiveDate(nextWeek.plusDays(1));
		employeeShiftModel.setEndActiveDate(nextWeek.plusDays(3));

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, true);

		assertNotNull(result);
		verify(employeeShiftRepository, times(3)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_EndDateBeforeToday_ShouldThrowException() {
		employeeShiftModel.setEndActiveDate(yesterday);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> employeeShiftService.swapShift(employeeShiftModel, false));

		assertEquals("New shift end date cannot be before today", exception.getMessage());
		verify(employeeShiftRepository, never()).findEmployeeShift(anyInt(), anyInt());
	}

	@Test
	void testSwapShift_ShiftNotFound_ShouldThrowException() {
		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.empty());

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> employeeShiftService.swapShift(employeeShiftModel, false));

		assertEquals("Selected shift not found", exception.getMessage());
		verify(employeeShiftRepository).findEmployeeShift(1, 1);
	}

	@Test
	void testSwapShift_ShiftDoesNotBelongToEmployee_ShouldThrowException() {
		Employee differentEmployee = new Employee();
		differentEmployee.setEmployeeId(999);

		EmployeeShift differentShift = new EmployeeShift();
		differentShift.setEmployee(differentEmployee);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(differentShift));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> employeeShiftService.swapShift(employeeShiftModel, false));

		assertEquals("Selected shift does not belong to this employee", exception.getMessage());
	}

	@Test
	void testSwapShift_NewShiftStartsToday() {
		employeeShiftModel.setStartActiveDate(today);
		employeeShiftModel.setEndActiveDate(tomorrow);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_NewShiftStartsAndEndsSameDay() {
		employeeShiftModel.setStartActiveDate(today);
		employeeShiftModel.setEndActiveDate(today);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	@Test
	void testSwapShift_TemporaryNull_ShouldThrowException() {
		employeeShiftModel.setStartActiveDate(tomorrow);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));

		assertThrows(NullPointerException.class, () -> employeeShiftService.swapShift(employeeShiftModel, null));
	}

	@Test
	void testSwapShift_TemporaryFalse() {
		employeeShiftModel.setStartActiveDate(tomorrow);

		when(employeeShiftRepository.findEmployeeShift(1, 1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeShift result = employeeShiftService.swapShift(employeeShiftModel, false);

		assertNotNull(result);
		verify(employeeShiftRepository, times(2)).save(any(EmployeeShift.class));
	}

	// ==================== createShift Tests ====================

	@Test
	void testCreateShift() {
		EmployeeShiftModel testModel = new EmployeeShiftModel();
		testModel.setEmployeeId(1);
		testModel.setShiftId(1);
		testModel.setActive(true);
		testModel.setStartActiveDate(today);
		testModel.setEndActiveDate(tomorrow);

		when(employeeShiftRepository.save(any(EmployeeShift.class))).thenReturn(employeeShift);

		EmployeeShift result = employeeShiftService.createShift(testModel);

		assertNotNull(result);
		assertEquals(1, result.getEmployeeShiftId());
		verify(employeeShiftRepository).save(any(EmployeeShift.class));
	}

	// ==================== getShiftsByIdAndFilters Tests ====================

	@Test
	void testGetShiftsByIdAndFilters_AllFilters() {
		List<EmployeeShift> expected = Arrays.asList(employeeShift);
		when(employeeShiftRepository.findShiftsByFilters(1, true, 1, Date.valueOf(today), Date.valueOf(tomorrow)))
				.thenReturn(expected);

		List<EmployeeShift> result = employeeShiftService.getShiftsByIdAndFilters(1, true, Date.valueOf(today),
				Date.valueOf(tomorrow), 1);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(employeeShiftRepository).findShiftsByFilters(1, true, 1, Date.valueOf(today), Date.valueOf(tomorrow));
	}

	// ==================== getShiftsById Tests ====================

	@Test
	void testGetShiftsById() {
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));

		EmployeeShift result = employeeShiftService.getShiftsById(1);

		assertNotNull(result);
		assertEquals(1, result.getEmployeeShiftId());
		verify(employeeShiftRepository).findById(1);
	}

	// ==================== updateEmployeeShift Tests ====================

	@Test
	void testUpdateEmployeeShift() {
		EmployeeShift updateDetails = new EmployeeShift();
		updateDetails.setActive(false);
		updateDetails.setStartActiveDate(tomorrow);
		updateDetails.setEndActiveDate(LocalDate.now().plusDays(2));

		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		when(employeeShiftRepository.save(any(EmployeeShift.class))).thenReturn(updateDetails);

		EmployeeShift result = employeeShiftService.updateEmployeeShift(1, updateDetails);

		assertNotNull(result);
		assertFalse(result.getActive());
		verify(employeeShiftRepository).findById(1);
		verify(employeeShiftRepository).save(any(EmployeeShift.class));
	}

	// ==================== deleteShifts Tests ====================

	@Test
	void testDeleteShifts() {
		when(employeeShiftRepository.findById(1)).thenReturn(Optional.of(employeeShift));
		doNothing().when(employeeShiftRepository).delete(employeeShift);

		employeeShiftService.deleteShifts(1);

		verify(employeeShiftRepository).findById(1);
		verify(employeeShiftRepository).delete(employeeShift);
	}

	// ==================== getEmployeeShiftIds Tests ====================

	@Test
	void testGetEmployeeShiftIds() {
		List<EmployeeShift> expected = Arrays.asList(employeeShift);
		when(employeeShiftRepository.findActiveShiftsByEmployeeId(1)).thenReturn(expected);

		List<EmployeeShift> result = employeeShiftService.getEmployeeShiftIds(1);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(employeeShiftRepository).findActiveShiftsByEmployeeId(1);
	}

	// ==================== getActiveShifts Tests ====================

	@Test
	void testGetActiveShifts() {
		List<Integer> expected = Arrays.asList(1, 2, 3);
		when(employeeShiftRepository.findActiveShiftIdsByEmployeeId(1)).thenReturn(expected);

		List<Integer> result = employeeShiftService.getActiveShifts(1);

		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals(expected, result);
		verify(employeeShiftRepository).findActiveShiftIdsByEmployeeId(1);
	}

	@Test
	void testGetActiveShifts_NoActiveShifts() {
		when(employeeShiftRepository.findActiveShiftIdsByEmployeeId(999)).thenReturn(Collections.emptyList());

		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
				() -> employeeShiftService.getActiveShifts(999));

		assertEquals("No active shifts found for employee with id: 999", exception.getMessage());
		verify(employeeShiftRepository).findActiveShiftIdsByEmployeeId(999);
	}
}