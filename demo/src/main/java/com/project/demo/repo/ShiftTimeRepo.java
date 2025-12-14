package com.project.demo.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.demo.entity.ShiftTime;

public interface ShiftTimeRepo extends JpaRepository<ShiftTime, Integer> ,JpaSpecificationExecutor<ShiftTime> {

	@Query(value = "SELECT st.* FROM shift_time st WHERE st.shift_id = (SELECT shift_id FROM employee WHERE employee_id = :employeeId)", nativeQuery = true)
	Optional<ShiftTime> findShiftTimeByEmployeeIdNative(@Param("employeeId") Integer employeeId);

	//FIXME why do you compare shift id using the employeeId?
	//FIXME-DONE
	 

	@Query(value = "SELECT st.* FROM shift_time st " + "WHERE st.shift_id IN ("
			+ "   SELECT es.shift_id FROM employee_shift es " + "   WHERE es.employee_id = :employeeId "
			+ "   AND es.active = 1 " + "   AND :date BETWEEN es.start_active_date AND es.end_active_date" + ") "
			+ "AND st.day_index = DAYOFWEEK(:date)", nativeQuery = true)
	Optional<ShiftTime> findByEmployeeIdAndDateNative(@Param("employeeId") Integer employeeId,
			@Param("date") LocalDate date);

	@Query(value = "SELECT st.* FROM shift_time st LIMIT 1", nativeQuery = true)
	Optional<ShiftTime> findAnyShiftTime();

	@Query(value = "SELECT st.* FROM shift_time st " + "WHERE st.shift_id IN ("
			+ "   SELECT es.shift_id FROM employee_shift es " + "   WHERE es.employee_id = :employeeId "
			+ "   AND es.active = 1" + ")", nativeQuery = true)
	List<ShiftTime> findByEmployeeIdNative(@Param("employeeId") Integer employeeId);
	
	@Query("SELECT DISTINCT FUNCTION('DAY', sta.attendanceDate) " +
	           "FROM ShiftTimeAttendance sta " +
	           "WHERE sta.employee.employeeId = :employeeId")
	    List<Integer> findDistinctDaysWithAttendance(@Param("employeeId") Integer employeeId);
	
	

}
