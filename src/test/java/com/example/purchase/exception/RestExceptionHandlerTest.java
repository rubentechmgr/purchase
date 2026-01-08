// java
package com.example.purchase.exception;

import com.example.purchase.dto.PurchaseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String TRANSACTION_DATE_MSG =
            "transactionDate must be a valid date in the format YYYY-MM-DD (example: 2023-10-31)";
    private static final String AMOUNT_USD_MSG =
            "amountUsd must be a valid numeric value greater than 0 (example: 10.50)";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        TestController controller = new TestController();
        RestExceptionHandler advice = new RestExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(advice)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void post_validRequest_returns200() throws Exception {
        PurchaseRequest req = new PurchaseRequest();
        req.setDescription("Valid");
        req.setTransactionDate(LocalDate.of(2025, 3, 3));
        req.setAmountUsd(new BigDecimal("12.34"));

        mockMvc.perform(post("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isOk());
    }

    @Test
    void post_badTransactionDate_format_mapsToTransactionDateMessage() throws Exception {
        String json = "{\"description\":\"White Paint\",\"transactionDate\":\"03-03-2025\",\"amountUsd\":10}";

        mockMvc.perform(post("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value(TRANSACTION_DATE_MSG));
    }

    @Test
    void get_missingPurchase_returns404_withExceptionMessage() throws Exception {
        mockMvc.perform(get("/test/1000"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Purchase with id 1000 was not found"));
    }

    @RestController
    static class TestController {

        @PostMapping("/test")
        public void create(@RequestBody PurchaseRequest req) {
            // succeed silently for valid payloads
        }

        @GetMapping("/test/{id}")
        public void get(@PathVariable Long id) {
            throw new NoSuchElementException("Purchase not found: " + id);
        }
    }
}
