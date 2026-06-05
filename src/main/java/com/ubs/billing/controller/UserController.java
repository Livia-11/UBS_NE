package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.UserService;
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
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management - ADMIN only", description = "Only ROLE_ADMIN can create users, assign roles, update roles, and view system users.")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List system users", description = "ROLE_ADMIN only. Use this to see operators, finance officers, customers, and password-change status.")
    public List<Dtos.UserResponse> findAll() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a system user", description = "ROLE_ADMIN only. Requires unique nationalId and address because every system user is also a billing customer. Do not send a password. The system generates a temporary password and emails it to the user. ROLE_CUSTOMER is added automatically.")
    public Dtos.UserResponse create(@Valid @RequestBody Dtos.UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile or roles", description = "ROLE_ADMIN only. Use the user id from GET /api/users. National ID remains unique. If roles change, the user receives a role update email.")
    public Dtos.UserResponse update(@PathVariable Long id, @Valid @RequestBody Dtos.UserRequest request) {
        return userService.update(id, request);
    }
}
