package com.project.demo.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.Sales;

@Repository
public interface SalesRepo extends JpaRepository<Sales, Integer> {
	// calculate present to employee
	@Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM Sales s WHERE s.employee.employeeId = :employeeId AND DATE(s.saleDate) = :date")
	Float calculateTotalSalesByEmployeeAndDate(@Param("employeeId") Integer employeeId, @Param("date") LocalDate date);

	// calculateAllSalesDuringShiftTime(shiftStart, shiftEnd);
	@Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM Sales s WHERE s.saleDate BETWEEN :shiftStart AND :shiftEnd")
	Float calculateAllSalesDuringShiftTime(@Param("shiftStart") LocalDateTime shiftStart,
			@Param("shiftEnd") LocalDateTime shiftEnd);

	@Query("SELECT SUM(s.saleAmount) FROM Sales s WHERE s.employee.employeeId = :employeeId AND YEAR(s.saleDate) = :year AND MONTH(s.saleDate) = :month")
	Float calculateTotalSalesByEmployeeAndMonth(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month);

	@Query("SELECT COUNT(s) FROM Sales s WHERE s.employee.employeeId = :employeeId AND YEAR(s.saleDate) = :year AND MONTH(s.saleDate) = :month")
	Long countByEmployeeAndMonth(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month);

	@Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM Sales s WHERE s.employee.employeeId = :employeeId "
			+ "AND YEAR(s.saleDate) = :year AND MONTH(s.saleDate) = :month "
			+ "AND FUNCTION('TIME', s.saleDate) BETWEEN :shiftStart AND :shiftEnd")
	Float calculateTotalSalesDuringShift(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month, @Param("shiftStart") LocalTime shiftStart,
			@Param("shiftEnd") LocalTime shiftEnd);

	@Query(value = "SELECT COALESCE(SUM(sale_amount), 0) FROM sales WHERE employee_id = :employeeId AND YEAR(sale_date) = :year AND MONTH(sale_date) = :month", nativeQuery = true)
	Float calculateEmployeeSalesByMonth(@Param("employeeId") Integer employeeId, @Param("year") Integer year,
			@Param("month") Integer month);

	@Query(value = "SELECT COALESCE(SUM(sale_amount), 0) FROM sales WHERE YEAR(sale_date) = :year AND MONTH(sale_date) = :month", nativeQuery = true)
	Float calculateTotalSalesByMonth(@Param("year") Integer year, @Param("month") Integer month);

	@Query(value = "SELECT COALESCE(SUM(sale_amount), 0) FROM sales "
			+ "WHERE YEAR(sale_date) = :year AND MONTH(sale_date) = :month "
			+ "AND TIME(sale_date) BETWEEN :shiftStart AND :shiftEnd", nativeQuery = true)
	Float calculateAllSalesDuringShiftHours(@Param("year") Integer year, @Param("month") Integer month,
			@Param("shiftStart") String shiftStart, @Param("shiftEnd") String shiftEnd);

}