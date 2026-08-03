package com.uko.eaas.payment.model.enums;

public enum EscrowStatus {
    INITIATED,
    FUNDED,
    MERCHANT_NOTIFIED,
    SHIPPED,
    DELIVERED,
    CONFIRMED,
    DISPUTED,
    UNDER_REVIEW,
    AUTO_RELEASED,
    RELEASED,
    RESOLVED_MERCHANT,
    RESOLVED_CUSTOMER,
    REFUNDED,
    CANCELLED
}
