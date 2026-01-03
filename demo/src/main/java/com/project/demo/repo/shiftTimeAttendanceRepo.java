package com.project.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.ShiftTimeAttendance;

@Repository
public interface shiftTimeAttendanceRepo
		extends JpaRepository<ShiftTimeAttendance, Integer>, JpaSpecificationExecutor<ShiftTimeAttendance> {

	// scheduler

//	@Query("SELECT a FROM ShiftTimeAttendance a WHERE a.totalActiveTime IS NULL AND a.attendanceDate >= :targetDate")
//	List<ShiftTimeAttendance> findByTotalActiveTimeIsNullAndAttendanceDate(@Param("targetDate") LocalDate targetDate);

	@Query(value = """
			    SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(s.total_active_time)))
			    FROM shift_time_attendance s
			    WHERE s.employee_id = :employeeId
			      AND YEAR(s.attendance_date) = :year
			      AND MONTH(s.attendance_date) = :month
			""", nativeQuery = true)
	String findTotalActivityTimeByEmployeeAndMonth(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month);

//	@Query(value = """
//		    SELECT * FROM shift_time_attendance st 
//		    WHERE st.employee_id = :employeeId 
//		    AND st.attendance_date = :attendanceDate
//		    """, nativeQuery = true)
//	Optional<ShiftTimeAttendance> findByEmployeeAndDate(
//		        @Param("employeeId") Integer employeeId,
//		        @Param("attendanceDate") LocalDate attendanceDate);
//	

//	@Query("""
//		    SELECT s
//		    FROM ShiftTimeAttendance s
//		    WHERE s.employee.id = :employeeId
//		      AND YEAR(s.attendanceDate) = :year
//		      AND MONTH(s.attendanceDate) = :month
//		""")
//		List<ShiftTimeAttendance> findByEmployeeAndMonth(
//		        @Param("employeeId") Integer employeeId,
//		        @Param("year") Integer year,
//		        @Param("month") Integer month
//		);
	// Select shift_time_attendance of today of the employee.
//		@Query("SELECT sta FROM ShiftTimeAttendance sta " + "WHERE sta.employee.employeeId = :employeeId "
//				+ "AND sta.attendanceDate = CURRENT_DATE")
//		Optional<ShiftTimeAttendance> findTodayAttendanceByEmployee(@Param("employeeId") Integer employeeId);

}