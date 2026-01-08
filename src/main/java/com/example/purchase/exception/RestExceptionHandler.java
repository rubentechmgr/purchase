package com.example.purchase.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final String TRANSACTION_DATE_MSG =
            "transactionDate must be a valid date in the format YYYY-MM-DD (example: 2023-10-31)";

    private static final String AMOUNT_USD_MSG =
            "amountUsd must be a valid numeric value greater than 0 (example: 10.50)";

    @ExceptionHandler({java.util.NoSuchElementException.class, ExchangeRateNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage();
        if (msg != null && msg.startsWith("Purchase not found: ")) {
            String id = msg.substring("Purchase not found: ".length()).trim();
            msg = "Purchase with id " + id + " was not found";
        } else if (msg == null || msg.isBlank()) {
            msg = "Resource not found";
        }

        String path = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("message", msg);
        body.put("path", path);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .distinct()
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .distinct()
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName() == null ? "parameter" : ex.getName();
        Class<?> requiredType = ex.getRequiredType();
        if (isLocalDateType(requiredType) || "transactionDate".equalsIgnoreCase(name)) {
            return badRequest(TRANSACTION_DATE_MSG);
        }
        if (isNumericType(requiredType) || "amountUsd".equalsIgnoreCase(name)) {
            return badRequest(AMOUNT_USD_MSG);
        }

        String msg = name + " has invalid value";
        return badRequest(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        // Walk the cause chain to find Jackson InvalidFormatException or DateTimeParseException or NumberFormatException
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof DateTimeParseException) {
                return badRequest(TRANSACTION_DATE_MSG);
            }
            if (cause instanceof InvalidFormatException) {
                InvalidFormatException ife = (InvalidFormatException) cause;
                if (isInvalidFormatForLocalDate(ife) || pathContainsTransactionDate(ife)) {
                    return badRequest(TRANSACTION_DATE_MSG);
                }
                if (isInvalidFormatForNumeric(ife) || pathContainsAmountUsd(ife)) {
                    return badRequest(AMOUNT_USD_MSG);
                }
            }
            if (cause instanceof NumberFormatException) {
                return badRequest(AMOUNT_USD_MSG);
            }
            cause = cause.getCause();
        }

        // Fallback: if the message mentions the transactionDate or amountUsd property, treat accordingly
        String msgText = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msgText.contains("transactiondate") || msgText.contains("transaction_date")) {
            return badRequest(TRANSACTION_DATE_MSG);
        }
        if (msgText.contains("amountusd") || msgText.contains("amount_usd")) {
            return badRequest(AMOUNT_USD_MSG);
        }

        String msg = "Malformed request body";
        return badRequest(msg);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(DateTimeParseException ex) {
        return badRequest(TRANSACTION_DATE_MSG);
    }

    private boolean isLocalDateType(Class<?> type) {
        if (type == null) return false;
        return LocalDate.class.isAssignableFrom(type);
    }

    private boolean isNumericType(Class<?> type) {
        if (type == null) return false;
        return Number.class.isAssignableFrom(type)
                || java.math.BigDecimal.class.isAssignableFrom(type)
                || java.math.BigInteger.class.isAssignableFrom(type)
                || Double.class.isAssignableFrom(type)
                || Float.class.isAssignableFrom(type)
                || Long.class.isAssignableFrom(type)
                || Integer.class.isAssignableFrom(type)
                || int.class.isAssignableFrom(type)
                || long.class.isAssignableFrom(type)
                || double.class.isAssignableFrom(type)
                || float.class.isAssignableFrom(type);
    }

    private boolean isInvalidFormatForLocalDate(InvalidFormatException ife) {
        Class<?> target = ife.getTargetType();
        return target != null && LocalDate.class.isAssignableFrom(target);
    }

    private boolean isInvalidFormatForNumeric(InvalidFormatException ife) {
        Class<?> target = ife.getTargetType();
        return target != null && isNumericType(target);
    }

    private boolean pathContainsTransactionDate(InvalidFormatException ife) {
        if (ife.getPath() == null) return false;
        return ife.getPath().stream()
                .anyMatch(ref -> "transactionDate".equalsIgnoreCase(ref.getFieldName()));
    }

    private boolean pathContainsAmountUsd(InvalidFormatException ife) {
        if (ife.getPath() == null) return false;
        return ife.getPath().stream()
                .anyMatch(ref -> "amountUsd".equalsIgnoreCase(ref.getFieldName()));
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Validation failed", Collections.singletonList(message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String mapFieldError(FieldError fe) {
        String field = fe.getField();
        String code = fe.getCode() == null ? "" : fe.getCode();
        String defaultMessage = fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage();

        if ("NotNull".equals(code) || defaultMessage.toLowerCase().contains("must not be null")) {
            return field + " is required";
        }
        if ("NotBlank".equals(code) || defaultMessage.toLowerCase().contains("must not be blank")) {
            return field + " is required";
        }
        if ("Size".equals(code)) {
            return field + " " + defaultMessage;
        }

        // Specific handling for amountUsd conversion/positive errors
        if ("amountUsd".equalsIgnoreCase(field)) {
            String lower = defaultMessage.toLowerCase();
            if (lower.contains("convert") || lower.contains("number") || lower.contains("numeric") || lower.contains("type mismatch") || lower.contains("failed to convert")) {
                return AMOUNT_USD_MSG;
            }
            if (lower.contains("must be greater") || lower.contains("must be positive") || lower.contains("greater than 0") || lower.contains("must be greater than 0")) {
                return AMOUNT_USD_MSG;
            }
        }

        return field + " " + defaultMessage;
    }

    private String mapConstraintViolation(ConstraintViolation<?> violation) {
        String msg = violation.getMessage();
        if (msg == null) {
            return "Invalid request parameter";
        }
        String lower = msg.toLowerCase();
        // currency mapping preserved
        if (lower.contains("treasurycurrency") || lower.contains("3-letter") || lower.contains("currency code")) {
            return "Currency Code must be a valid 3-letter ISO code (examples: USD, CAD, JPY)";
        }
        // amountUsd constraint mapping
        String path = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString().toLowerCase();
        if (path.contains("amountusd") || lower.contains("amountusd") || lower.contains("greater than 0") || lower.contains("must be positive")) {
            return AMOUNT_USD_MSG;
        }
        return msg;
    }

    public static record ErrorResponse(int status, String message, List<String> errors) {}
}
