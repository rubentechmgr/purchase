package com.example.purchase.client;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TreasuryClient {
    /**
     * Find an exchange rate for the given currency with a rate date <= targetDate and >= targetDate minus 6 months.
     * Return Optional.empty() if none found.
     */
    Optional<ExchangeRate> findRateOnOrBeforeWithinSixMonths(LocalDate targetDate, String currencyCode);

    @Getter
    class ExchangeRate {
        private final LocalDate rateDate;
        private final BigDecimal rate; // price of 1 USD in target currency (or appropriate convention)

        public ExchangeRate(LocalDate rateDate, BigDecimal rate) {
            this.rateDate = rateDate;
            this.rate = rate;
        }

    }
}