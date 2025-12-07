package com.project.demo.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.demo.entity.EmployeeShift;

@Repository
public interface EmployeeShiftRepo extends JpaRepository<EmployeeShift, Integer> {

	@Query(value = "SELECT shift_id FROM employeeShift WHERE employee_id = :employeeId AND active = true", nativeQuery = true)
	List<Integer> findActiveShiftIdsByEmployeeId(@Param("employeeId") Integer employeeId);

	@Query("SELECT es FROM EmployeeShift es " + "JOIN es.employee e "
			+ "WHERE (:employeeId IS NULL OR e.employeeId = :employeeId) "
			+ "AND (:active IS NULL OR es.active = :active) " + "AND (:companyId IS NULL OR e.company.id = :companyId) "
			+ "AND ((:startActiveDate IS NULL AND :endActiveDate IS NULL) "
			+ "OR (:startActiveDate IS NOT NULL AND :endActiveDate IS NULL AND es.startActiveDate >= :startActiveDate) "
			+ "OR (:startActiveDate IS NULL AND :endActiveDate IS NOT NULL AND es.endActiveDate <= :endActiveDate) "
			+ "OR (:startActiveDate IS NOT NULL AND :endActiveDate IS NOT NULL AND es.startActiveDate >= :startActiveDate AND es.endActiveDate <= :endActiveDate))")
	List<EmployeeShift> findShiftsByFilters(@Param("employeeId") Integer employeeId, @Param("active") Boolean active,
			@Param("companyId") Integer companyId, @Param("startActiveDate") Date startActiveDate,
			@Param("endActiveDate") Date endActiveDate);

}