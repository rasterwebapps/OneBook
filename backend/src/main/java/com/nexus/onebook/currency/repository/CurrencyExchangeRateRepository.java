package com.nexus.onebook.currency.repository;

import com.nexus.onebook.currency.model.CurrencyExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyExchangeRateRepository extends JpaRepository<CurrencyExchangeRate, Long> {
    List<CurrencyExchangeRate> findByTenantId(String tenantId);
    Optional<CurrencyExchangeRate> findFirstByTenantIdAndFromCurrencyAndToCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String tenantId, String fromCurrency, String toCurrency, LocalDate date);
    List<CurrencyExchangeRate> findByTenantIdAndFromCurrencyAndToCurrency(
            String tenantId, String fromCurrency, String toCurrency);
}
