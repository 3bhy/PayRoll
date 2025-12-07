package com.project.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.project.demo.entity.Company;
import com.project.demo.entity.Employee;
import com.project.demo.entity.Person;
import com.project.demo.entity.Profile;
import com.project.demo.model.EmployeeModel;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.specification.EmployeeSpec;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeService {
	@Autowired
	private EmployeeRepo employeeRepository;

	public EmployeeService(EmployeeRepo employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

//Filter by specification
	public List<Employee> getEmployeesByFilters(Integer companyId, Integer personId, Integer managerId) {
		if (companyId == null && personId == null && managerId == null) {
			System.out.println("Enter atleast 1");
			return new ArrayList<>();
		}

		Specification<Employee> specification = Specification.unrestricted();

		specification = specification.and(EmployeeSpec.hasCompany(companyId));
		specification = specification.and(EmployeeSpec.hasPerson(personId));
		specification = specification.and(EmployeeSpec.hasManager(managerId));

		return employeeRepository.findAll(specification);
	}

	// create
	public Employee createEmployee(EmployeeModel employeeModel) {

		if (employeeModel.getSalary() != null && employeeModel.getSalary() < 0) {
			throw new IllegalArgumentException("Salary cannot be negative");

		}
//mapper
		Employee employee = new Employee();

		Company company = new Company();
		company.setCompanyId(employeeModel.getCompanyId());
		employee.setCompany(company);

		Person person = new Person();
		person.setPersonId(employeeModel.getPersonId());
		employee.setPerson(person);

		if (employeeModel.getProfileId() != null) {
			Profile profile = new Profile();
			profile.setProfileId(employeeModel.getProfileId());
			employee.setProfile(profile);
		}

		if (employeeModel.getManagerId() != null) {
			Employee manager = new Employee();
			manager.setEmployeeId(employeeModel.getManagerId());
			employee.setManagerId(employeeModel.getManagerId());
		}

		employee.setSalary(employeeModel.getSalary());
		employee.setSalaryCycle(employeeModel.getSalaryCycle());
		employee.setSalesIncentivePercent(employeeModel.getSalesIncentivePercent());
		employee.setIncentiveOnAllSales(employeeModel.getIncentiveOnAllSales());

		Employee savedEmployee = employeeRepository.saveAndFlush(employee);

		return savedEmployee;
	}

	// find by id
	public Employee getEmployeeById(Integer id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
	}

	// update
	public Employee updateEmployee(Integer id, Employee employeeDetails) {
	    Employee employee = getEmployeeById(id);

	    if (employeeDetails.getCompany() != null) {
	        employee.setCompany(employeeDetails.getCompany());
	    }
	    if (employeeDetails.getPerson() != null) {
	        employee.setPerson(employeeDetails.getPerson());
	    }
	    if (employeeDetails.getManagerId() != null) {
	        employee.setManagerId(employeeDetails.getManagerId());
	    }
	    
	    if (employeeDetails.getSalary() != null) {
	        if (employeeDetails.getSalary() < 0) {
	            throw new IllegalArgumentException("Salary cannot be negative");
	        }
	        employee.setSalary(employeeDetails.getSalary());
	    }
	    
	    if (employeeDetails.getIncentiveOnAllSales() != null) {
	        employee.setIncentiveOnAllSales(employeeDetails.getIncentiveOnAllSales());
	    }
	    
	    if (employeeDetails.getProfile() != null) {
	        employee.setProfile(employeeDetails.getProfile());
	    }
	    if (employeeDetails.getSalaryCycle() != null) {
	        employee.setSalaryCycle(employeeDetails.getSalaryCycle());
	    }
	    if (employeeDetails.getSalesIncentivePercent() != null) {
	        employee.setSalesIncentivePercent(employeeDetails.getSalesIncentivePercent());
	    }

	    return employeeRepository.save(employee);
	}

	// delete by id
	public void deleteEmployee(Integer id) {
		Employee employee = getEmployeeById(id);
		employeeRepository.delete(employee);
	}

}
