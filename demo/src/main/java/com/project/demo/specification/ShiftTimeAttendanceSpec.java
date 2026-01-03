package com.project.demo.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.ShiftTimeAttendance;

public class ShiftTimeAttendanceSpec {
	  public static Specification<ShiftTimeAttendance> totalActiveTimeIsNullFromDate(LocalDate targetDate) {
	        return (root, query, cb) -> {
	            if (targetDate == null) return null;	
	            return cb.and(
	                    cb.isNull(root.get("totalActiveTime")),
	                    cb.greaterThanOrEqualTo(root.get("attendanceDate"), targetDate)
	            );
	        };
	    }
	  
	  public static Specification<ShiftTimeAttendance> byEmployeeAndDate(Integer employeeId, LocalDate attendanceDate) {
	        return (root, query, cb) -> {
	            if (employeeId == null || attendanceDate == null) return null;
	            return cb.and(
	                    cb.equal(root.get("employee").get("employeeId"), employeeId),
	                    cb.equal(root.get("attendanceDate"), attendanceDate)
	            );
	        };
	    }
	  
	  public static Specification<ShiftTimeAttendance> byEmployeeAndMonth(Integer employeeId, int year, int month) {
	        return (root, query, cb) -> {
	            if (employeeId == null) return null;

	            LocalDate startOfMonth = LocalDate.of(year, month, 1);
	            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

	            return cb.and(
	                    cb.equal(root.get("employee").get("id"), employeeId),
	                    cb.between(root.get("attendanceDate"), startOfMonth, endOfMonth)
	            );
	        };
	    }
	  
	  public static Specification<ShiftTimeAttendance> todayByEmployee(Integer employeeId) {
	        return (root, query, cb) -> {
	            if (employeeId == null) return null;
	            return cb.and(
	                    cb.equal(root.get("employee").get("employeeId"), employeeId),
	                    cb.equal(root.get("attendanceDate"), LocalDate.now())
	            );
	        };
	    }
}
