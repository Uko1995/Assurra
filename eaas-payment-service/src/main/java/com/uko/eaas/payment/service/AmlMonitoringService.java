package com.uko.eaas.payment.service;

import com.uko.eaas.payment.model.entity.AmlAlert;
import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.repository.AmlAlertRepository;
import com.uko.eaas.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AML transaction monitoring service.
 * Evaluates payments against AML rules and creates alerts when thresholds are exceeded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmlMonitoringService {

    private final PaymentTransactionRepository paymentRepository;
    private final AmlAlertRepository amlAlertRepository;

    private static final BigDecimal THRESHOLD_NGN = BigDecimal.valueOf(5_000_000);
    private static final BigDecimal VELOCITY_AMOUNT = BigDecimal.valueOf(1_000_000);
    private static final int VELOCITY_COUNT = 3;
    private static final BigDecimal STRUCTURING_MIN = BigDecimal.valueOf(4_500_000);
    private static final BigDecimal STRUCTURING_MAX = BigDecimal.valueOf(4_999_999);
    private static final int STRUCTURING_COUNT = 5;

    public void evaluateTransaction(PaymentTransaction payment) {
        if (payment.getAmount() == null || payment.getCustomerId() == null) {
            return;
        }

        // Rule 1: Large transaction threshold
        if (payment.getAmount().compareTo(THRESHOLD_NGN) >= 0) {
            createAlert(payment, "THRESHOLD_EXCEEDED",
                    "Transaction amount " + payment.getAmount() + " exceeds ₦5,000,000 threshold");
        }

        // Rule 2: Velocity — multiple large transactions in 24h
        checkVelocity(payment);

        // Rule 3: Structuring — multiple transactions just below threshold in 7 days
        checkStructuring(payment);
    }

    private void checkVelocity(PaymentTransaction payment) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<PaymentTransaction> recent = paymentRepository.findByCustomerIdAndAmountGreaterThanEqualAndCreatedAtAfter(
                payment.getCustomerId(), VELOCITY_AMOUNT, since);

        if (recent.size() >= VELOCITY_COUNT) {
            createAlert(payment, "VELOCITY_EXCEEDED",
                    "Customer made " + recent.size() + " transactions >= ₦1M in 24 hours");
        }
    }

    private void checkStructuring(PaymentTransaction payment) {
        if (payment.getAmount().compareTo(STRUCTURING_MIN) < 0 || payment.getAmount().compareTo(STRUCTURING_MAX) > 0) {
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<PaymentTransaction> recent = paymentRepository.findByCustomerIdAndAmountBetweenAndCreatedAtAfter(
                payment.getCustomerId(), STRUCTURING_MIN, STRUCTURING_MAX, since);

        if (recent.size() >= STRUCTURING_COUNT) {
            createAlert(payment, "STRUCTURING_SUSPECTED",
                    "Customer made " + recent.size() + " transactions between ₦4.5M and ₦4.999M in 7 days");
        }
    }

    private void createAlert(PaymentTransaction payment, String alertType, String notes) {
        // Prevent duplicate alerts for same payment and type
        if (amlAlertRepository.findAll().stream()
                .anyMatch(a -> a.getPaymentId().equals(payment.getId()) && a.getAlertType().equals(alertType))) {
            return;
        }

        AmlAlert alert = AmlAlert.builder()
                .paymentId(payment.getId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .alertType(alertType)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status("OPEN")
                .notes(notes)
                .build();

        amlAlertRepository.save(alert);
        log.warn("AML Alert created: {} for payment {}", alertType, payment.getReference());
    }
}
