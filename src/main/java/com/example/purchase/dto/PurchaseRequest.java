package com.example.purchase.dto;

import com.example.purchase.validation.NotFutureDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class PurchaseRequest {

    @NotBlank
    @Size(max = 50)
    private String description;

    @NotNull
    @NotFutureDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 16, fraction = 2)
    private BigDecimal amountUsd;

}
