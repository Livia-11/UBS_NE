package com.ubs.billing.entity;

import com.ubs.billing.enums.MeterType;
import com.ubs.billing.enums.TariffType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "tariffs",
        uniqueConstraints = @UniqueConstraint(name = "uk_tariff_type_version", columnNames = {"meter_type", "version"})
)
public class Tariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffType tariffType;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal flatRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierOneLimit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierOneRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierTwoLimit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierTwoRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tierThreeRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fixedServiceCharge;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRatePercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal latePenaltyRatePercent;

    public Long getId() {
        return id;
    }

    public MeterType getMeterType() {
        return meterType;
    }

    public void setMeterType(MeterType meterType) {
        this.meterType = meterType;
    }

    public TariffType getTariffType() {
        return tariffType;
    }

    public void setTariffType(TariffType tariffType) {
        this.tariffType = tariffType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public BigDecimal getFlatRate() {
        return flatRate;
    }

    public void setFlatRate(BigDecimal flatRate) {
        this.flatRate = flatRate;
    }

    public BigDecimal getTierOneLimit() {
        return tierOneLimit;
    }

    public void setTierOneLimit(BigDecimal tierOneLimit) {
        this.tierOneLimit = tierOneLimit;
    }

    public BigDecimal getTierOneRate() {
        return tierOneRate;
    }

    public void setTierOneRate(BigDecimal tierOneRate) {
        this.tierOneRate = tierOneRate;
    }

    public BigDecimal getTierTwoLimit() {
        return tierTwoLimit;
    }

    public void setTierTwoLimit(BigDecimal tierTwoLimit) {
        this.tierTwoLimit = tierTwoLimit;
    }

    public BigDecimal getTierTwoRate() {
        return tierTwoRate;
    }

    public void setTierTwoRate(BigDecimal tierTwoRate) {
        this.tierTwoRate = tierTwoRate;
    }

    public BigDecimal getTierThreeRate() {
        return tierThreeRate;
    }

    public void setTierThreeRate(BigDecimal tierThreeRate) {
        this.tierThreeRate = tierThreeRate;
    }

    public BigDecimal getFixedServiceCharge() {
        return fixedServiceCharge;
    }

    public void setFixedServiceCharge(BigDecimal fixedServiceCharge) {
        this.fixedServiceCharge = fixedServiceCharge;
    }

    public BigDecimal getTaxRatePercent() {
        return taxRatePercent;
    }

    public void setTaxRatePercent(BigDecimal taxRatePercent) {
        this.taxRatePercent = taxRatePercent;
    }

    public BigDecimal getLatePenaltyRatePercent() {
        return latePenaltyRatePercent;
    }

    public void setLatePenaltyRatePercent(BigDecimal latePenaltyRatePercent) {
        this.latePenaltyRatePercent = latePenaltyRatePercent;
    }
}
