package com.example.purchase.service;

import com.example.purchase.client.TreasuryClient;
import com.example.purchase.domain.TreasuryCurrency;
import com.example.purchase.dto.PurchaseRequest;
import com.example.purchase.dto.PurchaseResponse;
import com.example.purchase.entity.Purchase;
import com.example.purchase.exception.ExchangeRateNotFoundException;
import com.example.purchase.repository.PurchaseRepository;
import com.example.purchase.client.TreasuryClient.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PurchaseServiceTest {

    private PurchaseRepository repo;
    private TreasuryClient treasuryClient;
    private PurchaseService service;

    @BeforeEach
    void setUp() {
        repo = mock(PurchaseRepository.class);
        treasuryClient = mock(TreasuryClient.class);
        service = new PurchaseService(repo, treasuryClient);
    }

    @Test
    void createPurchase_savesAndReturnsId() {
        PurchaseRequest req = new PurchaseRequest();
        req.setDescription("desc");
        req.setTransactionDate(LocalDate.of(2024, 1, 1));
        req.setAmountUsd(new BigDecimal("12.345"));

        Purchase saved = new Purchase("desc", LocalDate.of(2024, 1, 1), new BigDecimal("12.35"));
        saved.setId(42L);

        when(repo.save(any(Purchase.class))).thenReturn(saved);

        Long id = service.createPurchase(req);

        assertEquals(42L, id);
        verify(repo).save(any(Purchase.class));
    }

    @Test
    void getPurchaseConverted_usd_noConversion() {
        Purchase p = new Purchase("desc", LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        p.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        PurchaseResponse resp = service.getPurchaseConverted(1L, "USD");

        assertEquals(1L, resp.getId());
        assertEquals("desc", resp.getDescription());
        assertEquals(new BigDecimal("10.00"), resp.getAmountUsd());
        assertEquals("USD", resp.getTargetCurrency());
        assertEquals(BigDecimal.ONE, resp.getExchangeRate());
        assertEquals(new BigDecimal("10.00"), resp.getConvertedAmount());
    }

    @Test
    void getPurchaseConverted_otherCurrency_conversionApplied() {
        Purchase p = new Purchase("desc", LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        p.setId(2L);
        when(repo.findById(2L)).thenReturn(Optional.of(p));

        ExchangeRate rate = new ExchangeRate(LocalDate.of(2024, 1, 1), new BigDecimal("1.234567"));
        when(treasuryClient.findRateOnOrBeforeWithinSixMonths(eq(LocalDate.of(2024, 1, 1)), anyString()))
                .thenReturn(Optional.of(rate));

        PurchaseResponse resp = service.getPurchaseConverted(2L, "CAD");

        assertEquals("CAD", resp.getTargetCurrency());
        assertEquals(new BigDecimal("1.234567"), resp.getExchangeRate());
        assertEquals(new BigDecimal("12.35"), resp.getConvertedAmount()); // 10.00 * 1.234567 = 12.34567 -> 12.35
    }

    @Test
    void getPurchaseConverted_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getPurchaseConverted(99L, "USD"));
    }

    @Test
    void getPurchaseConverted_noRate_throws() {
        Purchase p = new Purchase("desc", LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        p.setId(3L);
        when(repo.findById(3L)).thenReturn(Optional.of(p));
        when(treasuryClient.findRateOnOrBeforeWithinSixMonths(any(), any())).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class, () -> service.getPurchaseConverted(3L, "EUR"));
    }
}
