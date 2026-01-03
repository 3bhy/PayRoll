package com.project.demo.specification;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.EmployeeShift;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;

public class EmployeeShiftSpec {
	@PersistenceContext
    private EntityManager entityManager; 
	public static Specification<EmployeeShift> hasEmployee(Integer employeeId) {
	    return (root, query, cb) ->
	        employeeId == null ? null :
	        cb.equal(root.get("employee").get("employeeId"), employeeId);
	}

	public static Specification<EmployeeShift> isActive() {
	    return (root, query, cb) ->
	        cb.isTrue(root.get("active"));
	}
	
	public static Specification<EmployeeShift> filterShifts(
            Integer employeeId,
            Boolean active,
            Integer companyId,
            Date startActiveDate,
            Date endActiveDate) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (employeeId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("employee").get("employeeId"), employeeId));
            }

            if (active != null) {
                predicates = cb.and(predicates, cb.equal(root.get("active"), active));
            }

            if (companyId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("employee").get("company").get("companyId"), companyId));
            }

            if (startActiveDate != null && endActiveDate != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("startActiveDate"), startActiveDate),
                        cb.lessThanOrEqualTo(root.get("endActiveDate"), endActiveDate));
            } else if (startActiveDate != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("startActiveDate"), startActiveDate));
            } else if (endActiveDate != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("endActiveDate"), endActiveDate));
            }

            return predicates;
        };
    }
	
	
	 public static Specification<EmployeeShift> hasShiftAndEmployee(Integer shiftId, Integer employeeId) {
	        return (root, query, cb) -> {
	            if (shiftId == null && employeeId == null) return null;

	            if (shiftId != null && employeeId != null) {
	                return cb.and(
	                        cb.equal(root.get("shift").get("shiftId"), shiftId),
	                        cb.equal(root.get("employee").get("employeeId"), employeeId)
	                );
	            } else if (shiftId != null) {
	                return cb.equal(root.get("shift").get("shiftId"), shiftId);
	            } else {
	                return cb.equal(root.get("employee").get("employeeId"), employeeId);
	            }
	        };
	    }
	 
	 public int activateCurrentShifts(LocalDate today) {
	     CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	     CriteriaUpdate<EmployeeShift> update = cb.createCriteriaUpdate(EmployeeShift.class);
	     Root<EmployeeShift> root = update.from(EmployeeShift.class);

	     update.set(root.get("active"), true);
	     update.where(
	         cb.and(
	             cb.lessThanOrEqualTo(root.get("startActiveDate"), today),
	             cb.greaterThanOrEqualTo(root.get("endActiveDate"), today),
	             cb.isFalse(root.get("active"))
	         )
	     );

	     return entityManager.createQuery(update).executeUpdate();
	 }

	 public int deactivateExpiredShifts(LocalDate today) {
	        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	        CriteriaUpdate<EmployeeShift> update = cb.createCriteriaUpdate(EmployeeShift.class);
	        Root<EmployeeShift> root = update.from(EmployeeShift.class);

	        update.set(root.get("active"), false);
	        update.where(
	            cb.and(
	                cb.or(
	                    cb.lessThan(root.get("endActiveDate"), today),
	                    cb.greaterThan(root.get("startActiveDate"), today)
	                ),
	                cb.isTrue(root.get("active"))
	            )
	        );

	        return entityManager.createQuery(update).executeUpdate();
	    }
}

