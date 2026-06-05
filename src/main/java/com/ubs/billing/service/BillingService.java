package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Bill;
import com.ubs.billing.entity.MeterReading;
import com.ubs.billing.entity.Tariff;
import com.ubs.billing.enums.AccountStatus;
import com.ubs.billing.enums.BillStatus;
import com.ubs.billing.enums.TariffType;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.BillRepository;
import com.ubs.billing.repository.MeterReadingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final BillRepository billRepository;
    private final MeterReadingRepository readingRepository;
    private final TariffService tariffService;

    public BillingService(BillRepository billRepository, MeterReadingRepository readingRepository, TariffService tariffService) {
        this.billRepository = billRepository;
        this.readingRepository = readingRepository;
        this.tariffService = tariffService;
    }

    @Transactional(readOnly = true)
    public List<Dtos.BillResponse> findAll() {
        return billRepository.findAll().stream().map(DtoMapper::bill).toList();
    }

    @Transactional(readOnly = true)
    public List<Dtos.BillResponse> findByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId).stream().map(DtoMapper::bill).toList();
    }

    @Transactional
    public Dtos.BillResponse generate(Long readingId) {
        if (billRepository.existsByReadingId(readingId)) {
            throw new BusinessRuleException("A bill already exists for this reading");
        }
        MeterReading reading = readingRepository.findById(readingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));
        if (reading.getMeter().getCustomer().getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive bills");
        }

        Tariff tariff = tariffService.requireEffectiveTariff(reading.getMeter().getMeterType(), reading.getReadingDate());
        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal consumptionAmount = calculateConsumptionAmount(consumption, tariff);
        BigDecimal subtotal = consumptionAmount.add(tariff.getFixedServiceCharge());
        BigDecimal taxAmount = subtotal.multiply(tariff.getTaxRatePercent()).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        LocalDate issueDate = LocalDate.now();

        Bill bill = new Bill();
        bill.setBillReference("BILL-" + reading.getReadingYear() + "-" + reading.getReadingMonth() + "-" + reading.getMeter().getId() + "-" + System.currentTimeMillis());
        bill.setCustomer(reading.getMeter().getCustomer());
        bill.setMeter(reading.getMeter());
        bill.setReading(reading);
        bill.setTariff(tariff);
        bill.setBillMonth(reading.getReadingMonth());
        bill.setBillYear(reading.getReadingYear());
        bill.setConsumption(consumption);
        bill.setConsumptionAmount(consumptionAmount);
        bill.setFixedCharge(tariff.getFixedServiceCharge());
        bill.setTaxAmount(taxAmount);
        bill.setPenaltyAmount(BigDecimal.ZERO);
        bill.setTotalAmount(total);
        bill.setOutstandingBalance(total);
        bill.setStatus(BillStatus.PENDING_APPROVAL);
        bill.setIssueDate(issueDate);
        bill.setDueDate(issueDate.plusDays(15));
        return DtoMapper.bill(billRepository.save(bill));
    }

    @Transactional
    public Dtos.BillResponse approve(String billReference) {
        Bill bill = requireBill(billReference);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only pending bills can be approved");
        }
        bill.setStatus(BillStatus.APPROVED);
        return DtoMapper.bill(bill);
    }

    public Bill requireBill(String billReference) {
        return billRepository.findByBillReference(billReference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    private BigDecimal calculateConsumptionAmount(BigDecimal consumption, Tariff tariff) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal firstTierUnits = consumption.min(tariff.getTierOneLimit());
        BigDecimal amount = firstTierUnits.multiply(tariff.getTierOneRate());
        if (consumption.compareTo(tariff.getTierOneLimit()) > 0) {
            BigDecimal secondTierUnits = consumption.min(tariff.getTierTwoLimit()).subtract(tariff.getTierOneLimit());
            amount = amount.add(secondTierUnits.multiply(tariff.getTierTwoRate()));
        }
        if (consumption.compareTo(tariff.getTierTwoLimit()) > 0) {
            BigDecimal thirdTierUnits = consumption.subtract(tariff.getTierTwoLimit());
            amount = amount.add(thirdTierUnits.multiply(tariff.getTierThreeRate()));
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
