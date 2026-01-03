package com.project.demo.specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.demo.entity.Shift;

public class ShiftSpec {
	public static Specification<Shift> byCompanyId(Integer companyId) {
        return (root, query, cb) -> companyId == null ? null
                : cb.equal(root.get("company").get("companyId"), companyId);
    }
}
