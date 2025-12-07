package com.project.demo.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.demo.entity.Login;
import com.project.demo.entity.ShiftTime;
import com.project.demo.model.LoginModel;
import com.project.demo.repo.LoginRepo;
import com.project.demo.service.LoginService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginRepo loginRepository;

    // Lock login
    @PutMapping("/lock/{employeeId}")
    public ResponseEntity<?> lockLogin(@PathVariable Integer employeeId) {
        try {
            Optional<Login> activeLogin = loginService.findActiveLoginWithinShift(employeeId);
            
            if (activeLogin.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No active login found for employee id: " + employeeId));
            }
            
            if (activeLogin.get().getLocked()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Login is already locked"));
            }
            
           
            loginService.lockLoginByEmployeeId(employeeId);
            return ResponseEntity.ok(Map.of("message", "Login locked successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error locking login: " + e.getMessage()));
        }
    }

    // CREATE
    @PostMapping("/addLogin")
    public ResponseEntity<?> createLogin(@RequestBody LoginModel loginModel) {
        try {
            Login login = loginService.createLoginIfWasActiveLogin(loginModel);
            LoginModel createdModel = loginService.convertToModel(login);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating login: " + e.getMessage()));
        }
    }

    // GET BY ID
    @GetMapping("/getLoginsBy/{id}")
    public ResponseEntity<?> getLoginById(@PathVariable Integer id) {
        try {
            Optional<Login> loginOptional = loginService.getLoginById(id);
            if (loginOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Login not found with id: " + id));
            }
            LoginModel loginModel = loginService.convertToModel(loginOptional.get());
            return ResponseEntity.ok(loginModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching login: " + e.getMessage()));
        }
    }

    // DELETE
    @DeleteMapping("/deleteLogins/{id}")
    public ResponseEntity<?> deleteLogin(@PathVariable Integer id) {
        try {
            Optional<Login> login = loginService.getLoginById(id);
            if (login.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Login not found with id: " + id));
            }
            
            loginService.deleteLogin(id);
            return ResponseEntity.ok(Map.of("message", "Login deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting login: " + e.getMessage()));
        }
    }

    // Filter by
    @GetMapping("/searchLogins")
    public ResponseEntity<List<LoginModel>> getLoginsByFilters(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime loginDateTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime logoutDateTime,
            @RequestParam(required = false) Boolean logoutStatus,
            @RequestParam(required = false) Boolean locked) {
        try {
            List<Login> logins = loginService.getLoginsByFilters(employeeId, loginDateTime, logoutDateTime, logoutStatus, locked);
            if (logins == null) logins = Collections.emptyList();
            List<LoginModel> loginModels = logins.stream().map(loginService::convertToModel).collect(Collectors.toList());
            return ResponseEntity.ok(loginModels);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList()); // بدل ما تضرب
        }
    }

    // Logout employee by loginID
    @PutMapping("/logoutByLoginId/{loginId}")
    public ResponseEntity<?> logoutByLoginId(@PathVariable Integer loginId) {
        try {
            Login login = loginService.logoutByLoginId(loginId);
            LoginModel loginModel = loginService.convertToModel(login);
            return ResponseEntity.ok(loginModel);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error during logout: " + e.getMessage()));
        }
    }

    // getOpenLogins
    @GetMapping("/openLogins")
    public ResponseEntity<List<LoginModel>> getOpenLogins() {
        try {
            List<Login> openLogins = loginService.getOpenLogins();
            if (openLogins == null) openLogins = Collections.emptyList();
            List<LoginModel> loginModels = openLogins.stream()
                    .map(loginService::convertToModel)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(loginModels);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // Logout by employeeId
    @PostMapping("/logout/by-employee")
    public ResponseEntity<?> logoutByEmployeeId(@RequestParam Integer employeeId) {
        try {
            Login activeLogin = loginRepository.findActiveLogins(employeeId).stream().findFirst().orElseThrow(() ->
                    new EntityNotFoundException("No active login found for employee id: " + employeeId));

            Integer shiftAttendanceId = null;
            if (activeLogin.getShiftTimeAttendanceId() != null) {
                shiftAttendanceId = activeLogin.getShiftTimeAttendanceId().getShiftTimeAttendanceId();
            }

            Login logout = loginService.processLogout(employeeId, shiftAttendanceId);
            return ResponseEntity.ok(logout);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }



    // findActiveLoginWithinShift
    @GetMapping("/active/{employeeId}")
    public ResponseEntity<?> getActiveLogin(@PathVariable Integer employeeId) {
        try {
            Optional<Login> activeLogin = loginService.findActiveLoginWithinShift(employeeId);
            if (activeLogin.isPresent()) {
                LoginModel loginModel = loginService.convertToModel(activeLogin.get());
                lockLogin(employeeId); // ممكن تتركه optional
                return ResponseEntity.ok(loginModel);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No active login found for employee"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching active login: " + e.getMessage()));
        }
    }

    @GetMapping("/getCurrentShift")
    public ResponseEntity<?> getCurrentShift(@RequestParam Integer employeeId) {
        try {
            ShiftTime shiftTime = loginService.getCurrentShiftTimeForEmployee(employeeId);
            if (shiftTime == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No shift found for employee"));
            }
            return ResponseEntity.ok(shiftTime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching current shift: " + e.getMessage()));
        }
    }

}
