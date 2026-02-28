package com.example.user_service.config;

import com.example.user_service.service.JobService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class JobSchedule {
    private final JobService jobService;
    private final JobScheduler jobScheduler;

    @EventListener(ApplicationReadyEvent.class)
    public void registerPendingTs(){
        jobScheduler.scheduleRecurrently(
                "scan-pending-transaction",
                "0 */40 * * * *",
                ()-> jobService.jobAutoRetryPendingTransaction()
        );
        System.out.println("Job for scanning pending transaction registered");
    }
    @EventListener(ApplicationReadyEvent.class)
    public void registerDaliyLimit(){
        jobScheduler.scheduleRecurrently(
                "reset-daily-limit-transaction",
                "0 59 23 * * *",
                () -> jobService.jobResetDailyLimitTransaction()
        );
        System.out.println("Job for resetting daily limit transaction registered");
    }
//    @EventListener(ApplicationReadyEvent.class)
//    public void scheduleStatPerDay(){
//        jobScheduler.scheduleRecurrently(
//                "stat-transaction-per-day",
//                "0 59 23 * * *",
//                ()-> jobService.jobStatTransactionPerDay()
//        );
//    }
    public void schedulePendingTs(String requestId){
        jobScheduler.schedule(
                Instant.now().plusSeconds(120),
                ()-> jobService.jobHumanRetryPendingTransaction(requestId)
        );
    }
}
