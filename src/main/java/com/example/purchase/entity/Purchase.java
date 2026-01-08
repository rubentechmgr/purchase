package com.example.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount_usd", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountUsd;

    public Purchase() {}

    public Purchase(String description, LocalDate transactionDate, BigDecimal amountUsd) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.amountUsd = amountUsd;
    }

}
