package com.project.demo;

import com.project.demo.entity.*;
import com.project.demo.repo.*;
import com.project.demo.service.EmployeeSalaryService;
import com.project.demo.service.LoginService;
import com.project.demo.service.shiftTimeAttendanceService;
import com.project.demo.scheduler.SalaryCalculationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

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
    private ShiftTimeRepo shiftTimeRepo;

    @Mock
    private LoginService loginService;

    @Mock
    private EmployeeRepo employeeRepository;

    @Mock
    private EmployeeSalaryService employeeSalaryService;

    @Mock
    private SalesRepo salesRepository;

    @Mock
    private LoginRepo loginRepo;

    @Mock
    private SalaryCalculationService salaryCalculationService;

    @Mock
    private ShiftTimeRepo shiftRepository;

    private shiftTimeAttendanceService service;

    private Employee testEmployee;
    private Login testLogin;
    private ShiftTimeAttendance testAttendance;
    private ShiftTime testShiftTime;
    private LocalDateTime testDateTime;
    private ShiftTime dummyShiftTime;

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
        testShiftTime.setDayIndex(testDateTime.getDayOfWeek().getValue());

        dummyShiftTime = new ShiftTime();
        dummyShiftTime.setShiftTimeId(-1);
        dummyShiftTime.setFromTime(LocalTime.of(0, 0));
        dummyShiftTime.setToTime(LocalTime.of(23, 59, 59));
        dummyShiftTime.setTotalTime(LocalTime.of(23, 59, 59));

        // إنشاء service باستخدام constructor
        service = new shiftTimeAttendanceService(salaryCalculationService);
        
        // تعيين حقول service باستخدام reflection
        setField(service, "shiftTimeAttendanceRepository", shiftTimeAttendanceRepository);
        setField(service, "shiftRepository", shiftRepository);
        setField(service, "loginService", loginService);
        setField(service, "shiftTimeRepo", shiftTimeRepo);
        setField(service, "employeeRepository", employeeRepository);
        setField(service, "employeeSalaryService", employeeSalaryService);
        setField(service, "salesRepository", salesRepository);
        setField(service, "loginRepo", loginRepo);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    @Test
    void testUpdateDateAttendance_WithNewAttendance() {
        when(shiftTimeAttendanceRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());
        when(shiftTimeAttendanceRepository.save(any(ShiftTimeAttendance.class))).thenReturn(testAttendance);
        when(loginRepo.findAll(any(Specification.class))).thenReturn(Collections.singletonList(testLogin));

        service.updateDateAttendance(testLogin);

        verify(shiftTimeAttendanceRepository, times(2)).save(any(ShiftTimeAttendance.class));
        verify(loginRepo, times(1)).save(testLogin);
    }

    @Test
    void testUpdateDateAttendance_WithExistingAttendance() {
        testLogin.setShiftTimeAttendanceId(testAttendance);

        when(shiftTimeAttendanceRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(testAttendance));

        service.updateDateAttendance(testLogin);

        verify(shiftTimeAttendanceRepository, times(1)).save(testAttendance);
        verify(loginRepo, never()).save(any(Login.class));
    }

    @Test
    void testCalculateAndSetAttendanceData_WithEmptyLogins() {
        ShiftTimeAttendance attendance = new ShiftTimeAttendance();
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setEmployee(testEmployee);
        when(loginRepo.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        service.updateDateAttendance(attendance);

        assertNotNull(attendance.getTotalActiveTime());
        assertEquals(LocalTime.of(0,0,0), attendance.getTotalActiveTime());
        assertNull(attendance.getLessTime());
        assertNull(attendance.getOverTime());
    }

    @Test
    void testCalculateAndSetAttendanceData_WithLoginButNoShift() {
        List<Login> logins = Collections.singletonList(testLogin);
        when(loginRepo.findAll(any(Specification.class))).thenReturn(logins);

        testAttendance.setTotalActiveTime(null);

        service.updateDateAttendance(testAttendance);

        assertEquals(LocalTime.of(8,0,0), testAttendance.getTotalActiveTime());
        assertNull(testAttendance.getLessTime());
        assertNull(testAttendance.getOverTime());
    }

    @Test
    void testFindNearestShiftTimeForEmployee() {
        when(shiftTimeRepo.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(testShiftTime));

        ShiftTime result = service.findNearestShiftTimeForEmployee(1, testDateTime);

        assertNotNull(result);
        assertEquals(1, result.getShiftTimeId());
    }

    @Test
    void testFindNearestShiftTimeForEmployee_NoShifts() {
        when(shiftTimeRepo.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        ShiftTime result = service.findNearestShiftTimeForEmployee(1, testDateTime);

        assertNull(result);
    }

    @Test
    void testGetShiftTimeForEmployee_ShiftToday() {
        when(shiftTimeRepo.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(testShiftTime));

        ShiftTime result = service.getShiftTimeForEmployee(1, LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.getShiftTimeId());
    }

    @Test
    void testGetShiftTimeForEmployee_NoShiftToday_UseEmployeeShift_WithSpec() {
        when(shiftTimeRepo.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(testShiftTime));

        ShiftTime result = service.getShiftTimeForEmployee(1, LocalDate.now());

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
}