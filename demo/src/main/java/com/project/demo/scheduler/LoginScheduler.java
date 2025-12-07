package com.project.demo.scheduler;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class LoginScheduler {
	 @Autowired
	    private LoginSchedulerService loginSchedulerService;
	 
	@Scheduled(cron = "0 0 */12 * * ?")  
    public void scheduledLockOldLogins() {
        loginSchedulerService.lockOldLogins();
    }

}
