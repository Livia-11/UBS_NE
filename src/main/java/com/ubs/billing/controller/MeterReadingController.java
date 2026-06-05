package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.MeterReadingService;
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
@RequestMapping("/api/readings")
@Tag(name = "Meter Readings", description = "ROLE_OPERATOR and ROLE_ADMIN capture readings. ROLE_FINANCE can view readings for billing.")
public class MeterReadingController {
    private final MeterReadingService readingService;

    public MeterReadingController(MeterReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "List meter readings", description = "ROLE_ADMIN, ROLE_OPERATOR, or ROLE_FINANCE. Use reading id to generate a bill.")
    public List<Dtos.MeterReadingResponse> findAll() {
        return readingService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Capture meter reading", description = "ROLE_ADMIN or ROLE_OPERATOR. Use meterId from GET /api/meters. Only one reading per meter per month/year is allowed.")
    public Dtos.MeterReadingResponse capture(@Valid @RequestBody Dtos.MeterReadingRequest request) {
        return readingService.capture(request);
    }
}
