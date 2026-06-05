package com.ubs.billing.dto;

import com.ubs.billing.enums.AccountStatus;
import com.ubs.billing.enums.BillStatus;
import com.ubs.billing.enums.MeterStatus;
import com.ubs.billing.enums.MeterType;
import com.ubs.billing.enums.PaymentMethod;
import com.ubs.billing.enums.TariffType;
import com.ubs.billing.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public final class Dtos {
    private Dtos() {
    }

    public record SignupRequest(
            @NotBlank String fullNames,
            @Pattern(regexp = "\\d{16}", message = "National ID must contain exactly 16 digits") String nationalId,
            @Email @NotBlank String email,
            @NotBlank String phoneNumber,
            @NotBlank String address,
            @Size(min = 6) String password
    ) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record VerifyOtpRequest(@Email @NotBlank String email, @NotBlank String otp) {
    }

    public record AuthResponse(String token, String email, Set<UserRole> roles, boolean mustChangePassword, String message) {
    }

    public record UserRequest(
            @NotBlank String fullNames,
            @Pattern(regexp = "\\d{16}", message = "National ID must contain exactly 16 digits") String nationalId,
            @Email @NotBlank String email,
            @NotBlank String phoneNumber,
            @NotBlank String address,
            @NotNull AccountStatus status,
            @NotEmpty Set<UserRole> roles
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @Size(min = 6) String newPassword
    ) {
    }

    public record MessageResponse(String message) {
    }

    public record UserResponse(
            Long id,
            String fullNames,
            String email,
            String phoneNumber,
            AccountStatus status,
            Set<UserRole> roles,
            boolean mustChangePassword,
            boolean emailVerified,
            Long customerId
    ) {
    }

    public record CustomerRequest(
            @NotBlank String fullNames,
            @Pattern(regexp = "\\d{16}", message = "National ID must contain exactly 16 digits") String nationalId,
            @Email @NotBlank String email,
            @NotBlank String phoneNumber,
            @NotBlank String address,
            @NotNull AccountStatus status
    ) {
    }

    public record CustomerResponse(Long id, String fullNames, String nationalId, String email, String phoneNumber, String address, AccountStatus status) {
    }

    public record MeterRequest(
            @NotBlank String meterNumber,
            @NotNull MeterType meterType,
            @NotNull @PastOrPresent(message = "Installation date cannot be in the future") LocalDate installationDate,
            @NotNull MeterStatus status,
            @NotNull Long customerId
    ) {
    }

    public record MeterResponse(Long id, String meterNumber, MeterType meterType, LocalDate installationDate, MeterStatus status, Long customerId) {
    }

    public record MeterReadingRequest(
            @NotNull Long meterId,
            @NotNull @PositiveOrZero BigDecimal previousReading,
            @NotNull @Positive BigDecimal currentReading,
            @NotNull @PastOrPresent(message = "Reading date cannot be in the future") LocalDate readingDate
    ) {
    }

    public record MeterReadingResponse(
            Long id,
            Long meterId,
            BigDecimal previousReading,
            BigDecimal currentReading,
            LocalDate readingDate,
            int readingMonth,
            int readingYear
    ) {
    }

    public record TariffRequest(
            @NotNull MeterType meterType,
            @NotNull TariffType tariffType,
            @FutureOrPresent LocalDate effectiveFrom,
            @NotNull @PositiveOrZero BigDecimal flatRate,
            @NotNull @PositiveOrZero BigDecimal tierOneLimit,
            @NotNull @PositiveOrZero BigDecimal tierOneRate,
            @NotNull @PositiveOrZero BigDecimal tierTwoLimit,
            @NotNull @PositiveOrZero BigDecimal tierTwoRate,
            @NotNull @PositiveOrZero BigDecimal tierThreeRate,
            @NotNull @PositiveOrZero BigDecimal fixedServiceCharge,
            @NotNull @PositiveOrZero BigDecimal taxRatePercent,
            @NotNull @PositiveOrZero BigDecimal latePenaltyRatePercent
    ) {
    }

    public record TariffResponse(
            Long id,
            MeterType meterType,
            TariffType tariffType,
            int version,
            LocalDate effectiveFrom,
            BigDecimal flatRate,
            BigDecimal fixedServiceCharge,
            BigDecimal taxRatePercent,
            BigDecimal latePenaltyRatePercent
    ) {
    }

    public record BillResponse(
            Long id,
            String billReference,
            Long customerId,
            String customerName,
            Long meterId,
            int billMonth,
            int billYear,
            BigDecimal consumption,
            BigDecimal totalAmount,
            BigDecimal outstandingBalance,
            BillStatus status,
            LocalDate issueDate,
            LocalDate dueDate
    ) {
    }

    public record PaymentRequest(
            @NotBlank String billReference,
            @NotNull @Positive BigDecimal amountPaid,
            @NotNull PaymentMethod paymentMethod,
            @NotNull @PastOrPresent(message = "Payment date cannot be in the future") LocalDate paymentDate
    ) {
    }

    public record PaymentResponse(Long id, String billReference, BigDecimal amountPaid, PaymentMethod paymentMethod, LocalDate paymentDate) {
    }

    public record NotificationResponse(Long id, Long customerId, String billReference, String message, String status, LocalDateTime createdAt) {
    }
}
