package com.project.demo.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.demo.entity.ShiftTime;

public interface ShiftTimeRepo extends JpaRepository<ShiftTime, Integer>, JpaSpecificationExecutor<ShiftTime> {

	// FIXME why do you compare shift id using the employeeId?
	// FIXME-DONE id and date

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

	@Query("SELECT DISTINCT FUNCTION('DAY', sta.attendanceDate) " + "FROM ShiftTimeAttendance sta "
			+ "WHERE sta.employee.employeeId = :employeeId")
	List<Integer> findDistinctDaysWithAttendance(@Param("employeeId") Integer employeeId);

	@Query(value = "SELECT st.* " + "FROM shift_time st " + "JOIN shift s ON s.shift_id = st.shift_id "
			+ "JOIN employee_shift es ON es.shift_id = s.shift_id " + "WHERE es.employee_id = :employeeId "
			+ "AND es.active = TRUE " + "AND st.day_index = WEEKDAY(CURRENT_DATE) + 1 "
			+ "AND CURTIME() BETWEEN st.from_time AND st.to_time " + "LIMIT 1", nativeQuery = true)
	Optional<ShiftTime> findCurrentShiftTimeForEmployee(@Param("employeeId") Integer employeeId);

	@Query("SELECT st FROM ShiftTime st " + "JOIN st.shiftId s " + "JOIN EmployeeShift es ON es.shift = s "
			+ "WHERE es.employee.id = :employeeId AND st.dayIndex = :dayIndex")
	List<ShiftTime> findShiftsByEmployeeIdAndDate(@Param("employeeId") Integer employeeId,
			@Param("dayIndex") Integer dayIndex);

	@Query(value = "SELECT st1_0.shift_time_id,st1_0.day_index,st1_0.from_time,st1_0.shift_id,st1_0.to_time,st1_0.total_time "
			+ "FROM shift_time st1_0 " + "JOIN shift si1_0 ON si1_0.shift_id=st1_0.shift_id "
			+ "JOIN employee_shift es1_0 ON st1_0.shift_id=es1_0.shift_id "
			+ "JOIN employee e1_0 ON e1_0.employee_id=es1_0.employee_id "
			+ "WHERE e1_0.employee_id=:employeeId", nativeQuery = true)
	List<ShiftTime> findByEmployeeId(@Param("employeeId") Integer employeeId);

}
