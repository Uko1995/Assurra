package com.uko.eaas.payment.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.payment.model.entity.AmlAlert;
import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.model.entity.Payout;
import com.uko.eaas.payment.repository.AmlAlertRepository;
import com.uko.eaas.payment.repository.PaymentTransactionRepository;
import com.uko.eaas.payment.repository.PayoutRepository;
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
    private static final int BATCH_SIZE = 100;

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PayoutRepository payoutRepository;
    private final AmlAlertRepository amlAlertRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "payment.user.events")
    @Transactional
    public void onMessage(Message message) throws Exception {
        try {
            String json = new String(message.getBody());
            JsonNode root = objectMapper.readTree(json);
            String eventType = root.path("eventType").asText("");

            if (!"user.anonymized".equals(eventType)) {
                log.debug("Ignoring non-anonymization event on payment.user.events: {}", eventType);
                return;
            }

            String userIdStr = root.path("userId").asText();
            if (userIdStr.isBlank()) {
                log.error("Received user.anonymized event with blank userId");
                return;
            }

            UUID userId = UUID.fromString(userIdStr);
            log.info("Anonymizing payment data for userId={}", userId);

            anonymizePaymentTransactions(userId);
            anonymizePayouts(userId);
            anonymizeAmlAlerts(userId);

            log.info("Successfully anonymized payment data for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to process user.anonymized event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void anonymizePaymentTransactions(UUID userId) {
        Page<PaymentTransaction> asCustomer = paymentTransactionRepository
                .findByCustomerId(userId, PageRequest.of(0, BATCH_SIZE));
        int count = 0;
        for (PaymentTransaction tx : asCustomer) {
            tx.setCustomerId(ANONYMIZED_SENTINEL);
            paymentTransactionRepository.save(tx);
            count++;
        }

        Page<PaymentTransaction> asMerchant = paymentTransactionRepository
                .findByMerchantId(userId, PageRequest.of(0, BATCH_SIZE));
        for (PaymentTransaction tx : asMerchant) {
            tx.setMerchantId(ANONYMIZED_SENTINEL);
            paymentTransactionRepository.save(tx);
            count++;
        }

        if (count > 0) {
            log.info("Anonymized {} payment transactions for userId={}", count, userId);
        }
    }

    private void anonymizePayouts(UUID userId) {
        Page<Payout> payouts = payoutRepository
                .findByMerchantId(userId, PageRequest.of(0, BATCH_SIZE));
        int count = 0;
        for (Payout payout : payouts) {
            payout.setMerchantId(ANONYMIZED_SENTINEL);
            payoutRepository.save(payout);
            count++;
        }

        if (count > 0) {
            log.info("Anonymized {} payouts for userId={}", count, userId);
        }
    }

    private void anonymizeAmlAlerts(UUID userId) {
        var byCustomer = amlAlertRepository.findByCustomerId(userId);
        int count = 0;
        for (AmlAlert alert : byCustomer) {
            alert.setCustomerId(ANONYMIZED_SENTINEL);
            amlAlertRepository.save(alert);
            count++;
        }

        var byMerchant = amlAlertRepository.findByMerchantId(userId);
        for (AmlAlert alert : byMerchant) {
            alert.setMerchantId(ANONYMIZED_SENTINEL);
            amlAlertRepository.save(alert);
            count++;
        }

        if (count > 0) {
            log.info("Anonymized {} AML alert references for userId={}", count, userId);
        }
    }
}