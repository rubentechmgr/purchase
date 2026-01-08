package com.example.purchase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class PurchaseResponse {

    private Long id;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal amountUsd;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;

    public PurchaseResponse() {}

}
