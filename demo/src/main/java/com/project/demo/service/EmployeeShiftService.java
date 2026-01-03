package com.project.demo.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.demo.entity.Employee;
import com.project.demo.entity.EmployeeShift;
import com.project.demo.entity.Shift;
import com.project.demo.model.EmployeeShiftModel;
import com.project.demo.repo.EmployeeRepo;
import com.project.demo.repo.EmployeeShiftRepo;
import com.project.demo.specification.EmployeeShiftSpec;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeShiftService {

	@Autowired
	private EmployeeShiftRepo employeeshiftRepository;
	@Autowired
	private EmployeeRepo employeeRepository;

	public EmployeeShiftService(EmployeeShiftRepo employeeshiftRepository, EmployeeRepo employeeRepository) {
        this.employeeshiftRepository = employeeshiftRepository;
        this.employeeRepository = employeeRepository;
    }

	public List<EmployeeShift> getShiftsByIdAndFilters(Integer employeeId, Boolean active, Date startActiveDate,
	        Date endActiveDate, Integer companyId) {

	    return employeeshiftRepository.findAll(
	            EmployeeShiftSpec.filterShifts(employeeId, active, companyId, startActiveDate, endActiveDate)
	    );
	}

	public List<EmployeeShift> getEmployeeShiftIds(Integer employeeId) {
	    if (employeeId == null) {
	        throw new IllegalArgumentException("Employee ID cannot be null");
	    }
	    List<EmployeeShift> activeShifts = employeeshiftRepository.findAll(
	    	    EmployeeShiftSpec.hasEmployee(employeeId)
	    	        .and(EmployeeShiftSpec.isActive())
	    	);
	    return activeShifts;
	  
	}


	// create
	public EmployeeShift createShift(EmployeeShiftModel shiftModel) {
		if (shiftModel.getEmployeeId() == null) {
			throw new IllegalArgumentException("Employee ID must not be null");
		}

		if (shiftModel.getShiftId() == null) {
			throw new IllegalArgumentException("Shift ID must not be null");
		}

		if (shiftModel.getStartActiveDate() == null || shiftModel.getEndActiveDate() == null) {
			throw new IllegalArgumentException("Start and End dates must not be null");
		}

		if (shiftModel.getEndActiveDate().isBefore(shiftModel.getStartActiveDate())) {
			throw new IllegalArgumentException("End date cannot be before Start date");
		}

		EmployeeShift shift = new EmployeeShift();

		Employee employee = new Employee();
		employee.setEmployeeId(shiftModel.getEmployeeId());
		shift.setEmployee(employee);

		Shift shiftEntity = new Shift();
		shiftEntity.setShiftId(shiftModel.getShiftId());
		shift.setShift(shiftEntity);

		shift.setActive(shiftModel.getActive());
		shift.setStartActiveDate(shiftModel.getStartActiveDate());
		shift.setEndActiveDate(shiftModel.getEndActiveDate());

		return employeeshiftRepository.save(shift);
	}

	// find by id
	public EmployeeShift getShiftsById(Integer id) {
		return employeeshiftRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + id));
	}

	// update
	public EmployeeShift updateEmployeeShift(Integer id, EmployeeShift shiftDetails) {
		EmployeeShift shift = getEmployeeShiftById(id);

		if (shiftDetails.getEmployee() != null) {
			shift.setEmployee(shiftDetails.getEmployee());
		}
		if (shiftDetails.getShift() != null) {
			shift.setShift(shiftDetails.getShift());
		}
		if (shiftDetails.getActive() != null) {
			shift.setActive(shiftDetails.getActive());
		}

		if (shiftDetails.getStartActiveDate() != null) {
			if (shift.getEndActiveDate() != null
					&& shift.getEndActiveDate().isBefore(shiftDetails.getStartActiveDate())) {
				throw new IllegalArgumentException("Existing end date cannot be before new start date");
			}
			shift.setStartActiveDate(shiftDetails.getStartActiveDate());
		} else if (shift.getStartActiveDate() == null) {
			throw new IllegalArgumentException("Start date must not be null");
		}

		if (shiftDetails.getEndActiveDate() != null) {
			LocalDate startDate = shiftDetails.getStartActiveDate() != null ? shiftDetails.getStartActiveDate()
					: shift.getStartActiveDate();

			if (startDate != null && shiftDetails.getEndActiveDate().isBefore(startDate)) {
				throw new IllegalArgumentException("End date cannot be before start date");
			}
			shift.setEndActiveDate(shiftDetails.getEndActiveDate());
		} else if (shift.getEndActiveDate() == null) {
			throw new IllegalArgumentException("End date must not be null");
		}

		return employeeshiftRepository.save(shift);
	}

	public EmployeeShift getEmployeeShiftById(Integer id) {
		return employeeshiftRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Employee shift not found with id: " + id));
	}

	// delete by id
	public void deleteShifts(Integer id) {
		EmployeeShift shift = getEmployeeShiftById(id);
		employeeshiftRepository.delete(shift);
	}

	// new Update
	public List<Integer> getActiveShifts(Integer employeeId) {

	    List<EmployeeShift> shifts = employeeshiftRepository.findAll(
	            EmployeeShiftSpec.hasEmployee(employeeId)
	                    .and(EmployeeShiftSpec.isActive())
	    );

	    if (shifts.isEmpty()) {
	        throw new EntityNotFoundException(
	                "No active shifts found for employee with id: " + employeeId
	        );
	    }

	    return shifts.stream()
	            .map(es -> es.getShift().getShiftId())
	            .collect(Collectors.toList()); 
	}


	// Swap
	public EmployeeShift swapShift(EmployeeShiftModel shiftModel, Boolean temporary) {
		  if (temporary == null) {
		        throw new IllegalArgumentException("Temporary parameter cannot be null");
		    }
		LocalDate today = LocalDate.now();

		// Validation: end date of new shift
		if (shiftModel.getEndActiveDate().isBefore(today)) {
			throw new IllegalArgumentException("New shift end date cannot be before today");
		}

		EmployeeShift currentShift = employeeshiftRepository.findAll(
		        EmployeeShiftSpec.hasShiftAndEmployee(shiftModel.getShiftId(), shiftModel.getEmployeeId())
		).stream().findFirst()
		  .orElseThrow(() -> new IllegalStateException("Selected shift not found"));

		if (!currentShift.getEmployee().getEmployee().equals(shiftModel.getEmployeeId())) {
			throw new IllegalArgumentException("Selected shift does not belong to this employee");
		}

		if (shiftModel.getStartActiveDate().isBefore(today) && currentShift.getEndActiveDate().isAfter(today)) {

			currentShift.setEndActiveDate(today);
			employeeshiftRepository.save(currentShift);

			return createShift(shiftModel);
		}

		EmployeeShift newShift = convertToEmployeeShift(shiftModel);

		if (temporary && currentShift.getEndActiveDate().isAfter(newShift.getEndActiveDate())) {
			EmployeeShift afterTempShift = copyShift(currentShift);
			afterTempShift.setStartActiveDate(newShift.getEndActiveDate());
			afterTempShift.setEndActiveDate(currentShift.getEndActiveDate());
			employeeshiftRepository.save(afterTempShift);
		}

		// Case 2: new start < current end → adjust current shift
		if (newShift.getStartActiveDate().isBefore(currentShift.getEndActiveDate())) {
			currentShift.setEndActiveDate(newShift.getStartActiveDate());
			employeeshiftRepository.save(currentShift);
			employeeshiftRepository.save(newShift);
			return newShift;
		}

		// Case 3: new start >= current end → save new shift
		if (!newShift.getStartActiveDate().isBefore(currentShift.getEndActiveDate())) {
			employeeshiftRepository.save(newShift);
			return newShift;
		}

		throw new IllegalStateException("Shift swap conditions are not satisfied");
	}

	private EmployeeShift convertToEmployeeShift(EmployeeShiftModel model) {
		Employee employee = employeeRepository.findById(model.getEmployeeId())
				.orElseThrow(() -> new EntityNotFoundException("Employee not found"));

		EmployeeShift shift = new EmployeeShift();
		shift.setEmployee(employee);
		shift.setStartActiveDate(model.getStartActiveDate());
		shift.setEndActiveDate(model.getEndActiveDate());
		shift.setActive(true);

		return shift;
	}

	private EmployeeShift copyShift(EmployeeShift original) {
		EmployeeShift copy = new EmployeeShift();
		copy.setEmployee(original.getEmployee());
		copy.setStartActiveDate(original.getStartActiveDate());
		copy.setEndActiveDate(original.getEndActiveDate());
		copy.setActive(original.getActive());
		return copy;
	}

}