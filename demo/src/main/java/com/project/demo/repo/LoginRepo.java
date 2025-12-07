package com.project.demo.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.demo.entity.Login;
import com.project.demo.entity.ShiftTime;
import com.project.demo.entity.ShiftTimeAttendance;

@Repository
public interface LoginRepo extends JpaRepository<Login, Integer> {

	@Query("SELECT l FROM Login l WHERE " + "(:employeeId IS NULL OR l.employee.employeeId = :employeeId) "
			+ "AND (:loginStart IS NULL OR l.loginDateTime >= :loginStart) "
			+ "AND (:loginEnd IS NULL OR l.loginDateTime <= :loginEnd) "
			+ "AND (:logoutStart IS NULL OR l.logoutDateTime >= :logoutStart) "
			+ "AND (:logoutEnd IS NULL OR l.logoutDateTime <= :logoutEnd) "
			+ "AND (:logoutStatus IS NULL OR l.logoutStatus = :logoutStatus) "
			+ "AND (:locked IS NULL OR l.locked = :locked)")
	List<Login> findLoginsByFilters(@Param("employeeId") Integer employeeId,
			@Param("loginStart") LocalDateTime loginStart, @Param("loginEnd") LocalDateTime loginEnd,
			@Param("logoutStart") LocalDateTime logoutStart, @Param("logoutEnd") LocalDateTime logoutEnd,
			@Param("logoutStatus") Boolean logoutStatus, @Param("locked") Boolean locked);

	@Query("SELECT l FROM Login l WHERE l.employee.employeeId = :employeeId AND l.locked = false AND l.logoutStatus = false")
	List<Login> findActiveLogins(@Param("employeeId") Integer employeeId);

	// logout query
	@Query("SELECT l FROM Login l WHERE l.locked = true AND l.logoutStatus = false")
	List<Login> findLockedLoginsWithOpenLogout();

	@Query("SELECT l FROM Login l WHERE l.loginId = :loginId AND l.locked = false AND l.logoutStatus = false")
	Optional<Login> findActiveLoginById(@Param("loginId") Integer loginId);

	// scheduler query
	@Query("SELECT l FROM Login l WHERE l.locked = false AND l.loginDateTime <= :twentyFourHoursAgo")
	List<Login> findUnlockedLoginsBefore24Hours(@Param("twentyFourHoursAgo") LocalDateTime twentyFourHoursAgo);

	// This is updates
	// Select shift_time of the employee where now is between its (fromTime - 1) and
	// toTime
	@Query(value = "SELECT st.* FROM shift_time st " + "JOIN shift s ON s.shift_id = st.shift_id "
			+ "JOIN employee_shift es ON es.shift_id = s.shift_id " + "WHERE es.employee_id = :employeeId "
			+ "AND es.active = TRUE " + "AND CURTIME() BETWEEN SUBTIME(st.from_time, '01:00:00') AND st.to_time "
			+ "LIMIT 1", nativeQuery = true)
	Optional<ShiftTime> findCurrentShiftTimeForEmployee(@Param("employeeId") Integer employeeId);
	// Select login of an employee whose “locked” is false and logoutStatus is false
	// and within the selected shift_time.

	@Query(value = "SELECT l.* FROM login l " + "JOIN shift_time st ON l.shift_time_id = st.shift_time_id "
			+ "WHERE l.employee_id = :employeeId " + "AND l.locked = false " + "AND l.logout_status = false "
			+ "AND CURTIME() BETWEEN st.from_time AND st.to_time " + "AND st.day_index = DAYOFWEEK(CURDATE()) "
			+ "ORDER BY l.login_date_time DESC " + "LIMIT 1", nativeQuery = true)

	Optional<Login> findActiveLoginWithinShift(@Param("employeeId") Integer employeeId);

	// Select shift_time_attendance of today of the employee.
	@Query("SELECT sta FROM ShiftTimeAttendance sta " + "WHERE sta.employee.employeeId = :employeeId "
			+ "AND sta.attendanceDate = CURRENT_DATE")
	Optional<ShiftTimeAttendance> findTodayAttendanceByEmployee(@Param("employeeId") Integer employeeId);
	
    @Query(value = "SELECT SUM(TIME_TO_SEC(activity_time)) FROM login WHERE employee_id = :employeeId AND DATE(login_date_time) = :date", nativeQuery = true)
    Long sumActivityTimeByEmployeeAndDateNative(@Param("employeeId") Integer employeeId, @Param("date") LocalDate date);

}
