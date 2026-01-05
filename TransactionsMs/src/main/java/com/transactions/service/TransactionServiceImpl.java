package com.transactions.service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.BiFunction;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.client.ConnectorFeignClient;
import com.transactions.client.TerminalFeignClient;
import com.transactions.entity.Connector;
import com.transactions.entity.Terminal;
import com.transactions.entity.Transaction;
import com.transactions.entity.TransactionAdditional;
import com.transactions.repository.TransactionAdditionalRepository;
import com.transactions.repository.TransactionRepository;
import com.transactions.utility.AES256Api;
import com.transactions.utility.AES256Util;
import com.transactions.utility.Base64Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Subquery; 
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Set; 
import java.util.HashSet;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    @Autowired
    private TransactionRepository transactionDao;
    
    @Autowired
    private TransactionAdditionalRepository transactionAdditionalRepo;
    
    @Autowired
    private  TerminalFeignClient terminalClient;
    
    @Autowired
    private  ConnectorFeignClient connectorClient;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Transaction> getAllTransactions(Sort sort) {
        return transactionDao.findAll(sort);
    }

    @Override
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionDao.findAll(pageable);
    }

    @Override
    public Page<Transaction> getTransactionsByMerchantID(Long merchantID, Pageable pageable) {
        return transactionDao.findByMerchantID(merchantID, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionDao.findByTransactionDateBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsByMerchantIDAndDateRange(Long merchantID, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionDao.findByMerchantIDAndTransactionDateBetween(merchantID, startDate, endDate, pageable);
    }

    @Override
    public Page<Map<String, Object>> searchTransactionsWithPagination(String reference, Long merchantID, Short status, Pageable pageable) {
        Page<Transaction> transactionsPage = transactionDao.searchTransactions(reference, merchantID, status, pageable);
        return transactionsPage.map(transaction -> {
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("transaction", transaction);
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
            if (additional != null) {
                Map<String, Object> additionalMap = new HashMap<>();
                additionalMap.put("transactionResponse", additional.getTransactionResponse());
                additionalMap.put("connectorRef", additional.getConnectorRef());
                additionalMap.put("payloadStage1", additional.getPayloadStage1());
                additionalMap.put("connectorCredsProcessingFinal", additional.getConnectorCredsProcessingFinal());
                additionalMap.put("returnUrl", additional.getReturnUrl());
                additionalMap.put("webhookUrl", additional.getWebhookUrl());
                additionalMap.put("sourceUrl", additional.getSourceUrl());
                additionalMap.put("connectorResponseStage1", additional.getConnectorResponseStage1());
                additionalMap.put("connectorResponseStage2", additional.getConnectorResponseStage2());
                additionalMap.put("merchantNote", additional.getMerchantNote());
                additionalMap.put("supportNote", additional.getSupportNote());
                additionalMap.put("systemNote", additional.getSystemNote());
                additionalMap.put("connectorResponse", Base64Util.decodeBase64(additional.getConnectorResponse()));
                combinedData.put("additional", additionalMap);
            } else {
                combinedData.put("additional", new HashMap<>());
            }
            
            return combinedData;
        });
    }

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    @Override
    public List<Map<String, Object>> getAllTransactionsWithDetails() {
        List<Transaction> transactions = transactionDao.findAllByOrderByTransactionDateDesc(); // Sorting applied
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("transID", String.valueOf(transaction.getTransID()));
            combinedData.put("transaction", transaction);
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
            if (additional != null) {
                Map<String, Object> additionalMap = new HashMap<>();
                additionalMap.put("transID", String.valueOf(transaction.getTransID()));
                additionalMap.put("id", additional.getId());
                additionalMap.put("binNumber", additional.getBinNumber());
                additionalMap.put("cardNumber", maskCardNumber(additional.getCcnoDecrypted()));
                additionalMap.put("ccnoDecrypted", additional.getCcnoDecrypted()); 
                additionalMap.put("billingPhone", additional.getBillingPhone());
                additionalMap.put("billingAddress", additional.getBillingAddress());
                additionalMap.put("billingCity", additional.getBillingCity());
                additionalMap.put("billingState", additional.getBillingState());
                additionalMap.put("billingCountry", additional.getBillingCountry());
                additionalMap.put("billingZip", additional.getBillingZip());
                additionalMap.put("productName", additional.getProductName());
                additionalMap.put("authUrl", additional.getAuthUrl());
                additionalMap.put("rrn", additional.getRrn());
                additionalMap.put("upa", additional.getUpa());
                additionalMap.put("descriptor", additional.getDescriptor());
                additionalMap.put("transactionResponse", additional.getTransactionResponse());
                additionalMap.put("connectorRef", additional.getConnectorRef());
                additionalMap.put("payloadStage1", additional.getPayloadStage1());
                additionalMap.put("connectorCredsProcessingFinal", additional.getConnectorCredsProcessingFinal());
                additionalMap.put("returnUrl", additional.getReturnUrl());
                additionalMap.put("webhookUrl", additional.getWebhookUrl());
                additionalMap.put("sourceUrl", additional.getSourceUrl());
                additionalMap.put("connectorResponseStage1", additional.getConnectorResponseStage1());
                additionalMap.put("connectorResponseStage2", additional.getConnectorResponseStage2());
                additionalMap.put("merchantNote", additional.getMerchantNote());
                additionalMap.put("supportNote", additional.getSupportNote());
                additionalMap.put("systemNote", additional.getSystemNote());
                additionalMap.put("connectorResponse", Base64Util.decodeBase64(additional.getConnectorResponse()));
                additionalMap.put("authData", Base64Util.decodeBase64(additional.getAuthData()));
                combinedData.put("additional", additionalMap);
            } else {
                combinedData.put("additional", new HashMap<>());
            }
            
            result.add(combinedData);
        }
        
        return result;
    }

    @Override
    public String addTransaction(Transaction transaction) {
        transactionDao.save(transaction);
        return "Transaction added successfully";
    }

    @Override
    public Transaction saveTransaction(Transaction transaction, TransactionAdditional additional) {
        Transaction savedTransaction = transactionDao.save(transaction);
        additional.setTransIDAd(savedTransaction.getTransID());
        transactionAdditionalRepo.save(additional);
        return savedTransaction;
    }

    @Override
    public Transaction updateTransaction(String transID, Transaction transaction, TransactionAdditional additional) {
        try {
            Long transactionId = Long.parseLong(transID);
            Transaction existingTransaction = transactionDao.findByTransID(transactionId);
            if (existingTransaction == null) {
                log.error("Transaction not found with ID: {}", transID);
                return null;
            }
            if (transaction.getTransactionStatus() != null) {
                existingTransaction.setTransactionStatus(transaction.getTransactionStatus());
            }
            Transaction updatedTransaction = transactionDao.save(existingTransaction);
            log.info("Transaction updated successfully: {}", transID);
            if (additional != null) {
                TransactionAdditional existingAdditional = transactionAdditionalRepo.findByTransIDAd(transactionId);
                if (existingAdditional == null) {
                    additional.setTransIDAd(transactionId);
                    transactionAdditionalRepo.save(additional);
                    log.info("Created new additional data for transaction: {}", transID);
                } else {
                    if (additional.getSupportNote() != null) {
                        existingAdditional.setSupportNote(additional.getSupportNote());
                    }
                    if (additional.getMerchantNote() != null) {
                        existingAdditional.setMerchantNote(additional.getMerchantNote());
                    }
                    if (additional.getSystemNote() != null) {
                        existingAdditional.setSystemNote(additional.getSystemNote());
                    }
                    transactionAdditionalRepo.save(existingAdditional);
                    log.info("Updated additional data for transaction: {}", transID);
                }
            }

            return updatedTransaction;
        } catch (Exception e) {
            log.error("Error updating transaction: {}", transID, e);
            return null;
        }
    }

    @Override
    public Transaction getTransactionByTransID(Long transID) {
        return transactionDao.findByTransID(transID);
    }

    @Override
    public List<Map<String, Object>> searchTransactions(String reference, Long merchantID, Short status) {
        try {
            log.debug("Searching transactions with reference: {}, merchantID: {}, status: {}", reference, merchantID, status);
            Page<Transaction> transactionsPage = transactionDao.searchTransactions(reference, merchantID, status, Pageable.unpaged());
            List<Transaction> transactions = transactionsPage.getContent();
            if (transactions == null || transactions.isEmpty()) {
                log.debug("No transactions found matching criteria");
                return new ArrayList<>();
            }
            List<TransactionAdditional> additionalDetails = transactionAdditionalRepo.findAll();
            Map<Long, TransactionAdditional> additionalMap = new HashMap<>();
            for (TransactionAdditional additional : additionalDetails) {
                if (additional != null && additional.getTransIDAd() != null) {
                    try {
                        Long transId = Long.valueOf(additional.getTransIDAd());
                        additionalMap.put(transId, additional);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid transaction ID format in additional data: {}", additional.getTransIDAd());
                    }
                }
            }
            
            List<Map<String, Object>> combinedData = new ArrayList<>();
            for (Transaction transaction : transactions) {
                if (transaction != null && transaction.getTransID() != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("transaction", transaction);
                    data.put("additional", additionalMap.get(transaction.getTransID()));
                    combinedData.add(data);
                }
            }
            
            log.debug("Found {} matching transactions", combinedData.size());
            return combinedData;

        } catch (Exception e) {
            log.error("Error searching transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> searchTransactionsByFilters(Long merchantId, Map<String, Object> filters) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
            Root<Transaction> root = cq.from(Transaction.class);

            List<Predicate> predicates = new ArrayList<>();

            if (merchantId != null && merchantId > 0) {
                predicates.add(cb.equal(root.get("merchantID"), merchantId));
            }

            Object transactionId = filters.get("transactionId");
            if (transactionId != null) {
                try {
                    Long txId = Long.parseLong(transactionId.toString());
                    predicates.add(cb.equal(root.get("transID"), txId));
                } catch (NumberFormatException e) {
                    log.warn("Ignoring invalid transactionId filter: {}", transactionId);
                }
            }

            Object reference = filters.get("reference");
            if (reference != null) {
                predicates.add(cb.equal(cb.lower(root.get("reference")), reference.toString().trim().toLowerCase()));
            }

            Object terminalNumber = filters.get("terminalNumber");
            if (terminalNumber != null) {
                try {
                    Long terminal = Long.parseLong(terminalNumber.toString());
                    predicates.add(cb.equal(root.get("terminalNumber"), terminal));
                } catch (NumberFormatException e) {
                    log.warn("Ignoring invalid terminalNumber filter: {}", terminalNumber);
                }
            }

            Object billEmail = filters.get("billEmail");
            if (billEmail != null) {
                predicates.add(cb.equal(cb.lower(root.get("billEmail")), billEmail.toString().trim().toLowerCase()));
            }

            Object billAmountOperator = filters.get("billAmountOperator");
            Object billAmountValue = filters.get("billAmountValue");
            if (billAmountOperator != null && billAmountValue != null) {
                try {
                    Double amountValue = Double.parseDouble(billAmountValue.toString());
                    switch (billAmountOperator.toString()) {
                        case "GT" -> predicates.add(cb.greaterThan(root.get("billAmount"), amountValue));
                        case "GTE" -> predicates.add(cb.greaterThanOrEqualTo(root.get("billAmount"), amountValue));
                        case "LT" -> predicates.add(cb.lessThan(root.get("billAmount"), amountValue));
                        case "LTE" -> predicates.add(cb.lessThanOrEqualTo(root.get("billAmount"), amountValue));
                        case "EQ" -> predicates.add(cb.equal(root.get("billAmount"), amountValue));
                        default -> log.warn("Unsupported bill amount operator: {}", billAmountOperator);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Ignoring invalid bill amount value: {}", billAmountValue);
                }
            }

            cq.select(root).where(predicates.toArray(new Predicate[0]))
              .orderBy(cb.desc(root.get("transactionDate")));

            TypedQuery<Transaction> query = entityManager.createQuery(cq);
            query.setMaxResults(25);
            List<Transaction> candidates = query.getResultList();

            if (candidates.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> candidateIds = candidates.stream()
                    .map(Transaction::getTransID)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<Long, TransactionAdditional> additionalMap = new HashMap<>();
            if (!candidateIds.isEmpty()) {
                List<TransactionAdditional> additionalList = transactionAdditionalRepo.findByTransIDAdIn(candidateIds);
                for (TransactionAdditional additional : additionalList) {
                    if (additional != null && additional.getTransIDAd() != null) {
                        additionalMap.put(additional.getTransIDAd(), additional);
                    }
                }
            }

            String billingCityFilter = normalizeFilter(filters.get("billingCity"));
            String billingStateFilter = normalizeFilter(filters.get("billingState"));
            String billingCountryFilter = normalizeFilter(filters.get("billingCountry"));

            List<Map<String, Object>> results = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Transaction tx : candidates) {
                TransactionAdditional additional = additionalMap.get(tx.getTransID());

                if (!matchesAdditionalFilter(billingCityFilter, additional != null ? additional.getBillingCity() : null)) {
                    continue;
                }
                if (!matchesAdditionalFilter(billingStateFilter, additional != null ? additional.getBillingState() : null)) {
                    continue;
                }
                if (!matchesAdditionalFilter(billingCountryFilter, additional != null ? additional.getBillingCountry() : null)) {
                    continue;
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("transactionId", tx.getTransID());
                Short statusCode = tx.getTransactionStatus();
                row.put("statusCode", statusCode);
                row.put("status", getStatusDes(statusCode != null ? statusCode.toString() : null));
                row.put("billAmount", tx.getBillAmount());
                row.put("billCurrency", tx.getBillCurrency());
                row.put("terminalNumber", tx.getTerminalNumber());
                row.put("reference", tx.getReference());
                row.put("billEmail", tx.getBillEmail());
                row.put("transactionDate", tx.getTransactionDate() != null ? tx.getTransactionDate().format(formatter) : null);

                if (additional != null) {
                    row.put("response", additional.getTransactionResponse());
                    row.put("billingCity", additional.getBillingCity());
                    row.put("billingState", additional.getBillingState());
                    row.put("billingCountry", additional.getBillingCountry());
                }

                results.add(row);
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to search transactions by filters", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getTransactionStatusSummaryByBillIp(Long merchantId, String billIp, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("billIp", billIp);
        summary.put("merchantId", merchantId);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        long successCount = 0L;
        double successAmount = 0.0;
        long failedCount = 0L;
        double failedAmount = 0.0;
        long expiredCount = 0L;
        double expiredAmount = 0.0;

        List<Object[]> rows = transactionDao.getStatusSummaryByBillIp(merchantId, billIp, startDate, endDate);
        for (Object[] row : rows) {
            if (row == null || row.length < 3) {
                continue;
            }
            Short status = row[0] != null ? ((Number) row[0]).shortValue() : null;
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            double amount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            if (status == null) {
                continue;
            }

            if (status == 1 || status == 7) {
                successCount += count;
                successAmount += amount;
            } else if (status == 2 || status == 23 || status == 24) {
                failedCount += count;
                failedAmount += amount;
            } else if (status == 22) {
                expiredCount += count;
                expiredAmount += amount;
            }
        }

        long totalCount = successCount + failedCount + expiredCount;
        double totalAmount = successAmount + failedAmount + expiredAmount;

        summary.put("successCount", successCount);
        summary.put("successAmount", round2(successAmount));
        summary.put("failedCount", failedCount);
        summary.put("failedAmount", round2(failedAmount));
        summary.put("expiredCount", expiredCount);
        summary.put("expiredAmount", round2(expiredAmount));
        summary.put("totalCount", totalCount);
        summary.put("totalAmount", round2(totalAmount));

        return summary;
    }

    @Override
    public Map<String, Object> getTransactionStatusSummaryByTerminal(Long merchantId, Long terminalNumber, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("terminalNumber", terminalNumber);
        summary.put("merchantId", merchantId);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        long successCount = 0L;
        double successAmount = 0.0;
        long failedCount = 0L;
        double failedAmount = 0.0;
        long expiredCount = 0L;
        double expiredAmount = 0.0;

        List<Object[]> rows = transactionDao.getStatusSummaryByTerminal(merchantId, terminalNumber, startDate, endDate);
        for (Object[] row : rows) {
            if (row == null || row.length < 3) {
                continue;
            }
            Short status = row[0] != null ? ((Number) row[0]).shortValue() : null;
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            double amount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            if (status == null) {
                continue;
            }

            if (status == 1 || status == 7) {
                successCount += count;
                successAmount += amount;
            } else if (status == 2 || status == 23 || status == 24) {
                failedCount += count;
                failedAmount += amount;
            } else if (status == 22) {
                expiredCount += count;
                expiredAmount += amount;
            }
        }

        long totalCount = successCount + failedCount + expiredCount;
        double totalAmount = successAmount + failedAmount + expiredAmount;

        summary.put("successCount", successCount);
        summary.put("successAmount", round2(successAmount));
        summary.put("failedCount", failedCount);
        summary.put("failedAmount", round2(failedAmount));
        summary.put("expiredCount", expiredCount);
        summary.put("expiredAmount", round2(expiredAmount));
        summary.put("totalCount", totalCount);
        summary.put("totalAmount", round2(totalAmount));

        return summary;
    }

    public Transaction saves2sTrans(Transaction transaction) {
        return transactionDao.save(transaction);
    }

    public TransactionAdditional saves2sTransAdditional(TransactionAdditional transactionAdditional) {
        return transactionAdditionalRepo.save(transactionAdditional);
    }
    
    

    public ResponseEntity<Map<String, Object>> updateTransStatus(String transID, HttpServletRequest request, Boolean isWebhookBoolean, Boolean isRefundBoolean, Map<String, Object> getWebhookResponse) {
        log.debug("Fetching transaction details for transID: {}", transID);
        boolean isRefund = (isRefundBoolean != null) ? isRefundBoolean : false;
        String connector_payin_file = "Status_";
        if(isRefund) {
            connector_payin_file = "Refund_";
        } 
		Map<String, Object> response = new HashMap<>();
        boolean updateDb = false;	
        boolean adminRes = false; 
        boolean checkoutRes = false; 
        boolean isCronjob = false; 
        Integer  tr_status = 0;
        Integer  status_code_set = 0;
        String  orderStatus = "", 
                connector_status_code = "", 
                connector_response_msg = "", 
                feeId = "", 
                connector_payin = "", 
                merID = "", 
                webhook_url = "", 
                return_url = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
        String existingSystemNote = "";
        boolean systemNoteUpdateDb = false;
        String existingSupportNote = "";
        Map<String, Object> connectorData = new HashMap<>();
        Map<String, Object> credentials = new HashMap<>();
        Map<String, Object> getPost = new HashMap<>();
		try {

            if (request != null && request.getContentType() != null && request.getContentType().contains("application/json")) {
                try {
                    StringBuilder jsonBuilder = new StringBuilder();
                    BufferedReader reader = request.getReader();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    String jsonString = jsonBuilder.toString();
                    if (!jsonString.isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonBody = objectMapper.readValue(jsonString, Map.class);
                        getPost.putAll(jsonBody);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); 
                }
            }
            
           if (request != null) {
                if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
                    Map<String, String[]> parameterMap = request.getParameterMap();
                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        if (values != null && values.length > 0) {
                            getPost.put(key, values[0]);
                        }
                    }
                }
                else if ("GET".equalsIgnoreCase(request.getMethod())) {
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        String[] pairs = queryString.split("&");
                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=");
                            if (keyValue.length == 2) {
                                String key = null;
                                try {
                                    key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                                getPost.put(key, value);
                            }
                        }
                    }
                } 
            }

            String requestURI = request.getRequestURI(); 
            isCronjob = requestURI != null && (requestURI.contains("/s2s/cronjob/") || requestURI.contains("/status/s2s/cronjob/"));
            if (requestURI.contains("/status/s2s/admin")||requestURI.contains("/status/s2s/checkout")) {
                adminRes = true; 
            }
           
            if (requestURI.contains("/status/s2s/checkout")) {
                checkoutRes = true; 
            }

			Long transIDLong = Long.parseLong(transID);
			Optional<Transaction> transactionOpt = Optional.ofNullable(transactionDao.findByTransID(transIDLong));
			Optional<TransactionAdditional> additionalOpt = Optional.ofNullable(transactionAdditionalRepo.findByTransIDAd(transIDLong));
			if (transactionOpt.isPresent()) {
				Transaction transaction = transactionOpt.get();
				TransactionAdditional additional = additionalOpt.get();
                existingSystemNote = additional.getSystemNote() != null ? additional.getSystemNote() : "";
                existingSupportNote = additional.getSupportNote() != null ? additional.getSupportNote() : "";
                connectorData.put("transID", transaction.getTransID() ==  null ? "" : transaction.getTransID().toString());
                connectorData.put("billAmount", transaction.getBillAmount() ==  null ? "" : transaction.getBillAmount().toString());
                connectorData.put("billCurrency", transaction.getBillCurrency() ==  null ? "" : transaction.getBillCurrency().toString());
                Double bankProcessingAmount = 0.0;
                String bankProcessingCurrency = "";
                if(transaction.getBankProcessingAmount() != null)
                    bankProcessingAmount = Double.parseDouble(transaction.getBankProcessingAmount().toString());
                else if(transaction.getTransactionAmount() != null)
                    bankProcessingAmount = Double.parseDouble(transaction.getTransactionAmount().toString());
                else if(transaction.getBillAmount() != null)
                    bankProcessingAmount = Double.parseDouble(transaction.getBillAmount().toString());
                connectorData.put("bankProcessingAmount", bankProcessingAmount);
                if(transaction.getBankProcessingCurrency() != null)
                    bankProcessingCurrency = transaction.getBankProcessingCurrency().toString();
                else if(transaction.getTransactionCurrency() != null)
                    bankProcessingCurrency = transaction.getTransactionCurrency().toString();
                else if(transaction.getBillCurrency() != null)
                    bankProcessingCurrency = transaction.getBillCurrency().toString();
                connectorData.put("bankProcessingCurrency", bankProcessingCurrency);
                orderStatus = transaction.getTransactionStatus() == null ? "0"
						: transaction.getTransactionStatus().toString(); 
                tr_status = orderStatus.equals("0") ? 0 : Integer.parseInt(orderStatus);
                connector_response_msg = (additional.getTransactionResponse() == null) ? "Payment is pending" : additional.getTransactionResponse();
                webhook_url = additional.getWebhookUrl().toString();
                if(getPost.containsKey("test_auth") && "approved".equals(getPost.get("test_auth")) && tr_status == 27) {
                    status_code_set = 25;
                    connector_response_msg = "3DS test card approved";
                }
                else if(getPost.containsKey("test_auth") && "declined".equals(getPost.get("test_auth")) && tr_status == 27) {
                    status_code_set = 26;
                    connector_response_msg = "3DS test card declined";
                }
                merID = transaction.getMerchantID().toString();
                feeId = transaction.getFeeId() !=null ? transaction.getFeeId().toString() : "";
                connectorData.put("merID", merID);
                connectorData.put("feeId", feeId);
                connectorData.put("connectorRef", additional.getConnectorRef() ==  null ? "" : additional.getConnectorRef());
                String getCredentials = (additional.getConnectorCredsProcessingFinal() !=null) ? additional.getConnectorCredsProcessingFinal() : "";
                if(getCredentials !=null) credentials = jsonde(getCredentials);
                String getConnector = (transaction.getConnector() != null) ? transaction.getConnector().toString() : "";
                
                if (getConnector != null) {
                   Connector connector = connectorClient.findByConnectorNumber(getConnector);
                    if (connector != null) {
                        connectorData.put("connector", connector);
                        String connectorBaseUrl = connector.getConnectorBaseUrl();
                        String connectorStatusUrl = connector.getConnectorStatusUrl();
                        String defaultConnector = connector.getDefaultConnector();
                        String connectorStatus = connector.getConnectorStatus();
                        String connectorRefundUrl = connector.getConnectorRefundUrl();
                        connectorData.put("connectorStatusUrl", connectorStatusUrl);
                        connectorData.put("connectorBaseUrl", connectorBaseUrl);
                        connectorData.put("connectorStatus", connectorStatus);
                        connectorData.put("connectorRefundUrl", connectorRefundUrl);
                        connector_payin = defaultConnector;
                        String notificationEmail = connector.getNotificationEmail();
                        String webhookNotification = connector.getWebhookNotification();
                        if(getWebhookResponse !=null && isWebhookBoolean && notificationEmail != null && "Y".equalsIgnoreCase(webhookNotification) && !notificationEmail.isEmpty() && webhookNotification != null && !webhookNotification.isEmpty()) 
                        {

                            String current_url_webhook = getWebhookResponse.get("current_url_webhook") != null ? getWebhookResponse.get("current_url_webhook").toString() : "";
                            String current_method_webhook = getWebhookResponse.get("current_method_webhook") != null ? getWebhookResponse.get("current_method_webhook").toString() : "";
                            String referer_url_webhook = getWebhookResponse.get("referer_url_webhook") != null ? getWebhookResponse.get("referer_url_webhook").toString() : "";
                            try {
                                sendWebhookLogMail(notificationEmail, "Real Time Webhook Notification", jsonen(getWebhookResponse), jsonen(response), current_url_webhook, current_method_webhook, referer_url_webhook , transID, orderStatus);
                            } catch (Exception e) {
                                log.error("Error sending webhook email: ", e);
                            }
                            getWebhookResponse.put("isWebhookAction","Y");

                        }
                        
                        
                    }
                }

            if(connector_payin != null && connectorData !=null && credentials !=null ) 
                { 
                    try {
                        String connectorClassName = "com.webapp.controller.payin.pay_" + connector_payin + "." + connector_payin_file + connector_payin;
                        Class<?> connectorClass = Class.forName(connectorClassName);
                        Object connectorInstance = connectorClass.getDeclaredConstructor().newInstance();
                        Method mapPayloadMethod = connectorClass.getMethod("mapPayload", Object.class);
                        Map<String, Object> payload = new HashMap<>();
                        payload.putAll(connectorData);
                        payload.put("apc_get", credentials); 
                        if(isWebhookBoolean && getWebhookResponse !=null) {
                            payload.put("getWebhookReq", getWebhookResponse); 
                        }
                        
                        Object mappedPayload = mapPayloadMethod.invoke(connectorInstance, payload);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mappedResponseMap = (Map<String, Object>) mappedPayload;
                        if (mappedResponseMap.containsKey("connector_response_msg") && mappedResponseMap.get("connector_response_msg").toString() != null && mappedResponseMap.get("connector_response_msg").toString() != "Unknown") {
                            connector_response_msg = mappedResponseMap.get("connector_response_msg").toString();

                        }
                        Boolean isStatusCode = false;
                        if (mappedResponseMap.containsKey("connector_status_code") || (status_code_set != null && status_code_set != 0)) {
                            connector_status_code = mappedResponseMap.get("connector_status_code") != null ? mappedResponseMap.get("connector_status_code").toString() : status_code_set.toString();
                            Integer csc_int = Integer.parseInt(connector_status_code);
                            
                            if( (tr_status == 0 || tr_status == 22 || tr_status == 23) && (csc_int == 1 || csc_int == 2 || csc_int == 22 || csc_int == 23 || csc_int == 10) && (connector_status_code != null && !connector_status_code.isEmpty())) {
                                isStatusCode = true;
                            }
                            else if( (tr_status == 27) && (status_code_set == 25 || status_code_set == 26) && (connector_status_code != null && !connector_status_code.isEmpty())) {
                                isStatusCode = true;
                            } 
                            
                            if (isStatusCode) {
                                updateDb = true;
                                orderStatus = connector_status_code;	
                                transaction.setTransactionStatus(Short.parseShort(connector_status_code));
                                transactionDao.save(transaction); // Save the updated transaction
                                log.debug("Transaction status updated from {} to {}", orderStatus, connector_status_code);
                                systemNoteUpdateDb = true;
                                currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
                                if(existingSystemNote != null && existingSystemNote.length() > 0) {
                                    existingSystemNote += "\n";
                                }
                                existingSystemNote += currentDateTime + " | " + connector_payin_file + " " + getStatusDes(orderStatus) + " - " + connector_status_code;
                                if (connector_response_msg != null && !connector_response_msg.isEmpty()) {
                                    existingSystemNote += " - " + connector_response_msg;
                                }
                                if (isCronjob) {
                                    existingSystemNote += " via Cron";
                                }

                                if(existingSupportNote != null && existingSupportNote.length() > 0) {
                                    existingSupportNote += "\n";
                                }
                                existingSupportNote += currentDateTime + " | " + connector_payin_file + " " + getStatusDes(orderStatus);
                                additional.setSupportNote(existingSupportNote);
                                transactionAdditionalRepo.save(additional); 
                            }
                            
                            orderStatus = connector_status_code;
                            response.put("connector_status_code", connector_status_code);
                        }
                        
                        if (connector_response_msg != null && !connector_response_msg.isEmpty()) {
                            if(updateDb){
                                additional.setTransactionResponse(connector_response_msg);
                                transactionAdditionalRepo.save(additional); 
                            }
                            if(adminRes || checkoutRes)
                            response.put("connector_response_msg", connector_response_msg);
                        }
                        if (mappedResponseMap.containsKey("gateway_response")) {
                            if(adminRes)
                            response.put("gateway_response", mappedResponseMap.get("gateway_response")); 
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }	
              if(checkoutRes) {
                    String authdata64 = additional.getAuthData() != null ? additional.getAuthData() : "";
                    String authDataJson = Base64Util.decodeBase64(authdata64);
                    Map<String, Object> authDataMapDecode = new HashMap<>();
                    try {
                        if (authDataJson != null && !authDataJson.equals("null") && !authDataJson.isEmpty()) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            authDataMapDecode = objectMapper.readValue(authDataJson, new TypeReference<Map<String, Object>>() {});
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse authdata JSON: {}", e.getMessage());
                    }

                    response.put("authdata", authDataMapDecode);
                    if(authDataMapDecode.containsKey("payaddress") && authDataMapDecode.get("payaddress") != null && !authDataMapDecode.get("payaddress").toString().isEmpty()) {
                        response.put("payaddress", authDataMapDecode.get("payaddress"));
                    }
                    if(authDataMapDecode.containsKey("action") && authDataMapDecode.get("action") != null && !authDataMapDecode.get("action").toString().isEmpty()) {
                        response.put("action", authDataMapDecode.get("action"));
                    }
                    
                    if(authDataMapDecode.containsKey("payamt") && authDataMapDecode.get("payamt") != null) {
                        response.put("payamt", Double.parseDouble(authDataMapDecode.get("payamt").toString()));
                    }
                    
                    if(authDataMapDecode.containsKey("paytitle") && authDataMapDecode.get("paytitle") != null && !authDataMapDecode.get("paytitle").toString().isEmpty()) {
                        response.put("paytitle", authDataMapDecode.get("paytitle"));
                    }
                    
                    if(authDataMapDecode.containsKey("coinName") && authDataMapDecode.get("coinName") != null && !authDataMapDecode.get("coinName").toString().isEmpty()) {
                        response.put("paycurrency", authDataMapDecode.get("coinName"));
                    }
                    else if(authDataMapDecode.containsKey("paycurrency") && authDataMapDecode.get("paycurrency") != null && !authDataMapDecode.get("paycurrency").toString().isEmpty()) {
                        response.put("paycurrency", authDataMapDecode.get("paycurrency"));
                    }

                    if(additional.getReturnUrl() != null && !additional.getReturnUrl().isEmpty()) {
                        response.put("return_url", additional.getReturnUrl());
                    }
                    if(additional.getWebhookUrl() != null && !additional.getWebhookUrl().isEmpty()) {
                        response.put("webhook_url", additional.getWebhookUrl());
                    }
                    try {
                        String payloadStage1 = additional.getPayloadStage1();
                        if(payloadStage1 != null && !payloadStage1.isEmpty()) {
//                            JSONObject jsonPayload = new JSONObject(payloadStage1);
//                            if(jsonPayload.has("public_key")) {
//                                String publicKey = jsonPayload.getString("public_key");
//                                response.put("public_key", publicKey);
//                            }
                        }
                    } catch(Exception e) {
                        log.error("Error parsing public_key from payload_stage1: {}", e.getMessage());
                    }

                }
				String decryptedCCNO = additional.getCcnoDecrypted();
				String statusDescription = getStatusDes(orderStatus); 
				response.put("transID", transaction.getTransID().toString());
				response.put("order_status", orderStatus);
				response.put("status", statusDescription);
				response.put("bill_amt", transaction.getBillAmount().toString());
                String formattedDate = transaction.getTransactionDate().format(formatter);
				response.put("tdate", formattedDate);
				response.put("bill_currency", transaction.getBillCurrency());
				response.put("response", connector_response_msg);
				response.put("reference", transaction.getReference());
				response.put("mop", transaction.getMethodOfPayment());
				if (additionalOpt.isPresent()) {
					response.put("descriptor", additional.getDescriptor());
					response.put("upa", additional.getUpa());
					response.put("rrn", additional.getRrn());
					
					if(decryptedCCNO != null) {
						response.put("ccno", maskCardNumber(decryptedCCNO)); 
					}
					if(orderStatus.equals("0")) {
                        response.put("authurl", additional.getAuthUrl());
                        response.put("authdata", additional.getAuthData());
                    }
				} else {
					response.put("descriptor", null);
					response.put("upa", null);
					response.put("rrn", null);
					response.put("ccno", null);
				}

                if (webhook_url != null && !webhook_url.isEmpty() && updateDb) {
                    Instant now = Instant.now();
                    Timestamp notifyTime = Timestamp.from(now);
                    String formattedNotifyTime = dateFormat.format(notifyTime);
                    response.put("webhook_notify_time", formattedNotifyTime);
                    if(transaction.getIntegrationType() != null && transaction.getIntegrationType().contains("_aes256")) 
                    {
                        try {
                            String terminalNumber = transaction.getTerminalNumber() !=null ? transaction.getTerminalNumber().toString() : "";
                            Terminal terminal = terminalClient.getTerminalById(Integer.parseInt(terminalNumber));
                            if (terminal == null) {
                                response.put("error", "Terminal not found for terminalNumber: " + terminalNumber);
                                return ResponseEntity.ok(response);
                            }
                            String privateKey = terminal.getPrivateKey() == null ? "" : terminal.getPrivateKey().toString();
                            String publicKey = terminal.getPublicKey() == null ? "" : terminal.getPublicKey().toString();

                            if (privateKey == null || privateKey.isEmpty()) {
                                throw new IllegalArgumentException("Private key is missing for webhook notification");
                            }
                            if (publicKey == null || publicKey.isEmpty()) {
                                throw new IllegalArgumentException("Public key is missing for webhook notification");
                            }

                            String encryptedResponse = AES256Api.encrypt(jsonen(response), privateKey, publicKey);
		                    Map<String, Object> responseEncrypted = new HashMap<>();

                                responseEncrypted.put("private_key", privateKey);
                                responseEncrypted.put("public_key", publicKey);
                            responseEncrypted.put("data", encryptedResponse);
                            sendWebhookNotification(webhook_url, responseEncrypted);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        sendWebhookNotification(webhook_url, response);
                    }
                    
                    systemNoteUpdateDb = true;
                    currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
                    if(existingSystemNote != null && existingSystemNote.length() > 0) {
                        existingSystemNote += "\n";
                    }
                    existingSystemNote += currentDateTime + " | " + connector_payin_file + statusDescription  + " - Webhook notification sent to " + webhook_url ; 
                    log.debug("Webhook notification sent to {} at {} with response is {}", webhook_url, formattedNotifyTime, response);

                }

                if(systemNoteUpdateDb && existingSystemNote != null && existingSystemNote.length() > 0) {
                    additional.setSystemNote(existingSystemNote);
                    transactionAdditionalRepo.save(additional); 
                }

                return_url = additional.getReturnUrl();
                if (return_url != null && !return_url.isEmpty() && !isWebhookBoolean) {
                    try {
                        Map<String, String> params = new HashMap<>();
                        response.forEach((key, value) -> {
                            if (value != null) {
                                params.put(key, String.valueOf(value));
                            }
                        });
                        params.values().removeIf(value -> value == null || value.isEmpty());
                        return_url = normalizeAndBuildUrl(return_url, params);
                        log.debug("Final redirect URL: {}", return_url);
                        Map<String, Object> redirectResponse = new HashMap<>();
                        redirectResponse.put("redirect_url", return_url);
                        return ResponseEntity.status(302)
                                .header("Location", return_url)
                                .body(redirectResponse);

                    } catch (Exception e) {
                        log.error("Error building redirect URL: {}", e.getMessage());
                        response.put("error", "Failed to build redirect URL");
                        return ResponseEntity.ok(response);
                    }
                }
                return ResponseEntity.ok(response);
                }
			} 
            catch (NumberFormatException e) {
                response.put("error_number", "400");
                response.put("error_message", "Invalid transID format. Expected numeric value.");
                response.put("status", "Error");
                return ResponseEntity.ok(response);
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(response);
    }
   
    
    private void sendWebhookLogMail(String emailAddress, String subject , String reqData , String getResponse, String current_url_webhook, String current_method_webhook, String referer_url_webhook, String transID, String status)
    {

		StringBuilder emailBody = new StringBuilder();
		emailBody.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
		emailBody.append("<div style='max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px;'>");
        emailBody.append("<h2 style='color: #2c3e50;'>transID : " + transID + "</h2>");
        emailBody.append("<p>Status : " + status + "</p>");
        String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
        emailBody.append("<p>Webhook Date : " + currentDateTime + "</p>");
		emailBody.append("<h2 style='color: #2c3e50;'>Webhook URL : " + current_url_webhook + "</h2>");
		emailBody.append("<p>Method : " + current_method_webhook + "</p>");
		if(referer_url_webhook != null && !referer_url_webhook.isEmpty()) emailBody.append("<p>Referer : " + referer_url_webhook + "</p>");
		emailBody.append("<p>Webhook Received Data : <strong>" + reqData + "</strong>.</p>");
		if(getResponse != null && !getResponse.isEmpty()) emailBody.append("<p>Response : <strong>" + getResponse + "</strong>.</p>");
		emailBody.append("<p>Best regards,<br/>The Dashboard Support Team</p>");
		emailBody.append("</div>");
		emailBody.append("</body></html>");
		try {
			LOG.info("Find email to: {}", emailAddress);
            String[] emailAddresses = emailAddress.split(",");
            for(String email : emailAddresses) {
                LOG.info("Sending email to: {}", email);
                try {
//                  this.juvlonEmailSender.emailSender(email, transID +" - " + subject, emailBody.toString(), true);
                } catch (Exception e) {
                    log.error("Error sending email: ", e);
                }
            }

			
		} catch (Exception e) {
			LOG.error("Error sending email via sendVerificationMail====: ", e);
		}

		
	}


    @Override
    public Mono<ResponseEntity<Map<String, Object>>> updateTransStatusReactive(
        String transID,
        ServerWebExchange exchange,
        Boolean isWebhookBoolean,
        Boolean isRefundBoolean
    ) {
        return exchange.getFormData()
            .flatMap(formData -> {
                log.debug("Fetching transaction details for transID: {}", transID);
                boolean isRefund = (isRefundBoolean != null) ? isRefundBoolean : false;
                String connector_payin_file = "Status_";
                if(isRefund) {
                    connector_payin_file = "Refund_";
                } 
                Map<String, Object> response = new HashMap<>();
                boolean updateDb = false;	
                boolean adminRes = false; 
                boolean isCronjob = false; 
                Integer  tr_status = 0;
                Integer  status_code_set = 0;
                String  orderStatus = "", 
                        connector_status_code = "", 
                        connector_response_msg = "", 
                        feeId = "", 
                        connector_payin = "", 
                        merID = "", 
                        webhook_url = "", 
                        return_url = "";

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
                String existingSystemNote = "";
                boolean systemNoteUpdateDb = false;
                String existingSupportNote = "";
                Map<String, Object> connectorData = new HashMap<>();
                Map<String, Object> credentials = new HashMap<>();
                Map<String, Object> getPost = new HashMap<>();
                Optional<Transaction> transactionOpt = getTransactionById(Long.parseLong(transID));
                Optional<TransactionAdditional> additionalOpt = Optional.ofNullable(transactionAdditionalRepo.findByTransIDAd(Long.parseLong(transID)));
                if (!transactionOpt.isPresent() || !additionalOpt.isPresent()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Transaction not found");
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
                }
                Transaction transaction = transactionOpt.get();
                TransactionAdditional additional = additionalOpt.get();
                formData.forEach((key, value) -> {
                    if (!value.isEmpty()) {
                        getPost.put(key, value.get(0));
                    }
                });

                try {
                   
                    String requestURI = exchange.getRequest().getURI().getPath();
                    isCronjob = requestURI != null && (requestURI.contains("/s2s/cronjob/") || requestURI.contains("/status/s2s/cronjob/"));
                    if (requestURI.contains("/status/s2s/admin")) {
                        adminRes = true; 
                    }
                    
                    if (transactionOpt.isPresent()) {
                        existingSystemNote = additional.getSystemNote() != null ? additional.getSystemNote() : "";
                        existingSupportNote = additional.getSupportNote() != null ? additional.getSupportNote() : "";
                        connectorData.put("transID", transaction.getTransID() ==  null ? "" : transaction.getTransID().toString());
                        connectorData.put("billAmount", transaction.getBillAmount() ==  null ? "" : transaction.getBillAmount().toString());
                        connectorData.put("billCurrency", transaction.getBillCurrency() ==  null ? "" : transaction.getBillCurrency().toString());
                        Double bankProcessingAmount = 0.0;
                        String bankProcessingCurrency = "";
                        if(transaction.getBankProcessingAmount() != null)
                            bankProcessingAmount = Double.parseDouble(transaction.getBankProcessingAmount().toString());
                        else if(transaction.getTransactionAmount() != null)
                            bankProcessingAmount = Double.parseDouble(transaction.getTransactionAmount().toString());
                        else if(transaction.getBillAmount() != null)
                            bankProcessingAmount = Double.parseDouble(transaction.getBillAmount().toString());
                        connectorData.put("bankProcessingAmount", bankProcessingAmount);

                        if(transaction.getBankProcessingCurrency() != null)
                            bankProcessingCurrency = transaction.getBankProcessingCurrency().toString();
                        else if(transaction.getTransactionCurrency() != null)
                            bankProcessingCurrency = transaction.getTransactionCurrency().toString();
                        else if(transaction.getBillCurrency() != null)
                            bankProcessingCurrency = transaction.getBillCurrency().toString();
                        connectorData.put("bankProcessingCurrency", bankProcessingCurrency);
                        orderStatus = transaction.getTransactionStatus() == null ? "0"
                                : transaction.getTransactionStatus().toString(); // Default to Pending
                        tr_status = orderStatus.equals("0") ? 0 : Integer.parseInt(orderStatus);    
                        connector_response_msg = (additional.getTransactionResponse() == null) ? "Payment is pending" : additional.getTransactionResponse();
                        webhook_url = additional.getWebhookUrl().toString();
                        if(getPost.containsKey("test_auth") && "approved".equals(getPost.get("test_auth")) && tr_status == 27) {
                            status_code_set = 25;
                            connector_response_msg = "3DS test card approved";
                        }
                        else if(getPost.containsKey("test_auth") && "declined".equals(getPost.get("test_auth")) && tr_status == 27) {
                            status_code_set = 26;
                            connector_response_msg = "3DS test card declined";
                        }

                        merID = transaction.getMerchantID().toString();
                        feeId = transaction.getFeeId() !=null ? transaction.getFeeId().toString() : "";
                        connectorData.put("merID", merID);
                        connectorData.put("feeId", feeId);
                        connectorData.put("connectorRef", additional.getConnectorRef() ==  null ? "" : additional.getConnectorRef());
                        String getCredentials = (additional.getConnectorCredsProcessingFinal() !=null) ? additional.getConnectorCredsProcessingFinal() : "";
                        if(getCredentials !=null) credentials = jsonde(getCredentials);
                        String getConnector = (transaction.getConnector() != null) ? transaction.getConnector().toString() : "";
                        if (getConnector != null) {
                            Connector connector = connectorClient.findByConnectorNumber(getConnector);
                            if (connector != null) {
                                connectorData.put("connector", connector);
                                String connectorBaseUrl = connector.getConnectorBaseUrl();
                                String connectorStatusUrl = connector.getConnectorStatusUrl();
                                String defaultConnector = connector.getDefaultConnector();
                                String connectorStatus = connector.getConnectorStatus();
                                String connectorRefundUrl = connector.getConnectorRefundUrl();
                                connectorData.put("connectorStatusUrl", connectorStatusUrl);
                                connectorData.put("connectorBaseUrl", connectorBaseUrl);
                                connectorData.put("connectorStatus", connectorStatus);
                                connectorData.put("connectorRefundUrl", connectorRefundUrl);
                                
                                connector_payin = defaultConnector;
                                }
                            }

                                if(connector_payin != null && connectorData !=null && credentials !=null ) 
                                { 
                                    try {

                                        String connectorClassName = "com.webapp.controller.payin.pay_" + connector_payin + "." + connector_payin_file + connector_payin;
                                        Class<?> connectorClass = Class.forName(connectorClassName);
                                        Object connectorInstance = connectorClass.getDeclaredConstructor().newInstance();
                                        Method mapPayloadMethod = connectorClass.getMethod("mapPayload", Object.class);                                  
                                        Map<String, Object> payload = new HashMap<>();
                                        payload.putAll(connectorData);
                                        payload.put("apc_get", credentials); 
                                        Object mappedPayload = mapPayloadMethod.invoke(connectorInstance, payload);                                        
                          
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> mappedResponseMap = (Map<String, Object>) mappedPayload;

                                        if (mappedResponseMap.containsKey("connector_response_msg") && mappedResponseMap.get("connector_response_msg").toString() != null && mappedResponseMap.get("connector_response_msg").toString() != "Unknown") {
                                            connector_response_msg = mappedResponseMap.get("connector_response_msg").toString();

                                        }

                                        Boolean isStatusCode = false;
                                        if (mappedResponseMap.containsKey("connector_status_code") || (status_code_set != null && status_code_set != 0)) {
                                            connector_status_code = mappedResponseMap.get("connector_status_code") != null ? mappedResponseMap.get("connector_status_code").toString() : status_code_set.toString();
                                            Integer csc_int = Integer.parseInt(connector_status_code);
                                            if( (tr_status == 0 || tr_status == 22 || tr_status == 23) && (csc_int == 1 || csc_int == 2 || csc_int == 22 || csc_int == 23) && (connector_status_code != null && !connector_status_code.isEmpty())) {
                                                isStatusCode = true;
                                            }
                                            else if( (tr_status == 27) && (status_code_set == 25 || status_code_set == 26) && (connector_status_code != null && !connector_status_code.isEmpty())) {
                                                isStatusCode = true;
                                            } 
                                            if (isStatusCode) {
                                                updateDb = true;
                                                orderStatus = connector_status_code;	
                                                transaction.setTransactionStatus(Short.parseShort(connector_status_code));
                                                transactionDao.save(transaction); // Save the updated transaction
                                                log.debug("Transaction status updated from {} to {}", orderStatus, connector_status_code);

                                                

                                                //systemNote update for status
                                                systemNoteUpdateDb = true;
                                                currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
                                                if(existingSystemNote != null && existingSystemNote.length() > 0) {
                                                    existingSystemNote += "\n";
                                                }
                                                existingSystemNote += currentDateTime + " | " + connector_payin_file + " " + getStatusDes(orderStatus) + " - " + connector_status_code;
                                                if (connector_response_msg != null && !connector_response_msg.isEmpty()) {
                                                    existingSystemNote += " - " + connector_response_msg;
                                                }
                                                // Append " via Cron" if updated via cronjob
                                                if (isCronjob) {
                                                    existingSystemNote += " via Cron";
                                                }

                                                //existingSupportNote update for status
                                                if(existingSupportNote != null && existingSupportNote.length() > 0) {
                                                    existingSupportNote += "\n";
                                                }
                                                existingSupportNote += currentDateTime + " | " + connector_payin_file + " " + getStatusDes(orderStatus);
                                                additional.setSupportNote(existingSupportNote);
                                                transactionAdditionalRepo.save(additional); 

                                                
                                            }
                                            
                                            orderStatus = connector_status_code;
                                            response.put("connector_status_code", connector_status_code);
                                        }
                                        
                                        if (connector_response_msg != null && !connector_response_msg.isEmpty()) {
                                            if(updateDb){
                                                additional.setTransactionResponse(connector_response_msg);
                                                transactionAdditionalRepo.save(additional); 
                                            }
                                            if(adminRes)
                                            response.put("connector_response_msg", connector_response_msg);
                                        }
                                        

                                        // Assuming parsedResponse is already defined
                                        if (mappedResponseMap.containsKey("gateway_response")) {
                                            // Extracting gateway_response
                                            @SuppressWarnings("unchecked")
                                            String connector_response = Base64Util.encodeBase64(jsonen((Map<String, Object>) mappedResponseMap.get("gateway_response")));
                                            //transactionAdditional.setConnectorResponse(connector_response);
                                            if(adminRes)
                                            response.put("gateway_response", mappedResponseMap.get("gateway_response")); 
                                        }
                                        

                                    
                                        
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace(); 
                                    }
                                }	

                                String decryptedCCNO = additional.getCcnoDecrypted(); 
                                String statusDescription = getStatusDes(orderStatus); 
                                response.put("transID", transaction.getTransID().toString());
                                response.put("order_status", orderStatus);
                                response.put("status", statusDescription);
                                response.put("bill_amt", transaction.getBillAmount().toString());
                                String formattedDate = transaction.getTransactionDate().format(formatter);
                                response.put("tdate", formattedDate);
                                response.put("bill_currency", transaction.getBillCurrency());
                                response.put("response", connector_response_msg);
                                response.put("reference", transaction.getReference());
                                response.put("mop", transaction.getMethodOfPayment());
                                if (additionalOpt.isPresent()) {
                                    response.put("descriptor", additional.getDescriptor());
                                    response.put("upa", additional.getUpa());
                                    response.put("rrn", additional.getRrn());
                                    if(decryptedCCNO != null) {
                                        response.put("ccno", maskCardNumber(decryptedCCNO));
                                    }
                                    if(orderStatus.equals("0")) {
                                        response.put("authurl", additional.getAuthUrl());
                                        response.put("authdata", additional.getAuthData());
                                    }
                                } else {
                                    response.put("descriptor", null);
                                    response.put("upa", null);
                                    response.put("rrn", null);
                                    response.put("ccno", null);
                                }

                                if (webhook_url != null && !webhook_url.isEmpty() && updateDb) {
                                    Instant now = Instant.now();
                                    Timestamp notifyTime = Timestamp.from(now);
                                    String formattedNotifyTime = dateFormat.format(notifyTime);
                                    response.put("webhook_notify_time", formattedNotifyTime);
                                    if(transaction.getIntegrationType() != null && transaction.getIntegrationType().contains("_aes256")) 
                                    {
                                        try {
                                            String terminalNumber = transaction.getTerminalNumber() !=null ? transaction.getTerminalNumber().toString() : "";
                                            Terminal terminal = terminalClient.getTerminalById(Integer.parseInt(terminalNumber));
                                            if (terminal == null) {
                                                response.put("error", "Terminal not found for terminalNumber: " + terminalNumber);
                                                return Mono.just(ResponseEntity.ok(response));
                                            }

                                            String privateKey = terminal.getPrivateKey() == null ? "" : terminal.getPrivateKey().toString();
                                            String publicKey = terminal.getPublicKey() == null ? "" : terminal.getPublicKey().toString();

                                            if (privateKey == null || privateKey.isEmpty()) {
                                                System.out.println("\r privateKey => "+privateKey);
                                                throw new IllegalArgumentException("Private key is missing for webhook notification");
                                            }
                                            if (publicKey == null || publicKey.isEmpty()) {
                                                System.out.println("\r publicKey => "+publicKey);
                                                throw new IllegalArgumentException("Public key is missing for webhook notification");
                                            }

                                            // Subquery to get the data from response
                                            String queryString = response.entrySet()
                                                    .stream()
                                                    .map(e -> e.getKey() + "=" + e.getValue())
                                                    .collect(Collectors.joining("&"));
                                            
                                            String encryptedResponse = AES256Api.encrypt(jsonen(response), privateKey, publicKey);
                                            Map<String, Object> responseEncrypted = new HashMap<>();

                                                responseEncrypted.put("private_key", privateKey);
                                                responseEncrypted.put("public_key", publicKey);
                                            responseEncrypted.put("data", encryptedResponse);
                                            sendWebhookNotification(webhook_url, responseEncrypted);

                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        //response.put("data", encryptedResponse);
                                    }
                                    else {
                                        sendWebhookNotification(webhook_url, response);
                                    }
                                    

                                    //systemNote update for webhook
                                    systemNoteUpdateDb = true;
                                    currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
                                    if(existingSystemNote != null && existingSystemNote.length() > 0) {
                                        existingSystemNote += "\n";
                                    }
                                    existingSystemNote += currentDateTime + " | " + connector_payin_file + statusDescription  + " - Webhook notification sent to " + webhook_url ; 

                                
                                    log.debug("Webhook notification sent to {} at {} with response is {}", webhook_url, formattedNotifyTime, response);

                                }

                                if(systemNoteUpdateDb && existingSystemNote != null && existingSystemNote.length() > 0) {
                                    additional.setSystemNote(existingSystemNote);
                                    transactionAdditionalRepo.save(additional); 
                                }
                                return_url = additional.getReturnUrl();
                                if (return_url != null && !return_url.isEmpty() && !isWebhookBoolean) {
                                    try {
                                        Map<String, String> params = new HashMap<>();
                                        response.forEach((key, value) -> {
                                            if (value != null) {
                                                params.put(key, String.valueOf(value));
                                            }
                                        });

                                        params.values().removeIf(value -> value == null || value.isEmpty());
                                        return_url = normalizeAndBuildUrl(return_url, params);
                                        log.debug("Final redirect URL: {}", return_url);
                                        Map<String, Object> redirectResponse = new HashMap<>();
                                        redirectResponse.put("redirect_url", return_url);
                                        return Mono.just(ResponseEntity.status(302)
                                                .header("Location", return_url)
                                                .body(redirectResponse));

                                    } catch (Exception e) {
                                        log.error("Error building redirect URL: {}", e.getMessage());
                                        response.put("error", "Failed to build redirect URL");
                                        return Mono.just(ResponseEntity.ok(response));
                                    }
                                }

                                //s2s wise as curl or postman for non-browser/API requests, return JSON response
                                return Mono.just(ResponseEntity.ok(response));

                            }
                
                            
                            return Mono.just(ResponseEntity.ok(response));
                        } catch (Exception e) {
                            log.error("Error processing transaction: ", e);
                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("error", "Error processing transaction");
                            errorResponse.put("message", e.getMessage());
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                        }
                    
            })
            .onErrorResume(e -> {
                log.error("Error updating transaction status: ", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Internal server error");
                errorResponse.put("message", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
            });
    }
   
    
    // This method fetches transaction details based on transID
    // and returns a map with the details.
    public Map<String, Object> getTransactionDetails(String transID, Boolean isCheckoutBoolean) {
		Map<String, Object> response = new HashMap<>();
		try {

			Long transIDLong = Long.parseLong(transID); // Convert String to Long

			Optional<Transaction> transactionOpt = Optional
					.ofNullable(transactionDao.findByTransID(transIDLong));
			
			Optional<TransactionAdditional> additionalOpt = Optional
					.ofNullable(transactionAdditionalRepo.findByTransIDAd(transIDLong));

			if (transactionOpt.isPresent()) {
				Transaction transaction = transactionOpt.get();
				TransactionAdditional additional = additionalOpt.get();
				String decryptedCCNO = additional.getFormattedCardNumber(); 
				String orderStatus = transaction.getTransactionStatus() == null ? "0"
						: transaction.getTransactionStatus().toString(); 

				String statusDescription = getStatusDes(orderStatus); 
                if (orderStatus.equals("8")) {
                    statusDescription = "Request Processed";
                }
				response.put("transID", transaction.getTransID().toString());
				response.put("order_status", orderStatus);
				response.put("status", statusDescription);
				response.put("bill_amt", transaction.getBillAmount().toString());
                response.put("terno", transaction.getTerminalNumber().toString());
                try {
                    String payloadStage1 = additional.getPayloadStage1();
                    if(payloadStage1 != null && !payloadStage1.isEmpty()) {
//                        JSONObject jsonPayload = new JSONObject(payloadStage1);
//                        if(jsonPayload.has("public_key")) {
//                            String publicKey = jsonPayload.getString("public_key");
//                            response.put("public_key", publicKey);
//                           // LOG.info("Extracted public_key from payload_stage1: {}", publicKey);
//                        }
                    }
                } catch(Exception e) {
                    log.error("Error parsing public_key from payload_stage1: {}", e.getMessage());
                }

				
                // Format the date to include microseconds
		        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                //String formattedDate = dateFormat.format(transaction.getTransactionDate());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String formattedDate = transaction.getTransactionDate().format(formatter);

				response.put("tdate", formattedDate);

				response.put("bill_currency", transaction.getBillCurrency());
				response.put("response", additional.getTransactionResponse() == null ? "Payment is pending"
						: additional.getTransactionResponse());
				response.put("reference", transaction.getReference());
				response.put("mop", transaction.getMethodOfPayment());
                response.put("integration_type", transaction.getIntegrationType());
				//response.put("authstatus", "http://localhost:9003/api/authstatus/" + transID);

				// Fetch Additional Details
				if (additionalOpt.isPresent()) {

                    String issuingBank = additional.getIssuingBank();
                    String issuingCountry = additional.getIssuingCountry();
                    String cardType = additional.getCardType();
                    String cardBrand = additional.getCardBrand();
                    String cardTier = additional.getCardTier();
                    String billingPhone = additional.getBillingPhone();

                    String productName = additional.getProductName();
                    



					response.put("descriptor", additional.getDescriptor());
					response.put("upa", additional.getUpa());
					response.put("rrn", additional.getRrn());
					
					if(decryptedCCNO != null) {
						response.put("ccno", decryptedCCNO); // Mask after decryption
					}

                    if(issuingBank != null) {
                        response.put("issuing_bank", issuingBank);
                    }
                    if(issuingCountry != null) {
                        response.put("issuing_country", issuingCountry);
                    }
                    if(cardType != null) {
                        response.put("card_type", cardType);
                    }
                    if(cardBrand != null) {
                        response.put("card_brand", cardBrand);
                    }
                    if(cardTier != null) {
                        response.put("card_tier", cardTier);
                    }
                    if(billingPhone != null) {
                        response.put("billing_phone", billingPhone);
                    }
                    if(productName != null) {
                        response.put("product_name", productName);
                    }

					// by default isCheckoutBoolean is false
					if(isCheckoutBoolean == null) {
						isCheckoutBoolean = false;
					}

					


                    //checkout-s2s wise authdata decode and payaddress and action and payamt and return in response
                    if((transaction.getIntegrationType() != null && transaction.getIntegrationType().equals("checkout-s2s") && (orderStatus.equals("0") || orderStatus.equals("27"))) || isCheckoutBoolean) {   // orderStatus is 0 or 27 means payment is pending or payment is successful
                        String authdata64 = additional.getAuthData() == null ? "null" : additional.getAuthData();
                        String authDataJson = Base64Util.decodeBase64(authdata64);
                        Map<String, Object> authDataMapDecode = new HashMap<>();
                        try {
                            if (authDataJson != null && !authDataJson.equals("null") && !authDataJson.isEmpty()) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                authDataMapDecode = objectMapper.readValue(authDataJson, new TypeReference<Map<String, Object>>() {});
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse authdata JSON: {}", e.getMessage());
                        }

                        response.put("authurl", additional.getAuthUrl());
                        
                        
                        // Extract crypto payment fields from decoded authdata
                        if(authDataMapDecode.containsKey("payaddress") && authDataMapDecode.get("payaddress") != null && !authDataMapDecode.get("payaddress").toString().isEmpty()) {
                            response.put("payaddress", authDataMapDecode.get("payaddress"));
                        }
                        if(authDataMapDecode.containsKey("action") && authDataMapDecode.get("action") != null && !authDataMapDecode.get("action").toString().isEmpty()) {
                            response.put("action", authDataMapDecode.get("action"));
                        }

                        if(authDataMapDecode.containsKey("payamt") && authDataMapDecode.get("payamt") != null) {
                            response.put("payamt", authDataMapDecode.get("payamt").toString());
                        }
                        else if(transaction.getBankProcessingAmount() != null) {
                            response.put("payamt", transaction.getBankProcessingAmount().toString());
                        }
                        
                        if(authDataMapDecode.containsKey("paycurrency") && authDataMapDecode.get("paycurrency") != null && !authDataMapDecode.get("paycurrency").toString().isEmpty()) {
                            response.put("paycurrency", authDataMapDecode.get("paycurrency"));
                        }
                        else if(transaction.getBankProcessingCurrency() != null) {
                            response.put("paycurrency", transaction.getBankProcessingCurrency().toString());
                        }

                        if(authDataMapDecode.containsKey("paytitle") && authDataMapDecode.get("paytitle") != null && !authDataMapDecode.get("paytitle").toString().isEmpty()) {
                            response.put("paytitle", authDataMapDecode.get("paytitle"));
                        }

                        if(transaction.getFullName() != null) {
                            response.put("full_name", transaction.getFullName().toString());
                        }
                        if(transaction.getBillEmail() != null) {
                            response.put("bill_email", transaction.getBillEmail().toString());
                        }
                        if(additional.getBillingPhone() != null) {
                            response.put("billing_phone", additional.getBillingPhone().toString());
                        }
                        if(additional.getBillingAddress() != null) {
                            response.put("billing_address", additional.getBillingAddress().toString());
                        }
                        if(additional.getBillingCity() != null) {
                            response.put("billing_city", additional.getBillingCity().toString());
                        }
                        if(additional.getBillingState() != null) {
                            response.put("billing_state", additional.getBillingState().toString());
                        }
                        if(additional.getBillingCountry() != null) {
                            response.put("billing_country", additional.getBillingCountry().toString());
                        }
                        if(additional.getBillingZip() != null) {
                            response.put("billing_zip", additional.getBillingZip().toString());
                        }

                        if(transaction.getConnector() != null && transaction.getConnector() != 0) {
                            response.put("connector_id", transaction.getConnector().toString());
                        }


                        response.put("authdata", authDataMapDecode);
                    }
                    else if(orderStatus.equals("0") || orderStatus.equals("27")) 
                    {
                        response.put("authurl", additional.getAuthUrl());
                        response.put("authdata", additional.getAuthData());
                    }


				} else {
					response.put("descriptor", null);
					response.put("upa", null);
					response.put("rrn", null);
					response.put("ccno", null);
				}

				return response;
			}
		} catch (NumberFormatException e) {
			response.put("error_number", "400");
			response.put("error_message", "Invalid transID format. Expected numeric value.");
			response.put("status", "Error");
			return response;
		}
		return response;
    }

    private String getStatusDes(String statusCode) {
        if (statusCode == null) {
            return "Pending";
        }
	        switch (statusCode) {
	        case "0": return "Pending";
	        case "1": return "Approved";
	        case "2": return "Declined";
	        case "3": return "Refunded";
	        case "5": return "Chargeback";
	        case "7": return "Reversed";
	        case "8": return "Refund Pending";
	        case "9": return "Test";
	        case "10": return "Blocked";

            case "11": return "Predispute";
            case "12": return "Partial Refund";
            case "13": return "Withdraw Requested";
            case "14": return "Withdraw Rolling";
            case "20": return "Frozen Balance";
            case "21": return "Frozen Rolling";
            case "22": return "Expired";
            case "23": return "Cancelled";
            case "24": return "Failed";
            case "25": return "Test Approved";
            case "26": return "Test Declined";
            case "27": return "Test 3DS Authentication";
            case "28": return "Blocked - Regulatory Violation";

	        default: return "Pending";
	    }
	}
	
	private String maskCardNumber(String ccno) {
	    if (ccno == null || ccno.length() < 10) {
	        return ccno; // Not enough digits to mask
	    }
	    return ccno.substring(0, 6) + "XXXXXX" + ccno.substring(ccno.length() - 4);
	}

    private String normalizeFilter(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toString().trim();
        return normalized.isEmpty() ? null : normalized.toLowerCase();
    }

    private boolean matchesAdditionalFilter(String expected, String actual) {
        if (expected == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        return actual.trim().toLowerCase().contains(expected);
    }
	
	@Override
    public List<Transaction> findAllByOrderByTransactionDateDesc() {
        return transactionDao.findAll(Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Override
    public void updateTransactionID(Long id, Long transID) {
        Transaction transaction = transactionDao.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        transaction.setTransID(transID);
        transactionDao.save(transaction);
    }



    /**
	 * Encodes a Map into a JSON string, handling multiple arrays or nested structures.
	 * @param data The Map containing data to encode
	 * @return A JSON string representation of the Map, or an empty string if encoding fails
	 */
	public static String jsonen(Map<String, Object> data) {
	    if (data == null || data.isEmpty()) {
	        return "";
	    }

	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        return objectMapper.writeValueAsString(data);
	    } catch (Exception e) {
	        System.err.println("Error encoding JSON: " + e.getMessage());
	        return "";
	    }
	}

	/** 
	 * Decode JSON string into a Map and handle nested JSON structures
	 * @param jsonString The JSON string to decode from multiple JSON strings
	 * @return A Map representation of the JSON, or empty Map if parsing fails
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsonde(String jsonString) {
	    if (jsonString == null || jsonString.isEmpty()) {
	        return new HashMap<>();
	    }
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        Map<String, Object> decodedMap = objectMapper.readValue(jsonString, Map.class);
	        Map<String, Object> resultMap = new HashMap<>();

	        for (Map.Entry<String, Object> entry : decodedMap.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            if (value instanceof Map) {
	                Map<String, Object> nestedMap = (Map<String, Object>) value;
	                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
	                    String nestedKey = nestedEntry.getKey();
	                    Object nestedValue = nestedEntry.getValue();
	                    resultMap.put(key + "." + nestedKey, nestedValue);
	                }
	            } else {
	                resultMap.put(key, value);
	            }
	        }
	        return resultMap;
	    } catch (Exception e) {
	        return new HashMap<>();
	    }
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsondecode(String jsonString) {
	    if (jsonString == null || jsonString.isEmpty()) {
	        return new HashMap<>();
	    }
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        return objectMapper.readValue(jsonString, Map.class);
	    } catch (Exception e) {
	        return new HashMap<>();
	    }
	}

    private void sendWebhookNotification(String webhookUrl, Map<String, Object> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(data);
            java.net.URL url = new java.net.URL(webhookUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            log.debug("Webhook notification sent to {} with response code {}", webhookUrl, responseCode);
            
        } catch (Exception e) {
            log.error("Error sending webhook notification: {}", e.getMessage());
        }
    }

    private String normalizeAndBuildUrl(String baseUrl, Map<String, String> params) throws IOException {
        baseUrl = baseUrl.trim();
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            urlBuilder.append("?");
        } else if (!baseUrl.endsWith("&")) {
            urlBuilder.append("&");
        }

        boolean first = !baseUrl.contains("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(java.net.URLEncoder.encode(entry.getKey(), "UTF-8"))
                     .append("=")
                     .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            first = false;
        }

        return urlBuilder.toString();
    }

    @Override
    public List<Map<String, Object>> getCsvAllTransactionsByMerchantID(Long merchantID, String requestType) {
        List<Transaction> transactions = transactionDao.findByMerchantIDOrderByTransactionDateDesc(merchantID);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Transaction transaction : transactions) {
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("transID", String.valueOf(transaction.getTransID()));
                combinedData.put("billAmount", String.valueOf(transaction.getBillAmount()));
                combinedData.put("billCurrency", String.valueOf(transaction.getBillCurrency()));
                combinedData.put("transactionAmount", String.valueOf(transaction.getTransactionAmount()));
                combinedData.put("transactionCurrency", String.valueOf(transaction.getTransactionCurrency()));
                combinedData.put("reference", String.valueOf(transaction.getReference()));
                combinedData.put("transactionDate", String.valueOf(transaction.getTransactionDate()));
                combinedData.put("orderStatus", String.valueOf(transaction.getTransactionStatus()));
                combinedData.put("transactionStatus", getStatusDes(String.valueOf(transaction.getTransactionStatus())));
                combinedData.put("connector", String.valueOf(transaction.getConnector()));
                combinedData.put("terminalNumber", String.valueOf(transaction.getTerminalNumber()));
                combinedData.put("fullName", String.valueOf(transaction.getFullName()));
                combinedData.put("billEmail", String.valueOf(transaction.getBillEmail()));
                combinedData.put("billIP", String.valueOf(transaction.getBillIP()));
                combinedData.put("mopName", String.valueOf(transaction.getMopName()));
                combinedData.put("channelType", String.valueOf(transaction.getChannelType()));
                combinedData.put("gstAmount", String.valueOf(transaction.getGstAmount()));
                combinedData.put("createdDate", String.valueOf(transaction.getCreatedDate()));
                combinedData.put("methodOfPayment", String.valueOf(transaction.getMethodOfPayment()));
            
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
            if (additional != null) {
                    combinedData.put("binNumber", additional.getBinNumber());
                    combinedData.put("billingPhone", additional.getBillingPhone());
                    combinedData.put("billingAddress", additional.getBillingAddress());
                    combinedData.put("billingCity", additional.getBillingCity());
                    combinedData.put("billingState", additional.getBillingState());
                    combinedData.put("billingCountry", additional.getBillingCountry());
                    combinedData.put("billingZip", additional.getBillingZip());
                    combinedData.put("productName", additional.getProductName());
                    combinedData.put("authUrl", additional.getAuthUrl());
                    combinedData.put("rrn", additional.getRrn());
                    combinedData.put("upa", additional.getUpa());
                    combinedData.put("descriptor", additional.getDescriptor());
                  
            } 
            result.add(combinedData);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllTransactionsByMerchantID(Long merchantID) {
        List<Transaction> transactions = transactionDao.findByMerchantIDOrderByTransactionDateDesc(merchantID);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("transID", String.valueOf(transaction.getTransID()));
            combinedData.put("transactionStatusDec", getStatusDes(String.valueOf(transaction.getTransactionStatus())));
            combinedData.put("transaction", transaction);
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
            if (additional != null) {
                Map<String, Object> additionalMap = new HashMap<>();
                additionalMap.put("transID", String.valueOf(transaction.getTransID()));
                additionalMap.put("id", additional.getId());
                additionalMap.put("binNumber", additional.getBinNumber());
                additionalMap.put("cardNumber", maskCardNumber(additional.getCcnoDecrypted()));
                additionalMap.put("billingPhone", additional.getBillingPhone());
                additionalMap.put("billingAddress", additional.getBillingAddress());
                additionalMap.put("billingCity", additional.getBillingCity());
                additionalMap.put("billingState", additional.getBillingState());
                additionalMap.put("billingCountry", additional.getBillingCountry());
                additionalMap.put("billingZip", additional.getBillingZip());
                additionalMap.put("productName", additional.getProductName());
                additionalMap.put("authUrl", additional.getAuthUrl());
                additionalMap.put("rrn", additional.getRrn());
                additionalMap.put("upa", additional.getUpa());
                additionalMap.put("descriptor", additional.getDescriptor());
                additionalMap.put("transactionResponse", additional.getTransactionResponse());
                additionalMap.put("connectorRef", additional.getConnectorRef());
                additionalMap.put("connectorResponse", Base64Util.decodeBase64(additional.getConnectorResponse()));
                additionalMap.put("authData", Base64Util.decodeBase64(additional.getAuthData()));
                
                combinedData.put("additional", additionalMap);
            } else {
                combinedData.put("additional", new HashMap<>());
            }
            result.add(combinedData);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> findLatest10ApprovedByTransactionDateDesc(Short status, Long merchantID, int page, int size) {
        List<Transaction> transactions = new ArrayList<>();
        // Fetch latest 10 approved transactions ordered by transactionDate descending
        // and filter by merchantID
        if (merchantID != null) {
             transactions = transactionDao.findLatest10ApprovedByStatusAndMerchantId(status, merchantID, PageRequest.of(page, size));
        }
        else {
             transactions = transactionDao.findLatest10ApprovedByTransactionDateDesc(status, PageRequest.of(page, size));
        }
        //List<Transaction> transactions = transactionDao.findLatest10ApprovedByTransactionDateDesc(status, PageRequest.of(0, 10));
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("transID", String.valueOf(transaction.getTransID()));
            String formattedDate = transaction.getTransactionDate().format(formatter);
            combinedData.put("transactionDate", formattedDate);
            combinedData.put("transaction", transaction);
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
            if (additional != null) {
                Map<String, Object> additionalMap = new HashMap<>();
                additionalMap.put("transID", String.valueOf(transaction.getTransID()));
                additionalMap.put("id", additional.getId());
                additionalMap.put("binNumber", additional.getBinNumber());
                additionalMap.put("cardNumber", maskCardNumber(additional.getCcnoDecrypted()));
                additionalMap.put("billingPhone", additional.getBillingPhone());
                additionalMap.put("billingAddress", additional.getBillingAddress());
                additionalMap.put("billingCity", additional.getBillingCity());
                additionalMap.put("billingState", additional.getBillingState());
                additionalMap.put("billingCountry", additional.getBillingCountry());
                additionalMap.put("billingZip", additional.getBillingZip());
                additionalMap.put("productName", additional.getProductName());
                additionalMap.put("authUrl", additional.getAuthUrl());
                additionalMap.put("rrn", additional.getRrn());
                additionalMap.put("upa", additional.getUpa());
                additionalMap.put("descriptor", additional.getDescriptor());
                additionalMap.put("transactionResponse", additional.getTransactionResponse());
                additionalMap.put("connectorRef", additional.getConnectorRef());
                additionalMap.put("connectorResponse", Base64Util.decodeBase64(additional.getConnectorResponse()));
                //additionalMap.put("authData", Base64Util.decodeBase64(additional.getAuthData()));

                combinedData.put("cardNumber", maskCardNumber(additional.getCcnoDecrypted()));

                combinedData.put("additional", additionalMap);
            } else {
                combinedData.put("additional", new HashMap<>());
            }
            result.add(combinedData);
        }
        return result;
    }

    @Override
    public long countByTransactionStatus(short status) {
        return transactionDao.countByTransactionStatus(status);
    }

    @Override
    public Long countByTransactionStatusAndMerchantID(Short status, Long merchantID) {
        return transactionDao.countByTransactionStatusAndMerchantID(status, merchantID);
    }

    @Override
    public Long countByTransactionStatusAndMerchantIDAndDateRange(Short status, Long merchantID, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionDao.countByTransactionStatusAndMerchantIDAndDateRange(status, merchantID, startDate, endDate);
    }
    

    @Override
    public Double sumBillAmountByStatuses(Short statuses) {
        return transactionDao.sumBillAmountByStatuses(statuses);
    }
    @Override
    public Double sumBillAmountByStatusesAndMerchantID(Short status, Long merchantID) {
        return transactionDao.sumBillAmountByStatusesAndMerchantID(status, merchantID);
    }

    @Override
    public Double sumBillAmountByStatusesAndMerchantIDAndDateRange(Short status, Long merchantID, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionDao.sumBillAmountByStatusesAndMerchantIDAndDateRange(status, merchantID, startDate, endDate);
    }

    @Override
    @Cacheable(value = "transactionReferenceCache", key = "#reference + '-' + #merchantID", unless = "#result == null")
    public Transaction findByReferenceAndMerchantID(String reference, Long merchantID) {
        log.info("Fetching transaction by reference and merchantID from database: {}, {}", reference, merchantID);
        return transactionDao.findByReferenceAndMerchantID(reference, merchantID);
    }

    @Override
    @Cacheable(value = "transactionCache", key = "#id", unless = "#result == null")
    public Optional<Transaction> getTransactionById(Long id) {
        log.info("Fetching transaction from database for id: {}", id);
        return transactionDao.findById(id);
    }

    @Override
    @Cacheable(value = "transactionListCache", key = "'allTransactions'")
    public List<Transaction> getAllTransactions() {
        log.info("Fetching all transactions from database");
        return transactionDao.findAll();
    }

    @Override
    @Cacheable(value = "transactionCache", key = "#transactionId", unless = "#result == null")
    public Optional<Transaction> findByTransactionId(String transactionId) {
        log.info("Fetching transaction from database for transactionId: {}", transactionId);
        return transactionDao.findByTransactionStatus(Short.parseShort(transactionId));
    }

    @Override
    @Transactional
    @CachePut(value = "transactionCache", key = "#transaction.id")
    public Transaction saveTransaction(Transaction transaction) {
        log.info("Saving transaction to database: {}", transaction);
        return transactionDao.save(transaction);
    }

    

    @Override
    @Transactional
    @CacheEvict(value = {"transactionCache", "transactionListCache"}, allEntries = true)
    public boolean deleteTransaction(Long id) {
        log.info("Deleting transaction from database: {}", id);
        try {
            transactionDao.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting transaction with id: {}", id, e);
            return false;
        }
    }

    @Override
    public Page<Transaction> findAllWithFilters(
        Specification<Transaction> spec, // Updated to accept Specification directly
        Pageable pageable
    ) {
        // The Specification is now passed directly, so we just use it
        return transactionDao.findAll(spec, pageable);
    }

    // Helper method to build the Specification dynamically
    @Override
    public  Specification<Transaction> getTransactionSpecification(
        Long merchantID,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String connectorId,
        Short status,
        String keyName,
        String keyValue, 
        Map<String, Object> getReq
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ... existing predicates for merchantID, startDate, endDate, connectorId, status ...
            if (merchantID != null) {
                predicates.add(criteriaBuilder.equal(root.get("merchantID"), merchantID));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (connectorId != null && !connectorId.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("connector"), connectorId));
            }

            if (status != null) {
                // Only handle Short status; if you want to support comma-separated String, change the parameter type to String
                predicates.add(criteriaBuilder.equal(root.get("transactionStatus"), status));
            }

            /* 
            if(getReq != null && getReq.containsKey("methodOfPayment") && getReq.get("methodOfPayment") != null && !getReq.get("methodOfPayment").toString().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("methodOfPayment"), getReq.get("methodOfPayment")));
               // System.out.println("Method of Payment: " + getReq.get("methodOfPayment"));
            }
            */   

           
            

            if(getReq != null && getReq.containsKey("minAmount") && getReq.get("minAmount") != null && !getReq.get("minAmount").toString().isEmpty()) {
                // Convert minAmount to the correct type (assuming billAmount is BigDecimal)
                Object minAmountObj = getReq.get("minAmount");
                if (minAmountObj != null) {
                    try {
                        java.math.BigDecimal minAmount = new java.math.BigDecimal(minAmountObj.toString());
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("billAmount"), minAmount));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid minAmount value: " + minAmountObj);
                    }
                }
                System.out.println("minAmount : " + getReq.get("minAmount"));
            }

            if(getReq != null && getReq.containsKey("maxAmount") && getReq.get("maxAmount") != null && !getReq.get("maxAmount").toString().isEmpty()) {
                // Convert maxAmount to the correct type (assuming billAmount is BigDecimal)
                Object minAmountObj = getReq.get("maxAmount");
                if (minAmountObj != null) {
                    try {
                        java.math.BigDecimal maxAmount = new java.math.BigDecimal(minAmountObj.toString());
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("billAmount"), maxAmount));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid maxAmount value: " + minAmountObj);
                    }
                }
                System.out.println("maxAmount : " + getReq.get("maxAmount"));
            }


            if (keyName != null && keyValue != null && !keyValue.isEmpty()) {
                // Split keyValue by comma to handle multiple search terms
                String[] searchTerms = keyValue.split(",");
                List<Predicate> orPredicates = new ArrayList<>();

                Predicate keyPredicate = null; // Initialize keyPredicate to null

                // Define fields that are passed from frontend and belong to TransactionAdditional entity
                Set<String> frontendTransactionAdditionalKeyNames = new HashSet<>(Arrays.asList(
                    "billingPhone",
                    "billingAddress",
                    "billingCity",
                    "billingState",
                    "billingCountry",
                    "billingZip",
                    "rrn",
                    "upa",
                    "descriptor",
                    "productName",
                    "authUrl",
                    "authData",
                    "connectorRef",
                    "connectorResponse"
                ));

                // Mapping from frontend keyName to actual entity field name in TransactionAdditional
                Map<String, String> taFieldNameToEntityField = new HashMap<>();
                taFieldNameToEntityField.put("billingPhone", "billingPhone");
                taFieldNameToEntityField.put("billingAddress", "billingAddress");
                taFieldNameToEntityField.put("billingCity", "billingCity");
                taFieldNameToEntityField.put("billingState", "billingState");
                taFieldNameToEntityField.put("billingCountry", "billingCountry");
                taFieldNameToEntityField.put("billingZip", "billingZip");
                taFieldNameToEntityField.put("rrn", "rrn");
                taFieldNameToEntityField.put("upa", "upa");
                taFieldNameToEntityField.put("descriptor", "descriptor");
                taFieldNameToEntityField.put("productName", "productName");
                taFieldNameToEntityField.put("authUrl", "authUrl");
                taFieldNameToEntityField.put("authData", "authData");
                taFieldNameToEntityField.put("connectorRef", "connectorRef");
                taFieldNameToEntityField.put("connectorResponse", "connectorResponse");
                // Check if the keyName is in the TransactionAdditional fields


                if (frontendTransactionAdditionalKeyNames.contains(keyName)) {
                    String entityFieldName = taFieldNameToEntityField.get(keyName);

                    if (entityFieldName != null) {
                        for (String term : searchTerms) {
                            String likePattern = "%" + term.trim().toLowerCase() + "%"; // Trim and lowercase each term
                            Subquery<Long> subquery = query.subquery(Long.class);
                            Root<TransactionAdditional> taSubqueryRoot = subquery.from(TransactionAdditional.class);
                            subquery.select(taSubqueryRoot.get("transIDAd"));

                            List<Predicate> taPredicates = new ArrayList<>();
                            taPredicates.add(criteriaBuilder.equal(root.get("transID"), taSubqueryRoot.get("transIDAd")));
                            taPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(taSubqueryRoot.get(entityFieldName)), likePattern));

                            subquery.where(criteriaBuilder.and(taPredicates.toArray(new Predicate[0])));
                            orPredicates.add(criteriaBuilder.exists(subquery));
                        }
                    }
                } else {
                    // Original logic for Transaction fields
                    for (String term : searchTerms) {
                        String likePattern = "%" + term.trim().toLowerCase() + "%"; // Trim and lowercase each term
                        Predicate termPredicate = null;
                        switch (keyName) {
                            case "transID":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transID"))), likePattern);
                                break;
                            case "reference":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("reference")), likePattern);
                                break;
                            case "terNO":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("terminalNumber")), likePattern);
                                break;
                            case "fullname":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern);
                                break;
                            case "bill_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("billAmount"))), likePattern);
                                break;
                            case "billCurrency":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("billCurrency"))), likePattern);
                                break;
                            case "billEmail":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("billEmail"))), likePattern);
                                break;
                            case "trans_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transactionAmount"))), likePattern);
                                break;
                            case "available_balance":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("availableBalance"))), likePattern);
                                break;
                            case "tdate":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transactionDate"))), likePattern);
                                break;
                            case "mop":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("searchKey")), likePattern);
                                break;
                            case "trans_status":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transactionStatus"))), likePattern);
                                break;
                            case "merID":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("merchantID"))), likePattern);
                                break;
                            case "bill_ip":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("billIP")), likePattern);
                                break;
                            case "buy_mdr_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("buyMdrAmount"))), likePattern);
                                break;
                            case "buy_txnfee_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("buyTxnFeeAmount"))), likePattern);
                                break;
                            case "rolling_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("rollingAmount"))), likePattern);
                                break;
                            case "mdr_cb_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("mdrCashbackAmount"))), likePattern);
                                break;
                            case "mdr_cbk1_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("mdrCashback1Amount"))), likePattern);
                                break;
                            case "mdr_refundfee_amt":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("mdrRefundFeeAmount"))), likePattern);
                                break;
                            case "payable_amt_of_txn":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("payableTransactionAmount"))), likePattern);
                                break;
                            case "settelement_date":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("settlementDate"))), likePattern);
                                break;
                            case "created_date":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("createdAt"))), likePattern);
                                break;
                            case "risk_ratio":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("riskRatio"))), likePattern);
                                break;
                            case "transaction_period":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transactionPeriod"))), likePattern);
                                break;
                            case "card_type":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("cardType")), likePattern);
                                break;
                            case "payment_channel":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentChannel")), likePattern);
                                break;
                            case "payment_bank":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentBank")), likePattern);
                                break;
                            case "payment_gateway":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentGateway")), likePattern);
                                break;
                            case "auth_code":
                                termPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("authCode")), likePattern);
                                break;
                            default:
                                termPredicate = criteriaBuilder.isTrue(criteriaBuilder.literal(true));
                                break;
                        }
                        if (termPredicate != null) {
                            orPredicates.add(termPredicate);
                        }
                    }
                }
                // Combine all "OR" predicates into a single predicate
                if (!orPredicates.isEmpty()) {
                    keyPredicate = criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));
                } else {
                    keyPredicate = criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // Fallback if no valid terms
                }

                if (keyPredicate != null) {
                    predicates.add(keyPredicate);
                }
            }

                  
            
            // List of TransactionAdditional fields you want to support from getReq
            Set<String> taFields = new HashSet<>(Arrays.asList(
                "billingPhone",
                "billingAddress",
                "billingCity",
                "billingState",
                "billingCountry",
                "billingZip",
                "rrn",
                "upa",
                "descriptor",
                "productName",
                "authUrl",
                "authData",
                "connectorRef",
                "connectorResponse"
            ));
            
            if (getReq != null) {
                for (String field : taFields) {
                    if (getReq.containsKey(field) && getReq.get(field) != null && !getReq.get(field).toString().isEmpty()) {
                        String value = getReq.get(field).toString().trim();
            
                        // Support "not equal" if value starts with "!" or "NOT:"
                        boolean isNotEqual = false;
                        String actualValue = value;
                        if (value.startsWith("!")) {
                            isNotEqual = true;
                            actualValue = value.substring(1).trim();
                        } else if (value.toLowerCase().startsWith("not:")) {
                            isNotEqual = true;
                            actualValue = value.substring(4).trim();
                        }
            
                        // Support multiple values (comma-separated)
                        List<String> valuesList = Arrays.stream(actualValue.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
            
                        Subquery<Long> subquery = query.subquery(Long.class);
                        Root<TransactionAdditional> taRoot = subquery.from(TransactionAdditional.class);
                        subquery.select(taRoot.get("transIDAd"));
                        Predicate joinPredicate = criteriaBuilder.equal(root.get("transID"), taRoot.get("transIDAd"));
            
                        Predicate valuePredicate;
                        if (valuesList.size() > 1) {
                            // Multiple values: use IN or NOT IN
                            if (isNotEqual) {
                                valuePredicate = criteriaBuilder.not(taRoot.get(field).in(valuesList));
                            } else {
                                valuePredicate = taRoot.get(field).in(valuesList);
                            }
                        } else {
                            // Single value: use equal or notEqual
                            if (isNotEqual) {
                                valuePredicate = criteriaBuilder.notEqual(taRoot.get(field), actualValue);
                            } else {
                                valuePredicate = criteriaBuilder.equal(taRoot.get(field), actualValue);
                            }
                        }
            
                        subquery.where(criteriaBuilder.and(joinPredicate, valuePredicate));
                        predicates.add(criteriaBuilder.exists(subquery));
                        System.out.println(field + " (from TransactionAdditional): " + (isNotEqual ? "NOT " : "") + actualValue);
                    }
                }
            }
            

            // Like search for searchKey in "transID", "reference", "fullName", "billEmail"
            if(getReq != null && getReq.containsKey("searchKey") && getReq.get("searchKey") != null && !getReq.get("searchKey").toString().isEmpty()) {
                String searchKey = getReq.get("searchKey").toString().trim().toLowerCase();
                String likePattern = "%" + searchKey + "%";
                List<Predicate> orPredicates = new ArrayList<>();
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.toString(root.get("transID"))), likePattern));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reference")), likePattern));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billEmail")), likePattern));
                predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
            }

            // List of Transaction fields you want to support from getReq
            Set<String> txnFields = new HashSet<>(Arrays.asList(
                "transID",
                "reference",
                "fullName",
                "billEmail",
                "terminalNumber",
                "billAmount",
                "billCurrency",
                "transactionAmount",
                "availableBalance",
                "transactionDate",
                "methodOfPayment",
                "transactionStatus",
                "merchantID",
                "billIP",
                "paymentGateway",
                "cardType"
            ));
            
            Set<String> stringFields = new HashSet<>(Arrays.asList(
                "transID", "reference", "fullName", "billEmail", "terminalNumber",  "billCurrency", "methodOfPayment", "billIP", "paymentGateway", "cardType"
            ));
            
            if (getReq != null) {
                for (String field : txnFields) {
                    if (getReq.containsKey(field) && getReq.get(field) != null && !getReq.get(field).toString().isEmpty()) {
                        String value = getReq.get(field).toString().trim();
            
                        // Support "not equal" if value starts with "!" or "NOT:"
                        boolean isNotEqual = false;
                        String actualValue = value;
                        if (value.startsWith("!")) {
                            isNotEqual = true;
                            actualValue = value.substring(1).trim();
                        } else if (value.toLowerCase().startsWith("not:")) {
                            isNotEqual = true;
                            actualValue = value.substring(4).trim();
                        }
            
                        // Support multiple values (comma-separated)
                        List<String> valuesList = Arrays.stream(actualValue.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
            
                        Predicate valuePredicate;
                        if (stringFields.contains(field)) {
                            // LIKE/NOT LIKE for string fields
                            List<Predicate> likePredicates = new ArrayList<>();
                            for (String val : valuesList) {
                                String likePattern = "%" + val.toLowerCase() + "%";
                                Predicate likePredicate = criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(field)), likePattern
                                );
                                likePredicates.add(isNotEqual ? criteriaBuilder.not(likePredicate) : likePredicate);
                            }
                            valuePredicate = likePredicates.size() == 1
                                ? likePredicates.get(0)
                                : (isNotEqual
                                    ? criteriaBuilder.and(likePredicates.toArray(new Predicate[0]))
                                    : criteriaBuilder.or(likePredicates.toArray(new Predicate[0])));
                        } else {
                            // IN/NOT IN or EQUAL/NOT EQUAL for non-string fields
                            if (valuesList.size() > 1) {
                                if (isNotEqual) {
                                    valuePredicate = criteriaBuilder.not(root.get(field).in(valuesList));
                                } else {
                                    valuePredicate = root.get(field).in(valuesList);
                                }
                            } else {
                                if (isNotEqual) {
                                    valuePredicate = criteriaBuilder.notEqual(root.get(field), actualValue);
                                } else {
                                    valuePredicate = criteriaBuilder.equal(root.get(field), actualValue);
                                }
                            }
                        }
            
                        predicates.add(valuePredicate);
                        System.out.println(field + " (from Transaction): " + (isNotEqual ? "NOT " : "") + actualValue);
                    }
                }
            }
           

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Map<String, Object> getSuccessRateByMerchantIDAndDateRange(Long merchantID, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get success count (transactions with status 1 or 7)
            Long successCount = transactionDao.countSuccessTransactionsByMerchantIDAndDateRange(merchantID, startDate, endDate);
            
            // Get lists of emails for success and failure transactions
            List<String> successEmails = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, startDate, endDate);
            List<String> failedEmails = transactionDao.findFailedEmailsByMerchantIDAndDateRange(merchantID, startDate, endDate);
            
            // Find emails that have ONLY failure transactions (not in success email list)
            Set<String> successEmailSet = new HashSet<>(successEmails);
            Set<String> failedEmailSet = new HashSet<>(failedEmails);
            
            // Remove emails that exist in success list from failed list
            failedEmailSet.removeAll(successEmailSet);
            
            Long failCount = (long) failedEmailSet.size();
            
            // Calculate success rate percentage
            double successRate = 0.0;
            if (successCount > 0 || failCount > 0) {
                successRate = (double) successCount / (successCount + failCount) * 100.0;
            }
            
            result.put("successCount", successCount != null ? successCount : 0L);
            result.put("failCount", failCount != null ? failCount : 0L);
            result.put("totalTransactions", (successCount != null ? successCount : 0L) + (failCount != null ? failCount : 0L));
            result.put("successRate", Math.round(successRate * 100.0) / 100.0); // Round to 2 decimal places
            result.put("merchantID", merchantID);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            
            log.info("Success rate calculated for merchantID: {}, successCount: {}, failCount: {}, successRate: {}%", 
                    merchantID, successCount, failCount, successRate);
            
        } catch (Exception e) {
            log.error("Error calculating success rate for merchantID: {}", merchantID, e);
            result.put("error", "Error calculating success rate: " + e.getMessage());
            result.put("successCount", 0L);
            result.put("failCount", 0L);
            result.put("totalTransactions", 0L);
            result.put("successRate", 0.0);
        }
        
        return result;
    }

    

    private double round2(double value) {
        return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private double pctChange(double nowVal, double prevVal) {
        if (prevVal == 0.0) {
            return nowVal == 0.0 ? 0.0 : 100.0; // treat as 100% increase from zero
        }
        return ((nowVal - prevVal) / prevVal) * 100.0;
    }

   
    // Success Rate queries as per terNO
    @Override
    public Map<String, Object> getMetricsByTerminalNumber(Long terminalNumber) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get success count and sum billAmount (transactions with status 1 or 7)
            Object[] resultList = transactionDao.countSuccessTransactionsByTerminalNumber(terminalNumber);

            Long successCount = 0L;
            Double monthlyVolume = 0.0;

            for (Object rowObj : resultList) {
                Object[] row = (Object[]) rowObj;
                successCount = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                monthlyVolume = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
               
            }

            
            // Get lists of emails for success and failure transactions
            List<String> successEmails = transactionDao.findSuccessEmailsByTerminalNumber(terminalNumber);
            List<String> failedEmails = transactionDao.findFailedEmailsByTerminalNumber(terminalNumber);
            
            // Find emails that have ONLY failure transactions (not in success email list)
            Set<String> successEmailSet = new HashSet<>(successEmails);
            Set<String> failedEmailSet = new HashSet<>(failedEmails);
            
            // Remove emails that exist in success list from failed list
            failedEmailSet.removeAll(successEmailSet);
            
            Long failCount = (long) failedEmailSet.size();
            
            // Calculate success rate percentage
            double successRate = 0.0;
            if (successCount > 0 || failCount > 0) {
                successRate = (double) successCount / (successCount + failCount) * 100.0;
            }
            
            result.put("monthlyVolume", monthlyVolume != null ? monthlyVolume : 0.0);
            result.put("successCount", successCount != null ? successCount : 0L);
            result.put("failCount", failCount != null ? failCount : 0L);
            result.put("totalTransactions", (successCount != null ? successCount : 0L) + (failCount != null ? failCount : 0L));
            result.put("successRate", Math.round(successRate * 100.0) / 100.0); // Round to 2 decimal places
            result.put("terNO", terminalNumber);
            //result.put("startDate", startDate);
           // result.put("endDate", endDate);
            
            log.info("Success rate calculated for terminalNumber: {}, successCount: {}, failCount: {}, successRate: {}%", 
                    terminalNumber, successCount, failCount, successRate);
            
        } catch (Exception e) {
            log.error("Error calculating success rate for terminalNumber: {}", terminalNumber, e);
            result.put("error", "Error calculating success rate: " + e.getMessage());
            result.put("successCount", 0L);
            result.put("failCount", 0L);
            result.put("totalTransactions", 0L);
            result.put("successRate", 0.0);
        }
        
        return result;
    }


    //check-regulatory
    @Override
    public Map<String, Object> checkVolumeRegulatory(Long merchantID, String billEmail, String period, double volumeLimit, int successLimit, int failLimit, double billAmount) {
        Map<String, Object> result = new HashMap<>();
        try {

            log.info("Checking regulatory compliance for merchantID: {}, billEmail: {}, period: {}, volumeLimit: {}, successLimit: {}, failLimit: {}, billAmount: {}", 
                    merchantID, billEmail, period, volumeLimit, successLimit, failLimit, billAmount);

            Integer regulatoryDays = 0;
            if (period != null && period.matches("\\d+")) {
                regulatoryDays = Integer.parseInt(period);
            } else if (period != null && period.matches("\\d+\\s+Day[s]?")) {
                regulatoryDays = Integer.parseInt(period.split("\\s+")[0]);
            } else {
                regulatoryDays = 1; // default if not parsable
            }
            

            LocalDateTime startDate = LocalDate.now().minusDays(regulatoryDays).atStartOfDay();
            LocalDateTime endDate = LocalDateTime.now();
            
            // Fetch transactions based on terminalNumber and date range
            List<Object[]> transactions = transactionDao.findSuccessfulVolumeAndCount(startDate, endDate, merchantID, billEmail);

            if (transactions.isEmpty()) {
                result.put("message", "No transactions found for the given terminal number and date range.");
                return result;
            }
            double totalVolume = 0.0;
            int successCount = 0;
            int failCount = 0;  
            for (Object[] transaction : transactions) {
                Double volume = (Double) transaction[0];
                Long totalCount = transaction[1] != null ? ((Number) transaction[1]).longValue() : 0L;
                
                if (totalCount != null && totalCount > 0) {
                    totalVolume += volume != null ? volume : 0.0;
                    successCount += totalCount.intValue();
                } else {
                    failCount++;
                } 
            }

            // Check if the number of failed transactions exceeds the fail limit
            Long failedCount = transactionDao.countFailedTransactions(startDate, endDate, merchantID, billEmail);
            if (failedCount > 0 && failedCount > failLimit) {
                result.put("status", "failed");
                result.put("failCount", failedCount);
                result.put("message", "Failed transaction limit exceeded. Failed count: " + failedCount + " exceeds limit: " + failLimit);
                result.put("isBlocked", true); 
                return result;
            }
            
            if (successCount > successLimit) {
                result.put("status", "failed");
                result.put("message", "Success limit exceeded. Success count: " + successCount + " exceeds limit: " + successLimit);
                result.put("isBlocked", true);
            } else if (billAmount > volumeLimit) {
                result.put("status", "failed");
                result.put("message", "Bill amount limit exceeded. Bill amount: " + billAmount + " exceeds limit: " + volumeLimit);
                result.put("isBlocked", true);
            } else if (totalVolume > volumeLimit) {
                result.put("status", "failed");
                result.put("message", "Volume limit exceeded. Total volume: " + totalVolume + " exceeds limit: " + volumeLimit);
                result.put("isBlocked", true);
            } else {            
                result.put("status", "success");
                result.put("message", "Volume is within limit. Total volume: " + totalVolume + " does not exceed limit: " + volumeLimit);
                result.put("isBlocked", false); 
            }
            result.put("totalVolume", totalVolume);
            result.put("successCount", successCount);
            //result.put("failCount", failCount);
            result.put("regulatoryDays", regulatoryDays);
            result.put("volumeLimit", volumeLimit);
            result.put("failLimit", failLimit);
            log.info("Regulatory check completed for merchantID: {}, totalVolume: {}, successCount: {}, failCount: {}", 
                    merchantID, totalVolume, successCount, failCount);  
                 
        } catch (Exception e) {
            log.error("Error checking regulatory compliance: {}", e.getMessage());
            result.put("error", "Error checking regulatory compliance: " + e.getMessage());
        }
        return result;
    }


    //check-regulatory dynamic
    @Override
    public Map<String, Object> checkVolumeRegulatoryDynamic(Long merchantId, String fieldNameGet, String fieldValue, String period, double volumeLimit, int successLimit, int failLimit, double billAmount) {

        String fieldName = getFieldName(fieldNameGet);
        boolean isFromAdditional = isFromTransactionAdditional(fieldNameGet);
        String fromString = " via " + fieldNameGet.toString();
        String fieldValueForQuery = fieldValue;
        if (isFromAdditional && "ccno".equals(fieldNameGet)) {
            try {
                fieldValueForQuery = AES256Util.encrypt(fieldValue);
            } catch (Exception e) {
                log.error("Unable to encrypt ccno for regulatory check: {}", e.getMessage());
                
            }
        }
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("Checking regulatory for field: {}", fieldName);

            int regulatoryDays = 1;
            if (period != null) {
                if (period.matches("\\d+")) {
                    regulatoryDays = Integer.parseInt(period);
                } else if (period.matches("\\d+\\s+Day[s]?")) {
                    regulatoryDays = Integer.parseInt(period.split("\\s+")[0]);
                }
            }

            LocalDateTime startDate = LocalDate.now().minusDays(regulatoryDays).atStartOfDay();
            LocalDateTime endDate = LocalDateTime.now();
            String successQuery = "SELECT SUM(t.billAmount), COUNT(t) FROM Transaction t" +
                (isFromAdditional ? " LEFT JOIN TransactionAdditional a ON a.transIDAd = t.transID" : "") +
                " WHERE t.transactionStatus = 1" +
                " AND t.transactionDate BETWEEN :startDate AND :endDate" +
                " AND t.merchantID = :merchantId" +
                (isFromAdditional ? " AND a." + fieldName + " = :fieldValue"
                                   : " AND t." + fieldName + " = :fieldValue");
            log.info("[Regulatory] successQuery: {}", successQuery);
            log.info("[Regulatory] params → merchantId={}, startDate={}, endDate={}, fieldName={}, fieldValue={}",
                    merchantId, startDate, endDate, fieldName,
                    ("ccno".equals(fieldNameGet) ? "[ENCRYPTED]" : fieldValueForQuery));

            @SuppressWarnings("unchecked")
            List<Object[]> data = entityManager.createQuery(successQuery)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("merchantId", merchantId)
                .setParameter("fieldValue", fieldValueForQuery)
                .getResultList();

            

            double totalVolume = 0;
            int successCount = 0;
            if (!data.isEmpty()) {
                Object[] row = data.get(0);
                totalVolume = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                successCount = row[1] != null ? ((Number) row[1]).intValue() : 0;
            }

            String failedQuery = "SELECT COUNT(t) FROM Transaction t" +
                (isFromAdditional ? " LEFT JOIN TransactionAdditional a ON a.transIDAd = t.transID" : "") +
                " WHERE t.transactionStatus IN (2, 23, 24)" +
                " AND t.transactionDate BETWEEN :startDate AND :endDate" +
                " AND t.merchantID = :merchantId" +
                (isFromAdditional ? " AND a." + fieldName + " = :fieldValue"
                                   : " AND t." + fieldName + " = :fieldValue");

            // Log the failed query and params
            log.info("[Regulatory] failedQuery: {}", failedQuery);
            log.info("[Regulatory] params → merchantId={}, startDate={}, endDate={}, fieldName={}, fieldValue={}",
                    merchantId, startDate, endDate, fieldName,
                    ("ccno".equals(fieldNameGet) ?  "[ENCRYPTED]" : fieldValueForQuery));

            Long failedCount = (Long) entityManager.createQuery(failedQuery)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("merchantId", merchantId)
                .setParameter("fieldValue", fieldValueForQuery)
                .getSingleResult();

            System.out.println("\n\n======checkVolumeRegulatoryDynamic=====totalVolume: " + totalVolume + ", successCount: " + successCount + ", failedCount: " + failedCount + "==========\n\n");  

            if (failedCount != null && failedCount > failLimit) {
                result.put("status", "failed");
                result.put("failCount", failedCount);
                result.put("message", "Failed transaction limit exceeded" + fromString + ". Failed count: " + failedCount + " exceeds limit: " + failLimit);
                result.put("isBlocked", true);
                return result;
            }

            if (successCount > successLimit) {
                result.put("status", "failed");
                result.put("message", "Success limit exceeded" + fromString + ". Success count: " + successCount + " exceeds limit: " + successLimit);
                result.put("isBlocked", true);
                return result;
            }

            if (billAmount > volumeLimit) {
                result.put("status", "failed");
                result.put("message", "Bill amount limit exceeded" + fromString + ". Bill amount: " + billAmount + " exceeds limit: " + volumeLimit);
                result.put("isBlocked", true);
                return result;
            }

            if (totalVolume > volumeLimit) {
                result.put("status", "failed");
                result.put("message", "Volume limit exceeded" + fromString + ". Total volume: " + totalVolume + " exceeds limit: " + volumeLimit);
                result.put("isBlocked", true);
                return result;
            }

            result.put("status", "success");
            result.put("isBlocked", false);
            result.put("successCount", successCount);
            result.put("totalVolume", totalVolume);
            return result;

        } catch (Exception e) {
            log.error("Regulatory error: {}", e.getMessage());
            result.put("status", "error");
            result.put("error", e.getMessage());
            return result;
        }
    }

    private String getFieldName(String fieldName) {
        switch (fieldName) {
            // Transaction - master_trans_table_3
            case "bill_email": return "billEmail";
            case "bill_currency": return "billCurrency";
            case "bill_amt": return "billAmount";
            case "bill_ip": return "billIP";
            case "terNO": return "terminalNumber";
            case "mop": return "methodOfPayment";
            case "mopName": return "mopName";
            case "channel_type": return "channelType";
            case "connector": return "connector";
            // TransactionAdditional - master_trans_additional_3
            case "bill_phone": return "billingPhone";
            case "bill_country": return "billingCountry";
            case "bill_zip": return "billingZip";
            case "bill_state": return "billingState";
            case "bill_city": return "billingCity";
            case "bill_address": return "billingAddress";
            case "ccno": return "cardNumber";
            case "bin_no": return "binNumber";
            case "upa": return "upa";
            case "rrn": return "rrn";
            case "product_name": return "productName";
            default: return fieldName;
        }
    }

    private boolean isFromTransactionAdditional(String fieldName) {
        switch (fieldName) {
            case "bill_phone":
            case "bill_country":
            case "bill_zip":
            case "bill_state":
            case "bill_city":
            case "bill_address":
            case "ccno":
            case "bin_no":
            case "upa":
            case "rrn":
            case "product_name":
                return true;
            default:
                return false;
        }
    }
    @Override
    public Map<String, Object> getUnifiedAnalytics(Long merchantID, String period, LocalDateTime startDate, LocalDateTime endDate, String groupType, int responseType, String previous) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;
        String p = (period == null || period.isBlank()) ? "today" : period;
        switch (p) {
            case "10s":
                end = now;
                start = end.minusSeconds(10);
                break;
            case "20s":
                end = now;
                start = end.minusSeconds(20);
                break;
            case "40s":
                end = now;
                start = end.minusSeconds(40);
                break;
            case "1m":
                end = now;
                start = end.minusMinutes(1);
                break;
            case "2m":
                end = now;
                start = end.minusMinutes(2);
                break;
            case "5m":
                end = now;
                start = end.minusMinutes(5);
                break;
            case "10m":
                end = now;
                start = end.minusMinutes(10);
                break;
            case "30m":
                end = now;
                start = end.minusMinutes(30);
                break;
            case "1h":
                end = now;
                start = end.minusHours(1);
            break;
                case "2h":
                    end = now;
                    start = end.minusHours(2);
                    break;
            case "yesterday":
                start = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = start.withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);
                break;
            case "last24h":
                end = now;
                start = end.minusHours(24);
                break;
            case "last7":
                end = now;
                start = end.minusDays(7);
                break;
            case "last30":
                end = now;
                start = end.minusDays(30);
                break;
            case "last90":
                end = now;
                start = end.minusDays(90);
                break;
            case "1y":
                end = now;
                start = end.minusYears(1);
                break;
            case "prevMonth":
                start = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);
                break;
            case "thisMonth":
                start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = now;
                break;
            case "custom":
                start = (startDate != null) ? startDate : now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = (endDate != null) ? endDate : now;
                break;
            case "today":
            default:
                start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = now;
        }
        LocalDateTime prevStart;
        LocalDateTime prevEnd;
        String compareLabel;
        String p2 = p;
        if (previous != null && !previous.isEmpty()) {
            p2 = previous;
        }
        switch (p2) {
            case "10s":
                prevEnd = start;
                prevStart = start.minusSeconds(10);
                compareLabel = "previous 10 seconds";
                break;
            case "20s":
                prevEnd = start;
                prevStart = start.minusSeconds(20);
                compareLabel = "previous 20 seconds";
                break;
            case "40s":
                prevEnd = start;
                prevStart = start.minusSeconds(40);
                compareLabel = "previous 40 seconds";
                break;
            case "1m":
                prevEnd = start;
                prevStart = start.minusMinutes(1);
                compareLabel = "previous 1 minute";
                break;
            case "2m":
                prevEnd = start;
                prevStart = start.minusMinutes(2);
                compareLabel = "previous 2 minutes";
                break;
            case "5m":
                prevEnd = start;
                prevStart = start.minusMinutes(5);
                compareLabel = "previous 5 minutes";
                break;
            case "10m":
                prevEnd = start;
                prevStart = start.minusMinutes(10);
                compareLabel = "previous 10 minutes";
                break;
            case "30m":
                prevEnd = start;
                prevStart = start.minusMinutes(30);
                compareLabel = "previous 30 minutes";
                break;
            case "1h":
                prevEnd = start;
                prevStart = start.minusHours(1);
                compareLabel = "previous 1 hour";
                break;
            case "2h":
                prevEnd = start;
                prevStart = start.minusHours(2);
                compareLabel = "previous 2 hours";
                break;
            case "yesterday":
            case "today":
                prevStart = start.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                prevEnd = prevStart.withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);
                compareLabel = "yesterday";
                break;
            case "last7":
                prevEnd = start;
                prevStart = start.minusDays(7);
                compareLabel = "previous 7 days";
                break;
            case "last24h":
                prevEnd = start;
                prevStart = start.minusHours(24);
                compareLabel = "previous 24 hours";
                break;
            case "last30":
                prevEnd = start;
                prevStart = start.minusDays(30);
                compareLabel = "previous 30 days";
                break;
            case "last90":
                prevEnd = start;
                prevStart = start.minusDays(90);
                compareLabel = "previous 90 days";
                break;
            case "1y":
                prevEnd = start;
                prevStart = start.minusYears(1);
                compareLabel = "previous year";
                break;
            case "thisMonth":
                prevStart = start.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                prevEnd = prevStart.withDayOfMonth(prevStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);
                compareLabel = "previous month";
                break;
            case "prevMonth":
                prevStart = start.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                prevEnd = prevStart.withDayOfMonth(prevStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);
                compareLabel = "month before";
                break;
            case "custom":
                long days = java.time.Duration.between(start, end).toDays();
                prevEnd = start;
                prevStart = start.minusDays(Math.max(days, 1));
                compareLabel = "previous period";
                break;
            default:
                prevEnd = start;
                prevStart = start.minusDays(7);
                compareLabel = "previous period";
        }
        Map<String, Object> result = new HashMap<>();
            result.put("period", p);
            result.put("start", start);
            result.put("end", end);
            result.put("compareLabel", compareLabel);
        List<Map<String, Object>> countries = new ArrayList<>();
        
        if (responseType == 1) 
        {
            Long totalCount = transactionDao.countAllByMerchantAndDateRange(merchantID, start, end);
            Double revenue = transactionDao.sumSuccessfulBillAmountByMerchantAndDateRange(merchantID, start, end);
            Double avgTxn = transactionDao.avgSuccessfulTransactionAmount(merchantID, start, end);
            Long chargebacks = transactionDao.countChargebacksByMerchantAndDateRange(merchantID, start, end);
            Double avgFraud = transactionDao.avgFraudScoreByMerchantAndDateRange(merchantID, start, end);
            Map<String, Object> sr = getSuccessRateByMerchantIDAndDateRange(merchantID, start, end);
            Long successCount = (sr.get("successCount") instanceof Number) ? ((Number) sr.get("successCount")).longValue() : 0L;
            double successRate = (sr.get("successRate") instanceof Number) ? ((Number) sr.get("successRate")).doubleValue() : 0.0;
            List<String> activeEmails = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, start, end);
            int activeCustomers = activeEmails != null ? activeEmails.size() : 0;
            Long totalCountPrev = transactionDao.countAllByMerchantAndDateRange(merchantID, prevStart, prevEnd);
            Double revenuePrev = transactionDao.sumSuccessfulBillAmountByMerchantAndDateRange(merchantID, prevStart, prevEnd);
            Long chargebacksPrev = transactionDao.countChargebacksByMerchantAndDateRange(merchantID, prevStart, prevEnd);
            Double avgFraudPrev = transactionDao.avgFraudScoreByMerchantAndDateRange(merchantID, prevStart, prevEnd);
            Map<String, Object> srPrev = getSuccessRateByMerchantIDAndDateRange(merchantID, prevStart, prevEnd);
            double successRatePrev = (srPrev.get("successRate") instanceof Number) ? ((Number) srPrev.get("successRate")).doubleValue() : 0.0;
            List<Object[]> cardTypeRows = transactionDao.getCardTypeBreakdown(start, end, merchantID);
            List<Map<String, Object>> cardTypes = new ArrayList<>();
            for (Object[] row : cardTypeRows) {
                String cardType = row[0] != null ? row[0].toString() : "Unknown";
                Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                Double amount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                Map<String, Object> entry = new HashMap<>();
                entry.put("cardType", cardType);
                entry.put("count", count);
                entry.put("totalAmount", round2(amount));
                cardTypes.add(entry);
            }
            String grouping = (groupType != null && !groupType.isBlank()) ? groupType :
                    (java.time.Duration.between(start, end).toDays() >= 365 ? "month" :
                    java.time.Duration.between(start, end).toDays() >= 90 ? "week" : "day");
             List<Object[]> trendRows = fetchTrendRowsDynamic(merchantID, start, end, grouping);
            Map<String, Map<String, Object>> byPeriod = new LinkedHashMap<>();
            for (Object[] row : trendRows) {
                String periodKey = String.valueOf(row[0]);
                Integer status = (row[1] instanceof Number) ? ((Number) row[1]).intValue() : null;
                Long count = (row[2] instanceof Number) ? ((Number) row[2]).longValue() : 0L;
                Double amountSum = (row[3] instanceof Number) ? ((Number) row[3]).doubleValue() : 0.0;
                Map<String, Object> bucket = byPeriod.computeIfAbsent(periodKey, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("successful", 0L);
                    m.put("failed", 0L);
                    m.put("volume", 0.0);
                    return m;
                });
                if (status != null && (status == 1 || status == 7)) {
                    bucket.put("successful", ((Long) bucket.get("successful")) + count);
                    bucket.put("volume", ((Double) bucket.get("volume")) + amountSum);
                } else {
                    if (status == 2 || status == 23 || status == 24) {
                        bucket.put("failed", ((Long) bucket.get("failed")) + count);
                    }
                }
            }

            List<Map<String, Object>> trend = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> e : byPeriod.entrySet()) {
                Map<String, Object> point = new HashMap<>();
                point.put("period", e.getKey());
                point.put("successful", e.getValue().get("successful"));
                point.put("failed", e.getValue().get("failed"));
                point.put("volume", round2((Double) e.getValue().get("volume")));
                trend.add(point);
            }
            double volNow = revenue == null ? 0.0 : revenue;
            double volPrev = revenuePrev == null ? 0.0 : revenuePrev;
            result.put("totalRevenue", round2(volNow));
            result.put("totalVolume", round2(volNow));
            result.put("totalVolumePrev", round2(volPrev));
            result.put("totalVolumeDeltaPct", round2(pctChange(volNow, volPrev))); 
            long txNow = totalCount == null ? 0 : totalCount;
            long txPrev = totalCountPrev == null ? 0 : totalCountPrev;
            result.put("transactions", txNow);
            result.put("transactionsPrev", txPrev);
            result.put("transactionsDeltaPct", round2(pctChange(txNow, txPrev)));
            result.put("successRate", round2(successRate));
            result.put("successRatePrev", round2(successRatePrev));
            result.put("successRateDeltaPct", round2(successRate - successRatePrev));
            result.put("activeCustomers", activeCustomers);
            int activeCustomersPrev = 0;
            try {
                List<String> activeEmailsPrev = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, prevStart, prevEnd);
                activeCustomersPrev = activeEmailsPrev != null ? activeEmailsPrev.size() : 0;
            } catch (Exception ignore) {}
            result.put("activeCustomersPrev", activeCustomersPrev);
            result.put("activeCustomersDeltaPct", round2(pctChange(activeCustomers, activeCustomersPrev)));
            long cbNow = chargebacks == null ? 0 : chargebacks;
            long cbPrev = chargebacksPrev == null ? 0 : chargebacksPrev;
            result.put("chargebacks", cbNow);
            result.put("chargebacksPrev", cbPrev);
            result.put("chargebacksDelta", cbNow - cbPrev);
            double afNow = avgFraud == null ? 0.0 : avgFraud;
            double afPrev = avgFraudPrev == null ? 0.0 : avgFraudPrev;
            result.put("avgFraudScore", round2(afNow));
            result.put("avgFraudScorePrev", round2(afPrev));
            result.put("avgFraudScoreDelta", round2(afNow - afPrev));
            result.put("avgTransaction", avgTxn == null ? 0.0 : round2(avgTxn));
            result.put("successfulTransactions", successCount == null ? 0 : successCount);
            result.put("cardTypes", cardTypes);
            result.put("groupType", grouping);
            result.put("trend", trend);

        }       
        else if (responseType == 10) {
            countries = new ArrayList<>();
            try {
                String countryExpr = "COALESCE(NULLIF(ta.billingCountry, ''), NULLIF(ta.issuingCountry, ''), 'XX')";
                String hqlCountries = "SELECT " + countryExpr + ", COUNT(t), " +
                        "SUM(CASE WHEN t.transactionStatus IN (1,7) THEN 1 ELSE 0 END), " +
                        "SUM(t.billAmount) " +
                        "FROM Transaction t LEFT JOIN com.webapp.entity.TransactionAdditional ta ON ta.transIDAd = t.transID " +
                        "WHERE t.transactionDate BETWEEN :start AND :end " +
                        "AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
                        "GROUP BY " + countryExpr;

                @SuppressWarnings("unchecked")
                List<Object[]> countryRows = entityManager.createQuery(hqlCountries)
                        .setParameter("merchantID", merchantID)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();

                String cityExpr = "COALESCE(NULLIF(ta.billingCity, ''), 'Unknown')";
                String hqlCityCounts = "SELECT " + countryExpr + ", " + cityExpr + ", COUNT(t) " +
                        "FROM Transaction t LEFT JOIN com.webapp.entity.TransactionAdditional ta ON ta.transIDAd = t.transID " +
                        "WHERE t.transactionDate BETWEEN :start AND :end " +
                        "AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
                        "GROUP BY " + countryExpr + ", " + cityExpr;

                @SuppressWarnings("unchecked")
                List<Object[]> cityRows = entityManager.createQuery(hqlCityCounts)
                        .setParameter("merchantID", merchantID)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();

                Map<String, List<Object[]>> cityByCountry = new HashMap<>();
                for (Object[] r : cityRows) {
                    String ctry = r[0] != null ? r[0].toString().toUpperCase() : "XX";
                    cityByCountry.computeIfAbsent(ctry, k -> new ArrayList<>()).add(r);
                }

                String typeExpr = "COALESCE(NULLIF(ta.cardType, ''), 'Unknown')";
                String hqlTypeCounts = "SELECT " + countryExpr + ", " + typeExpr + ", COUNT(t) " +
                        "FROM Transaction t LEFT JOIN com.webapp.entity.TransactionAdditional ta ON ta.transIDAd = t.transID " +
                        "WHERE t.transactionDate BETWEEN :start AND :end " +
                        "AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
                        "GROUP BY " + countryExpr + ", " + typeExpr;

                @SuppressWarnings("unchecked")
                List<Object[]> typeRows = entityManager.createQuery(hqlTypeCounts)
                        .setParameter("merchantID", merchantID)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();

                Map<String, List<Object[]>> typeByCountry = new HashMap<>();
                for (Object[] r : typeRows) {
                    String ctry = r[0] != null ? r[0].toString().toUpperCase() : "XX";
                    typeByCountry.computeIfAbsent(ctry, k -> new ArrayList<>()).add(r);
                }

                String brandExpr = "COALESCE(NULLIF(ta.cardBrand, ''), 'Unknown')";
                String hqlBrandCounts = "SELECT " + countryExpr + ", " + brandExpr + ", COUNT(t) " +
                        "FROM Transaction t LEFT JOIN com.webapp.entity.TransactionAdditional ta ON ta.transIDAd = t.transID " +
                        "WHERE t.transactionDate BETWEEN :start AND :end " +
                        "AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
                        "GROUP BY " + countryExpr + ", " + brandExpr;

                @SuppressWarnings("unchecked")
                List<Object[]> brandRows = entityManager.createQuery(hqlBrandCounts)
                        .setParameter("merchantID", merchantID)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();

                Map<String, List<Object[]>> brandByCountry = new HashMap<>();
                for (Object[] r : brandRows) {
                    String ctry = r[0] != null ? r[0].toString().toUpperCase() : "XX";
                    brandByCountry.computeIfAbsent(ctry, k -> new ArrayList<>()).add(r);
                }
                countries = new ArrayList<>();
                for (Object[] row : countryRows) {
                    String code = row[0] != null ? row[0].toString().toUpperCase() : "XX";
                    long txCount = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                    long successTx = row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L;
                    double volumeSum = row[3] instanceof Number ? ((Number) row[3]).doubleValue() : 0.0;
                    double srPct = txCount > 0 ? (successTx * 100.0) / txCount : 0.0;

                    Map<String, Object> item = new HashMap<>();
                    item.put("code", code);                  
                    item.put("country", code);              
                    item.put("transactions", txCount);
                    item.put("volume", round2(volumeSum));
                    item.put("successRate", round2(srPct));
                    List<Object[]> cityList = cityByCountry.getOrDefault(code, new ArrayList<>());
                    cityList.sort((a, b) -> Long.compare(((Number)b[2]).longValue(), ((Number)a[2]).longValue()));
                    List<String> topCities = cityList.stream()
                            .limit(3)
                            .map(r -> r[1] == null ? "Unknown" : r[1].toString())
                            .collect(Collectors.toList());
                    item.put("topCities", topCities);
                    Map<String, Object> paymentMethods = new LinkedHashMap<>();
                    if (txCount > 0) {
                        List<Object[]> typeList = typeByCountry.getOrDefault(code, new ArrayList<>());
                        typeList.sort((a, b) -> Long.compare(((Number)b[2]).longValue(), ((Number)a[2]).longValue()));
                        for (Object[] trow : typeList) {
                            String type = trow[1] == null ? "Unknown" : trow[1].toString();
                            long cnt = trow[2] instanceof Number ? ((Number)trow[2]).longValue() : 0L;
                            double pct = Math.max(0.0, Math.min(100.0, (cnt * 100.0) / txCount));
                            paymentMethods.put(type, round2(pct));
                        }
                    }
                    item.put("paymentMethods", paymentMethods);
                    Map<String, Object> paymentBrands = new LinkedHashMap<>();
                    if (txCount > 0) {
                        List<Object[]> brandList = brandByCountry.getOrDefault(code, new ArrayList<>());
                        brandList.sort((a, b) -> Long.compare(((Number)b[2]).longValue(), ((Number)a[2]).longValue()));
                        for (Object[] brow : brandList) {
                            String brand = brow[1] == null ? "Unknown" : brow[1].toString();
                            long cnt = brow[2] instanceof Number ? ((Number)brow[2]).longValue() : 0L;
                            double pct = Math.max(0.0, Math.min(100.0, (cnt * 100.0) / txCount));
                            paymentBrands.put(brand, round2(pct));
                        }
                    }
                    item.put("paymentBrands", paymentBrands);

                    countries.add(item);
                }
                result.put("countries", countries);
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }

        else if (responseType == 11) {
            try {
                List<String> currentEmailsList = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, start, end);
                Set<String> currentEmails = new HashSet<>();
                if (currentEmailsList != null) {
                    currentEmailsList.stream()
                        .filter(Objects::nonNull)
                        .map(String::toLowerCase)
                        .forEach(currentEmails::add);
                }
                
                Set<String> prevEmails = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, prevStart, prevEnd)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
                List<Map<String, Object>> geographicData = new ArrayList<>();
                try {
                    List<Object[]> geoDistribution = transactionDao.getGeographicDistribution(merchantID, start, end);
                    if (geoDistribution != null) {
                        geographicData = geoDistribution.stream()
                            .filter(Objects::nonNull)
                            .map(row -> {
                                try {
                                    Map<String, Object> geo = new HashMap<>();
                                    geo.put("country", row[0] != null ? row[0] : "Unknown");
                                    geo.put("transactions", row[1] != null ? row[1] : 0);
                                    geo.put("percentage", row.length > 2 && row[2] != null ? row[2] : 0);
                                    geo.put("totalAmount", row.length > 3 && row[3] != null ? row[3] : 0);
                                    return geo;
                                } catch (Exception e) {
                                    log.error("Error processing geographic distribution row: " + Arrays.toString(row), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    log.error("Error fetching geographic distribution for merchant: " + merchantID, e);
                }
                
                List<Map<String, Object>> paymentData = new ArrayList<>();
                try {
                    List<Object[]> paymentMethods = transactionDao.getPaymentMethodDistribution(merchantID, start, end);
                    if (paymentMethods != null) {
                        paymentData = paymentMethods.stream()
                            .filter(Objects::nonNull)
                            .map(row -> {
                                try {
                                    Map<String, Object> payment = new HashMap<>();
                                    payment.put("method", row[0] != null ? row[0] : "Unknown");
                                    payment.put("count", row[1] != null ? row[1] : 0);
                                    payment.put("percentage", row.length > 2 && row[2] != null ? row[2] : 0);
                                    return payment;
                                } catch (Exception e) {
                                    log.error("Error processing payment method row: " + Arrays.toString(row), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    log.error("Error fetching payment method distribution for merchant: " + merchantID, e);
                }
                
                List<Map<String, Object>> topCustomersData = new ArrayList<>();
                try {
                    List<Object[]> topCustomers = transactionDao.getTopPerformingCustomers(merchantID, start, end, 10);
                    if (topCustomers != null) {
                        topCustomersData = topCustomers.stream()
                            .filter(Objects::nonNull)
                            .map(row -> {
                                try {
                                    Map<String, Object> customer = new HashMap<>();
                                    customer.put("email", row[0] != null ? row[0] : "Unknown");
                                    customer.put("totalSpend", row.length > 1 && row[1] != null ? row[1] : 0);
                                    customer.put("transactionCount", row.length > 2 && row[2] != null ? row[2] : 0);
                                    customer.put("lastTransactionDate", row.length > 3 ? row[3] : null);
                                    return customer;
                                } catch (Exception e) {
                                    log.error("Error processing customer row: " + Arrays.toString(row), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    log.error("Error fetching top performing customers for merchant: " + merchantID, e);
                }

                result.put("geographicDistribution", geographicData != null ? geographicData : Collections.emptyList());
                result.put("paymentMethodPreferences", paymentData != null ? paymentData : Collections.emptyList());
                result.put("topPerformingCustomers", topCustomersData != null ? topCustomersData : Collections.emptyList());
                long winDays = Math.max(1L, Duration.between(start, end).toDays());
                LocalDateTime prevPrevEnd = prevStart;
                LocalDateTime prevPrevStart = prevStart.minusDays(winDays);
                List<String> prevPrevEmailsList = transactionDao.findSuccessEmailsByMerchantIDAndDateRange(merchantID, prevPrevStart, prevPrevEnd);
                Set<String> prevPrevEmails = new HashSet<>();
                if (prevPrevEmailsList != null)
                    prevPrevEmailsList.forEach(e -> { if (e != null) prevPrevEmails.add(e.toLowerCase()); });
                Set<String> retained = new HashSet<>(currentEmails);
                retained.retainAll(prevEmails);
                Set<String> newNow = new HashSet<>(currentEmails);
                newNow.removeAll(prevEmails);
                Set<String> atRiskNow = new HashSet<>(prevEmails);
                atRiskNow.removeAll(currentEmails);
                int currentActive = currentEmails.size();
                int prevActive = prevEmails.size();
                int vipCount = (currentActive > 0) ? Math.max(1, (int)Math.round(currentActive * 0.10)) : 0;
                vipCount = Math.min(vipCount, currentActive);
                int regularCount = retained.size();
                int newCount = newNow.size();
                int atRiskCount = atRiskNow.size();
                int prevVipCount = (prevActive > 0) ? Math.max(1, (int)Math.round(prevActive * 0.10)) : 0;
                prevVipCount = Math.min(prevVipCount, prevActive);
                Set<String> prevRetained = new HashSet<>(prevEmails);
                prevRetained.retainAll(prevPrevEmails);
                int prevRegularCount = prevRetained.size();
                Set<String> prevNew = new HashSet<>(prevEmails);
                prevNew.removeAll(prevPrevEmails);
                int prevNewCount = prevNew.size();
                Set<String> prevAtRisk = new HashSet<>(prevPrevEmails);
                prevAtRisk.removeAll(prevEmails);
                int prevAtRiskCount = prevAtRisk.size();
                Double revenueNow = transactionDao.sumSuccessfulBillAmountByMerchantAndDateRange(merchantID, start, end);
                double totalRevenueNow = (revenueNow == null) ? 0.0 : revenueNow;
                double effectiveRevenue = totalRevenueNow > 0 ? totalRevenueNow : 100; 
                double vipWeight = Math.max(1, vipCount);
                double regularWeight = Math.max(1, regularCount);
                double newWeight = Math.max(1, newCount);
                double totalWeight = vipWeight + regularWeight + newWeight;
                double vipRevenue = (vipCount > 0) ? effectiveRevenue * (vipWeight / totalWeight) : 0.0;
                double regularRevenue = (regularCount > 0) ? effectiveRevenue * (regularWeight / totalWeight) : 0.0;
                double newRevenue = (newCount > 0) ? effectiveRevenue * (newWeight / totalWeight) : 0.0;
                double atRiskRevenue = 0.0;
                BiFunction<Integer, Integer, Double> pctChange = (curr, prev) -> {
                    if (prev == null || prev == 0) return curr > 0 ? 100.0 : 0.0;
                    return ((curr - prev) * 100.0) / prev;
                };
                Map<String, Object> segments = new LinkedHashMap<>();
                Map<String, Object> vip = new LinkedHashMap<>();
                vip.put("name", "VIP Customers");
                vip.put("count", vipCount);
                vip.put("revenue", round2(vipRevenue));
                vip.put("avgSpend", vipCount > 0 ? round2(vipRevenue / vipCount) : 0.0);
                vip.put("retention", 95.0); // high retention (configurable)
                vip.put("growth", round2(pctChange.apply(vipCount, prevVipCount)));
                segments.put("vip", vip);

                Map<String, Object> regular = new LinkedHashMap<>();
                regular.put("name", "Regular Customers");
                regular.put("count", regularCount);
                regular.put("revenue", regularRevenue);
                regular.put("avgSpend", regularCount > 0 ? round2(regularRevenue / regularCount) : 0.0);
                regular.put("retention", 100.0); // explicitly retained
                regular.put("growth", round2(pctChange.apply(regularCount, prevRegularCount)));
                segments.put("regular", regular);

                Map<String, Object> newer = new LinkedHashMap<>();
                newer.put("name", "New Customers");
                newer.put("count", newCount);
                newer.put("revenue", round2(newRevenue));
                newer.put("avgSpend", newCount > 0 ? round2(newRevenue / newCount) : 0.0);
                newer.put("retention", 0.0);
                newer.put("growth", round2(pctChange.apply(newCount, prevNewCount)));
                segments.put("new", newer);

                Map<String, Object> atRisk = new LinkedHashMap<>();
                atRisk.put("name", "At-Risk Customers");
                atRisk.put("count", atRiskCount);
                atRisk.put("revenue", round2(atRiskRevenue));
                atRisk.put("avgSpend", 0.0);
                atRisk.put("retention", 0.0);
                atRisk.put("growth", round2(-pctChange.apply(atRiskCount, prevAtRiskCount)));
                segments.put("atRisk", atRisk);

                result.put("segments", segments);
            } catch (Exception e) {
                result.put("segments", new HashMap<String, Object>());
            }
        }

        return result;
    }
    
    private List<Object[]> fetchTrendRowsDynamic(Long merchantID, LocalDateTime start, LocalDateTime end, String grouping) {
        String periodExpr;
        switch (grouping) {
            case "week":
                periodExpr = "FUNCTION('to_char', FUNCTION('date_trunc','week', t.transactionDate), 'IYYY-IW')";
                break;
            case "month":
                periodExpr = "FUNCTION('to_char', t.transactionDate, 'YYYY-MM')";
                break;
            case "year":
                periodExpr = "FUNCTION('to_char', t.transactionDate, 'YYYY')";
                break;
            case "day":
            default:
                periodExpr = "FUNCTION('to_char', t.transactionDate, 'YYYY-MM-DD')";
        }

        String hql = "SELECT " + periodExpr + ", t.transactionStatus, COUNT(t), SUM(t.billAmount) " +
                "FROM Transaction t WHERE (:merchantID IS NULL OR t.merchantID = :merchantID) " +
                "AND t.transactionDate BETWEEN :start AND :end " +
                "GROUP BY " + periodExpr + ", t.transactionStatus ORDER BY " + periodExpr + " ASC";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(hql)
                .setParameter("merchantID", merchantID)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return rows;
    }



}