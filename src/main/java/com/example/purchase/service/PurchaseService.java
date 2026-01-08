package com.example.purchase.service;

import com.example.purchase.client.TreasuryClient;
import com.example.purchase.domain.TreasuryCurrency;
import com.example.purchase.dto.PurchaseRequest;
import com.example.purchase.dto.PurchaseResponse;
import com.example.purchase.entity.Purchase;
import com.example.purchase.exception.ExchangeRateNotFoundException;
import com.example.purchase.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository repo;
    private final TreasuryClient treasuryClient;

    public PurchaseService(PurchaseRepository repo, TreasuryClient treasuryClient) {
        this.repo = repo;
        this.treasuryClient = treasuryClient;
    }

    /**
     * Creates a new purchase and saves it to the repository.
     * @param req the purchase request DTO
     * @return the ID of the saved purchase
     */
    @Transactional
    public Long createPurchase(PurchaseRequest req) {
        log.info("Creating purchase: {}", req);
        // Round amount to 2 decimal places
        Purchase p = new Purchase(
                req.getDescription(),
                req.getTransactionDate(),
                req.getAmountUsd().setScale(2, RoundingMode.HALF_UP)
        );
        p = repo.save(p);
        log.info("Purchase saved with ID: {}", p.getId());
        return p.getId();
    }

    /**
     * Retrieves a purchase by ID and converts its amount to the requested currency.
     * @param id the purchase ID
     * @param currencyCode the target currency code
     * @return the purchase response with converted amount
     */
    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseConverted(Long id, String currencyCode) {
        log.info("Fetching purchase with ID: {} for currency: {}", id, currencyCode);
        Purchase p = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase not found: {}", id);
                    return new NoSuchElementException("Purchase not found: " + id);
                });

        // If currency is USD, return without conversion
        if (currencyCode == null || currencyCode.equalsIgnoreCase("USD")) {
            log.info("Currency is USD or null, returning purchase without conversion.");
            PurchaseResponse resp = new PurchaseResponse();
            resp.setId(p.getId());
            resp.setDescription(p.getDescription());
            resp.setTransactionDate(p.getTransactionDate());
            resp.setAmountUsd(p.getAmountUsd());
            resp.setTargetCurrency("USD");
            resp.setExchangeRate(BigDecimal.ONE);
            resp.setConvertedAmount(p.getAmountUsd());
            return resp;
        }

        // Normalize currency code and map to treasury value if available
        String code = currencyCode.trim().toUpperCase();
        String treasuryValue = TreasuryCurrency.descForCode(code);
        if (treasuryValue == null) {
            treasuryValue = code;
        }
        LocalDate txDate = p.getTransactionDate();

        log.info("Looking up exchange rate for {} on or before {}", treasuryValue, txDate);

        return treasuryClient.findRateOnOrBeforeWithinSixMonths(txDate, treasuryValue)
                .map(rate -> {
                    BigDecimal converted = p.getAmountUsd().multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
                    log.info("Exchange rate found: {}. Converted amount: {}", rate.getRate(), converted);
                    PurchaseResponse r = new PurchaseResponse();
                    r.setId(p.getId());
                    r.setDescription(p.getDescription());
                    r.setTransactionDate(txDate);
                    r.setAmountUsd(p.getAmountUsd());
                    r.setTargetCurrency(code);
                    r.setExchangeRate(rate.getRate().setScale(6, RoundingMode.HALF_UP));
                    r.setConvertedAmount(converted);
                    return r;
                })
                .orElseThrow(() -> {
                    log.error("No exchange rate available within 6 months on-or-before {}", txDate);
                    return new ExchangeRateNotFoundException("No exchange rate available within 6 months on-or-before " + txDate);
                });
    }
}
