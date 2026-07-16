package com.payment.action.service;

import com.payment.action.dto.PaymentRequest;
import com.payment.action.dto.PaymentResponse;
import com.payment.action.model.Payment;
import com.payment.action.model.PaymentStatus;
import com.payment.action.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment of {} {} for customer {}", request.getAmount(), request.getCurrency(), request.getCustomerEmail());
        
        String transactionId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Instant now = Instant.now();

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .customerEmail(request.getCustomerEmail())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    public PaymentResponse processPayment(String transactionId) {
        log.info("Processing payment for transaction ID: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for transaction ID: " + transactionId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment has already been processed with status: " + payment.getStatus());
        }

        // Mock payment processing logic
        // If amount is exactly 99.99, simulate a payment failure. Otherwise, mark as SUCCESS.
        if (payment.getAmount().compareTo(new BigDecimal("99.99")) == 0) {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment processing failed for transaction ID: {}", transactionId);
        } else {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment processing successful for transaction ID: {}", transactionId);
        }

        payment.setUpdatedAt(Instant.now());
        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }

    public PaymentResponse getPaymentDetails(String transactionId) {
        log.info("Fetching payment details for transaction ID: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for transaction ID: " + transactionId));

        return mapToResponse(payment);
    }

    public PaymentResponse refundPayment(String transactionId) {
        log.info("Refunding payment for transaction ID: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for transaction ID: " + transactionId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        Payment updatedPayment = paymentRepository.save(payment);
        
        log.info("Payment refunded successfully for transaction ID: {}", transactionId);
        return mapToResponse(updatedPayment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .customerEmail(payment.getCustomerEmail())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
