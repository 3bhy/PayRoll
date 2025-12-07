package com.project.demo;

import com.project.demo.entity.*;
import com.project.demo.model.LoginModel;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.LoginRepo;
import com.project.demo.repo.ShiftTimeRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.service.*;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Time;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginTest {

	@Mock
	private LoginRepo loginRepository;

	@Mock
	private EmployeeService employeeService;

	@Mock
	private EmployeeRepo employeeRepository;

	@Mock
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;

	@Mock
	private shiftTimeAttendanceService shiftTimeAttendanceService;

	@Mock
	private ShiftTimeRepo shiftTimeRepo;

	@Mock
	private EmployeeSalaryService employeeSalaryService;

	@InjectMocks
	private LoginService loginService;

	private Login login;
	private LoginModel loginModel;
	private Employee employee;
	private ShiftTimeAttendance shiftTimeAttendance;
	private ShiftTime shiftTime;
	private LocalDateTime now;
	private LocalDateTime loginTime;
	private LocalDateTime logoutTime;

	@BeforeEach
	void setUp() {
		// Initialize time objects
		now = LocalDateTime.now();
		loginTime = now.minusHours(2);
		logoutTime = now.minusHours(1);

		// Initialize entities
		employee = new Employee();
		employee.setEmployeeId(1);

		shiftTimeAttendance = new ShiftTimeAttendance();
		shiftTimeAttendance.setShiftTimeAttendanceId(1);

		shiftTime = new ShiftTime();
		shiftTime.setShiftTimeId(1);
		shiftTime.setFromTime(Time.valueOf("09:00:00"));
		shiftTime.setToTime(Time.valueOf("17:00:00"));
		shiftTime.setTotalTime(Time.valueOf("08:00:00"));

		Shift shift = new Shift();
		shift.setShiftId(1);
		shift.setShiftName("Morning Shift");
		shiftTime.setShiftId(shift);

		login = new Login();
		login.setLoginId(1);
		login.setEmployee(employee);
		login.setShiftTimeAttendanceId(shiftTimeAttendance);
		login.setShiftTimeId(shiftTime);
		login.setLoginDateTime(loginTime);
		login.setLogoutDateTime(logoutTime);
		login.setLogoutStatus(true);
		login.setLocked(false);
		login.setActivityTime(Time.valueOf("01:00:00"));

		loginModel = new LoginModel();
		loginModel.setEmployeeId(1);
		loginModel.setShiftTimeAttendanceId(1);
	}

	// ==================== createLoginIfWasActiveLogin Tests ====================

	@Test
	void testCreateLoginIfWasActiveLogin() {
		when(loginRepository.findActiveLogins(1)).thenReturn(Collections.emptyList());
		when(employeeService.getEmployeeById(1)).thenReturn(employee);
		when(shiftTimeAttendanceService.getshittimeattendance(1)).thenReturn(shiftTimeAttendance);
		when(shiftTimeRepo.findByEmployeeIdNative(1)).thenReturn(Arrays.asList(shiftTime));
		when(loginRepository.save(any(Login.class))).thenReturn(login);

		Login result = loginService.createLoginIfWasActiveLogin(loginModel);

		assertNotNull(result);
		verify(loginRepository).save(any(Login.class));
	}

	@Test
	void testCreateLoginIfWasActiveLogin_NullEmployeeId() {
		loginModel.setEmployeeId(null);

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> loginService.createLoginIfWasActiveLogin(loginModel));

		assertEquals("Employee ID cannot be null while creating a login.", exception.getMessage());
		verify(loginRepository, never()).save(any(Login.class));
	}

	@Test
	void testCreateLoginIfWasActiveLogin_WithNullShiftTimeAttendanceId() {
		loginModel.setShiftTimeAttendanceId(null);

		when(loginRepository.findActiveLogins(1)).thenReturn(Collections.emptyList());
		when(employeeService.getEmployeeById(1)).thenReturn(employee);
		when(shiftTimeAttendanceService.getshittimeattendance(null))
				.thenThrow(new RuntimeException("Shift time attendance ID cannot be null"));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> loginService.createLoginIfWasActiveLogin(loginModel));

		assertTrue(exception.getMessage().contains("cannot be null"));
		verify(loginRepository, never()).save(any(Login.class));

	}

	// ==================== lockLogin Tests ====================

	@Test
	void testLockLogin() {
		Login activeLogin = new Login();
		activeLogin.setLoginId(1);
		activeLogin.setLoginDateTime(loginTime);
		activeLogin.setLogoutDateTime(logoutTime);

		List<Login> activeLogins = Arrays.asList(activeLogin);

		when(loginRepository.save(any(Login.class))).thenReturn(activeLogin);

		List<Login> result = loginService.lockLogin(1, activeLogins);

		assertNotNull(result);
		assertTrue(activeLogin.getLocked());
		verify(loginRepository).save(activeLogin);
	}

	@Test
	void testLockLogin_EmptyList() {
		List<Login> result = loginService.lockLogin(1, Collections.emptyList());

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(loginRepository, never()).save(any(Login.class));
	}

	@Test
	void testLockLogin_WithNullLogoutDateTime() {
		Login activeLogin = new Login();
		activeLogin.setLoginId(1);
		activeLogin.setLoginDateTime(loginTime);
		activeLogin.setLogoutDateTime(null);

		List<Login> activeLogins = Arrays.asList(activeLogin);

		when(loginRepository.save(any(Login.class))).thenReturn(activeLogin);

		List<Login> result = loginService.lockLogin(1, activeLogins);

		assertNotNull(result);
		assertTrue(activeLogin.getLocked());
		verify(loginRepository).save(activeLogin);
	}

	// ==================== calculateAndSetActivityTime Tests ====================

	@Test
	void testCalculateAndSetActivityTime_ValidTimes() {
		Login testLogin = new Login();
		testLogin.setLoginDateTime(loginTime);
		testLogin.setLogoutDateTime(logoutTime);

		Time result = loginService.calculateAndSetActivityTime(testLogin);

		assertNotNull(result);
		assertEquals(Time.valueOf("01:00:00"), result);
		assertEquals(Time.valueOf("01:00:00"), testLogin.getActivityTime());
	}

	@Test
	void testCalculateAndSetActivityTime_NullLoginDateTime() {
		Login testLogin = new Login();
		testLogin.setLoginDateTime(null);
		testLogin.setLogoutDateTime(logoutTime);

		Time result = loginService.calculateAndSetActivityTime(testLogin);

		assertEquals(Time.valueOf("00:00:00"), result);
		assertEquals(Time.valueOf("00:00:00"), testLogin.getActivityTime());
	}

	@Test
	void testCalculateAndSetActivityTime_NullLogoutDateTime() {
		Login testLogin = new Login();
		testLogin.setLoginDateTime(loginTime);
		testLogin.setLogoutDateTime(null);

		Time result = loginService.calculateAndSetActivityTime(testLogin);

		assertEquals(Time.valueOf("00:00:00"), result);
		assertEquals(Time.valueOf("00:00:00"), testLogin.getActivityTime());
	}

	@Test
	void testCalculateAndSetActivityTime_BothNull() {
		Login testLogin = new Login();
		testLogin.setLoginDateTime(null);
		testLogin.setLogoutDateTime(null);

		Time result = loginService.calculateAndSetActivityTime(testLogin);

		assertEquals(Time.valueOf("00:00:00"), result);
		assertEquals(Time.valueOf("00:00:00"), testLogin.getActivityTime());
	}

	@Test
	void testCalculateAndSetActivityTime_LogoutBeforeLogin() {
		Login testLogin = new Login();
		testLogin.setLoginDateTime(logoutTime);
		testLogin.setLogoutDateTime(loginTime);

		Time result = loginService.calculateAndSetActivityTime(testLogin);

		assertNotNull(result);
	}

	// ==================== lockLoginByEmployeeId Tests ====================

	@Test
	void testLockLoginByEmployeeId() {
		Login activeLogin = new Login();
		activeLogin.setLoginId(1);
		activeLogin.setLocked(false);

		when(loginRepository.findActiveLogins(1)).thenReturn(Arrays.asList(activeLogin));
		when(loginRepository.save(any(Login.class))).thenReturn(activeLogin);

		loginService.lockLoginByEmployeeId(1);

		assertTrue(activeLogin.getLocked());
		verify(loginRepository).findActiveLogins(1);
		verify(loginRepository).save(activeLogin);
	}

	@Test
	void testLockLoginByEmployeeId_NoActiveLogins() {
		when(loginRepository.findActiveLogins(1)).thenReturn(Collections.emptyList());

		loginService.lockLoginByEmployeeId(1);

		verify(loginRepository).findActiveLogins(1);
		verify(loginRepository, never()).save(any(Login.class));
	}

	@Test
	void testLockLoginByEmployeeId_MultipleActiveLogins() {
		Login login1 = new Login();
		login1.setLoginId(1);
		login1.setLocked(false);

		Login login2 = new Login();
		login2.setLoginId(2);
		login2.setLocked(false);

		when(loginRepository.findActiveLogins(1)).thenReturn(Arrays.asList(login1, login2));
		when(loginRepository.save(any(Login.class))).thenAnswer(invocation -> invocation.getArgument(0));

		loginService.lockLoginByEmployeeId(1);

		assertTrue(login1.getLocked());
		assertTrue(login2.getLocked());
		verify(loginRepository, times(2)).save(any(Login.class));
	}

	// ==================== getLoginById Tests ====================

	@Test
	void testGetLoginById() {
		when(loginRepository.findById(1)).thenReturn(Optional.of(login));

		Optional<Login> result = loginService.getLoginById(1);

		assertTrue(result.isPresent());
		assertEquals(1, result.get().getLoginId());
		verify(loginRepository).findById(1);
	}

	@Test
	void testGetLoginById_NotFound() {
		when(loginRepository.findById(999)).thenReturn(Optional.empty());

		Optional<Login> result = loginService.getLoginById(999);

		assertFalse(result.isPresent());
		verify(loginRepository).findById(999);
	}

	@Test
	void testGetLoginById_NullId() {
		when(loginRepository.findById(null)).thenReturn(Optional.empty());

		Optional<Login> result = loginService.getLoginById(null);

		assertFalse(result.isPresent());
		verify(loginRepository).findById(null);
	}

	// ==================== deleteLogin Tests ====================

	@Test
	void testDeleteLogin() {
		when(loginRepository.findById(1)).thenReturn(Optional.of(login));
		doNothing().when(loginRepository).delete(login);

		loginService.deleteLogin(1);

		verify(loginRepository).findById(1);
		verify(loginRepository).delete(login);
	}

	@Test
	void testDeleteLogin_NotFound() {
		when(loginRepository.findById(999)).thenReturn(Optional.empty());

		loginService.deleteLogin(999);

		verify(loginRepository).findById(999);
		verify(loginRepository, never()).delete(any(Login.class));
	}

	// ==================== getLoginsByFilters Tests ====================

	@Test
	void testGetLoginsByFilters_EmployeeIdOnly() {
		when(loginRepository.findLoginsByFilters(eq(1), any(), any(), any(), any(), any(), any()))
				.thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(1, null, null, null, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(eq(1), any(), any(), any(), any(), any(), any());
	}

	@Test
	void testGetLoginsByFilters_LoginDateTimeOnly() {
		when(loginRepository.findLoginsByFilters(any(), eq(now.withNano(0)), isNull(), any(), any(), any(), any()))
				.thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(null, now, null, null, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(any(), eq(now.withNano(0)), isNull(), any(), any(), any(), any());
	}

	@Test
	void testGetLoginsByFilters_LogoutDateTimeOnly() {
		when(loginRepository.findLoginsByFilters(any(), any(), any(), isNull(), eq(now.withNano(0)), any(), any()))
				.thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(null, null, now, null, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(any(), any(), any(), isNull(), eq(now.withNano(0)), any(), any());
	}

	@Test
	void testGetLoginsByFilters_BothDates() {
		LocalDateTime start = now.minusHours(5);
		LocalDateTime end = now.minusHours(1);

		when(loginRepository.findLoginsByFilters(any(), eq(start.withNano(0)), eq(end.withNano(0)),
				eq(start.withNano(0)), eq(end.withNano(0)), any(), any())).thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(null, start, end, null, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(any(), eq(start.withNano(0)), eq(end.withNano(0)),
				eq(start.withNano(0)), eq(end.withNano(0)), any(), any());
	}

	@Test
	void testGetLoginsByFilters_LogoutStatusTrue() {
		when(loginRepository.findLoginsByFilters(any(), any(), any(), any(), any(), eq(true), any()))
				.thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(null, null, null, true, null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(any(), any(), any(), any(), any(), eq(true), any());
	}

	@Test
	void testGetLoginsByFilters_LockedFalse() {
		when(loginRepository.findLoginsByFilters(any(), any(), any(), any(), any(), any(), eq(false)))
				.thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(null, null, null, null, false);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(any(), any(), any(), any(), any(), any(), eq(false));
	}

	@Test
	void testGetLoginsByFilters_AllFilters() {
		when(loginRepository.findLoginsByFilters(eq(1), eq(now.withNano(0)), eq(now.withNano(0)), eq(now.withNano(0)),
				eq(now.withNano(0)), eq(true), eq(false))).thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getLoginsByFilters(1, now, now, true, false);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLoginsByFilters(eq(1), eq(now.withNano(0)), eq(now.withNano(0)),
				eq(now.withNano(0)), eq(now.withNano(0)), eq(true), eq(false));
	}

	@Test
	void testGetLoginsByFilters_NoResults() {
		when(loginRepository.findLoginsByFilters(any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(Collections.emptyList());

		List<Login> result = loginService.getLoginsByFilters(999, now, now, true, false);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(loginRepository).findLoginsByFilters(eq(999), any(), any(), any(), any(), any(), any());
	}

	// ==================== processLogout Tests ====================

	@Test
	void testProcessLogout_NoActiveLogin_CreatesNew() {
		when(loginRepository.findActiveLogins(1)).thenReturn(Collections.emptyList());
		when(employeeService.getEmployeeById(1)).thenReturn(employee);
		when(shiftTimeAttendanceService.getshittimeattendance(1)).thenReturn(shiftTimeAttendance);
		when(shiftTimeRepo.findByEmployeeIdNative(1)).thenReturn(Arrays.asList(shiftTime));
		when(loginRepository.save(any(Login.class))).thenReturn(login);

		Login result = loginService.processLogout(1, 1);

		assertNotNull(result);
		verify(loginRepository).save(any(Login.class));
	}

	// ==================== logoutByLoginId Tests ====================

	@Test
	void testLogoutByLoginId() {
		Login activeLogin = new Login();
		activeLogin.setLoginId(1);
		activeLogin.setEmployee(employee);
		activeLogin.setShiftTimeAttendanceId(shiftTimeAttendance);
		activeLogin.setLogoutStatus(false);

		when(loginRepository.findActiveLoginById(1)).thenReturn(Optional.of(activeLogin));
		when(loginRepository.findActiveLogins(1)).thenReturn(Arrays.asList(activeLogin));
		when(loginRepository.save(any(Login.class))).thenReturn(activeLogin);

		Login result = loginService.logoutByLoginId(1);

		assertNotNull(result);
		assertTrue(result.getLogoutStatus());
		verify(loginRepository).findActiveLoginById(1);
	}

	@Test
	void testLogoutByLoginId_NotFound() {
		when(loginRepository.findActiveLoginById(999)).thenReturn(Optional.empty());

		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
				() -> loginService.logoutByLoginId(999));

		assertEquals("Active login not found with id: 999", exception.getMessage());
		verify(loginRepository).findActiveLoginById(999);
	}

	// ==================== getOpenLogins Tests ====================

	@Test
	void testGetOpenLogins() {
		when(loginRepository.findLockedLoginsWithOpenLogout()).thenReturn(Arrays.asList(login));

		List<Login> result = loginService.getOpenLogins();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(loginRepository).findLockedLoginsWithOpenLogout();
	}

	@Test
	void testGetOpenLogins_Empty() {
		when(loginRepository.findLockedLoginsWithOpenLogout()).thenReturn(Collections.emptyList());

		List<Login> result = loginService.getOpenLogins();

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(loginRepository).findLockedLoginsWithOpenLogout();
	}

	// ==================== getCurrentShiftTimeForEmployee Tests
	// ====================

	@Test
	void testGetCurrentShiftTimeForEmployee_Found() {
		when(loginRepository.findCurrentShiftTimeForEmployee(1)).thenReturn(Optional.of(shiftTime));

		ShiftTime result = loginService.getCurrentShiftTimeForEmployee(1);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeId());
		verify(loginRepository).findCurrentShiftTimeForEmployee(1);
	}

	@Test
	public void testGetCurrentShiftTimeForEmployee_NotFound_ReturnsDummy() {
		Integer employeeId = 1;

		Shift dummyShift = new Shift();
		dummyShift.setShiftId(1);
		dummyShift.setShiftName("Default Shift");

		ShiftTime dummyShiftTime = new ShiftTime();
		dummyShiftTime.setShiftTimeId(-1);
		dummyShiftTime.setShiftId(dummyShift); // This was missing!

		when(loginRepository.findCurrentShiftTimeForEmployee(employeeId)).thenReturn(Optional.empty());

		ShiftTime result = loginService.getCurrentShiftTimeForEmployee(employeeId);

		assertNotNull(result);
	}
	// ==================== findActiveLoginWithinShift Tests ====================

	@Test
	void testFindActiveLoginWithinShift() {
		when(loginRepository.findActiveLoginWithinShift(1)).thenReturn(Optional.of(login));

		Optional<Login> result = loginService.findActiveLoginWithinShift(1);

		assertTrue(result.isPresent());
		assertEquals(1, result.get().getLoginId());
		verify(loginRepository).findActiveLoginWithinShift(1);
	}

	@Test
	void testFindActiveLoginWithinShift_NotFound() {
		when(loginRepository.findActiveLoginWithinShift(999)).thenReturn(Optional.empty());

		Optional<Login> result = loginService.findActiveLoginWithinShift(999);

		assertFalse(result.isPresent());
		verify(loginRepository).findActiveLoginWithinShift(999);
	}

	// ==================== getTodayAttendance Tests ====================

	@Test
	void testGetTodayAttendance_Found() {
		when(loginRepository.findTodayAttendanceByEmployee(1)).thenReturn(Optional.of(shiftTimeAttendance));

		ShiftTimeAttendance result = loginService.getTodayAttendance(1);

		assertNotNull(result);
		assertEquals(1, result.getShiftTimeAttendanceId());
		verify(loginRepository).findTodayAttendanceByEmployee(1);
	}

	@Test
	void testGetTodayAttendance_NotFound_CreatesNew() {
		when(loginRepository.findTodayAttendanceByEmployee(1)).thenReturn(Optional.empty());
		when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
		when(shiftTimeAttendanceRepository.save(any(ShiftTimeAttendance.class))).thenReturn(shiftTimeAttendance);

		ShiftTimeAttendance result = loginService.getTodayAttendance(1);

		assertNotNull(result);
		verify(loginRepository).findTodayAttendanceByEmployee(1);
		verify(employeeRepository).findById(1);
		verify(shiftTimeAttendanceRepository).save(any(ShiftTimeAttendance.class));
	}

	@Test
	void testGetTodayAttendance_EmployeeNotFound() {
		when(loginRepository.findTodayAttendanceByEmployee(999)).thenReturn(Optional.empty());
		when(employeeRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> loginService.getTodayAttendance(999));

		verify(loginRepository).findTodayAttendanceByEmployee(999);
		verify(employeeRepository).findById(999);
		verify(shiftTimeAttendanceRepository, never()).save(any(ShiftTimeAttendance.class));
	}

}