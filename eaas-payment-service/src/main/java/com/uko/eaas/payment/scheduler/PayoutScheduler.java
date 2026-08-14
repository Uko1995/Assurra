package com.uko.eaas.payment.scheduler;

import com.uko.eaas.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PayoutScheduler {

    private final PayoutService payoutService;

    @Scheduled(fixedRate = 300000)
    @SchedulerLock(name = "processScheduledPayouts", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void processScheduledPayouts() {
        log.debug("Running scheduled payout processor");
        try {
            payoutService.processScheduledPayouts();
        } catch (Exception e) {
            log.error("Error in payout scheduler: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 900000)
    @SchedulerLock(name = "retryFailedPayouts", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void retryFailedPayouts() {
        log.debug("Running failed payout retry processor");
        try {
            payoutService.retryFailedPayouts();
        } catch (Exception e) {
            log.error("Error in payout retry scheduler: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 600000)
    @SchedulerLock(name = "recoverUnknownPayouts", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void recoverUnknownPayouts() {
        log.debug("Running UNKNOWN payout recovery processor");
        try {
            payoutService.recoverUnknownPayouts();
        } catch (Exception e) {
            log.error("Error in UNKNOWN payout recovery scheduler: {}", e.getMessage(), e);
        }
    }
}
