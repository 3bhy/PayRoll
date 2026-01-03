package com.project.demo.specification;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.ShiftTime;

public class ShiftTimeSpec {
	
	public static Specification<ShiftTime> forEmployeeAndDate(Integer employeeId, LocalDate date) {
        return (root, query, cb) -> {
            if (employeeId == null || date == null) return null;

            var join = root.join("shiftTime").join("employeeShift");

            return cb.and(
                    cb.equal(join.get("employee").get("employeeId"), employeeId),
                    cb.equal(join.get("active"), true),
                    cb.lessThanOrEqualTo(join.get("startActiveDate"), date),
                    cb.greaterThanOrEqualTo(join.get("endActiveDate"), date),
                    cb.equal(root.get("dayIndex"), date.getDayOfWeek().getValue())
            );
        };
    }
	  public static Specification<ShiftTime> forEmployeeActiveShifts(Integer employeeId, LocalDate date) {
	        return (root, query, cb) -> {
	            if (employeeId == null || date == null) return null;

	            var shiftJoin = root.join("shiftId");
	            var empShiftJoin = shiftJoin.join("employeeShift"); 

	            return cb.and(
	                    cb.equal(empShiftJoin.get("employee").get("employeeId"), employeeId), 
	                    cb.isTrue(empShiftJoin.get("active")),                                  
	                    cb.lessThanOrEqualTo(empShiftJoin.get("startActiveDate"), date),      
	                    cb.greaterThanOrEqualTo(empShiftJoin.get("endActiveDate"), date),   
	                    cb.equal(root.get("dayIndex"), date.getDayOfWeek().getValue())        
	            );
	        };
	    }
	  public static Specification<ShiftTime> currentShiftForEmployee(Integer employeeId, LocalDate date, LocalTime time) {
	        return (root, query, cb) -> {
	            if (employeeId == null || date == null || time == null) return null;

	            var shiftJoin = root.join("shiftId");
	            var empShiftJoin = shiftJoin.join("employeeShift");

	            return cb.and(
	                    cb.equal(empShiftJoin.get("employee").get("employeeId"), employeeId),   
	                    cb.isTrue(empShiftJoin.get("active")),                                  
	                    cb.lessThanOrEqualTo(empShiftJoin.get("startActiveDate"), date),      
	                    cb.greaterThanOrEqualTo(empShiftJoin.get("endActiveDate"), date),       
	                    cb.equal(root.get("dayIndex"), date.getDayOfWeek().getValue()),         
	                    cb.lessThanOrEqualTo(root.get("fromTime"), time),                       
	                    cb.greaterThanOrEqualTo(root.get("toTime"), time)                      
	            );
	        };
	        
	        
	    }
	  public static Specification<ShiftTime> forEmployeeAndDay(Integer employeeId, int dayIndex) {
	        return (root, query, cb) -> {
	            if (employeeId == null) return null;

	            var shiftJoin = root.join("shiftId");
	            var empShiftJoin = shiftJoin.join("employeeShift");

	            return cb.and(
	                    cb.equal(empShiftJoin.get("employee").get("employeeId"), employeeId),
	                    cb.equal(root.get("dayIndex"), dayIndex)
	            );
	        };
	    }
	  
	  public static Specification<ShiftTime> forEmployee(Integer employeeId) {
	        return (root, query, cb) -> {
	            if (employeeId == null) return null;

	            var shiftJoin = root.join("shiftId");
	            var empShiftJoin = shiftJoin.join("employeeShift");

	            return cb.equal(empShiftJoin.get("employee").get("employeeId"), employeeId);
	        };
	    }
}
