package com.project.demo.service;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.project.demo.entity.Company;
import com.project.demo.entity.Employee;
import com.project.demo.entity.Login;
import com.project.demo.entity.Shift;
import com.project.demo.entity.ShiftTime;
import com.project.demo.entity.ShiftTimeAttendance;
import com.project.demo.model.LoginModel;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.LoginRepo;
import com.project.demo.repo.ShiftTimeRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.specification.LoginSpec;
import com.project.demo.specification.ShiftTimeAttendanceSpec;
import com.project.demo.specification.ShiftTimeSpec;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class LoginService {

	@Autowired
	private LoginRepo loginRepository;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private EmployeeRepo employeeRepository;
	@Autowired
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;
	@Autowired
	private shiftTimeAttendanceService shiftTimeAttendanceService;
	@Autowired
	private ShiftTimeRepo shiftTimeRepo;
	@Autowired
	private EmployeeSalaryService EmployeeSalaryService;

	// Login Process if there active login close it and open new
	private Login processLogin(Integer employeeId, Integer shiftTimeAttendanceId) {
		List<Login> activeLogins = loginRepository.findAll(
			    LoginSpec.activeLoginsForEmployee(employeeId)
			);
		if (!activeLogins.isEmpty()) {
			lockLogin(employeeId, activeLogins);
		}
		return createNewLoginWithEmIdShId(employeeId, shiftTimeAttendanceId);
	}

	// CREATE
	public Login createLoginIfWasActiveLogin(LoginModel loginModel) {

		if (loginModel.getEmployeeId() == null) {
			throw new RuntimeException("Employee ID cannot be null while creating a login.");
		}

		return processLogin(loginModel.getEmployeeId(), loginModel.getShiftTimeAttendanceId());
	}

	// FIXME what do you do with the employeeId?
	// FIXME DONE lock the open logins with employeeid, needs to be reviewed
	// check if you need this check, and what if employee id is null, will this method work?

	public List<Login> lockLogin(Integer employeeId, List<Login> activeLogins) {
	    if (employeeId == null) {
	    	//XXX you already call the method with null at some places
	    	//FIXME-DONE
	        throw new IllegalArgumentException("Employee ID is required");
	    }
	    
	    if (activeLogins == null) {
	        return Collections.emptyList();
	    }
	    
	    List<Login> employeeLogins = activeLogins.stream()
	            .filter(login -> login != null && 
	                    login.getEmployee() != null && 
	                    employeeId.equals(login.getEmployee().getEmployee()))
	            .collect(Collectors.toList());
	    
	    if (employeeLogins.isEmpty()) {
	        return Collections.emptyList();
	    }
	    
	    List<Login> lockedLogins = new ArrayList<>();
	    
	    for (Login login : employeeLogins) {
	        try {
	            if (Boolean.TRUE.equals(login.getLocked())) {
	                lockedLogins.add(login);
	                continue;
	            }
	            
	            calculateAndSetActivityTime(login);
	            
	            login.setLocked(true);
	            loginRepository.save(login);
	            
	            lockedLogins.add(login);
	            
	            shiftTimeAttendanceService.updateDateAttendance(login);
	            
	        } catch (Exception e) {
	            System.err.println("Failed to lock login " + login.getLoginId() + 
	                             " for employee " + employeeId + ": " + e.getMessage());
	        }
	    }
	    
	    return lockedLogins;
	}

	// calculate active time
	public Time calculateAndSetActivityTime(Login login) {
		if (login.getLoginDateTime() != null && login.getLogoutDateTime() != null) {
			Duration duration = Duration.between(login.getLoginDateTime(), login.getLogoutDateTime());
			long seconds = duration.getSeconds();

			Time activityTime = Time
					.valueOf(String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60));
			login.setActivityTime(activityTime);
			return activityTime;
		}
		return Time.valueOf("00:00:00");
	}

	// Lock login by employeeId
	public void lockLoginByEmployeeId(Integer employeeId) {

		try {

			List<Login> activeLogins = loginRepository.findAll(
				    LoginSpec.activeLoginsForEmployee(employeeId)
				);

			if (activeLogins.isEmpty()) {

				return;
			}

			for (Login login : activeLogins) {
				login.setLocked(true);
				loginRepository.save(login);

			}

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Create new login record by employeeId and default values
	private Login createNewLoginWithEmIdShId(Integer employeeId, Integer shiftTimeAttendanceId) {
		Login login = new Login();

		// Set employee
		Employee employee = employeeService.getEmployeeById(employeeId);
		login.setEmployee(employee);

		LocalDateTime now = LocalDateTime.now();
		login.setLoginDateTime(now);
		login.setLogoutDateTime(now.plusMinutes(15));
		login.setLocked(false);
		login.setLogoutStatus(false);
		login.setActivityTime(Time.valueOf("00:15:00"));

		ShiftTime nearestShift = shiftTimeAttendanceService.findNearestShiftTimeForEmployee(employeeId, now);
		if (nearestShift != null) {
			login.setShiftTimeId(nearestShift);
			System.out.println(
					"Assigned nearest shift: " + nearestShift.getShiftTimeId() + " for employee: " + employeeId);
		} else {
			System.out.println("No shift found for employee: " + employeeId);
		}
		ShiftTimeAttendance attendance = shiftTimeAttendanceRepository.findAll(
		        ShiftTimeAttendanceSpec.todayByEmployee(employeeId)
		).stream().findFirst().orElseGet(() -> CreateNewAttendance(employeeId));


		    login.setShiftTimeAttendanceId(attendance);
		Login savedLogin = loginRepository.save(login);

		return savedLogin;
	}

	// get login by id
	public Optional<Login> getLoginById(Integer id) {
		return loginRepository.findById(id);
	}

	// Delete login row

	public void deleteLogin(Integer id) {
		getLoginById(id).ifPresent(loginRepository::delete);
	}

	public LoginModel convertToModel(Login entity) {
		return new LoginModel(entity.getLoginId(),
				entity.getEmployee() != null ? entity.getEmployee().getEmployee() : null,
				entity.getShiftTimeId() != null ? entity.getShiftTimeId().getShiftTimeId() : null,
				entity.getShiftTimeAttendanceId() != null ? entity.getShiftTimeAttendanceId().getShiftTimeAttendanceId()
						: null,
				entity.getLoginDateTime(), entity.getLogoutDateTime(), entity.getLogoutStatus(),
				entity.getActivityTime(), entity.getLocked());
	}
	// FILTER BY

	public List<Login> getLoginsByFilters(Integer employeeId, LocalDateTime loginDateTime, LocalDateTime logoutDateTime,
			Boolean logoutStatus, Boolean locked) {

		LocalDateTime loginStart = null;
		LocalDateTime loginEnd = null;
		LocalDateTime logoutStart = null;
		LocalDateTime logoutEnd = null;

//  loginDateTime
		if (loginDateTime != null) {
			loginStart = loginDateTime.withNano(0);
			loginEnd = null;
		}

//  logoutDateTime  
		if (logoutDateTime != null) {
			logoutStart = null;
			logoutEnd = logoutDateTime.withNano(0);
		}

		if (loginDateTime != null && logoutDateTime != null) {
			loginStart = loginDateTime.withNano(0);
			loginEnd = logoutDateTime.withNano(0);
			logoutStart = loginDateTime.withNano(0);
			logoutEnd = logoutDateTime.withNano(0);
		}

		Specification<Login> spec = LoginSpec.hasEmployee(employeeId)
		        .and(LoginSpec.loginAfter(loginStart))
		        .and(LoginSpec.loginBefore(loginEnd))
		        .and(LoginSpec.logoutAfter(logoutStart))
		        .and(LoginSpec.logoutBefore(logoutEnd))
		        .and(LoginSpec.hasLogoutStatus(logoutStatus))
		        .and(LoginSpec.isLocked(locked));

		return loginRepository.findAll(spec);

	}

	// Logout by employeeId

	public Login processLogout(Integer employeeId, Integer shiftTimeAttendanceId) {
		List<Login> activeLogins = loginRepository.findAll(
			    LoginSpec.activeLoginsForEmployee(employeeId)
			);

		if (activeLogins.isEmpty()) {
			return processLogin(employeeId, shiftTimeAttendanceId);
		}

		Login loginToLogout = activeLogins.get(0);
		loginToLogout.setLogoutStatus(true);
		loginToLogout.setLogoutDateTime(LocalDateTime.now());

		lockLogin(employeeId, activeLogins);
		YearMonth currentYearMonth = YearMonth.now();
		int currentYear = currentYearMonth.getYear();
		int currentMonth = currentYearMonth.getMonthValue();
		EmployeeSalaryService.calculateEmployeeSalary(employeeId, currentYear, currentMonth);
		shiftTimeAttendanceService.updateDateAttendance(loginToLogout);
		return loginRepository.save(loginToLogout);
	}

	// Logout employee by loginID
	public Login logoutByLoginId(Integer loginId) {
	    Login login = loginRepository.findAll(LoginSpec.activeLoginById(loginId))
	            .stream()
	            .findFirst()
	            .orElseThrow(() -> new EntityNotFoundException(
	                "Active login not found with id: " + loginId
	            ));

	    return processLogout(
	            login.getEmployee().getEmployee(),
	            login.getShiftTimeAttendanceId().getShiftTimeAttendanceId()
	    );
	}

	// getOpenLogins
	public List<Login> getOpenLogins() {
	    return loginRepository.findAll(LoginSpec.lockedWithOpenLogout());
	}


	// This is updates

	// Select shift_time of the employee where now is between its (from hour - 1)
	// and to hour and same day

	public ShiftTime getCurrentShiftTimeForEmployee(Integer employeeId) {
	    LocalDate today = LocalDate.now();
	    LocalTime now = LocalTime.now();

	    Specification<ShiftTime> spec = ShiftTimeSpec.currentShiftForEmployee(employeeId, today, now);

	    List<ShiftTime> shifts = shiftTimeRepo.findAll(spec);

	    return shifts.isEmpty() ? createDummyShiftTime() : shifts.get(0);
	}

	// DummyShift
	public ShiftTime createDummyShiftTime() {
		Shift dummyShift = new Shift();
		dummyShift.setShiftId(-1);

		dummyShift.setShiftName("UNKNOWN");

		Company dummyCompany = new Company();
		dummyCompany.setCompanyId(-1);

		ShiftTime dummyShiftTime = new ShiftTime();
		dummyShiftTime.setShiftTimeId(-1);

		Shift dummyShiftId = new Shift();
		dummyShiftId.setShiftId(-1);

		dummyShiftTime.setDayIndex(0);
		dummyShiftTime.setFromTime(LocalTime.of(0, 0, 0));
		dummyShiftTime.setToTime(LocalTime.of(23, 59, 59));
		dummyShiftTime.setTotalTime(LocalTime.of(23, 59, 59));
		return dummyShiftTime;
	}

//get active login and now is between totime and fromtime
	public Optional<Login> findActiveLoginWithinShift(Integer employeeId) {
	    LocalTime now = LocalTime.now();
	    DayOfWeek today = LocalDate.now().getDayOfWeek();

	    return loginRepository.findAll(
	            LoginSpec.activeLoginWithinShift(employeeId, now, today)
	    ).stream().findFirst();
	}

	// get shift_time_attendance
	public ShiftTimeAttendance getTodayAttendance(Integer employeeId) {
		Optional<ShiftTimeAttendance> attendance =
			    shiftTimeAttendanceRepository.findAll(
			        ShiftTimeAttendanceSpec.todayByEmployee(employeeId)
			    ).stream().findFirst();

		if (attendance.isPresent()) {
			return attendance.get();
		} else {
			// New attendance
			return CreateNewAttendance(employeeId);

		}
	}

	private ShiftTimeAttendance CreateNewAttendance(Integer employeeId) {

		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

		ShiftTimeAttendance attendance = new ShiftTimeAttendance();
		attendance.setEmployee(employee);

		LocalDate today = LocalDate.now();
		attendance.setAttendanceDate(today);

		attendance.setTotalActiveTime(LocalTime.of(0, 0, 0));
		attendance.setLessTime(null);
		attendance.setOverTime(null);

		return shiftTimeAttendanceRepository.save(attendance);
	}

}