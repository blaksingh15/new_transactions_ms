package com.transactions.controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.service.TransactionServiceWebFlux;
import com.transactions.utility.AES256Util;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/status/webflux")
@CrossOrigin
public class TransactionControllerWebFluxController {

    private static final Logger log = LoggerFactory.getLogger(TransactionControllerWebFluxController.class);

    @Autowired
    private TransactionServiceWebFlux transactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(
        value = "/transid/{transID}",
        method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS}
    )
    public Mono<ResponseEntity<?>> updateTransStatusMono(
            @PathVariable(required = false) String transID,
            ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, PUT, OPTIONS")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                            "Content-Type, Authorization, Origin, Accept, X-Requested-With")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                    .header(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600")
                    .build());
        }

        return exchange.getFormData()
                .defaultIfEmpty(new org.springframework.util.LinkedMultiValueMap<>())
                .flatMap(formData -> {
                    Map<String, Object> postParams = new HashMap<>();
                    formData.forEach((key, value) -> {
                        if (!value.isEmpty()) postParams.put(key, value.get(0));
                    });

                    String effectiveTransID = determineTransID(transID, postParams);
                    if (effectiveTransID == null || effectiveTransID.isEmpty()) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of(
                                "error", "Invalid transaction ID"
                        )));
                    }
                    return transactionService.updateTransStatusReactive(effectiveTransID, null, false, false)
                            .flatMap(serviceResponse -> {
                                Map<String, Object> body = serviceResponse.getBody();
                                if (body == null || body.isEmpty()) {
                                    return Mono.just(ResponseEntity.status(404)
                                            .body(Map.of("error", "Transaction not found")));
                                }

                                if (body.containsKey("redirect_url")) {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.add("Location", body.get("redirect_url").toString());
                                    return Mono.just(ResponseEntity.status(307).headers(headers).build());
                                }

                                return Mono.just(ResponseEntity.ok(body));
                            })
                            .onErrorResume(e -> {
                                log.error("Error updating transaction status: ", e);
                                return Mono.just(ResponseEntity.status(500).body(Map.of(
                                        "error", "Internal server error",
                                        "message", e.getMessage()
                                )));
                            });
                });
    }


    private String determineTransID(String pathTransID, Map<String, Object> postParams) {
        String transID = (pathTransID != null && !pathTransID.isEmpty())? pathTransID :(String) postParams.get("transID");
        if (transID == null) return null;
        try {
            if (transID.contains("=") || transID.contains("%") || transID.contains("+") || transID.contains("%2B")) {
                transID = AES256Util.decrypt(transID);
            }
        } catch (Exception e) {
            log.error("Error decrypting transID '{}': {}", transID, e.getMessage());
        }

        return transID;
    }

    public String toJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error converting to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) return Map.of();
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return Map.of();
        }
    }
}
