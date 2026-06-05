package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "ROLE_FINANCE records payments. ROLE_ADMIN, ROLE_FINANCE, and ROLE_CUSTOMER can view payment history.")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/bill/{billReference}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List payments for a bill", description = "ROLE_ADMIN, ROLE_FINANCE, or ROLE_CUSTOMER. Use billReference from GET /api/bills/customer/{customerId}.")
    public List<Dtos.PaymentResponse> findByBill(@PathVariable String billReference) {
        return paymentService.findByBill(billReference);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Record bill payment", description = "ROLE_FINANCE only. Bill must be approved first. Supports partial and full payments; full payment creates a notification.")
    public Dtos.PaymentResponse recordPayment(@Valid @RequestBody Dtos.PaymentRequest request) {
        return paymentService.recordPayment(request);
    }
}
