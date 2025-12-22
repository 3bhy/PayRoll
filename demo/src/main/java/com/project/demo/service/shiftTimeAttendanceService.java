package com.project.demo.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	private EmployeeSalaryService employeeSalaryService;
	@Autowired
	private ShiftTimeRepo shiftRepository;
	@Lazy
	@Autowired
	private LoginService loginService;

	@Autowired
	private EmployeeRepo employeeRepository;

	@Autowired
	private SalesRepo salesRepository;
	@Autowired
	private LoginRepo loginRepo;

	// If attendance doesn't exist, create a new one and save it
	// FIXME may be there is another attendance for the date of the login but not
	// attached to this login.
	// this may happen if the employee did a login two times at that date.
	// FIXME DONE still the case not fixed, 
	// finding one shift time by limit the return to 1 leads to 
	// the possibility of having more than one with no errors which will cause problems later
	// if the logic says you will not have more than one, no need to limit.
	// if the logic says that it is normal to get more than one, you have to get them all and deal with this case.

	public void updateDateAttendance(Login login) {
		LocalDate loginDate = login.getLoginDateTime().toLocalDate();
		Integer employeeId = login.getEmployee().getEmployee();

		Optional<ShiftTimeAttendance> optionalAttendance = shiftTimeAttendanceRepository
				.findOneByEmployeeAndDate(employeeId, loginDate);

		ShiftTimeAttendance attendance;
		if (optionalAttendance.isPresent()) {
			attendance = optionalAttendance.get();
		} else {
			attendance = new ShiftTimeAttendance();
			attendance.setEmployee(login.getEmployee());
			attendance.setAttendanceDate(loginDate);

			attendance = shiftTimeAttendanceRepository.save(attendance);

			employeeSalaryService.updateSalaryOnAttendanceChange(employeeId, loginDate);
		}

		if (login.getShiftTimeAttendanceId() == null) {
			login.setShiftTimeAttendanceId(attendance);
			loginRepo.save(login);
		}

		List<Login> logins = loginRepo.findAllByEmployeeAndDate(employeeId, loginDate);
		calculateAndSetAttendanceData(attendance, logins);

		shiftTimeAttendanceRepository.save(attendance);
	}

	// FIXME this method logic and role should be revised
	// should this method be called to update the attendance with every login (this
	// should be controlled and a way to know if this login calculated or not)
	// or it should be called for every attendance to calculate its full data (in
	// this case no need to send the login)
	// FIXME -DONE - logic is still wrong I recommend to remove this method and re-implement it again  

	// Calculate time difference between activity time and shift time
	// FIXME if the employee attended 2 shift times
	// the less and overtime will be replaced and keep just one shift time result.
	// FIXME -DONE still if the employee has 2 shift times a day, the logic will be wrong
	private void calculateAndSetAttendanceData(ShiftTimeAttendance attendance, List<Login> logins) {

		if (attendance == null) {
			throw new IllegalArgumentException("Attendance must not be null");
		}

		LocalDate attendanceDate = attendance.getAttendanceDate();
		if (attendanceDate == null) {
			throw new IllegalStateException("Attendance date should not be null");
		}

		if (logins == null || logins.isEmpty()) {
			attendance.setTotalActiveTime(LocalTime.of(0, 0, 0));
			attendance.setLessTime(null);
			attendance.setOverTime(null);
			return;
		}

		Duration totalWorked = Duration.ZERO;
		for (Login login : logins) {
			if (login.getActivityTime() != null) {
				totalWorked = totalWorked.plusSeconds(login.getActivityTime().toLocalTime().toSecondOfDay());
			}
		}

		attendance.setTotalActiveTime(LocalTime.ofSecondOfDay(totalWorked.getSeconds()));

		ShiftTime shiftTime = getShiftTimeForEmployee(logins.get(0).getEmployee().getEmployee(), attendanceDate);

		if (shiftTime == null || shiftTime.getTotalTime() == null) {
			attendance.setLessTime(null);
			attendance.setOverTime(null);
			return;
		}

		Duration shiftRequired = Duration.ofSeconds(shiftTime.getTotalTime().toSecondOfDay());

		Duration diff = totalWorked.minus(shiftRequired);

		if (diff.isNegative()) {
			attendance.setLessTime(LocalTime.ofSecondOfDay(Math.abs(diff.getSeconds())));
			attendance.setOverTime(null);
		} else if (!diff.isZero()) {
			attendance.setOverTime(LocalTime.ofSecondOfDay(diff.getSeconds()));
			attendance.setLessTime(null);
		} else {
			attendance.setLessTime(null);
			attendance.setOverTime(null);
		}
	}

	// Find the nearest shift time for employee based on login time
	public ShiftTime findNearestShiftTimeForEmployee(Integer employeeId, LocalDateTime loginTime) {
		try {
			List<ShiftTime> shifts = shiftRepository.findShiftsByEmployeeIdAndDate(employeeId,
					loginTime.getDayOfWeek().getValue());

			if (shifts.isEmpty()) {
				return null;
			}

			LocalTime loginLocalTime = loginTime.toLocalTime();

			ShiftTime nearestShift = shifts.stream().min(Comparator.comparingLong(shift -> {
				LocalTime from = shift.getFromTime();
				LocalTime to = shift.getToTime();
				long diffToStart = Math.abs(Duration.between(loginLocalTime, from).toMinutes());
				long diffToEnd = Math.abs(Duration.between(loginLocalTime, to).toMinutes());
				return Math.min(diffToStart, diffToEnd);
			})).orElse(null);

			if (nearestShift != null) {
			}

			return nearestShift;

		} catch (Exception e) {
			return null;
		}
	}

	// calculate total incentive sales

	// Incentive On All Sales=1
	// FIXME for one attendance the employee may has more than one shift time
	// FIXME DONE still the case of having more than one shift time is not covered
	public Float calculateTotalIncentiveSales(Integer employeeId, LocalDate date) {
		try {

			Employee employee = employeeRepository.findById(employeeId)
					.orElseThrow(() -> new RuntimeException("Employee not found"));

			if (employee.getSalesIncentivePercent() == null) {
				return 0.0f;
			}

			Float incentivePercent = employee.getSalesIncentivePercent();

			if (incentivePercent <= 0.0f) {
				return 0.0f;
			}

			Optional<ShiftTimeAttendance> attendance = shiftTimeAttendanceRepository
					.findOneByEmployeeAndDate(employeeId, date);

			if (!attendance.isPresent()) {
				return 0.0f;
			}

			Boolean incentiveOnAllSales = employee.getIncentiveOnAllSales();
			if (incentiveOnAllSales == null) {
				incentiveOnAllSales = false;
			}

			Float result;
			if (incentiveOnAllSales) {
				result = calculateIncentiveForAllSales(employeeId, date, incentivePercent);
			} else {
				result = calculateIncentiveForPersonalSales(employeeId, date, incentivePercent);
			}

			return result;

		} catch (Exception e) {

			return 0.0f;
		}
	}

	private Float calculateIncentiveForAllSales(Integer employeeId, LocalDate date, Float incentivePercent) {

		List<ShiftTime> shifts = getShiftsForEmployee(employeeId, date);

		if (shifts.isEmpty()) {
			return 0.0f;
		}

		Float totalIncentive = 0.0f;

		for (int i = 0; i < shifts.size(); i++) {
			ShiftTime shift = shifts.get(i);

			boolean attendedThisShift = didEmployeeAttendThisSpecificShift(employeeId, date, shift);

			if (!attendedThisShift) {
				continue;
			}

			Float salesDuringShift = getTotalSalesDuringShift(date, shift);

			if (salesDuringShift == null || salesDuringShift <= 0) {
				continue;
			}

			Float shiftIncentive = salesDuringShift * (incentivePercent / 100);

			totalIncentive += shiftIncentive;
		}

		return totalIncentive;
	}

	private boolean didEmployeeAttendThisSpecificShift(Integer employeeId, LocalDate date, ShiftTime shift) {

		if (shift.getDayIndex() != null) {
			int todayDayIndex = date.getDayOfWeek().getValue();
			if (shift.getDayIndex() != todayDayIndex) {

				return false;
			}
		}
		Optional<ShiftTimeAttendance> attendance = shiftTimeAttendanceRepository.findOneByEmployeeAndDate(employeeId,
				date);

		if (!attendance.isPresent()) {

			return false;
		}

		ShiftTimeAttendance att = attendance.get();

		if (att.getTotalActiveTime() == null) {

			return false;
		}

		LocalTime activeTime = att.getTotalActiveTime();

		long activeMinutes = Duration.between(LocalTime.MIDNIGHT, activeTime).toMinutes();

		if (activeTime.equals(LocalTime.MIDNIGHT) || activeMinutes < 30) {

			return false;
		}

		List<Login> logins = loginRepo.findAllByEmployeeAndDate(employeeId, date);

		if (logins.isEmpty()) {

			return false;
		}

		LocalTime from = shift.getFromTime();
		LocalTime to = shift.getToTime();
		boolean isOvernightShift = to.isBefore(from);
		System.out.println("DEBUG: Shift is overnight: " + isOvernightShift);

		for (Login login : logins) {
			if (login.getLoginDateTime() == null || login.getActivityTime() == null) {
				continue;
			}

			LocalTime loginTime = login.getLoginDateTime().toLocalTime();
			LocalTime logoutTime = loginTime.plusSeconds(login.getActivityTime().toLocalTime().toSecondOfDay());

			boolean attended = false;

			if (!isOvernightShift) {
				attended = !loginTime.isAfter(to) && !logoutTime.isBefore(from);
			} else {
				if (loginTime.isBefore(to)) {
					attended = loginTime.isBefore(to) && logoutTime.isAfter(from);
				} else {
					attended = loginTime.isAfter(from) || logoutTime.isBefore(to);
				}
			}

			long loginDuration = Duration.between(loginTime, logoutTime).toMinutes();

			if (attended && loginDuration >= 30) {

				return true;
			}
		}

		return false;
	}

	private Float calculateIncentiveForPersonalSales(Integer employeeId, LocalDate date, Float incentivePercent) {
		List<ShiftTime> shifts = getShiftsForEmployee(employeeId, date);

		if (shifts.isEmpty()) {
			return 0.0f;
		}

		Float totalIncentive = 0.0f;

		for (ShiftTime shift : shifts) {
			boolean attendedThisShift = didEmployeeAttendThisSpecificShift(employeeId, date, shift);

			if (!attendedThisShift) {
				continue;
			}

			Float personalSalesDuringShift = getPersonalSalesDuringShift(employeeId, date, shift);

			if (personalSalesDuringShift == null || personalSalesDuringShift <= 0) {
				continue;
			}

			Float shiftIncentive = personalSalesDuringShift * (incentivePercent / 100);
			totalIncentive += shiftIncentive;
		}

		return totalIncentive;
	}

	private Float getTotalSalesDuringShift(LocalDate date, ShiftTime shift) {
		try {
			if (shift == null || shift.getFromTime() == null || shift.getToTime() == null) {
				return 0.0f;
			}

			LocalDateTime shiftStart = LocalDateTime.of(date, shift.getFromTime());
			LocalDateTime shiftEnd = LocalDateTime.of(date, shift.getToTime());

			if (shift.getToTime().isBefore(shift.getFromTime())) {
				shiftEnd = shiftEnd.plusDays(1);
			}

			Float sales = salesRepository.calculateAllSalesDuringShiftTime(shiftStart, shiftEnd);
			return sales != null ? sales : 0.0f;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	private Float getPersonalSalesDuringShift(Integer employeeId, LocalDate date, ShiftTime shift) {
		try {
			if (shift == null || shift.getFromTime() == null || shift.getToTime() == null) {
				return 0.0f;
			}

			LocalDateTime shiftStart = LocalDateTime.of(date, shift.getFromTime());
			LocalDateTime shiftEnd = LocalDateTime.of(date, shift.getToTime());

			if (shift.getToTime().isBefore(shift.getFromTime())) {
				shiftEnd = shiftEnd.plusDays(1);
			}

			Float sales = salesRepository.calculateEmployeeSalesDuringShiftTime(employeeId, shiftStart, shiftEnd);
			return sales != null ? sales : 0.0f;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	private List<ShiftTime> getShiftsForEmployee(Integer employeeId, LocalDate date) {
		try {
			int todayDayIndex = date.getDayOfWeek().getValue();
			List<ShiftTime> allShifts = shiftRepository.findByEmployeeId(employeeId);

			List<ShiftTime> shiftsForToday = allShifts.stream().filter(shift -> {
				if (shift.getDayIndex() == null) {
					return true;
				}
				return shift.getDayIndex() == todayDayIndex;
			}).collect(Collectors.toList());

			return shiftsForToday;

		} catch (Exception e) {
			return List.of();
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

	public void updateDateAttendance(ShiftTimeAttendance attendance) {
		if (attendance == null || attendance.getEmployee() == null || attendance.getAttendanceDate() == null) {
			return;
		}

		Integer employeeId = attendance.getEmployee().getEmployee();
		LocalDate attendanceDate = attendance.getAttendanceDate();

		List<Login> logins = loginRepo.findAllByEmployeeAndDate(employeeId, attendanceDate);

		calculateAndSetAttendanceData(attendance, logins);

		shiftTimeAttendanceRepository.save(attendance);
	}

	public ShiftTimeAttendance getShiftTimeAttendance(Integer shiftTimeAttendanceId) {
		if (shiftTimeAttendanceId == null) {
			return null;
		}

		Optional<ShiftTimeAttendance> attendance = shiftTimeAttendanceRepository.findById(shiftTimeAttendanceId);

		return attendance.orElse(null);
	}
}