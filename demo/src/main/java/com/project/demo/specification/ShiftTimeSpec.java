package com.project.demo.specification;

import org.springframework.data.jpa.domain.Specification;
import com.project.demo.entity.ShiftTime;

public class ShiftTimeSpec {

	public static Specification<ShiftTime> hasEmployee(Integer employeeId) {
		return (root, query, cb) -> {
			if (employeeId == null)
				return null;

			return cb.equal(root.get("shiftId").get("employeeShift").get("employee").get("employeeId"), employeeId);
		};
	}

	public static Specification<ShiftTime> isActive() {
		return (root, query, cb) -> {
			return cb.isTrue(root.get("shiftId").get("employeeShift").get("active"));
		};
	}

	public static Specification<ShiftTime> hasValidTimeRange() {
		return (root, query, cb) -> cb.and(cb.isNotNull(root.get("fromTime")), cb.isNotNull(root.get("toTime")));
	}
}