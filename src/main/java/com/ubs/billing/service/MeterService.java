package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.entity.Meter;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.CustomerRepository;
import com.ubs.billing.repository.MeterRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterService {
    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    public MeterService(MeterRepository meterRepository, CustomerRepository customerRepository) {
        this.meterRepository = meterRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Dtos.MeterResponse> findAll() {
        return meterRepository.findAll().stream().map(DtoMapper::meter).toList();
    }

    @Transactional(readOnly = true)
    public List<Dtos.MeterResponse> findByCustomer(Long customerId) {
        return meterRepository.findByCustomerId(customerId).stream().map(DtoMapper::meter).toList();
    }

    @Transactional
    public Dtos.MeterResponse create(Dtos.MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new BusinessRuleException("Meter number already exists");
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Meter meter = new Meter();
        apply(meter, request, customer);
        return DtoMapper.meter(meterRepository.save(meter));
    }

    @Transactional
    public Dtos.MeterResponse update(Long id, Dtos.MeterRequest request) {
        Meter meter = requireMeter(id);
        meterRepository.findByMeterNumber(request.meterNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Meter number already exists");
                });
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        apply(meter, request, customer);
        return DtoMapper.meter(meter);
    }

    public Meter requireMeter(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
    }

    private void apply(Meter meter, Dtos.MeterRequest request, Customer customer) {
        if (request.installationDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Installation date cannot be in the future");
        }
        meter.setMeterNumber(request.meterNumber());
        meter.setMeterType(request.meterType());
        meter.setInstallationDate(request.installationDate());
        meter.setStatus(request.status());
        meter.setCustomer(customer);
    }
}
