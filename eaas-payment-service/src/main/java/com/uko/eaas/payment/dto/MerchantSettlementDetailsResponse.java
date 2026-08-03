package com.uko.eaas.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSettlementDetailsResponse {
    private String merchantId;
    private String businessName;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String settlementEmail;
}
