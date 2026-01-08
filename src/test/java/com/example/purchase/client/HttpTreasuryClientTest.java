package com.example.purchase.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HttpTreasuryClientTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private HttpTreasuryClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        objectMapper = new ObjectMapper();
        client = new HttpTreasuryClient(restTemplate, objectMapper);
    }

    @Test
    void findRateOnOrBeforeWithinSixMonths_returnsExchangeRate_whenApiReturnsValidData() {
        String json = "{ \"data\": [ { \"exchange_rate\": \"1.2345\", \"effective_date\": \"2024-01-01\" } ] }";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        Optional<TreasuryClient.ExchangeRate> result = client.findRateOnOrBeforeWithinSixMonths(
                LocalDate.of(2024, 1, 1), "CAD");

        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2024, 1, 1), result.get().getRateDate());
        assertEquals(new BigDecimal("1.2345"), result.get().getRate());
    }

    @Test
    void findRateOnOrBeforeWithinSixMonths_returnsEmpty_whenApiReturnsNoData() {
        String json = "{ \"data\": [] }";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        Optional<TreasuryClient.ExchangeRate> result = client.findRateOnOrBeforeWithinSixMonths(
                LocalDate.of(2024, 1, 1), "CAD");

        assertFalse(result.isPresent());
    }

    @Test
    void findRateOnOrBeforeWithinSixMonths_returnsEmpty_whenApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);

        Optional<TreasuryClient.ExchangeRate> result = client.findRateOnOrBeforeWithinSixMonths(
                LocalDate.of(2024, 1, 1), "CAD");

        assertFalse(result.isPresent());
    }
}
