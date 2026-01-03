package com.project.demo.scheduler;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.project.demo.specification.EmployeeShiftSpec;

public class EmployeeShift {
	 @Autowired
	    private EmployeeShiftSpec employeeShiftSpec;
	    @Scheduled(cron = "0 0 0 * * ?")
	    public void activateEmployeeShift() {
	        LocalDate today = LocalDate.now();
	        employeeShiftSpec.activateCurrentShifts(today);
	        employeeShiftSpec.deactivateExpiredShifts(today);
	    }
}
