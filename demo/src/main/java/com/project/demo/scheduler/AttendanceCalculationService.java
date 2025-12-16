package com.project.demo.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.demo.entity.ShiftTimeAttendance;
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

	    List<ShiftTimeAttendance> attendancesToCalculate =
	        shiftTimeAttendanceRepo.findByTotalActiveTimeIsNullAndAttendanceDate(targetDate);

	    for (ShiftTimeAttendance attendance : attendancesToCalculate) {
	        try {
	            shiftTimeAttendanceService.updateDateAttendance(attendance);
	        } catch (Exception e) {
	            System.err.println(
	                "Error calculating attendance for employee ID: "
	                + attendance.getEmployee().getEmployee()
	            );
	        }
	    }
	}
}