package com.project.demo.specification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;
import com.project.demo.entity.Login;
import com.project.demo.entity.Employee;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class LoginSpec {

    public static Specification<Login> hasEmployee(Integer employeeId) {
        return (root, query, cb) -> employeeId == null ? cb.conjunction()
                : cb.equal(root.get("employee").get("employeeId"), employeeId);  
    }

    public static Specification<Login> loginAfter(LocalDateTime loginStart) {
        return (root, query, cb) -> loginStart == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("loginDateTime"), loginStart);
    }

    public static Specification<Login> loginBefore(LocalDateTime loginEnd) {
        return (root, query, cb) -> loginEnd == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("loginDateTime"), loginEnd);
    }

    public static Specification<Login> logoutAfter(LocalDateTime logoutStart) {
        return (root, query, cb) -> logoutStart == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("logoutDateTime"), logoutStart);
    }

    public static Specification<Login> logoutBefore(LocalDateTime logoutEnd) {
        return (root, query, cb) -> logoutEnd == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("logoutDateTime"), logoutEnd);
    }

    public static Specification<Login> hasLogoutStatus(Boolean logoutStatus) {
        return (root, query, cb) -> logoutStatus == null ? cb.conjunction()
                : cb.equal(root.get("logoutStatus"), logoutStatus);
    }

    public static Specification<Login> isLocked(Boolean locked) {
        return (root, query, cb) -> locked == null ? cb.conjunction()
                : cb.equal(root.get("locked"), locked);
    }
    
    public static Specification<Login> activeLoginsForEmployee(Integer employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) {
                return cb.conjunction();
            }
            
            return cb.and(
                cb.equal(root.get("employee").get("employeeId"), employeeId),
                cb.isFalse(root.get("locked")),
                cb.isFalse(root.get("logoutStatus"))
            );
        };
    }
    
    public static Specification<Login> lockedWithOpenLogout() {
        return (root, query, cb) -> cb.and(
            cb.isTrue(root.get("locked")),
            cb.isFalse(root.get("logoutStatus"))
        );
    }
    
    public static Specification<Login> activeLoginById(Integer loginId) {
        return (root, query, cb) -> {
            if (loginId == null) return cb.conjunction();
            
            root.fetch("employee", JoinType.LEFT);
            query.distinct(true);
            
            return cb.and(
                cb.equal(root.get("loginId"), loginId),
                cb.isFalse(root.get("locked")),
                cb.isFalse(root.get("logoutStatus"))
            );
        };
    }

    public static Specification<Login> unlockedBefore(LocalDateTime twentyFourHoursAgo) {
        return (root, query, cb) -> {
            if (twentyFourHoursAgo == null) return cb.conjunction();
            
            root.fetch("employee", JoinType.LEFT);
            query.distinct(true);
            
            return cb.and(
                cb.isFalse(root.get("locked")),
                cb.lessThanOrEqualTo(root.get("loginDateTime"), twentyFourHoursAgo)
            );
        };
    }
    
    public static Specification<Login> activeLoginWithinShift(
            Integer employeeId, LocalTime currentTime, DayOfWeek currentDay) {

        return (root, query, cb) -> {
            if (employeeId == null || currentTime == null || currentDay == null) {
                return cb.conjunction();
            }

           
            
            root.fetch("employee", JoinType.LEFT);
            query.distinct(true);
            
            var shiftJoin = root.join("shiftTimeId", JoinType.LEFT);

            return cb.and(
                cb.equal(root.get("employee").get("employeeId"), employeeId),  // ← get("employeeId")
                cb.isFalse(root.get("locked")),
                cb.isFalse(root.get("logoutStatus")),
                cb.lessThanOrEqualTo(shiftJoin.get("fromTime"), currentTime),
                cb.greaterThanOrEqualTo(shiftJoin.get("toTime"), currentTime),
                cb.equal(shiftJoin.get("dayIndex"), currentDay.getValue())
            );
        };
    }
    
    public static Specification<Login> byEmployeeAndDate(Integer employeeId, LocalDate date) {
        return (root, query, cb) -> {
            if (employeeId == null || date == null) return cb.conjunction();

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            root.fetch("employee", JoinType.LEFT);
            query.distinct(true);

            return cb.and(
                cb.equal(root.get("employee").get("employeeId"), employeeId), 
                cb.between(root.get("loginDateTime"), startOfDay, endOfDay)
            );
        };
    }
}