package com.uko.eaas.escrow.scheduler;

import com.uko.eaas.escrow.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoReleaseScheduler {

    private final EscrowService escrowService;

    @Scheduled(fixedRate = 300000)
    @SchedulerLock(name = "autoReleaseExpiredEscrows", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void autoReleaseExpiredEscrows() {
        log.debug("Running auto-release scheduler");
        try {
            escrowService.autoReleaseEscrows();
        } catch (Exception e) {
            log.error("Error in auto-release scheduler: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    @SchedulerLock(name = "expireUnfundedEscrows", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void expireUnfundedEscrows() {
        log.debug("Running unfunded escrow expiration scheduler");
        try {
            escrowService.expireUnfundedEscrows();
        } catch (Exception e) {
            log.error("Error in unfunded escrow expiration scheduler: {}", e.getMessage(), e);
        }
    }
}
