package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.AppUser;
import com.ubs.billing.entity.Bill;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.entity.Meter;
import com.ubs.billing.entity.MeterReading;
import com.ubs.billing.entity.Notification;
import com.ubs.billing.entity.Payment;
import com.ubs.billing.entity.Tariff;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static Dtos.UserResponse user(AppUser user) {
        return new Dtos.UserResponse(
                user.getId(),
                user.getFullNames(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getRoles(),
                user.isMustChangePassword(),
                user.getEmailVerificationOtp() == null,
                user.getCustomer() == null ? null : user.getCustomer().getId()
        );
    }

    public static Dtos.CustomerResponse customer(Customer customer) {
        return new Dtos.CustomerResponse(
                customer.getId(),
                customer.getFullNames(),
                customer.getNationalId(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getStatus()
        );
    }

    public static Dtos.MeterResponse meter(Meter meter) {
        return new Dtos.MeterResponse(
                meter.getId(),
                meter.getMeterNumber(),
                meter.getMeterType(),
                meter.getInstallationDate(),
                meter.getStatus(),
                meter.getCustomer().getId()
        );
    }

    public static Dtos.MeterReadingResponse reading(MeterReading reading) {
        return new Dtos.MeterReadingResponse(
                reading.getId(),
                reading.getMeter().getId(),
                reading.getPreviousReading(),
                reading.getCurrentReading(),
                reading.getReadingDate(),
                reading.getReadingMonth(),
                reading.getReadingYear()
        );
    }

    public static Dtos.TariffResponse tariff(Tariff tariff) {
        return new Dtos.TariffResponse(
                tariff.getId(),
                tariff.getMeterType(),
                tariff.getTariffType(),
                tariff.getVersion(),
                tariff.getEffectiveFrom(),
                tariff.getFlatRate(),
                tariff.getFixedServiceCharge(),
                tariff.getTaxRatePercent(),
                tariff.getLatePenaltyRatePercent()
        );
    }

    public static Dtos.BillResponse bill(Bill bill) {
        return new Dtos.BillResponse(
                bill.getId(),
                bill.getBillReference(),
                bill.getCustomer().getId(),
                bill.getCustomer().getFullNames(),
                bill.getMeter().getId(),
                bill.getBillMonth(),
                bill.getBillYear(),
                bill.getConsumption(),
                bill.getTotalAmount(),
                bill.getOutstandingBalance(),
                bill.getStatus(),
                bill.getIssueDate(),
                bill.getDueDate()
        );
    }

    public static Dtos.PaymentResponse payment(Payment payment) {
        return new Dtos.PaymentResponse(
                payment.getId(),
                payment.getBill().getBillReference(),
                payment.getAmountPaid(),
                payment.getPaymentMethod(),
                payment.getPaymentDate()
        );
    }

    public static Dtos.NotificationResponse notification(Notification notification) {
        return new Dtos.NotificationResponse(
                notification.getId(),
                notification.getCustomer().getId(),
                notification.getBill().getBillReference(),
                notification.getMessage(),
                notification.getStatus().name(),
                notification.getCreatedAt()
        );
    }
}
