package com.project.demo.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.demo.entity.Employee;
import com.project.demo.model.EmployeeModel;
import com.project.demo.service.EmployeeService;

@RestController
@RequestMapping("/api")
public class EmployeeController {
    
    @Autowired
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    // get by id
    @GetMapping("/getEmployeeBy/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.ok().body(null); 
            }
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }

    // filters
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> getEmployeeByFilters(@RequestParam(required = false) Integer company_id,
            @RequestParam(required = false) Integer person_id, @RequestParam(required = false) Integer manger_id) {

        try {
            List<Employee> employees = employeeService.getEmployeesByFilters(company_id, person_id, manger_id);
            
            if (employees == null || employees.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }


    // create
    @PostMapping("/addEmployee")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeModel employeeModel) {
        try {
            if (employeeModel == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Employee data cannot be null"));
            }
            
            System.out.println("Creating employee: " + employeeModel.toString());
            Employee createdEmployee = employeeService.createEmployee(employeeModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating employee: " + e.getMessage()));
        }
    }

    // update
    @PutMapping("/updateBy/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Integer id, @RequestBody Employee employeeDetails) {
        try {
            if (employeeDetails == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Employee details cannot be null"));
            }
            
            Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating employee: " + e.getMessage()));
        }
    }

    // delete
    @DeleteMapping("/deletBy/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting employee: " + e.getMessage()));
        }
    }
}