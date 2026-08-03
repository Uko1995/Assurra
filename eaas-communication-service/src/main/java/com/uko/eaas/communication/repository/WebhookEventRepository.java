package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    List<WebhookEvent> findByStatusAndNextAttemptAtBefore(String status, LocalDateTime now);
}
