package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "ROLE_ADMIN manages customer records. ROLE_FINANCE and ROLE_CUSTOMER can view customer records.")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List customers", description = "ROLE_ADMIN, ROLE_FINANCE, or ROLE_CUSTOMER. Use returned customer id for meters, bills, and notifications.")
    public List<Dtos.CustomerResponse> findAll() {
        return customerService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register customer record", description = "ROLE_ADMIN only. National ID must be unique. This is the billing customer profile, separate from the login account.")
    public Dtos.CustomerResponse create(@Valid @RequestBody Dtos.CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer record", description = "ROLE_ADMIN only. Use customer id from GET /api/customers.")
    public Dtos.CustomerResponse update(@PathVariable Long id, @Valid @RequestBody Dtos.CustomerRequest request) {
        return customerService.update(id, request);
    }
}
