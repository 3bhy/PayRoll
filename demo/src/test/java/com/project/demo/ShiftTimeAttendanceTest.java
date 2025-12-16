package com.project.demo;

import com.project.demo.entity.*;
import com.project.demo.repo.*;
import com.project.demo.service.LoginService;
import com.project.demo.service.shiftTimeAttendanceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Time;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShiftTimeAttendanceTest {

	@Mock
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;

	@Mock
	private ShiftTimeRepo shiftRepository;

	@Mock
	private LoginService loginService;

	@Mock
	private EmployeeRepo employeeRepository;

	@Mock
	private SalesRepo salesRepository;

	@Mock
	private LoginRepo loginRepo;

	@InjectMocks
	private shiftTimeAttendanceService service;

	private Employee testEmployee;
	private Login testLogin;
	private ShiftTimeAttendance testAttendance;
	private ShiftTime testShiftTime;
	private LocalDateTime testDateTime;

	@BeforeEach
	void setUp() {
		testEmployee = new Employee();
		testEmployee.setEmployeeId(1);
		testEmployee.setSalesIncentivePercent(10.0f);
		testEmployee.setIncentiveOnAllSales(true);

		testDateTime = LocalDateTime.of(2024, 1, 15, 9, 0);

		testLogin = new Login();
		testLogin.setLoginId(100);
		testLogin.setEmployee(testEmployee);
		testLogin.setLoginDateTime(testDateTime);
		testLogin.setActivityTime(Time.valueOf("08:00:00"));

		testAttendance = new ShiftTimeAttendance();
		testAttendance.setShiftTimeAttendanceId(1);
		testAttendance.setEmployee(testEmployee);
		testAttendance.setAttendanceDate(testDateTime.toLocalDate());
		testAttendance.setTotalActiveTime(LocalTime.of(8, 0, 0));

		testShiftTime = new ShiftTime();
		testShiftTime.setShiftTimeId(1);
		testShiftTime.setFromTime(LocalTime.of(9, 0));
		testShiftTime.setToTime(LocalTime.of(17, 0));
		testShiftTime.setTotalTime(LocalTime.of(8, 0));
	}

	@Test
	void testUpdateDateAttendance_WithNewAttendance() {
		when(shiftTimeAttendanceRepository.findOneByEmployeeAndDate(1, testDateTime.toLocalDate()))
				.thenReturn(Optional.empty());
		when(shiftTimeAttendanceRepository.save(any(ShiftTimeAttendance.class))).thenReturn(testAttendance); // أول save
		when(loginRepo.findAllByEmployeeAndDate(1, testDateTime.toLocalDate()))
				.thenReturn(Collections.singletonList(testLogin));

		when(shiftRepository.findByEmployeeId(1)).thenReturn(Collections.singletonList(testShiftTime));

		service.updateDateAttendance(testLogin);

		verify(shiftTimeAttendanceRepository, times(2)).save(any(ShiftTimeAttendance.class));
		verify(loginRepo, times(1)).save(testLogin);
	}

	@Test
	void testUpdateDateAttendance_WithExistingAttendance() {
		testLogin.setShiftTimeAttendanceId(testAttendance);

		when(shiftTimeAttendanceRepository.findOneByEmployeeAndDate(1, testDateTime.toLocalDate()))
				.thenReturn(Optional.of(testAttendance));
		when(loginRepo.findAllByEmployeeAndDate(1, testDateTime.toLocalDate()))
				.thenReturn(Collections.singletonList(testLogin));

		when(shiftRepository.findByEmployeeId(1)).thenReturn(Collections.singletonList(testShiftTime));

		service.updateDateAttendance(testLogin);

		verify(shiftTimeAttendanceRepository, never()).save(argThat(att -> att.getShiftTimeAttendanceId() == null
				|| att.getShiftTimeAttendanceId() != testAttendance.getShiftTimeAttendanceId()));

		verify(shiftTimeAttendanceRepository, times(1)).save(testAttendance);
		verify(loginRepo, never()).save(any(Login.class));
	}

	@Test
	void testCalculateAndSetAttendanceData_WithEmptyLogins() {
		ShiftTimeAttendance attendance = new ShiftTimeAttendance();
		attendance.setAttendanceDate(LocalDate.now());
		attendance.setEmployee(testEmployee);
		when(loginRepo.findAllByEmployeeAndDate(anyInt(), any(LocalDate.class))).thenReturn(Collections.emptyList());

		service.updateDateAttendance(attendance);

		assertNotNull(attendance.getTotalActiveTime());
		assertEquals(Time.valueOf("00:00:00"), attendance.getTotalActiveTime());
		assertNull(attendance.getLessTime());
		assertNull(attendance.getOverTime());
	}

	@Test
	void testCalculateAndSetAttendanceData_WithLoginButNoShift() {
		List<Login> logins = Collections.singletonList(testLogin);
		when(shiftRepository.findByEmployeeId(1)).thenReturn(Collections.emptyList());

		testAttendance.setTotalActiveTime(null);
		when(loginRepo.findAllByEmployeeAndDate(1, testDateTime.toLocalDate())).thenReturn(logins);
		when(shiftTimeAttendanceRepository.findOneByEmployeeAndDate(1, testDateTime.toLocalDate()))
				.thenReturn(Optional.of(testAttendance));

		service.updateDateAttendance(testLogin);

		assertEquals(Time.valueOf("08:00:00"), testAttendance.getTotalActiveTime());
		assertNull(testAttendance.getLessTime());
		assertNull(testAttendance.getOverTime());
	}

	@Test
	void testFindNearestShiftTimeForEmployee() {
		List<ShiftTime> shifts = new ArrayList<>();

		ShiftTime shift1 = new ShiftTime();
		shift1.setShiftTimeId(1);
		shift1.setFromTime(LocalTime.of(8, 0));
		shift1.setToTime(LocalTime.of(16, 0));
		shifts.add(shift1);

		ShiftTime shift2 = new ShiftTime();
		shift2.setShiftTimeId(2);
		shift2.setFromTime(LocalTime.of(9, 0));
		shift2.setToTime(LocalTime.of(17, 0));
		shifts.add(shift2);

		when(shiftRepository.findByEmployeeId(1)).thenReturn(shifts);

		ShiftTime result = service.findNearestShiftTimeForEmployee(1, LocalDateTime.of(2024, 1, 15, 8, 30));

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeId());
	}

	@Test
	void testFindNearestShiftTimeForEmployee_NoShifts() {
		when(shiftRepository.findByEmployeeId(1)).thenReturn(Collections.emptyList());

		ShiftTime result = service.findNearestShiftTimeForEmployee(1, LocalDateTime.now());

		assertNull(result);
	}

	@Test
	void testCalculateTotalIncentiveSales_IncentiveOnAllSales() {
		List<ShiftTime> shifts = Collections.singletonList(testShiftTime);

		when(employeeRepository.findById(1)).thenReturn(Optional.of(testEmployee));
		when(shiftRepository.findShiftsByEmployeeIdAndDate(1, LocalDate.now().getDayOfWeek().getValue()))
				.thenReturn(shifts);
		when(salesRepository.calculateAllSalesDuringShiftTime(any(), any())).thenReturn(1000.0f);

		Float result = invokePrivateMethod("calculateTotalIncentiveSales",
				new Class[] { Integer.class, LocalDate.class }, new Object[] { 1, LocalDate.now() });

		assertNotNull(result);
		assertEquals(100.0f, result, 0.01);
	}

	@Test
	void testGetShiftTimeForEmployee_ShiftToday() {
		LocalDate today = LocalDate.now();
		when(shiftRepository.findByEmployeeIdAndDateNative(1, today)).thenReturn(Optional.of(testShiftTime));

		ShiftTime result = service.getShiftTimeForEmployee(1, today);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeId());
	}

	@Test
	void testGetShiftTimeForEmployee_NoShiftToday_UseEmployeeShift() {
		LocalDate today = LocalDate.now();
		when(shiftRepository.findByEmployeeIdAndDateNative(1, today)).thenReturn(Optional.empty());
		when(shiftRepository.findByEmployeeIdNative(1)).thenReturn(Collections.singletonList(testShiftTime));

		ShiftTime result = service.getShiftTimeForEmployee(1, today);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeId());
	}

	@Test
	void testGetShiftTimeForEmployee_UseDefaultShift() {
		LocalDate today = LocalDate.now();
		when(shiftRepository.findByEmployeeIdAndDateNative(1, today)).thenReturn(Optional.empty());
		when(shiftRepository.findByEmployeeIdNative(1)).thenReturn(Collections.emptyList());
		when(shiftRepository.findAnyShiftTime()).thenReturn(Optional.of(testShiftTime));

		ShiftTime result = service.getShiftTimeForEmployee(1, today);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeId());
	}

	@Test
	void testGetShiftTimeAttendance_Exists() {
		when(shiftTimeAttendanceRepository.findById(1)).thenReturn(Optional.of(testAttendance));

		ShiftTimeAttendance result = service.getShiftTimeAttendance(1);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeAttendanceId());
	}

	@Test
	void testGetShiftTimeAttendance_NotExists() {
		when(shiftTimeAttendanceRepository.findById(999)).thenReturn(Optional.empty());

		ShiftTimeAttendance result = service.getShiftTimeAttendance(999);

		assertNull(result);
	}

	@Test
	void testGetShiftTimeAttendance_NullId() {
		ShiftTimeAttendance result = service.getShiftTimeAttendance(null);

		assertNull(result);
	}

	private Float invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object[] args) {
		try {
			java.lang.reflect.Method method = shiftTimeAttendanceService.class.getDeclaredMethod(methodName,
					parameterTypes);
			method.setAccessible(true);
			return (Float) method.invoke(service, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}