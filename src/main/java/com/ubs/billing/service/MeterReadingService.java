package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Meter;
import com.ubs.billing.entity.MeterReading;
import com.ubs.billing.enums.MeterStatus;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.MeterReadingRepository;
import com.ubs.billing.repository.MeterRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterReadingService {
    private final MeterReadingRepository readingRepository;
    private final MeterRepository meterRepository;

    public MeterReadingService(MeterReadingRepository readingRepository, MeterRepository meterRepository) {
        this.readingRepository = readingRepository;
        this.meterRepository = meterRepository;
    }

    @Transactional(readOnly = true)
    public List<Dtos.MeterReadingResponse> findAll() {
        return readingRepository.findAll().stream().map(DtoMapper::reading).toList();
    }

    @Transactional
    public Dtos.MeterReadingResponse capture(Dtos.MeterReadingRequest request) {
        Meter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive meters cannot receive readings");
        }
        if (request.readingDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Reading date cannot be in the future");
        }
        if (request.readingDate().isBefore(meter.getInstallationDate())) {
            throw new BusinessRuleException("Reading date cannot be before the meter installation date");
        }
        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw new BusinessRuleException("Current reading must be greater than previous reading");
        }

        int month = request.readingDate().getMonthValue();
        int year = request.readingDate().getYear();
        if (readingRepository.existsByMeterIdAndReadingMonthAndReadingYear(meter.getId(), month, year)) {
            throw new BusinessRuleException("Only one reading is allowed per meter per month/year");
        }

        MeterReading reading = new MeterReading();
        reading.setMeter(meter);
        reading.setPreviousReading(request.previousReading());
        reading.setCurrentReading(request.currentReading());
        reading.setReadingDate(request.readingDate());
        reading.setReadingMonth(month);
        reading.setReadingYear(year);
        return DtoMapper.reading(readingRepository.save(reading));
    }
}
