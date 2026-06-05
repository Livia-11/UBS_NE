package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Bill;
import com.ubs.billing.entity.Payment;
import com.ubs.billing.enums.BillStatus;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.repository.BillRepository;
import com.ubs.billing.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;

    public PaymentService(PaymentRepository paymentRepository, BillRepository billRepository, BillingService billingService) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    @Transactional(readOnly = true)
    public List<Dtos.PaymentResponse> findByBill(String billReference) {
        Bill bill = billingService.requireBill(billReference);
        return paymentRepository.findByBillId(bill.getId()).stream().map(DtoMapper::payment).toList();
    }

    @Transactional
    public Dtos.PaymentResponse recordPayment(Dtos.PaymentRequest request) {
        Bill bill = billingService.requireBill(request.billReference());
        if (bill.getStatus() == BillStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Bill must be approved before payment");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already fully paid");
        }
        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException("Payment amount cannot exceed outstanding balance");
        }
        if (request.paymentDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Payment date cannot be in the future");
        }
        if (request.paymentDate().isBefore(bill.getIssueDate())) {
            throw new BusinessRuleException("Payment date cannot be before the bill issue date");
        }

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmountPaid(request.amountPaid());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentDate(request.paymentDate());

        BigDecimal newBalance = bill.getOutstandingBalance().subtract(request.amountPaid());
        bill.setOutstandingBalance(newBalance);
        bill.setStatus(newBalance.compareTo(BigDecimal.ZERO) == 0 ? BillStatus.PAID : BillStatus.PARTIALLY_PAID);
        billRepository.save(bill);
        return DtoMapper.payment(paymentRepository.save(payment));
    }
}
