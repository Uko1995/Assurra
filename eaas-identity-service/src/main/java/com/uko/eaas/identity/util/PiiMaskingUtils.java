package com.uko.eaas.identity.util;

/**
 * Utility for masking PII in logs and API responses.
 */
public class PiiMaskingUtils {

    private PiiMaskingUtils() {}

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) return "***" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    public static String maskBvn(String bvn) {
        if (bvn == null || bvn.length() < 4) return bvn;
        return "****" + bvn.substring(bvn.length() - 4);
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    public static String maskIpAddress(String ip) {
        if (ip == null || ip.isBlank()) return ip;
        return "***.***.***.***";
    }
}
