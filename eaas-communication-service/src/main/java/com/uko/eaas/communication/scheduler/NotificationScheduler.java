package com.uko.eaas.communication.scheduler;

import com.uko.eaas.communication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Process pending notifications every minute
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void processPendingNotifications() {
        log.debug("Processing pending notifications");
        try {
            notificationService.sendPendingNotifications();
        } catch (Exception e) {
            log.error("Error processing pending notifications: {}", e.getMessage(), e);
        }
    }
}
