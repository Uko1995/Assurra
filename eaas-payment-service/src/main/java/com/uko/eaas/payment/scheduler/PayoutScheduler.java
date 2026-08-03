package com.uko.eaas.payment.scheduler;

import com.uko.eaas.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PayoutScheduler {

    private final PayoutService payoutService;

    /**
     * Process scheduled payouts every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processScheduledPayouts() {
        log.debug("Running scheduled payout processor");
        try {
            payoutService.processScheduledPayouts();
        } catch (Exception e) {
            log.error("Error in payout scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Retry failed payouts every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void retryFailedPayouts() {
        log.debug("Running failed payout retry processor");
        try {
            payoutService.retryFailedPayouts();
        } catch (Exception e) {
            log.error("Error in payout retry scheduler: {}", e.getMessage(), e);
        }
    }
}
