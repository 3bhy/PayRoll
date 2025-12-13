package com.project.demo.service;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.project.demo.entity.Employee;
import com.project.demo.entity.Login;
import com.project.demo.entity.ShiftTime;
import com.project.demo.entity.ShiftTimeAttendance;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.LoginRepo;
import com.project.demo.repo.SalesRepo;
import com.project.demo.repo.ShiftTimeRepo;
import com.project.demo.repo.shiftTimeAttendanceRepo;

@Service
public class shiftTimeAttendanceService {

	@Autowired
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepository;
	@Autowired
	private ShiftTimeRepo shiftRepository;

	private LoginService loginService;

	@Autowired
	private EmployeeRepo employeeRepository;

	@Autowired
	private SalesRepo salesRepository;
	@Autowired
	private LoginRepo loginRepo;

	

	public void updateDateAttendance(Login login) {
		ShiftTimeAttendance attendance = login.getShiftTimeAttendanceId();
		Integer employeeId = login.getEmployee().getEmployee();

		// If attendance doesn't exist, create a new one and save it
		//FIXME may be there is another attendance for the date of the login but not attached to this login.
		// this may happen if the employee did a login two times at that date.
		if (attendance == null) {
			attendance = new ShiftTimeAttendance();
			attendance.setEmployee(login.getEmployee());
			//FIXME using now is rong as attendance should be related to login
			attendance.setAttendanceDate(java.sql.Date.valueOf(LocalDate.now()));

			attendance = shiftTimeAttendanceRepository.save(attendance);

			login.setShiftTimeAttendanceId(attendance);
			loginRepo.save(login);
		}

		// Get the nearest shift time for the employee
		ShiftTime shiftTime = findNearestShiftTimeForEmployee(employeeId, login.getLoginDateTime());

		// Calculate and set attendance data
		calculateAndSetAttendanceData(login, attendance, employeeId, shiftTime);

		shiftTimeAttendanceRepository.save(attendance);
	}

	//FIXME this method logic and role should be revised
	//should this method be called to update the attendance with every login (this should be controlled and a way to know if this login calculated or not)
	// or it should be called for every attendance to calculate its full data (in this case no need to send the login)
	private void calculateAndSetAttendanceData(Login login, ShiftTimeAttendance attendance, Integer employeeId,
			ShiftTime shiftTime) {
		LocalDate attendanceDate;

		if (attendance.getAttendanceDate() == null) {
			attendanceDate = LocalDate.now();
		} else {
			Date date = attendance.getAttendanceDate();
			if (date instanceof java.sql.Date) {
				attendanceDate = ((java.sql.Date) date).toLocalDate();
			} else {
				attendanceDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
		}

		// Calculate time difference between activity time and shift time
		//FIXME if the employee attended 2 shift times
		// the less and overtime will be replaced and keep just one shift time result.
		calculateTimeDifference(login, attendance, shiftTime);

		Time totalActiveTime = calculateTotalActiveTimeForEmployee(employeeId, attendanceDate);
		attendance.setTotalActiveTime(totalActiveTime);

		// Total incentive sales
		Float totalIncentiveSales = calculateTotalIncentiveSales(employeeId, attendanceDate);
		attendance.setTotalIncentiveSales(totalIncentiveSales);
	}

	private Time calculateTotalActiveTimeForEmployee(Integer employeeId, LocalDate date) {
	    try {
	        Long totalSeconds = loginRepo.sumActivityTimeByEmployeeAndDateNative(employeeId, date);

	        if (totalSeconds != null && totalSeconds > 0) {
	            long hours = totalSeconds / 3600;
	            long minutes = (totalSeconds % 3600) / 60;
	            long seconds = totalSeconds % 60;
	            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	            return Time.valueOf(timeString);
	        } else {
	            return Time.valueOf("00:00:00");
	        }
	    } catch (Exception e) {
	       
	        return Time.valueOf("00:00:00");
	    }
	}

	// Calculate time difference between activity time and shift time
	private void calculateTimeDifference(Login login, ShiftTimeAttendance attendance, ShiftTime shiftTime) {
		 if (shiftTime != null && shiftTime.getTotalTime() != null && login.getActivityTime() != null) {
			try {
				// Convert activity time to minutes
				LocalTime activityLocalTime = login.getActivityTime().toLocalTime();
				long activityMinutes = activityLocalTime.getHour() * 60 + activityLocalTime.getMinute();
				
				// Convert shift total time to minutes
				LocalTime shiftTotalLocalTime = shiftTime.getTotalTime().toLocalTime();
				long shiftTotalMinutes = shiftTotalLocalTime.getHour() * 60 + shiftTotalLocalTime.getMinute();

				// Calculate difference
				// TODO difference beteen 2 times can be calculated by
//				LocalTime startTime = LocalTime.parse("08:30:00");
//		        LocalTime stopTime = LocalTime.parse("12:45:30");
//		        Duration duration = Duration.between(startTime, stopTime);

				long timeDifferenceMinutes = activityMinutes - shiftTotalMinutes;

				System.out.println("Activity Time: " + activityLocalTime + " (" + activityMinutes + " minutes)");
				System.out.println("Shift Time: " + shiftTotalLocalTime + " (" + shiftTotalMinutes + " minutes)");
				System.out.println("Time Difference: " + timeDifferenceMinutes + " minutes");

				if (timeDifferenceMinutes < 0) {
					// Less time - activity time is less than required shift time
					long lessMinutes = Math.abs(timeDifferenceMinutes);
					Time lessTime = Time.valueOf(String.format("%02d:%02d:00", lessMinutes / 60, lessMinutes % 60));
					attendance.setLessTime(lessTime);
					attendance.setOverTime(null);
					System.out.println("Less time detected: " + lessTime);
				} else if (timeDifferenceMinutes > 0) {
					// Overtime - activity time is more than required shift time
					long overMinutes = timeDifferenceMinutes;
					Time overTime = Time.valueOf(String.format("%02d:%02d:00", overMinutes / 60, overMinutes % 60));
					attendance.setOverTime(overTime);
					attendance.setLessTime(null);
					System.out.println("Overtime detected: " + overTime);
				} else {
					// Exact time - activity time equals shift time
					attendance.setLessTime(null);
					attendance.setOverTime(null);
					System.out.println("Time is exactly as required");
				}

			} catch (Exception e) {
				System.err.println("Error calculating time difference: " + e.getMessage());
				attendance.setLessTime(null);
				attendance.setOverTime(null);
			}
		} else {
			attendance.setLessTime(null);
			attendance.setOverTime(null);
			System.out.println("Cannot calculate overtime/less time - missing shift or activity data");
		}
	}

	// Find the nearest shift time for employee based on login time
	private ShiftTime findNearestShiftTimeForEmployee(Integer employeeId, LocalDateTime loginTime) {
	    try {
	    	//FIXME this method does not return the shift time related to the login as it uses the current time not the login time
	        Optional<ShiftTime> optionalShift = loginRepo.findCurrentShiftTimeForEmployee(employeeId);

	        if (optionalShift.isEmpty()) {
	            System.out.println("No shift times found for employee: " + employeeId);
	            return null;
	        }

	        ShiftTime shiftTime = optionalShift.get();

	        //FIXME why do you do this calculation? you do not use its result.
	        // Convert times
	        LocalTime loginLocalTime = loginTime.toLocalTime();
	        LocalTime shiftFromTime = shiftTime.getFromTime().toLocalTime();
	        LocalTime shiftToTime = shiftTime.getToTime().toLocalTime();

	        long diffToStart = calculateTimeDifferenceInMinutes(loginLocalTime, shiftFromTime);
	        long diffToEnd = calculateTimeDifferenceInMinutes(loginLocalTime, shiftToTime);

	        long minDiff = Math.min(diffToStart, diffToEnd);

	        System.out.println("Nearest shift found: " + shiftTime.getShiftTimeId() 
	            + " with time difference: " + minDiff + " minutes");

	        return shiftTime;

	    } catch (Exception e) {
	        System.err.println("Error finding nearest shift for employee " + employeeId + ": " + e.getMessage());
	        return null;
	    }
	}


	// Calculate time difference in minutes between two LocalTime objects
	private long calculateTimeDifferenceInMinutes(LocalTime time1, LocalTime time2) {
		long minutes1 = time1.getHour() * 60 + time1.getMinute();
		long minutes2 = time2.getHour() * 60 + time2.getMinute();

		return Math.abs(minutes1 - minutes2);
	}

	// calculate total incentive sales
	private Float calculateTotalIncentiveSales(Integer employeeId, LocalDate date) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

		Float incentivePercent = employee.getSalesIncentivePercent() != null ? employee.getSalesIncentivePercent()
				: 0.0f;

		if (employee.getIncentiveOnAllSales()) {
			// Incentive On All Sales=1
			//FIXME for one attendance the employee may has more than one shift time
			ShiftTime shiftTime = getShiftTimeForEmployee(employeeId, date);
			Float allSalesDuringShift = calculateAllSalesDuringShiftTime(date, shiftTime);
			return allSalesDuringShift * (incentivePercent / 100);
		} else {

			Float allEmployeeSales = calculateAllEmployeeSalesForDate(employeeId, date);
			return allEmployeeSales * (incentivePercent / 100);
		}
	}

	// shift time for employee
	public ShiftTime getShiftTimeForEmployee(Integer employeeId, LocalDate date) {
		try {

			Optional<ShiftTime> shiftToday = shiftRepository.findByEmployeeIdAndDateNative(employeeId, date);

			if (shiftToday.isPresent()) {
				return shiftToday.get();
			}

			List<ShiftTime> employeeShifts = shiftRepository.findByEmployeeIdNative(employeeId);

			if (!employeeShifts.isEmpty()) {
				return employeeShifts.get(0);
			}

			Optional<ShiftTime> defaultShift = shiftRepository.findAnyShiftTime();

			if (defaultShift.isPresent()) {
				return defaultShift.get();
			}

			return loginService.createDummyShiftTime();

		} catch (Exception e) {
			System.out.println("ERROR in getShiftTimeForEmployee: " + e.getMessage());
			return loginService.createDummyShiftTime();
		}
	}

	private Float calculateAllSalesDuringShiftTime(LocalDate date, ShiftTime shiftTime) {
		if (shiftTime == null) {
			return 0.0f;
		}

		LocalDateTime shiftStart = LocalDateTime.of(date, shiftTime.getFromTime().toLocalTime());
		LocalDateTime shiftEnd = LocalDateTime.of(date, shiftTime.getToTime().toLocalTime());

		return salesRepository.calculateAllSalesDuringShiftTime(shiftStart, shiftEnd);
	}

	private Float calculateAllEmployeeSalesForDate(Integer employeeId, LocalDate date) {
		return salesRepository.calculateTotalSalesByEmployeeAndDate(employeeId, date);
	}

	//TODO fix naming conventions
	public ShiftTimeAttendance getshittimeattendance(Integer shiftTimeAttendanceId) {
		if (shiftTimeAttendanceId == null) {
			return null;
		}

		Optional<ShiftTimeAttendance> attendance = shiftTimeAttendanceRepository.findById(shiftTimeAttendanceId);

		return attendance.orElse(null);
	}
}