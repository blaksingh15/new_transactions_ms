package com.transactions.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import com.transactions.entity.Transaction;
import com.transactions.entity.TransactionAdditional;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
public interface TransactionService {
	    String addTransaction(Transaction transaction);
	    Transaction saves2sTrans(Transaction transaction);
	    Map<String, Object> getTransactionDetails(String transID, Boolean isCheckoutBoolean);
	    Transaction getTransactionByTransID(Long transID);
	    List<Transaction> getAllTransactions(Sort sort);
	    List<Map<String, Object>> getAllTransactionsWithDetails();
	    List<Map<String, Object>> searchTransactions(String reference, Long merchantID, Short status);
	    List<Transaction> findAllByOrderByTransactionDateDesc();
	    ResponseEntity<Map<String, Object>> updateTransStatus(String transID, HttpServletRequest request, Boolean isWebhookBoolean, Boolean isRefundBoolean, Map<String, Object> getWebhookResponse);
	    Mono<ResponseEntity<Map<String, Object>>> updateTransStatusReactive( String transID, ServerWebExchange exchange, Boolean isWebhookBoolean, Boolean isRefundBoolean );
	    void updateTransactionID(Long id, Long transID);
	    List<Map<String, Object>> getAllTransactionsByMerchantID(Long merchantID);
	    List<Map<String, Object>> getCsvAllTransactionsByMerchantID(Long merchantID, String requestType);
	    List<Map<String, Object>> findLatest10ApprovedByTransactionDateDesc(Short status_id, Long merchantID, int page, int size);
	    long countByTransactionStatus(short status);
	    Long countByTransactionStatusAndMerchantID(Short status, Long merchantID);
	    Long countByTransactionStatusAndMerchantIDAndDateRange(Short status, Long merchantID, LocalDateTime startDate, LocalDateTime endDate);
	    Double sumBillAmountByStatuses(Short statuses);
	    Double sumBillAmountByStatusesAndMerchantID(Short status, Long merchantID);
	    Double sumBillAmountByStatusesAndMerchantIDAndDateRange(Short status, Long merchantID, LocalDateTime startDate, LocalDateTime endDate);
	    Transaction findByReferenceAndMerchantID(String reference, Long merchantID);
	    Map<String, Object> getSuccessRateByMerchantIDAndDateRange(Long merchantID, LocalDateTime startDate, LocalDateTime endDate);
	    Map<String, Object> getMetricsByTerminalNumber(Long terminalNumber);
	    @Cacheable(value = "transactionCache", key = "#id", unless = "#result == null")
	    Optional<Transaction> getTransactionById(Long id);
	    @Cacheable(value = "transactionListCache", key = "'allTransactions'")
	    List<Transaction> getAllTransactions();
	    @Cacheable(value = "transactionCache", key = "#transactionId", unless = "#result == null")
	    Optional<Transaction> findByTransactionId(String transactionId);
	    @CachePut(value = "transactionCache", key = "#transaction.id")
	    Transaction saveTransaction(Transaction transaction);
	    @CacheEvict(value = {"transactionCache", "transactionListCache"}, allEntries = true)
	    boolean deleteTransaction(Long id);
	    Page<Transaction> getAllTransactions(Pageable pageable);
	    Page<Transaction> getTransactionsByMerchantID(Long merchantID, Pageable pageable);
	    Page<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	    Page<Transaction> getTransactionsByMerchantIDAndDateRange(Long merchantID, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	    Page<Map<String, Object>> searchTransactionsWithPagination(String reference, Long merchantID, Short status, Pageable pageable);
	    Page<Transaction> findAllWithFilters(Specification<Transaction> spec, Pageable pageable);
	    Map<String, Object> checkVolumeRegulatory(Long merchantID, String billEmail, String regulatoryDays, double volumeLimit, int successLimit, int failLimit, double billAmount);
	    Map<String, Object> checkVolumeRegulatoryDynamic(Long merchantId, String fieldNameGet, String fieldValue, String period, double volumeLimit, int successLimit, int failLimit, double billAmount);
	    Map<String, Object> getUnifiedAnalytics(Long merchantID, String period, LocalDateTime startDate, LocalDateTime endDate, String groupType, int responseType, String previous);
	    java.util.List<java.util.Map<String, Object>> searchTransactionsByFilters(Long merchantId, java.util.Map<String, Object> filters);
	    java.util.Map<String, Object> getTransactionStatusSummaryByBillIp(Long merchantId, String billIp, LocalDateTime startDate, LocalDateTime endDate);
	    java.util.Map<String, Object> getTransactionStatusSummaryByTerminal(Long merchantId, Long terminalNumber, LocalDateTime startDate, LocalDateTime endDate);
	    TransactionAdditional saves2sTransAdditional(TransactionAdditional transactionAdditional);
	    Transaction updateTransaction(String transID, Transaction transaction, TransactionAdditional additional);
		Transaction saveTransaction(Transaction transaction, TransactionAdditional transactionAdditional);
		 public  Specification<Transaction> getTransactionSpecification(Long merchantID, LocalDateTime startDate,LocalDateTime endDate, String connectorId, Short status, String keyName,String keyValue, Map<String, Object> getReq ) ;
		
}
