// src/test/java/com/example/purchase/controller/PurchaseControllerTest.java
package com.example.purchase.controller;

import com.example.purchase.dto.PurchaseRequest;
import com.example.purchase.dto.PurchaseResponse;
import com.example.purchase.service.PurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseControllerTest {

    private MockMvc mockMvc;
    private PurchaseService service;
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @BeforeEach
    void setUp() {
        service = Mockito.mock(PurchaseService.class);
        PurchaseController controller = new PurchaseController(service);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createPurchase_returnsId() throws Exception {
        PurchaseRequest req = new PurchaseRequest();
        req.setDescription("Test purchase");
        req.setAmountUsd(new BigDecimal("12.34"));
        req.setTransactionDate(LocalDate.of(2026, 1, 1));

        when(service.createPurchase(ArgumentMatchers.any(PurchaseRequest.class))).thenReturn(123L);

        mockMvc.perform(post("/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("123"));

        verify(service).createPurchase(ArgumentMatchers.any(PurchaseRequest.class));
    }

    @Test
    void getPurchase_returnsPurchaseResponse_and_callsService() throws Exception {
        Long id = 1L;
        String currency = "USD";
        PurchaseResponse resp = new PurchaseResponse();
        resp.setId(id);
        resp.setDescription("Test purchase");
        resp.setTransactionDate(LocalDate.of(2026, 1, 1));
        resp.setAmountUsd(new BigDecimal("10.00"));

        when(service.getPurchaseConverted(ArgumentMatchers.eq(id), ArgumentMatchers.eq(currency))).thenReturn(resp);

        mockMvc.perform(get("/purchases/{id}", id).param("currency", currency))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(isEmptyString())));

        verify(service).getPurchaseConverted(ArgumentMatchers.eq(id), ArgumentMatchers.eq(currency));
    }
}
