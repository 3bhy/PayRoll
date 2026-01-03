package com.project.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.stereotype.Repository;

import com.project.demo.entity.Shift;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, Integer>, JpaSpecificationExecutor<Shift> {

//	@Query("SELECT s FROM Shift s WHERE s.company.companyId = :companyId")
//	List<Shift> findByCompanyCompanyId(@Param("companyId") Integer companyId);

}