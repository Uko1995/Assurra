package com.uko.eaas.escrow.service;

import com.uko.eaas.escrow.dto.FeeBreakdown;
import com.uko.eaas.escrow.model.entity.FeeConfiguration;
import com.uko.eaas.escrow.repository.FeeConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeCalculationService {

    private final FeeConfigurationRepository feeConfigurationRepository;

    @Value("${escrow.fee.percentage:1.5}")
    private BigDecimal defaultFeePercentage;

    @Value("${escrow.fee.min:500}")
    private BigDecimal defaultMinFee;

    @Value("${escrow.fee.max:50000}")
    private BigDecimal defaultMaxFee;

    public FeeBreakdown calculateFee(BigDecimal amount) {
        return calculateFee(amount, null);
    }

    public FeeBreakdown calculateFee(BigDecimal amount, UUID merchantId) {
        FeeConfiguration config;
        if (merchantId != null) {
            config = feeConfigurationRepository.findActiveByMerchantId(merchantId)
                    .orElseGet(() -> feeConfigurationRepository.findGlobalDefault()
                            .orElse(null));
        } else {
            config = feeConfigurationRepository.findGlobalDefault().orElse(null);
        }

        BigDecimal feeValue = defaultFeePercentage.divide(BigDecimal.valueOf(100));
        BigDecimal minFee = defaultMinFee;
        BigDecimal maxFee = defaultMaxFee;

        if (config != null) {
            feeValue = config.getFeeValue();
            minFee = config.getMinFee();
            maxFee = config.getMaxFee();
        } else {
            log.warn("No fee configuration found, using default values");
        }

        BigDecimal rawFee;
        switch (config != null ? config.getFeeType() : "PERCENTAGE") {
            case "FLAT" -> rawFee = feeValue;
            case "BLENDED" -> rawFee = amount.multiply(feeValue);
            default -> rawFee = amount.multiply(feeValue);
        }

        BigDecimal escrowFee = rawFee.max(minFee).min(maxFee);
        BigDecimal merchantAmount = amount.subtract(escrowFee);

        return FeeBreakdown.builder()
                .amount(amount)
                .escrowFee(escrowFee)
                .merchantAmount(merchantAmount)
                .build();
    }
}
