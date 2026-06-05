package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Tariff;
import com.ubs.billing.enums.MeterType;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.TariffRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TariffService {
    private final TariffRepository tariffRepository;

    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Transactional(readOnly = true)
    public List<Dtos.TariffResponse> findAll() {
        return tariffRepository.findAll().stream().map(DtoMapper::tariff).toList();
    }

    @Transactional
    public Dtos.TariffResponse create(Dtos.TariffRequest request) {
        int nextVersion = tariffRepository.findTopByMeterTypeOrderByVersionDesc(request.meterType())
                .map(tariff -> tariff.getVersion() + 1)
                .orElse(1);
        Tariff tariff = new Tariff();
        tariff.setMeterType(request.meterType());
        tariff.setTariffType(request.tariffType());
        tariff.setVersion(nextVersion);
        tariff.setEffectiveFrom(request.effectiveFrom());
        tariff.setFlatRate(request.flatRate());
        tariff.setTierOneLimit(request.tierOneLimit());
        tariff.setTierOneRate(request.tierOneRate());
        tariff.setTierTwoLimit(request.tierTwoLimit());
        tariff.setTierTwoRate(request.tierTwoRate());
        tariff.setTierThreeRate(request.tierThreeRate());
        tariff.setFixedServiceCharge(request.fixedServiceCharge());
        tariff.setTaxRatePercent(request.taxRatePercent());
        tariff.setLatePenaltyRatePercent(request.latePenaltyRatePercent());
        return DtoMapper.tariff(tariffRepository.save(tariff));
    }

    public Tariff requireEffectiveTariff(MeterType meterType, LocalDate billingDate) {
        return tariffRepository.findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionDesc(meterType, billingDate)
                .orElseThrow(() -> new ResourceNotFoundException("No effective tariff found for " + meterType));
    }
}
