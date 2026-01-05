package com.transactions.service;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import com.transactions.entity.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionServiceWebFlux {
  Optional<Transaction> getTransactionById(Long id);
  Mono<ResponseEntity<Map<String, Object>>> updateTransStatusReactive(String transID,ServerWebExchange exchange, Boolean isWebhookBoolean, Boolean isRefundBoolean);
}
