package com.project.demo.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo.entity.Sales;

@Repository
public interface SalesRepo extends JpaRepository<Sales, Integer> {

	@Query("""
			SELECT COALESCE(SUM(s.saleAmount), 0)
			FROM Sales s
			WHERE s.saleDate >= :start
			  AND s.saleDate <= :end
			""")
	Float calculateAllSalesDuringShiftTime(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query(value = """
			SELECT COALESCE(SUM(s.sale_amount), 0)
			FROM Sales s
			WHERE s.employee_id = :employeeId
			  AND s.sale_date >= :start
			  AND s.sale_date <= :end
			""", nativeQuery = true)
	Float calculateEmployeeSalesDuringShiftTime(@Param("employeeId") Integer employeeId,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}