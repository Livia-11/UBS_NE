package com.ubs.billing.repository;

import com.ubs.billing.entity.MeterReading;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    boolean existsByMeterIdAndReadingMonthAndReadingYear(Long meterId, int readingMonth, int readingYear);

    List<MeterReading> findByMeterId(Long meterId);
}
