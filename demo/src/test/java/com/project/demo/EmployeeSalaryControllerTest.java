package com.project.demo;

import com.project.demo.controller.EmployeeSalaryController;
import com.project.demo.entity.EmployeeSalary;
import com.project.demo.service.EmployeeSalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSalaryControllerTest {

	@Mock
	private EmployeeSalaryService employeeSalaryService;

	@InjectMocks
	private EmployeeSalaryController employeeSalaryController;

	private EmployeeSalary employeeSalary;

	@BeforeEach
	void setUp() {
		employeeSalary = new EmployeeSalary();
		employeeSalary.setSalaryPaymentId(1);
		employeeSalary.setEmployeeId(100);
		employeeSalary.setYear(2024);
		employeeSalary.setMonth(12);
		employeeSalary.setSalaryAmountPaid(5000.0f);
		employeeSalary.setRewardReason("Test Reward");
		employeeSalary.setDiscountReason("Test Discount");
	}

	// ============ ADD DISCOUNT TESTS ============

	@Test
	void testAddDiscount_Success() {
		when(employeeSalaryService.addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString()))
				.thenReturn(employeeSalary);

		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, 500.0f, "Late arrival");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof EmployeeSalary);

		EmployeeSalary result = (EmployeeSalary) response.getBody();
		assertEquals(1, result.getSalaryPaymentId());
		assertEquals(100, result.getEmployee());
		assertEquals(2024, result.getYear());
		assertEquals(12, result.getMonth());
		assertEquals(5000.0f, result.getSalaryAmountPaid());

		verify(employeeSalaryService, times(1)).addSalaryDiscount(100, 2024, 12, 500.0f, "Late arrival");
	}

	@Test
	void testAddDiscount_InvalidAmount() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, 0.0f, "No discount");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Discount amount must be greater than 0"));

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_NegativeAmount() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, -100.0f, "Negative discount");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Discount amount must be greater than 0"));

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_NullReason() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, 500.0f, null);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Reason cannot be empty"));

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_EmptyReason() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, 500.0f, "");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Reason cannot be empty"));

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_InvalidYear() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 1999, 12, 500.0f, "Reason");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_InvalidMonth() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 13, 500.0f, "Reason");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddDiscount_ServiceThrowsException() {
		when(employeeSalaryService.addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString()))
				.thenThrow(new RuntimeException("Database error"));

		ResponseEntity<?> response = employeeSalaryController.addDiscount(100, 2024, 12, 500.0f, "Late arrival");

		assertNotNull(response);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Error adding salary discount"));

		verify(employeeSalaryService, times(1)).addSalaryDiscount(100, 2024, 12, 500.0f, "Late arrival");
	}

	// ============ ADD REWARD TESTS ============

	@Test
	void testAddReward_Success() {
		when(employeeSalaryService.addSalaryReward(anyInt(), anyInt(), anyInt(), anyFloat(), anyString()))
				.thenReturn(employeeSalary);

		ResponseEntity<?> response = employeeSalaryController.addReward(100, 2024, 12, 1000.0f,
				"Employee of the month");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof EmployeeSalary);

		EmployeeSalary result = (EmployeeSalary) response.getBody();
		assertEquals(1, result.getSalaryPaymentId());
		assertEquals("Test Reward", result.getRewardReason());

		verify(employeeSalaryService, times(1)).addSalaryReward(100, 2024, 12, 1000.0f, "Employee of the month");
	}

	@Test
	void testAddReward_InvalidMonth() {
		ResponseEntity<?> response = employeeSalaryController.addReward(100, 2024, 13, 1000.0f, "Invalid month");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).addSalaryReward(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddReward_InvalidYear() {
		ResponseEntity<?> response = employeeSalaryController.addReward(100, 1999, 12, 1000.0f, "Invalid year");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).addSalaryReward(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddReward_ZeroAmount() {
		ResponseEntity<?> response = employeeSalaryController.addReward(100, 2024, 12, 0.0f, "No reward");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Reward amount must be greater than 0"));

		verify(employeeSalaryService, never()).addSalaryReward(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	// ============ ADD INCENTIVE TESTS ============

	@Test
	void testAddIncentive_Success() {
		when(employeeSalaryService.addSalaryIncentive(anyInt(), anyInt(), anyInt(), anyFloat(), anyString()))
				.thenReturn(employeeSalary);

		ResponseEntity<?> response = employeeSalaryController.addIncentive(100, 2024, 12, 1500.0f,
				"Sales target achieved");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof EmployeeSalary);

		EmployeeSalary result = (EmployeeSalary) response.getBody();
		assertEquals(1, result.getSalaryPaymentId());
		assertEquals(5000.0f, result.getSalaryAmountPaid());

		verify(employeeSalaryService, times(1)).addSalaryIncentive(100, 2024, 12, 1500.0f, "Sales target achieved");
	}

	@Test
	void testAddIncentive_ZeroAmount() {
		ResponseEntity<?> response = employeeSalaryController.addIncentive(100, 2024, 12, 0.0f, "No incentive");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Incentive amount must be greater than 0"));

		verify(employeeSalaryService, never()).addSalaryIncentive(anyInt(), anyInt(), anyInt(), anyFloat(),
				anyString());
	}

	// ============ PAY SALARY TESTS ============

	@Test
	void testPaySalary_Success() {
		when(employeeSalaryService.paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat()))
				.thenReturn(employeeSalary);

		ResponseEntity<?> response = employeeSalaryController.paySalary(100, 2024, 12, 5000.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof EmployeeSalary);

		EmployeeSalary result = (EmployeeSalary) response.getBody();
		assertEquals(5000.0f, result.getSalaryAmountPaid());

		verify(employeeSalaryService, times(1)).paySalaryDirect(100, 2024, 12, 5000.0f);
	}

	@Test
	void testPaySalary_ZeroAmount() {
		ResponseEntity<?> response = employeeSalaryController.paySalary(100, 2024, 12, 0.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Salary amount must be greater than 0"));

		verify(employeeSalaryService, never()).paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testPaySalary_NegativeAmount() {
		ResponseEntity<?> response = employeeSalaryController.paySalary(100, 2024, 12, -1000.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Salary amount must be greater than 0"));

		verify(employeeSalaryService, never()).paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testPaySalary_InvalidMonth() {
		ResponseEntity<?> response = employeeSalaryController.paySalary(100, 2024, 13, 5000.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testPaySalary_ServiceThrowsException() {
		when(employeeSalaryService.paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat()))
				.thenThrow(new RuntimeException("Database error"));

		ResponseEntity<?> response = employeeSalaryController.paySalary(100, 2024, 12, 5000.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Error paying salary"));

		verify(employeeSalaryService, times(1)).paySalaryDirect(100, 2024, 12, 5000.0f);
	}

	// ============ UPDATE FINAL SALARY TESTS ============

	@Test
	void testUpdateFinalSalary_Success() {
		when(employeeSalaryService.addFinalSalary(anyInt(), anyInt(), anyInt(), anyFloat())).thenReturn(employeeSalary);

		ResponseEntity<?> response = employeeSalaryController.updateFinalSalary(100, 2024, 12, 5500.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof EmployeeSalary);

		EmployeeSalary result = (EmployeeSalary) response.getBody();
		assertEquals(5000.0f, result.getSalaryAmountPaid());

		verify(employeeSalaryService, times(1)).addFinalSalary(100, 2024, 12, 5500.0f);
	}

	@Test
	void testUpdateFinalSalary_InvalidMonth() {
		ResponseEntity<?> response = employeeSalaryController.updateFinalSalary(100, 2024, 0, 5500.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).addFinalSalary(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testUpdateFinalSalary_NegativeSalary() {
		ResponseEntity<?> response = employeeSalaryController.updateFinalSalary(100, 2024, 12, -100.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Final salary cannot be negative"));

		verify(employeeSalaryService, never()).addFinalSalary(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testUpdateFinalSalary_ServiceThrowsException() {
		when(employeeSalaryService.addFinalSalary(anyInt(), anyInt(), anyInt(), anyFloat()))
				.thenThrow(new RuntimeException("Database error"));

		ResponseEntity<?> response = employeeSalaryController.updateFinalSalary(100, 2024, 12, 5500.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Error updating final salary"));

		verify(employeeSalaryService, times(1)).addFinalSalary(100, 2024, 12, 5500.0f);
	}

	// ============ NULL PARAMETERS TESTS ============

	@Test
	void testAddDiscount_NullEmployeeId() {
		ResponseEntity<?> response = employeeSalaryController.addDiscount(null, 2024, 12, 500.0f, "Reason");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Employee ID cannot be null"));

		verify(employeeSalaryService, never()).addSalaryDiscount(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testAddReward_NullAmount() {
		ResponseEntity<?> response = employeeSalaryController.addReward(100, 2024, 12, null, "Reason");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Amount cannot be null"));

		verify(employeeSalaryService, never()).addSalaryReward(anyInt(), anyInt(), anyInt(), anyFloat(), anyString());
	}

	@Test
	void testPaySalary_NullYear() {
		ResponseEntity<?> response = employeeSalaryController.paySalary(100, null, 12, 5000.0f);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		verify(employeeSalaryService, never()).paySalaryDirect(anyInt(), anyInt(), anyInt(), anyFloat());
	}

	@Test
	void testUpdateFinalSalary_NullSalary() {
		ResponseEntity<?> response = employeeSalaryController.updateFinalSalary(100, 2024, 12, null);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody() instanceof java.util.Map);

		@SuppressWarnings("unchecked")
		java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
		assertTrue(body.get("message").contains("Final salary cannot be null"));

		verify(employeeSalaryService, never()).addFinalSalary(anyInt(), anyInt(), anyInt(), anyFloat());
	}
}