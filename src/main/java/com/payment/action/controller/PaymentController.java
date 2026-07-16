package com.payment.action.controller;

import com.payment.action.dto.PaymentRequest;
import com.payment.action.dto.PaymentResponse;
import com.payment.action.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{transactionId}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.processPayment(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getPaymentDetails(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.refundPayment(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportPaymentsToJson() {
        byte[] data = paymentService.exportPaymentsToJson();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.json")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(data);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportPaymentsToExcel() {
        byte[] data = paymentService.exportPaymentsToExcel();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
