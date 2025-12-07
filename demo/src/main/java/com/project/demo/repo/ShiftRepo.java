package com.project.demo.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.demo.entity.Shift;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, Integer> {

	List<Shift> findAll();

	@Query("SELECT s FROM Shift s WHERE s.company.companyId = :companyId")
	List<Shift> findByCompanyCompanyId(@Param("companyId") Integer companyId);
	
	
	

}