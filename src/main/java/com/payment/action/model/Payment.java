package com.payment.action.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private String paymentMethod;

    private String customerEmail;

    private Instant createdAt;

    private Instant updatedAt;
}
