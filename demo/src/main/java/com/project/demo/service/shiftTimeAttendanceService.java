package com.project.demo.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private ShiftTimeRepo shiftTimeRepo;
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

		List<ShiftTimeAttendance> attendances = shiftTimeAttendanceRepository
				.findAllByEmployeeAndDate(employeeId, loginDate);

		ShiftTimeAttendance attendance;
		if (attendances.isEmpty()) {
			attendance = new ShiftTimeAttendance();
			attendance.setEmployee(login.getEmployee());
			attendance.setAttendanceDate(loginDate);
			attendance = shiftTimeAttendanceRepository.save(attendance);
			//XXX why do you recalculate salary with creating new attendance? 
			employeeSalaryService.updateSalaryOnAttendanceChange(employeeId, loginDate);
		} else if (attendances.size() == 1) {
			attendance = attendances.get(0);
		} else {
			attendance = mergeOrChooseAttendance(attendances, login);
		}

		if (login.getShiftTimeAttendanceId() == null) {
			login.setShiftTimeAttendanceId(attendance);
			loginRepo.save(login);
		}

		List<Login> allLogins = loginRepo.findAllByEmployeeAndDate(employeeId, loginDate);
		recalculateAttendanceForMultipleShifts(attendance, allLogins);

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
		
	private ShiftTimeAttendance mergeOrChooseAttendance(List<ShiftTimeAttendance> attendances, Login login) {
		if (login.getShiftTimeAttendanceId() != null) {
			for (ShiftTimeAttendance att : attendances) {
				if (att.getShiftTimeAttendanceId().equals(login.getShiftTimeAttendanceId().getShiftTimeAttendanceId())) {
					return att;
				}
			}
		}

		if (login.getShiftTimeId() != null) {
			Integer shiftTimeId = login.getShiftTimeId().getShiftTimeId();
			
			for (ShiftTimeAttendance att : attendances) {
				List<Login> attLogins = loginRepo.findAllByShiftTimeAttendanceId(att.getShiftTimeAttendanceId());
				if (attLogins.isEmpty()) {
					return att; 
				}
			}
			
			for (ShiftTimeAttendance att : attendances) {
				List<Login> attLogins = loginRepo.findAllByShiftTimeAttendanceId(att.getShiftTimeAttendanceId());
				boolean hasSameShift = attLogins.stream()
						.anyMatch(l -> l.getShiftTimeId() != null && 
								shiftTimeId.equals(l.getShiftTimeId().getShiftTimeId()));
				if (hasSameShift) {
					return att;
				}
			}
			
			for (ShiftTimeAttendance att : attendances) {
				List<Login> attLogins = loginRepo.findAllByShiftTimeAttendanceId(att.getShiftTimeAttendanceId());
				if (!attLogins.isEmpty()) {
					Optional<Integer> firstShiftTimeId = attLogins.stream()
							.filter(l -> l.getShiftTimeId() != null)
							.map(l -> l.getShiftTimeId().getShiftTimeId())
							.findFirst();
					
					if (firstShiftTimeId.isPresent()) {
						boolean allSameShift = attLogins.stream()
								.allMatch(l -> l.getShiftTimeId() != null && 
										firstShiftTimeId.get().equals(l.getShiftTimeId().getShiftTimeId()));
						
						if (allSameShift) {
							
							continue;
						}
					}
				}
			}
		}


		return attendances.stream()
				.max(Comparator.comparing(ShiftTimeAttendance::getShiftTimeAttendanceId))
				.orElse(attendances.get(0));
	}

	private void recalculateAttendanceForMultipleShifts(ShiftTimeAttendance attendance, List<Login> allLogins) {
		if (allLogins == null || allLogins.isEmpty()) {
			attendance.setTotalActiveTime(LocalTime.of(0, 0, 0));
			attendance.setLessTime(null);
			attendance.setOverTime(null);
			attendance.setTotalOverTime(LocalTime.of(0, 0, 0));
			return;
		}

		//FIXME here you get the shift from the login then get its Id 
		// and below at 203 you re-fetch the shift using the ID !!! 
		Map<Integer, List<Login>> loginsByShift = new HashMap<>();
		for (Login login : allLogins) {
			if (login.getShiftTimeId() != null) {
				Integer shiftTimeId = login.getShiftTimeId().getShiftTimeId();
				loginsByShift.computeIfAbsent(shiftTimeId, k -> new java.util.ArrayList<>()).add(login);
			}
		}

		if (loginsByShift.isEmpty()) {
			calculateSimpleAttendance(attendance, allLogins);
			return;
		}

		Duration totalWorkedAllShifts = Duration.ZERO;
		Duration totalRequiredAllShifts = Duration.ZERO;
		Duration totalLessTime = Duration.ZERO;
		Duration totalOverTime = Duration.ZERO;

		for (Map.Entry<Integer, List<Login>> entry : loginsByShift.entrySet()) {
			Integer shiftTimeId = entry.getKey();
			List<Login> shiftLogins = entry.getValue();

			Duration shiftWorked = Duration.ZERO;
			for (Login login : shiftLogins) {
				if (login.getActivityTime() != null) {
					shiftWorked = shiftWorked.plusSeconds(
							login.getActivityTime().toLocalTime().toSecondOfDay());
				}
			}
			totalWorkedAllShifts = totalWorkedAllShifts.plus(shiftWorked);

			Optional<ShiftTime> shiftTimeOpt = shiftTimeRepo.findById(shiftTimeId);
			if (shiftTimeOpt.isPresent()) {
				ShiftTime shiftTime = shiftTimeOpt.get();
				if (shiftTime.getTotalTime() != null) {
					Duration shiftRequired = Duration.ofSeconds(
							shiftTime.getTotalTime().toSecondOfDay());
					totalRequiredAllShifts = totalRequiredAllShifts.plus(shiftRequired);

					Duration diff = shiftWorked.minus(shiftRequired);
					if (diff.isNegative()) {
						totalLessTime = totalLessTime.plus(diff.abs());
					} else if (diff.isZero()) {
					} else {
						totalOverTime = totalOverTime.plus(diff);
					}
				}
			}
		}

		attendance.setTotalActiveTime(LocalTime.ofSecondOfDay(totalWorkedAllShifts.getSeconds()));

		if (!totalLessTime.isZero()) {
			attendance.setLessTime(LocalTime.ofSecondOfDay(totalLessTime.getSeconds()));
		} else {
			attendance.setLessTime(null);
		}

		if (!totalOverTime.isZero()) {
			attendance.setOverTime(LocalTime.ofSecondOfDay(totalOverTime.getSeconds()));
			attendance.setTotalOverTime(LocalTime.ofSecondOfDay(totalOverTime.getSeconds()));
		} else {
			attendance.setOverTime(null);
			attendance.setTotalOverTime(LocalTime.of(0, 0, 0));
		}
	}

	private void calculateSimpleAttendance(ShiftTimeAttendance attendance, List<Login> logins) {
		if (attendance == null) {
			throw new IllegalArgumentException("Attendance must not be null");
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
		
		attendance.setLessTime(null);
		attendance.setOverTime(null);
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

			return nearestShift;

		} catch (Exception e) {
			System.out.println("ERROR in findNearestShiftTimeForEmployee: " + e.getMessage());
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

			List<ShiftTimeAttendance> attendances = shiftTimeAttendanceRepository
					.findAllByEmployeeAndDate(employeeId, date);

			if (attendances.isEmpty()) {
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
			System.out.println("ERROR in calculateTotalIncentiveSales: " + e.getMessage());
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
	    try {
	        if (shift == null || shift.getFromTime() == null || shift.getToTime() == null) {
	            return false;
	        }
	        
	        if (shift.getDayIndex() != null) {
	            int todayDayIndex = date.getDayOfWeek().getValue();
	            if (shift.getDayIndex() != todayDayIndex) {
	                return false;
	            }
	        }
	        
	        List<Login> dayLogins = loginRepo.findAllByEmployeeAndDate(employeeId, date);
	        if (dayLogins.isEmpty()) {
	            return false;
	        }
	        
	        LocalTime shiftStart = shift.getFromTime();
	        LocalTime shiftEnd = shift.getToTime();
	        boolean isOvernight = shiftEnd.isBefore(shiftStart);
	        
	        LocalDateTime shiftStartDateTime = LocalDateTime.of(date, shiftStart);
	        LocalDateTime shiftEndDateTime = LocalDateTime.of(date, shiftEnd);
	        
	        if (isOvernight) {
	            shiftEndDateTime = shiftEndDateTime.plusDays(1);
	        }
	        
	        for (Login login : dayLogins) {
	            if (login.getLoginDateTime() == null) {
	                continue;
	            }
	            
	            LocalDateTime loginDateTime = login.getLoginDateTime();
	            LocalDateTime logoutDateTime = null;
	            
	            if (login.getLogoutDateTime() != null) {
	                logoutDateTime = login.getLogoutDateTime();
	            } else if (login.getActivityTime() != null) {
	                logoutDateTime = loginDateTime.plusSeconds(
	                    login.getActivityTime().toLocalTime().toSecondOfDay());
	            } else {
	                logoutDateTime = loginDateTime.plusMinutes(1);
	            }
	            
	            if (logoutDateTime.isBefore(loginDateTime)) {
	                continue;
	            }
	            
	            LocalDateTime overlapStart = loginDateTime.isAfter(shiftStartDateTime) ? 
	                                        loginDateTime : shiftStartDateTime;
	            LocalDateTime overlapEnd = logoutDateTime.isBefore(shiftEndDateTime) ? 
	                                      logoutDateTime : shiftEndDateTime;
	            
	            if (overlapStart.isBefore(overlapEnd)) {
	                Duration overlapDuration = Duration.between(overlapStart, overlapEnd);
	                long overlapMinutes = overlapDuration.toMinutes();
	                
	                if (overlapMinutes >= 30) {
	                    return true;
	                }
	            }
	        }
	        
	        return false;
	        
	    } catch (Exception e) {
	        System.out.println("ERROR in didEmployeeAttendThisSpecificShift: " + e.getMessage());
	        return false;
	    }
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
			System.out.println("ERROR in getTotalSalesDuringShift: " + e.getMessage());
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
			System.out.println("ERROR in getPersonalSalesDuringShift: " + e.getMessage());
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
			System.out.println("ERROR in getShiftsForEmployee: " + e.getMessage());
			return List.of();
		}
	}

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

		recalculateAttendanceForMultipleShifts(attendance, logins);

		shiftTimeAttendanceRepository.save(attendance);
	}

	public ShiftTimeAttendance getShiftTimeAttendance(Integer shiftTimeAttendanceId) {
		if (shiftTimeAttendanceId == null) {
			return null;
		}

		Optional<ShiftTimeAttendance> attendance = shiftTimeAttendanceRepository.findById(shiftTimeAttendanceId);

		return attendance.orElse(null);
	}

	public List<ShiftTimeAttendance> findAllByEmployeeAndDate(Integer employeeId, LocalDate date) {
		return shiftTimeAttendanceRepository.findAllByEmployeeAndDate(employeeId, date);
	}
}