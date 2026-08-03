package com.uko.eaas.payment.service;

import com.uko.eaas.payment.client.InterswitchClient;
import com.uko.eaas.payment.dto.*;
import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.model.enums.PaymentChannel;
import com.uko.eaas.payment.model.enums.PaymentStatus;
import com.uko.eaas.payment.repository.PaymentTransactionRepository;
import com.uko.eaas.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentTransactionRepository paymentRepository;

    @Mock
    private InterswitchClient interswitchClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private InitializePaymentRequest initRequest;
    private PaymentTransaction testPayment;

    @BeforeEach
    void setUp() {
        initRequest = new InitializePaymentRequest();
        initRequest.setEscrowReference("ESC-20240101-ABC123");
        initRequest.setCustomerEmail("test@example.com");
        initRequest.setAmount(new BigDecimal("10500.00"));
        initRequest.setCurrency("NGN");
        initRequest.setCallbackUrl("https://example.com/callback");

        testPayment = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .reference("PAY-20240101-XYZ789")
                .escrowReference("ESC-20240101-ABC123")
                .customerId(UUID.randomUUID())
                .merchantId(UUID.randomUUID())
                .amount(new BigDecimal("10500.00"))
                .currency("NGN")
                .status(PaymentStatus.PENDING)
                .paymentLink("https://checkout.interswitch.com/pay/xyz")
                .build();
    }

    @Test
    void initializePayment_Success() {
        // Given
        InitializePaymentResponse interswitchResponse = InitializePaymentResponse.builder()
                .reference("PAY-20240101-XYZ789")
                .escrowReference("ESC-20240101-ABC123")
                .paymentLink("https://checkout.interswitch.com/pay/xyz")
                .amount(new BigDecimal("10500.00"))
                .currency("NGN")
                .status("PENDING")
                .build();

        when(interswitchClient.initializePayment(any(), anyString())).thenReturn(interswitchResponse);
        when(paymentRepository.save(any(PaymentTransaction.class))).thenReturn(testPayment);

        // When
        InitializePaymentResponse response = paymentService.initializePayment(initRequest);

        // Then
        assertNotNull(response);
        assertEquals("https://checkout.interswitch.com/pay/xyz", response.getPaymentLink());
        assertEquals("10500.00", response.getAmount().toString());

        verify(paymentRepository).save(any(PaymentTransaction.class));
    }

    @Test
    void initializePayment_Idempotency() {
        // Given
        initRequest.setIdempotencyKey("unique-key");

        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(testPayment));

        // When
        InitializePaymentResponse response = paymentService.initializePayment(initRequest);

        // Then
        assertNotNull(response);
        verify(interswitchClient, never()).initializePayment(any(), anyString());
    }

    @Test
    void verifyPayment_Success() {
        // Given
        testPayment.setInterswitchRef("INT-123456");

        PaymentVerificationResponse verificationResponse = PaymentVerificationResponse.builder()
                .reference("PAY-20240101-XYZ789")
                .status("SUCCESS")
                .amount(new BigDecimal("10500.00"))
                .channel("CARD")
                .cardLast4("4242")
                .cardBrand("VISA")
                .paidAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByReference(anyString())).thenReturn(Optional.of(testPayment));
        when(interswitchClient.verifyPayment(anyString())).thenReturn(verificationResponse);
        when(paymentRepository.save(any(PaymentTransaction.class))).thenReturn(testPayment);

        // When
        PaymentVerificationResponse response = paymentService.verifyPayment(testPayment.getReference());

        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, testPayment.getStatus());
        assertEquals("4242", testPayment.getCardLast4());
        assertEquals("VISA", testPayment.getCardBrand());
    }

    @Test
    void getPayment_Success() {
        // Given
        when(paymentRepository.findByReference(anyString())).thenReturn(Optional.of(testPayment));

        // When
        PaymentVerificationResponse response = paymentService.getPayment(testPayment.getReference());

        // Then
        assertNotNull(response);
        assertEquals(testPayment.getReference(), response.getReference());
    }

    @Test
    void handleWebhook_Success() {
        // Given
        PaymentWebhookPayload payload = new PaymentWebhookPayload();
        payload.setReference(testPayment.getReference());
        payload.setPaymentReference("INT-123456");
        payload.setStatus("success");
        payload.setAmount(new BigDecimal("10500.00"));
        payload.setChannel("CARD");
        payload.setCardLast4("4242");
        payload.setCardBrand("VISA");
        payload.setPaidAt(LocalDateTime.now());

        when(paymentRepository.findByReference(anyString())).thenReturn(Optional.of(testPayment));
        when(interswitchClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        when(paymentRepository.save(any(PaymentTransaction.class))).thenReturn(testPayment);

        // When
        paymentService.handleWebhook(payload, "valid-signature");

        // Then
        assertEquals(PaymentStatus.SUCCESS, testPayment.getStatus());
        assertEquals("INT-123456", testPayment.getInterswitchRef());
    }

    @Test
    void handleWebhook_InvalidSignature() {
        // Given
        PaymentWebhookPayload payload = new PaymentWebhookPayload();
        payload.setReference(testPayment.getReference());
        payload.setStatus("success");

        when(interswitchClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(SecurityException.class,
                () -> paymentService.handleWebhook(payload, "invalid-signature"));
    }

    @Test
    void processRefund_Success() {
        // Given
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setAmount(new BigDecimal("10500.00"));

        when(paymentRepository.findByReference(anyString())).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(PaymentTransaction.class))).thenReturn(testPayment);

        // When
        paymentService.processRefund(testPayment.getReference(), "Customer request");

        // Then
        assertEquals(PaymentStatus.REFUNDED, testPayment.getStatus());
        assertNotNull(testPayment.getRefundedAt());
        assertEquals(new BigDecimal("10500.00"), testPayment.getRefundAmount());
    }

    @Test
    void processRefund_NotSuccessfulPayment() {
        // Given
        testPayment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByReference(anyString())).thenReturn(Optional.of(testPayment));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> paymentService.processRefund(testPayment.getReference(), "Customer request"));
    }
}
