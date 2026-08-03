package com.uko.eaas.escrow.scheduler;

import com.uko.eaas.escrow.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class    AutoReleaseScheduler {

    private final EscrowService escrowService;

    /**
     * Run every 5 minutes to auto-release escrows that have passed their confirmation deadline
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void autoReleaseExpiredEscrows() {
        log.debug("Running auto-release scheduler");
        try {
            escrowService.autoReleaseEscrows();
        } catch (Exception e) {
            log.error("Error in auto-release scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Run every hour to cancel escrows that have expired without payment
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void expireUnfundedEscrows() {
        log.debug("Running unfunded escrow expiration scheduler");
        try {
            escrowService.expireUnfundedEscrows();
        } catch (Exception e) {
            log.error("Error in unfunded escrow expiration scheduler: {}", e.getMessage(), e);
        }
    }
}
