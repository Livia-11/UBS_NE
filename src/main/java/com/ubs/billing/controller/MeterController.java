package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.MeterService;
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
@RequestMapping("/api/meters")
@Tag(name = "Meter Management", description = "ROLE_ADMIN creates and updates meters. ADMIN, OPERATOR, FINANCE, and CUSTOMER can view meters.")
public class MeterController {
    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "List meters", description = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, or ROLE_CUSTOMER. Use meter id when capturing readings.")
    public List<Dtos.MeterResponse> findAll() {
        return meterService.findAll();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "List meters for a customer", description = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, or ROLE_CUSTOMER. Use customerId from GET /api/customers. Helpful before capturing meter readings.")
    public List<Dtos.MeterResponse> findByCustomer(@PathVariable Long customerId) {
        return meterService.findByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create meter", description = "ROLE_ADMIN only. meterNumber must be unique. customerId must be an existing customer id.")
    public Dtos.MeterResponse create(@Valid @RequestBody Dtos.MeterRequest request) {
        return meterService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update meter", description = "ROLE_ADMIN only. Use meter id from GET /api/meters.")
    public Dtos.MeterResponse update(@PathVariable Long id, @Valid @RequestBody Dtos.MeterRequest request) {
        return meterService.update(id, request);
    }
}
