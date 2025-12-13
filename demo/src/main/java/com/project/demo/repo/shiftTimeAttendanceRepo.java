package com.project.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.Login;
import com.project.demo.entity.ShiftTimeAttendance;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface shiftTimeAttendanceRepo extends JpaRepository<ShiftTimeAttendance, Integer> {

	// scheduler
	//FIXME how this method returns list of logins?
	@Query("SELECT l FROM ShiftTimeAttendance l WHERE l.totalActiveTime IS NULL AND DATE(l.attendanceDate) >= :targetDate")
	List<Login> findByTotalActiveTimeIsNullAndAttendanceDate(@Param("targetDate") LocalDate targetDate);
	
	
	  @Query("SELECT SUM(s.totalActiveTime) FROM ShiftTimeAttendance s WHERE s.employee.employeeId = :employeeId AND YEAR(s.attendanceDate) = :year AND MONTH(s.attendanceDate) = :month")
	    Float findTotalActivityTimeByEmployeeAndMonth(@Param("employeeId") Integer employeeId, 
	                                                 @Param("year") Integer year, 
	                                                 @Param("month") Integer month);

}