package com.uko.eaas.escrow.service;

import com.uko.eaas.escrow.dto.*;
import com.uko.eaas.escrow.model.entity.EscrowTransaction;
import com.uko.eaas.escrow.model.enums.EscrowStatus;
import com.uko.eaas.escrow.repository.EscrowStateHistoryRepository;
import com.uko.eaas.escrow.repository.EscrowTransactionRepository;
import com.uko.eaas.escrow.service.impl.EscrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscrowServiceImplTest {

    @Mock
    private EscrowTransactionRepository escrowRepository;

    @Mock
    private EscrowStateHistoryRepository stateHistoryRepository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EscrowServiceImpl escrowService;

    private CreateEscrowRequest createRequest;
    private EscrowTransaction testEscrow;
    private UUID customerId;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        merchantId = UUID.randomUUID();

        createRequest = new CreateEscrowRequest();
        createRequest.setMerchantId(merchantId.toString());
        createRequest.setAmount(new BigDecimal("10000.00"));
        createRequest.setProductDescription("Test Product");
        createRequest.setProductQuantity(1);
        createRequest.setAgreedDeliveryDays(7);

        testEscrow = EscrowTransaction.builder()
                .id(UUID.randomUUID())
                .reference("ESC-20240101-ABC123")
                .customerId(customerId)
                .merchantId(merchantId)
                .merchantProfileId(merchantId)
                .amount(new BigDecimal("10000.00"))
                .escrowFee(new BigDecimal("500.00"))
                .merchantAmount(new BigDecimal("9500.00"))
                .currency("NGN")
                .status(EscrowStatus.INITIATED)
                .productDescription("Test Product")
                .build();
    }

    @Test
    void createEscrow_Success() {
        // Given
        FeeBreakdown feeBreakdown = FeeBreakdown.builder()
                .amount(new BigDecimal("10000.00"))
                .escrowFee(new BigDecimal("500.00"))
                .merchantAmount(new BigDecimal("9500.00"))
                .build();

        when(feeCalculationService.calculateFee(any())).thenReturn(feeBreakdown);
        when(escrowRepository.save(any(EscrowTransaction.class))).thenReturn(testEscrow);
        when(stateHistoryRepository.save(any())).thenReturn(null);

        // When
        EscrowResponse response = escrowService.createEscrow(createRequest, customerId.toString(), null);

        // Then
        assertNotNull(response);
        assertEquals(EscrowStatus.INITIATED, response.getStatus());
        assertEquals(new BigDecimal("10000.00"), response.getAmount());
        assertEquals(new BigDecimal("500.00"), response.getEscrowFee());

        verify(escrowRepository).save(any(EscrowTransaction.class));
        verify(stateHistoryRepository).save(any());
    }

    @Test
    void createEscrow_IdempotencyCheck() {
        // Given - idempotency key is now passed as parameter, not in request body
        String idempotencyKey = "unique-key-123";

        when(escrowRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(testEscrow));

        // When
        EscrowResponse response = escrowService.createEscrow(createRequest, customerId.toString(), idempotencyKey);

        // Then
        assertNotNull(response);
        assertEquals(testEscrow.getReference(), response.getReference());

        verify(escrowRepository, never()).save(any(EscrowTransaction.class));
    }

    @Test
    void getEscrow_Success() {
        // Given
        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));

        // When
        EscrowResponse response = escrowService.getEscrow("ESC-20240101-ABC123");

        // Then
        assertNotNull(response);
        assertEquals(testEscrow.getId(), response.getId());
        assertEquals(testEscrow.getReference(), response.getReference());
    }

    @Test
    void getEscrow_NotFound() {
        // Given
        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> escrowService.getEscrow("NON-EXISTENT"));
    }

    @Test
    void shipEscrow_Success() {
        // Given
        testEscrow.setStatus(EscrowStatus.MERCHANT_NOTIFIED);

        ShipEscrowRequest shipRequest = new ShipEscrowRequest();
        shipRequest.setTrackingNumber("TRK123456");
        shipRequest.setLogisticsProvider("DHL");

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));
        when(escrowRepository.save(any(EscrowTransaction.class))).thenReturn(testEscrow);

        // When
        EscrowResponse response = escrowService.shipEscrow(testEscrow.getReference(), shipRequest);

        // Then
        assertNotNull(response);
        assertEquals(EscrowStatus.SHIPPED, testEscrow.getStatus());
        assertEquals("TRK123456", testEscrow.getTrackingNumber());
    }

    @Test
    void shipEscrow_InvalidStateTransition() {
        // Given - Try to ship from INITIATED (invalid)
        testEscrow.setStatus(EscrowStatus.INITIATED);

        ShipEscrowRequest shipRequest = new ShipEscrowRequest();
        shipRequest.setTrackingNumber("TRK123456");
        shipRequest.setLogisticsProvider("DHL");

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> escrowService.shipEscrow(testEscrow.getReference(), shipRequest));
    }

    @Test
    void confirmEscrow_Success() {
        // Given
        testEscrow.setStatus(EscrowStatus.DELIVERED);

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));
        when(escrowRepository.save(any(EscrowTransaction.class))).thenReturn(testEscrow);

        // When
        EscrowResponse response = escrowService.confirmEscrow(testEscrow.getReference(), customerId.toString());

        // Then
        assertNotNull(response);
        assertEquals(EscrowStatus.CONFIRMED, testEscrow.getStatus());
    }

    @Test
    void confirmEscrow_WrongCustomer() {
        // Given
        testEscrow.setStatus(EscrowStatus.DELIVERED);
        UUID wrongCustomerId = UUID.randomUUID();

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> escrowService.confirmEscrow(testEscrow.getReference(), wrongCustomerId.toString()));
    }

    @Test
    void cancelEscrow_Success() {
        // Given
        testEscrow.setStatus(EscrowStatus.INITIATED);

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));
        when(escrowRepository.save(any(EscrowTransaction.class))).thenReturn(testEscrow);

        // When
        escrowService.cancelEscrow(testEscrow.getReference(), customerId.toString(), "CUSTOMER");

        // Then
        assertEquals(EscrowStatus.CANCELLED, testEscrow.getStatus());
    }

    @Test
    void cancelEscrow_AlreadyCancelled_ThrowsIllegalStateException() {
        // Given - try to cancel an escrow that is already CANCELLED
        testEscrow.setStatus(EscrowStatus.CANCELLED);

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> escrowService.cancelEscrow(testEscrow.getReference(), customerId.toString(), "CUSTOMER"));

        assertTrue(exception.getMessage().contains("Cannot cancel escrow in status"));
        verify(escrowRepository, never()).save(any(EscrowTransaction.class));
    }

    @Test
    void handlePaymentWebhook_Success() {
        // Given
        PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest();
        webhookRequest.setReference(testEscrow.getReference());
        webhookRequest.setPaymentReference("PAY-123");
        webhookRequest.setStatus("success");
        webhookRequest.setAmount(new BigDecimal("10500.00"));
        webhookRequest.setChannel("card");

        testEscrow.setTotalCharge(new BigDecimal("10500.00"));

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));
        when(escrowRepository.save(any(EscrowTransaction.class))).thenReturn(testEscrow);

        // When
        escrowService.handlePaymentWebhook(webhookRequest);

        // Then
        assertEquals(EscrowStatus.FUNDED, testEscrow.getStatus());
        assertEquals("PAY-123", testEscrow.getPaymentReference());
    }

    @Test
    void handlePaymentWebhook_AmountMismatch() {
        // Given
        PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest();
        webhookRequest.setReference(testEscrow.getReference());
        webhookRequest.setStatus("success");
        webhookRequest.setAmount(new BigDecimal("5000.00")); // Wrong amount

        testEscrow.setTotalCharge(new BigDecimal("10500.00"));

        when(escrowRepository.findByReference(anyString())).thenReturn(Optional.of(testEscrow));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> escrowService.handlePaymentWebhook(webhookRequest));
    }
}
