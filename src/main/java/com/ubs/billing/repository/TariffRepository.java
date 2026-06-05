package com.ubs.billing.repository;

import com.ubs.billing.entity.Tariff;
import com.ubs.billing.enums.MeterType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);

    Optional<Tariff> findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionDesc(
            MeterType meterType,
            LocalDate effectiveFrom
    );

    List<Tariff> findByMeterTypeOrderByVersionDesc(MeterType meterType);
}
