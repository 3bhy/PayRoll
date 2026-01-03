package com.project.demo.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.demo.entity.Login;
import com.project.demo.repo.LoginRepo;
import com.project.demo.service.LoginService;
import com.project.demo.specification.LoginSpec;

@Component
public class LoginScheduler {

	@Autowired
	private LoginRepo loginRepo;

	@Autowired
	private LoginService loginService;

	@Scheduled(cron = "0 0 */12 * * ?")
	public void scheduledLockOldLogins() {
		lockOldLogins();
	}

	private void lockOldLogins() {

		try {
			// select all rows from login whose "locked" is false and loginDateTime is
			// before now with at least 24 hours
			LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
			List<Login> oldUnlockedLogins = loginRepo.findAll(LoginSpec.unlockedBefore(twentyFourHoursAgo));

			if (oldUnlockedLogins.isEmpty()) {
				System.out.println("No old unlocked logins found");
				return;
			}

			// Call lockLogin function
			for (Login login : oldUnlockedLogins) {
			    if (login.getEmployee() != null) {
			        Integer employeeId = login.getEmployee().getEmployeeId(); 
			        loginService.lockLogin(employeeId, oldUnlockedLogins);
			    }
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
