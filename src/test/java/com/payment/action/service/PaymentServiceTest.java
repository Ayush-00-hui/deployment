package com.payment.action.service;

import com.payment.action.dto.PaymentRequest;
import com.payment.action.dto.PaymentResponse;
import com.payment.action.model.Payment;
import com.payment.action.model.PaymentStatus;
import com.payment.action.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest request;
    private Payment payment;

    @BeforeEach
    void setUp() {
        request = PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .customerEmail("customer@example.com")
                .build();

        payment = Payment.builder()
                .id("1")
                .transactionId("PAY-12345678")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .customerEmail("customer@example.com")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testInitiatePayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals(payment.getTransactionId(), response.getTransactionId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPaymentSuccess() {
        when(paymentRepository.findByTransactionId("PAY-12345678")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment("PAY-12345678");

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository, times(1)).findByTransactionId("PAY-12345678");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPaymentFailureForSpecificAmount() {
        payment.setAmount(new BigDecimal("99.99"));
        when(paymentRepository.findByTransactionId("PAY-12345678")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment("PAY-12345678");

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentDetails() {
        when(paymentRepository.findByTransactionId("PAY-12345678")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentDetails("PAY-12345678");

        assertNotNull(response);
        assertEquals(payment.getTransactionId(), response.getTransactionId());
        verify(paymentRepository, times(1)).findByTransactionId("PAY-12345678");
    }

    @Test
    void testRefundPaymentSuccess() {
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByTransactionId("PAY-12345678")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.refundPayment("PAY-12345678");

        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPaymentFailureIfNotSuccess() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByTransactionId("PAY-12345678")).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentService.refundPayment("PAY-12345678"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
