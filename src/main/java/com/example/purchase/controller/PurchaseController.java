package com.example.purchase.controller;

import com.example.purchase.dto.PurchaseRequest;
import com.example.purchase.dto.PurchaseResponse;
import com.example.purchase.service.PurchaseService;
import com.example.purchase.validation.ValidCurrency;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing purchases.
 */
@RestController
@RequestMapping("/purchases")
@Validated
public class PurchaseController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    private final PurchaseService service;

    /**
     * Constructs a new PurchaseController with the given service.
     * @param service the purchase service
     */
    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    /**
     * Creates a new purchase.
     * @param request the purchase request payload
     * @return the ID of the created purchase
     */
    @PostMapping
    public ResponseEntity<Long> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        log.info("Received request to create purchase: {}", request);
        Long id = service.createPurchase(request);
        log.info("Purchase created with ID: {}", id);
        return ResponseEntity.ok(id);
    }

    /**
     * Retrieves a purchase by ID and converts the amount to the requested currency.
     * @param id the purchase ID
     * @param currency the target currency code (validated)
     * @return the purchase response with converted amount
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchase(
            @PathVariable Long id,
            @ValidCurrency @RequestParam(name = "currency", required = true) String currency) {
        log.info("Fetching purchase with ID: {} and currency: {}", id, currency);
        PurchaseResponse resp = service.getPurchaseConverted(id, currency);
        log.info("Returning purchase response: {}", resp);
        return ResponseEntity.ok(resp);
    }
}
