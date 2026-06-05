package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "ROLE_ADMIN and ROLE_FINANCE can view all notifications. ROLE_CUSTOMER can view customer notifications.")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List all notifications", description = "ROLE_ADMIN or ROLE_FINANCE. Notifications are created by PostgreSQL triggers.")
    public List<Dtos.NotificationResponse> findAll() {
        return notificationService.findAll();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List customer notifications", description = "ROLE_ADMIN, ROLE_FINANCE, or ROLE_CUSTOMER. Use customerId from GET /api/customers. Shows bill generation and full payment messages.")
    public List<Dtos.NotificationResponse> findByCustomer(@PathVariable Long customerId) {
        return notificationService.findByCustomer(customerId);
    }
}
