package com.project.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.ShiftTimeAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface shiftTimeAttendanceRepo extends JpaRepository<ShiftTimeAttendance, Integer> {

	// scheduler

	@Query("SELECT a FROM ShiftTimeAttendance a WHERE a.totalActiveTime IS NULL AND a.attendanceDate >= :targetDate")
	List<ShiftTimeAttendance> findByTotalActiveTimeIsNullAndAttendanceDate(@Param("targetDate") LocalDate targetDate);

	@Query(value = """
			    SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(s.total_active_time)))
			    FROM shift_time_attendance s
			    WHERE s.employee_id = :employeeId
			      AND YEAR(s.attendance_date) = :year
			      AND MONTH(s.attendance_date) = :month
			""", nativeQuery = true)
	String findTotalActivityTimeByEmployeeAndMonth(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month);

	@Query(value = "SELECT * FROM shift_time_attendance st WHERE st.employee_id = :employeeId AND st.attendance_date = :attendanceDate LIMIT 1", nativeQuery = true)
	Optional<ShiftTimeAttendance> findOneByEmployeeAndDate(@Param("employeeId") Integer employeeId,
			@Param("attendanceDate") LocalDate attendanceDate);
	
	@Query(value = """
		    SELECT * FROM shift_time_attendance st 
		    WHERE st.employee_id = :employeeId 
		    AND st.attendance_date = :attendanceDate
		    """, nativeQuery = true)
		List<ShiftTimeAttendance> findAllByEmployeeAndDate(
		        @Param("employeeId") Integer employeeId,
		        @Param("attendanceDate") LocalDate attendanceDate);
	
	
	@Query("""
		    SELECT s
		    FROM ShiftTimeAttendance s
		    WHERE s.employee.id = :employeeId
		      AND YEAR(s.attendanceDate) = :year
		      AND MONTH(s.attendanceDate) = :month
		""")
		List<ShiftTimeAttendance> findByEmployeeAndMonth(
		        @Param("employeeId") Integer employeeId,
		        @Param("year") Integer year,
		        @Param("month") Integer month
		);

}