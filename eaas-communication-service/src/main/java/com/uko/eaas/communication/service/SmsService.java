package com.uko.eaas.communication.service;

public interface SmsService {

    void sendSms(String phoneNumber, String message);

    boolean isValidPhoneNumber(String phoneNumber);
}
