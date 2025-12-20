package com.project.demo.scheduler;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.project.demo.repo.EmployeeShiftRepo;


public class EmployeeShift {
	 @Autowired
	    private EmployeeShiftRepo employeeShiftRepo;
	    @Scheduled(cron = "0 0 0 * * ?")
	    public void activateEmployeeShift() {
	        LocalDate today = LocalDate.now();
	        employeeShiftRepo.activateCurrentShifts(today);
	        employeeShiftRepo.deactivateExpiredShifts(today);
	    }
}
