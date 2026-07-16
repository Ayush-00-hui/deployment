package com.payment.action.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.action.dto.PaymentRequest;
import com.payment.action.dto.PaymentResponse;
import com.payment.action.model.Payment;
import com.payment.action.model.PaymentStatus;
import com.payment.action.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

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

    public byte[] exportPaymentsToJson() {
        log.info("Exporting all payments to JSON");
        List<Payment> payments = paymentRepository.findAll();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payments);
        } catch (IOException e) {
            log.error("Failed to export payments to JSON", e);
            throw new RuntimeException("JSON export failed", e);
        }
    }

    public byte[] exportPaymentsToExcel() {
        log.info("Exporting all payments to Excel");
        List<Payment> payments = paymentRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payments");
            
            // Header cell style
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Transaction ID", "Amount", "Currency", "Status", "Payment Method", "Customer Email", "Created At", "Updated At"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            int rowIdx = 1;
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowIdx++);
                
                row.createCell(0).setCellValue(payment.getId() != null ? payment.getId() : "");
                row.createCell(1).setCellValue(payment.getTransactionId() != null ? payment.getTransactionId() : "");
                row.createCell(2).setCellValue(payment.getAmount() != null ? payment.getAmount().doubleValue() : 0.0);
                row.createCell(3).setCellValue(payment.getCurrency() != null ? payment.getCurrency() : "");
                row.createCell(4).setCellValue(payment.getStatus() != null ? payment.getStatus().name() : "");
                row.createCell(5).setCellValue(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
                row.createCell(6).setCellValue(payment.getCustomerEmail() != null ? payment.getCustomerEmail() : "");
                row.createCell(7).setCellValue(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : "");
                row.createCell(8).setCellValue(payment.getUpdatedAt() != null ? payment.getUpdatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to export payments to Excel", e);
            throw new RuntimeException("Excel export failed", e);
        }
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
