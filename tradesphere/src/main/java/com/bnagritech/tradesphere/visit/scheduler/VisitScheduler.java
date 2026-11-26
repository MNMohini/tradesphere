package com.bnagritech.tradesphere.visit.scheduler;

import com.bnagritech.tradesphere.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Needs @EnableScheduling on the application class or a config class. */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitScheduler {

    private final VisitService visitService;

    /**
     * Every night at 00:30 IST:
     *  - PLANNED visits from earlier days become MISSED
     *  - IN_PROGRESS visits from earlier days (the user never ended them) are closed
     */
    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Kolkata")
    public void markMissedVisits() {
        int changed = visitService.markOverduePlannedVisitsAsMissed();
        log.info("Nightly visit job: {} visits marked MISSED or closed", changed);
    }
}
