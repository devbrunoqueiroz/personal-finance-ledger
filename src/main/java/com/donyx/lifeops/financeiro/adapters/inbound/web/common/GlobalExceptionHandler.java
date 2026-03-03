package com.donyx.lifeops.financeiro.adapters.inbound.web.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROP_TIMESTAMP = "timestamp";
    private static final String PROP_PATH = "path";
    private static final String PROP_ERRORS = "errors";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("https://lifeops/errors/validation"));
        pd.setTitle("Validation error");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty(PROP_TIMESTAMP, Instant.now().toString());
        pd.setProperty(PROP_PATH, req.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty(PROP_ERRORS, errors);

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        // temporário: mapeia a tua bagunça atual pra um contrato estável
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(422));
        pd.setType(URI.create("https://lifeops/errors/business-rule"));
        pd.setTitle("Business rule violation");
        pd.setDetail(ex.getMessage());
        pd.setProperty(PROP_TIMESTAMP, Instant.now().toString());
        pd.setProperty(PROP_PATH, req.getRequestURI());
        return ResponseEntity.status(HttpStatusCode.valueOf(422)).body(pd);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> handleSpringErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        pd.setProperty(PROP_TIMESTAMP, Instant.now().toString());
        pd.setProperty(PROP_PATH, req.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("https://lifeops/errors/unexpected"));
        pd.setTitle("Unexpected error");
        pd.setDetail("An unexpected error occurred.");
        pd.setProperty(PROP_TIMESTAMP, Instant.now().toString());
        pd.setProperty(PROP_PATH, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

}
