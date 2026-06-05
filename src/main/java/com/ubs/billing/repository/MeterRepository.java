package com.ubs.billing.repository;

import com.ubs.billing.entity.Meter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    boolean existsByMeterNumber(String meterNumber);

    Optional<Meter> findByMeterNumber(String meterNumber);

    List<Meter> findByCustomerId(Long customerId);
}
