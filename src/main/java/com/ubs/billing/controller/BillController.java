package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing", description = "ROLE_ADMIN and ROLE_FINANCE generate and approve bills. ROLE_CUSTOMER can view customer bills.")
public class BillController {
    private final BillingService billingService;

    public BillController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List all bills", description = "ROLE_ADMIN or ROLE_FINANCE. Customers should use /api/bills/customer/{customerId}.")
    public List<Dtos.BillResponse> findAll() {
        return billingService.findAll();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List bills for a customer", description = "ROLE_ADMIN, ROLE_FINANCE, or ROLE_CUSTOMER. Use customerId from GET /api/customers. Returns billReference needed for approval and payment.")
    public List<Dtos.BillResponse> findByCustomer(@PathVariable Long customerId) {
        return billingService.findByCustomer(customerId);
    }

    @PostMapping("/generate/{readingId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Generate bill from reading", description = "ROLE_ADMIN or ROLE_FINANCE. Use readingId from GET /api/readings. Creates a notification through PostgreSQL trigger.")
    public Dtos.BillResponse generate(@PathVariable Long readingId) {
        return billingService.generate(readingId);
    }

    @PostMapping("/{billReference}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Approve generated bill", description = "ROLE_ADMIN or ROLE_FINANCE. Use billReference returned from bill generation or customer bill listing.")
    public Dtos.BillResponse approve(@PathVariable String billReference) {
        return billingService.approve(billReference);
    }
}
