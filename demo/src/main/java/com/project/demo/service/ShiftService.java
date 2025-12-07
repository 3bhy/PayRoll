package com.project.demo.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.demo.entity.Company;
import com.project.demo.entity.Shift;
import com.project.demo.entity.ShiftTime;
import com.project.demo.model.ShiftModel;
import com.project.demo.model.ShiftTimeModel;
import com.project.demo.repo.CompanyRepo;
import com.project.demo.repo.ShiftRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ShiftService {

	@Autowired
	private ShiftRepo shiftRepository;

	@Autowired
	private CompanyRepo companyRepository;

	public ShiftService(ShiftRepo shiftRepository) {
		this.shiftRepository = shiftRepository;
        this.companyRepository = companyRepository;

	}

	// CREATE
	public Shift createShift(ShiftModel shiftModel) {
		Shift shift = new Shift();

		if (shiftModel.getCompany() != null) {
			Company company = companyRepository.findById(shiftModel.getCompany())
					.orElseThrow(() -> new EntityNotFoundException("Company not found"));
			shift.setCompany(company);
		}

		shift.setShiftName(shiftModel.getShiftName());

		Shift savedShift = shiftRepository.save(shift);

		if (shiftModel.getShiftTimes() != null) {
			for (ShiftTimeModel timeModel : shiftModel.getShiftTimes()) {
				ShiftTime shiftTime = new ShiftTime();
				shiftTime.setShiftId(savedShift);
				shiftTime.setDayIndex(timeModel.getDayIndex());
				shiftTime.setFromTime(timeModel.getFromTime());
				shiftTime.setToTime(timeModel.getToTime());
				shiftTime.setTotalTime(timeModel.getTotalTime());

				savedShift.getShiftTimes().add(shiftTime);
			}
			return shiftRepository.save(savedShift);
		}

		return savedShift;
	}

	// get By ID
	public Shift getShiftById(Integer id) {
		Shift shift = shiftRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + id));
		return shift;
	}

	// Filter by companyId
	public List<Shift> getShiftsByCompanyId(Integer companyId) {
		List<Shift> shifts;
		if (companyId != null) {
			shifts = shiftRepository.findByCompanyCompanyId(companyId);
		} else {
			shifts = shiftRepository.findAll();
		}
		return shifts;
	}

	// UPDATE
	public Shift updateShift(Integer id, Shift shiftDetails) {
		Shift shift = getShiftById(id);

		if (shiftDetails.getCompany() != null) {
			shift.setCompany(shiftDetails.getCompany());
		}
		if (shiftDetails.getShiftName() != null) {
			shift.setShiftName(shiftDetails.getShiftName());
		}

		if (shiftDetails.getShiftTimes() != null && !shiftDetails.getShiftTimes().isEmpty()) {
			// update shift time
			shift.getShiftTimes().clear();

			for (ShiftTime shiftTime : shiftDetails.getShiftTimes()) {
				shiftTime.setShiftId(shift);
				shift.getShiftTimes().add(shiftTime);
			}
		}

		return shiftRepository.save(shift);
	}

	// DELETE
	public void deleteShift(Integer id) {
		Shift shift = getShiftById(id);
		shiftRepository.delete(shift);
	}

}