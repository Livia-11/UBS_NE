package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tariffs")
@Tag(name = "Tariff Configuration", description = "ROLE_ADMIN creates versioned tariffs. ROLE_FINANCE can view tariff configuration.")
public class TariffController {
    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List tariffs", description = "ROLE_ADMIN or ROLE_FINANCE. New tariffs are versioned by meter type.")
    public List<Dtos.TariffResponse> findAll() {
        return tariffService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tariff version", description = "ROLE_ADMIN only. effectiveFrom controls future billing cycles. Supports FLAT and TIER_BASED tariffs.")
    public Dtos.TariffResponse create(@Valid @RequestBody Dtos.TariffRequest request) {
        return tariffService.create(request);
    }
}
