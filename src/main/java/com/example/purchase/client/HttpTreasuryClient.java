package com.example.purchase.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Optional;

/**
 * TreasuryClient implementation that fetches exchange rates from the US Treasury Fiscal Data API.
 */
@Component
public class HttpTreasuryClient implements TreasuryClient {

    private static final Logger log = LoggerFactory.getLogger(HttpTreasuryClient.class);
    private static final String API_URL =
            "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";

    private final RestTemplate rest;
    private final ObjectMapper mapper;

    public HttpTreasuryClient(RestTemplate restTemplate, ObjectMapper mapper) {
        this.rest = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Finds the most recent exchange rate for the given currency on or before the target date,
     * within the last six months.
     */
    @Override
    public Optional<ExchangeRate> findRateOnOrBeforeWithinSixMonths(LocalDate targetDate, String currencyValue) {
        LocalDate sixMonthsAgo = targetDate.minusMonths(6);

        String[] candidateFields = new String[] { "currency", "country_currency_desc" };

        for (String currencyField : candidateFields) {
            String uri = buildUri(currencyField, currencyValue, targetDate, sixMonthsAgo);
            log.debug("Attempting to fetch exchange rates from URI: {}", uri);

            try {
                String json = rest.getForObject(uri, String.class);
                if (json == null || json.isEmpty()) {
                    log.debug("Empty response for filter {}={} (targetDate={})", currencyField, currencyValue, targetDate);
                    continue;
                }

                log.trace("Treasury API response: {}", json);

                Optional<ExchangeRate> maybe = parseRates(json, targetDate, sixMonthsAgo, currencyField, currencyValue);
                if (maybe.isPresent()) {
                    log.info("Found exchange rate for {}={} on or before {}", currencyField, currencyValue, targetDate);
                    return maybe;
                } else {
                    log.debug("No usable rate found using filter {}={}", currencyField, currencyValue);
                }
            } catch (Exception ex) {
                log.warn("Error fetching/parsing treasury rates for filter {}={} : {}", currencyField, currencyValue, ex.toString());
            }
        }

        log.warn("No exchange rate found for currency {} on or before {} within 6 months", currencyValue, targetDate);
        return Optional.empty();
    }

    /**
     * Builds the API URI with the appropriate filters for currency and date range.
     */
    private String buildUri(String currencyField, String currencyValue, LocalDate targetDate, LocalDate sixMonthsAgo) {
        // Use FiscalData operators `:lte:` and `:gte:`
        String filter = String.format("%s:eq:%s,effective_date:lte:%s,effective_date:gte:%s",
                currencyField,
                encodeValue(currencyValue),
                targetDate.toString(),
                sixMonthsAgo.toString());

        log.debug("Constructed filter: {}", filter);

        return UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("fields", "exchange_rate,effective_date,record_date")
                .queryParam("filter", filter)
                .queryParam("page[size]", "200")
                .queryParam("sort", "-effective_date,-record_date")
                .toUriString();
    }

    /**
     * Encodes special characters in the currency value for use in the API filter.
     */
    private String encodeValue(String v) {
        return v == null ? "" : v.replace(" ", "%20").replace(",", "%2C").replace("(", "%28").replace(")", "%29");
    }

    /**
     * Parses the JSON response and finds the best (most recent) exchange rate within the date range.
     */
    private Optional<ExchangeRate> parseRates(String json, LocalDate targetDate, LocalDate sixMonthsAgo, String usedFilterField, String usedFilterValue) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode data = root;
            if (!root.isArray()) {
                JsonNode maybeData = root.path("data");
                if (maybeData.isArray()) {
                    data = maybeData;
                } else {
                    log.debug("No 'data' array found in response");
                    return Optional.empty();
                }
            }

            if (!data.isArray() || data.isEmpty()) {
                log.debug("No data returned from Treasury API");
                return Optional.empty();
            }

            LocalDate bestDate = null;
            BigDecimal bestRate = null;

            Iterator<JsonNode> it = data.elements();
            while (it.hasNext()) {
                JsonNode node = it.next();

                JsonNode rateNode = node.path("exchange_rate");
                if (rateNode.isMissingNode() || rateNode.isNull()) {
                    rateNode = node.path("exchangeRate");
                }

                JsonNode dateNode = node.path("effective_date");
                if (dateNode.isMissingNode() || dateNode.isNull()) {
                    dateNode = node.path("record_date");
                }

                if (rateNode.isMissingNode() || rateNode.isNull() || dateNode.isMissingNode() || dateNode.isNull()) {
                    log.trace("Skipping node due to missing rate or date");
                    continue;
                }

                BigDecimal rate;
                try {
                    rate = new BigDecimal(rateNode.asText());
                } catch (NumberFormatException ex) {
                    log.trace("Skipping node due to invalid rate: {}", rateNode.asText());
                    continue;
                }

                LocalDate rateDate;
                try {
                    rateDate = LocalDate.parse(dateNode.asText());
                } catch (DateTimeParseException ex) {
                    log.trace("Skipping node due to invalid date: {}", dateNode.asText());
                    continue;
                }

                // Only consider rates within the allowed date range
                if (rateDate.isAfter(targetDate) || rateDate.isBefore(sixMonthsAgo)) {
                    continue;
                }

                // Keep the most recent rate
                if (bestDate == null || rateDate.isAfter(bestDate)) {
                    bestDate = rateDate;
                    bestRate = rate;
                }
            }

            if (bestDate != null) {
                log.debug("Found exchange rate {} on {} using filter {}={}", bestRate, bestDate, usedFilterField, usedFilterValue);
                return Optional.of(new ExchangeRate(bestDate, bestRate));
            }
        } catch (Exception ex) {
            log.trace("parseRates error: {}", ex.toString());
        }
        return Optional.empty();
    }
}
