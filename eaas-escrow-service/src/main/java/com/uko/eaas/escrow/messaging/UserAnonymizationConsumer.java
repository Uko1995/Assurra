package com.uko.eaas.escrow.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.escrow.model.entity.EscrowStateHistory;
import com.uko.eaas.escrow.model.entity.EscrowTransaction;
import com.uko.eaas.escrow.model.enums.TriggeredBy;
import com.uko.eaas.escrow.repository.EscrowStateHistoryRepository;
import com.uko.eaas.escrow.repository.EscrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAnonymizationConsumer {

    private static final UUID ANONYMIZED_SENTINEL = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int BATCH_SIZE = 200;

    private final EscrowTransactionRepository escrowTransactionRepository;
    private final EscrowStateHistoryRepository escrowStateHistoryRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "escrow.user.events")
    @Transactional
    public void onMessage(Message message) throws Exception {
        try {
            String json = new String(message.getBody());
            JsonNode root = objectMapper.readTree(json);
            String eventType = root.path("eventType").asText("");

            if (!"user.anonymized".equals(eventType)) {
                log.debug("Ignoring non-anonymization event on escrow.user.events: {}", eventType);
                return;
            }

            String userIdStr = root.path("userId").asText();
            if (userIdStr.isBlank()) {
                log.error("Received user.anonymized event with blank userId");
                return;
            }

            UUID userId = UUID.fromString(userIdStr);
            log.info("Anonymizing escrow data for userId={}", userId);

            anonymizeEscrowTransactions(userId);
            anonymizeEscrowStateHistory(userId);

            log.info("Successfully anonymized escrow data for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to process user.anonymized event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void anonymizeEscrowTransactions(UUID userId) {
        Page<EscrowTransaction> asCustomerPage = escrowTransactionRepository
                .findByCustomerId(userId, PageRequest.of(0, BATCH_SIZE));
        int customerCount = 0;
        for (EscrowTransaction tx : asCustomerPage) {
            tx.setCustomerId(ANONYMIZED_SENTINEL);
            escrowTransactionRepository.save(tx);
            customerCount++;
        }

        Page<EscrowTransaction> asMerchantPage = escrowTransactionRepository
                .findByMerchantId(userId, PageRequest.of(0, BATCH_SIZE));
        int merchantCount = 0;
        for (EscrowTransaction tx : asMerchantPage) {
            tx.setMerchantId(ANONYMIZED_SENTINEL);
            escrowTransactionRepository.save(tx);
            merchantCount++;
        }

        if (customerCount > 0 || merchantCount > 0) {
            log.info("Anonymized customer_id in {} escrow transactions and merchant_id in {} escrow transactions for userId={}",
                    customerCount, merchantCount, userId);
        }
    }

    private void anonymizeEscrowStateHistory(UUID userId) {
        var entries = escrowStateHistoryRepository.findByTriggeredById(userId);
        for (EscrowStateHistory entry : entries) {
            entry.setTriggeredById(ANONYMIZED_SENTINEL);
            entry.setTriggeredBy(TriggeredBy.SYSTEM);
            escrowStateHistoryRepository.save(entry);
        }

        if (!entries.isEmpty()) {
            log.info("Anonymized triggeredById in {} escrow state history entries for userId={}",
                    entries.size(), userId);
        }
    }
}