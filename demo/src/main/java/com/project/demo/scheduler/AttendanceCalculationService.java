package com.project.demo.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.demo.entity.Login;
import com.project.demo.repo.shiftTimeAttendanceRepo;
import com.project.demo.service.shiftTimeAttendanceService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AttendanceCalculationService {

	@Autowired
	private shiftTimeAttendanceRepo shiftTimeAttendanceRepo;

	@Autowired
	private shiftTimeAttendanceService shiftTimeAttendanceService;

	@Scheduled(cron = "0 0 2 * * ?")
	public void calculateAttendance() {
		LocalDate targetDate = LocalDate.now().minusDays(2);

		List<Login> attendancesToCalculate = shiftTimeAttendanceRepo
				.findByTotalActiveTimeIsNullAndAttendanceDate(targetDate);

		for (Login login : attendancesToCalculate) {
			try {
				shiftTimeAttendanceService.updateDateAttendance(login);
			} catch (Exception e) {
				System.err.println(
						"Error calculating attendance for employee ID: " + login.getEmployee().getEmployee());
			}
		}
	}
}