package com.transactions.controller;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.entity.Transaction;
import com.transactions.entity.TransactionAdditional;
import com.transactions.repository.TransactionAdditionalRepository;
import com.transactions.service.TransactionService;
import com.transactions.utility.Base64Util;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Mono;
import org.springframework.data.jpa.domain.Specification;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    
    @Value("${spring.web.cors-allowed-origins}")
    private String corsAllowedOrigins;

    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private TransactionAdditionalRepository transactionAdditionalRepo;
    
    @Autowired
    private ObjectMapper objectMapper;  
    
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Long merchantID,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String connectorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyName,
            @RequestParam(required = false) String keyValue,
            HttpServletRequest httpRequest) 
    {
        LocalDateTime parsedStartDate = parseFlexibleDate(startDate, true);
        LocalDateTime parsedEndDate = parseFlexibleDate(endDate, false);
        log.info("page: {}, size: {}, sortBy: {}, direction: {}, merchantID: {}, startDate: {}, endDate: {}, connectorId: {}, status: {}, keyName: {}, keyValue: {}", 
                page, size, sortBy, direction, merchantID, parsedStartDate, parsedEndDate, connectorId, status, keyName, keyValue);
        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;
		String referer = httpRequest.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) 
		{
			log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }
        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }
        log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);
        if("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }

        if (session == null || session.getAttribute("USER_ID") == null) {
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }
        Map<String, Object> getReq = new HashMap<>();
		if (httpRequest.getMethod().equalsIgnoreCase("GET")) {
            String queryString = httpRequest.getQueryString();
            if (queryString != null) {
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyVal = pair.split("=");
                    if (keyVal.length == 2) {
                        String key = java.net.URLDecoder.decode(keyVal[0], java.nio.charset.StandardCharsets.UTF_8);
                        String value = java.net.URLDecoder.decode(keyVal[1], java.nio.charset.StandardCharsets.UTF_8);
                        getReq.put(key, value);
                    }
                }
            }
        }
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Sort sort = Sort.by(sortDirection, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Short statusShort = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusShort = Short.parseShort(status);
                } catch (NumberFormatException e) {
                	e.printStackTrace();
                }
            }
            Specification<Transaction> spec = transactionService.getTransactionSpecification(merchantID,parsedStartDate,parsedEndDate,connectorId,statusShort,keyName,keyValue,getReq);
            Page<Transaction> transactionsPage = transactionService.findAllWithFilters(spec, pageable);
            Page<Map<String, Object>> transactions = transactionsPage.map(transaction -> {
                Map<String, Object> combinedData = new HashMap<>();
                combinedData.put("transID", transaction.getTransID().toString());
                combinedData.put("transaction", transaction);
                TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transaction.getTransID());
                if (additional != null) {
                    Map<String, Object> additionalMap = new HashMap<>();
                    additionalMap.put("transID", String.valueOf(transaction.getTransID()));
                    additionalMap.put("id", additional.getId());
                    additionalMap.put("binNumber", additional.getBinNumber());
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
                    additionalMap.put("authData", Base64Util.decodeBase64(additional.getAuthData()));
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
                    String formattedCardNumber = additional.getFormattedCardNumber();
                    if (formattedCardNumber != null && !formattedCardNumber.isEmpty()) {
                        additionalMap.put("cardNumber", maskCardNumber(formattedCardNumber));
                        additionalMap.put("ccnoDecrypted", maskCardNumber(formattedCardNumber));
                    } else {
                        if (additional.getBinNumber() != null) {
                            String binStr = String.valueOf(additional.getBinNumber());
                            while (binStr.length() < 4) {
                                binStr = "0" + binStr;
                            }
                            String binDisplay = binStr.substring(0, Math.min(4, binStr.length()));
                            additionalMap.put("cardNumber", binDisplay + "********");
                            additionalMap.put("ccnoDecrypted", binDisplay + "********");
                        } else {
                            additionalMap.put("cardNumber", null);
                            additionalMap.put("ccnoDecrypted", null);
                        }
                    }
                    additionalMap.put("cardType", additional.getCardType());
                    additionalMap.put("cardBrand", additional.getCardBrand());
                    additionalMap.put("issuingCountry", additional.getIssuingCountry());
                    additionalMap.put("issuingBankName", additional.getIssuingBank());
                    combinedData.put("additional", additionalMap);
                } else {
                    combinedData.put("additional", new HashMap<>());
                }
                
                return combinedData;
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", transactions.getContent());
            response.put("currentPage", transactions.getNumber());
            response.put("totalItems", transactions.getTotalElements());
            response.put("totalPages", transactions.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching transactions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching transactions"));
        }
    }

    @GetMapping("/all/{merchantID}")
    public ResponseEntity<?> getAllTransactionsByMerchantID(@PathVariable Long merchantID, HttpServletRequest request) {
        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = request.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;
		String referer = request.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) 
		{
			log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }
        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }
          log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);
        if("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }

        if (session == null || session.getAttribute("USER_ID") == null) {
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }

        try {
            List<Map<String, Object>> combinedData = transactionService.getAllTransactionsByMerchantID(merchantID);
            log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);
            return ResponseEntity.ok(combinedData);
        } catch (Exception e) {
            log.error("Error fetching transactions for merchant {}: ", merchantID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching transactions"));
        }
    }
    

   

    // refund-request make and update transaction and getting post method for transID,refundAmount,refundReason and update transactionStatus is 8 transactionResponse from refundReason
    @PostMapping("/refund-request")
    public ResponseEntity<?> handleRefundRequest(@RequestBody Map<String, Object> refundData, HttpServletRequest serverRequest) {
        try {
            log.info("=== handleRefundRequest === Starting refund request processing");
            log.info("Received refund request data: {}", refundData);

            Boolean retrunResponse = false;
            String statusMsg = "Transaction is already in Refund Request Processed";

            String transID = refundData.get("transID").toString();
            // Get existing transactionGet
            Transaction transactionGet = transactionService.getTransactionByTransID(Long.parseLong(transID));
            if (transactionGet == null) {
                log.error("Transaction not found for ID: {}", transID);
                return ResponseEntity.notFound().build();
            }
            else if (!refundData.containsKey("transID") || !refundData.containsKey("refundAmount") ) {
                log.error("Missing required fields in refund request");
                return ResponseEntity.badRequest().body("Missing required fields: transID, refundAmount");
            }
            else if(transactionGet.getTransactionStatus().equals((short) 8) || transactionGet.getTransactionStatus().equals((short) 7)) {
                log.error("Transaction is already in Refund Processed Generated");
                retrunResponse = true;
            }
            else if(transactionGet.getTransactionStatus() != (short) 1 ) {
                statusMsg = "Not authorized to process because this is not successfull transaction";
                log.error(statusMsg);
                retrunResponse = true;
            }

            if(retrunResponse)
            {
                Map<String, Object> transResponse = transactionService.getTransactionDetails(transID.toString(), false);

                transResponse.put("message", statusMsg);

	            return ResponseEntity.ok(transResponse);
            }

           
            // Format refund amount to 2 decimal places
            Double refundAmount = Math.round(Double.parseDouble(refundData.get("refundAmount").toString()) * 100.0) / 100.0;

            //Double originalAmount = Double.parseDouble(refundData.get("originalAmount").toString());
            String refundReason = "";
            Double originalAmount = 0.0;
            
            if(transactionGet.getBankProcessingAmount() != null)
                originalAmount = Double.parseDouble(transactionGet.getBankProcessingAmount().toString());
            else if(transactionGet.getTransactionAmount() != null)
                originalAmount = Double.parseDouble(transactionGet.getTransactionAmount().toString());
            else if(transactionGet.getBillAmount() != null)
                originalAmount = Double.parseDouble(transactionGet.getBillAmount().toString());

            String loginUser = "";
            // Check if loginUser is null or empty
            if (refundData.containsKey("loginUser ") && refundData.get("loginUser ") != null && !refundData.get("loginUser ").toString().isEmpty()) {
                loginUser  = " by " + refundData.get("loginUser ").toString() + ", ";
            } 
            if (refundData.containsKey("refundReason") && refundData.get("refundReason") != null) {
                refundReason = refundData.get("refundReason").toString();
            } 

            // Fix log formatting syntax
            log.info("Processing refund - TransID: {}, Amount: {}, Reason: {}", 
                    transID, 
                    String.format("%.2f", refundAmount), 
                    refundReason);

            // Validate refund amount
            if (refundAmount <= 0 || refundAmount > originalAmount) {
                log.error("Invalid refund amount: {} (Original amount: {})", 
                        String.format("%.2f", refundAmount),
                        String.format("%.2f", originalAmount));
                return ResponseEntity.badRequest().body("Invalid refund amount. Must be greater than 0 and less than or equal to original amount");
            }
            log.info("Found transactionGet for ID: {}", transID);
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(Long.parseLong(transID));
            if (additional == null) {
                log.info("Creating new TransactionAdditional for ID: {}", transID);
                additional = new TransactionAdditional();
                additional.setTransIDAd(Long.parseLong(transID));
            }
            transactionGet.setTransactionStatus((short) 8);
            String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
            String messageString = "";
            if (refundAmount != null && originalAmount != null && !refundAmount.equals(originalAmount))
            {
                messageString = "Partial Refund Request Processed " + loginUser + "from ip " + serverRequest.getRemoteAddr() + ": Amount " + refundAmount ;
            }
            else
            {
                messageString = "Request Processed" + loginUser + " from ip " + serverRequest.getRemoteAddr();    
            }

            String existingNotes = additional.getSupportNote() != null ? additional.getSupportNote() + "\n" : "";
            additional.setSupportNote(existingNotes + currentDateTime + " | " + messageString + " - " + refundReason);
            additional.setTransactionResponse(messageString);
            
            // Save updates
            log.info("Saving transactionGet updates - New status: {}", transactionGet.getTransactionStatus());
            Transaction updatedTransaction = transactionService.updateTransaction(transID, transactionGet, additional);
            
            if (updatedTransaction == null) {
                log.error("Failed to update transactionGet - " + transID);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update transactionGet - " + transID);
            }

            log.info("Successfully processed refund request for TransID: {}", transID);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("transactionGet", updatedTransaction);
            response.put("additional", additional);
            response.put("message", transID + " :: Refund request submitted successfully");

            Map<String, Object> transResponse = transactionService.getTransactionDetails(transID.toString(), false);
	        return ResponseEntity.ok(transResponse);


        } catch (NumberFormatException e) {
            log.error("Invalid number format in refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid number format in request");
        } catch (Exception e) {
            log.error("Error processing refund request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error processing refund request: " + e.getMessage());
        }
    }

    // refund-accept make a create same duplicate method as a new transactionGet and save it in the database with transactionStatus is 3 - refunded 
    // and update transactionGet and getting post method for transID,refundAmount,refundReason and update transactionStatus is 7 transactionResponse from refundReason
    @PostMapping({"/refund-accept","/chargeback","/predisput"})
    public ResponseEntity<?> handleRefundAccept(@RequestBody Map<String, Object> refundData, HttpServletRequest serverRequest) {
        try {
            log.info("=== handleRefundAccept === Starting refund request processing");
            log.info("Received refund accept data: {}", refundData);
            boolean isRefund = true;
            String systemNote = "";
            String chargebackType = "";
            String chargebackAlertMessage = "";
            String chargebackMessage = "";
            String requestURI = serverRequest.getRequestURI(); 
            if (requestURI.contains("/chargeback")) {
                isRefund = false;
            }
            if( !isRefund && refundData.containsKey("chargebackType") ) {
                chargebackType = refundData.get("chargebackType").toString();
                chargebackAlertMessage = ( refundData.containsKey("chargebackAlertMessage") && refundData.get("chargebackAlertMessage") !=null ) ? refundData.get("chargebackAlertMessage").toString() : "";
                chargebackMessage = chargebackType + " Reason: " + chargebackAlertMessage + " - Transation " + chargebackType + " by System with ";
            }
            String transID = refundData.get("transID").toString();
            if (!refundData.containsKey("transID")) {
                log.error("Missing transID in refund - chargeback request");
                return ResponseEntity.badRequest().body("Missing transID in refund - chargeback request");
            }

            Transaction transactionGet = transactionService.getTransactionByTransID(Long.parseLong(transID));
            if (transactionGet == null) {
                log.error("Transaction not found for ID: {}", transID);
                return ResponseEntity.notFound().build();
            }

            log.info("Found transactionGet for ID: {}", transID);
            String billAmount = transactionGet.getBillAmount().toString().replace("-", "");
            TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(Long.parseLong(transID));
            if (additional == null) {
                log.info("Creating new TransactionAdditional for ID: {}", transID);
                additional = new TransactionAdditional();
                additional.setTransIDAd(Long.parseLong(transID));
            }

            String existingsystemNote = additional.getSystemNote() != null ? additional.getSystemNote() + "\n" : "";
            String existingNotes = additional.getSupportNote() != null ? additional.getSupportNote() + "\n" : "";
            String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
            String loginUser = "";
            if(refundData.containsKey("loginUser") && refundData.get("loginUser") == null || loginUser.isEmpty()) {
                log.error("Login user is null or empty");
                loginUser = "by " + refundData.get("loginUser").toString() + ", ";
            }
            Short updateStatus = (short) 3;
            Double refundAmount = (double) 0;
            Double originalAmount = (double) 0;
            String refundReason = "";
            String currency = "";
            if(isRefund)
            {
                if (!refundData.containsKey("transID") || !refundData.containsKey("refundAmount") || !refundData.containsKey("refundReason")) {
                    log.error("Missing required fields in refund request");
                    return ResponseEntity.badRequest().body("Missing required fields: transID, refundAmount, refundReason");
                }

                refundAmount = Math.round(Double.parseDouble(refundData.get("refundAmount").toString()) * 100.0) / 100.0;
                
                refundReason = refundData.get("refundReason").toString();
                originalAmount = Double.parseDouble(refundData.get("originalAmount").toString());
                currency = refundData.get("currency").toString();

                log.info("Processing refund - TransID: {}, Amount: {}{}, Reason: {}", 
                        transID, 
                        String.format("%.2f", refundAmount), 
                        currency, 
                        refundReason);

                if (refundAmount <= 0 || refundAmount > originalAmount) {
                    log.error("Invalid refund amount: {} (Original amount: {})", 
                            String.format("%.2f", refundAmount),
                            String.format("%.2f", originalAmount));
                    return ResponseEntity.badRequest().body("Invalid refund amount. Must be greater than 0 and less than or equal to original amount");
                }

                

                if(transactionGet.getTransactionStatus().equals((short) 7)) 
                {
                    log.error("Transaction is already in Refund Approved status for transID : "+transID);
                    return ResponseEntity.badRequest().body("Transaction is already in Refund Approved status for transID : "+transID);
                }

            
                if(refundAmount !=null  && currency !=null )
                {
                    systemNote = "Refund proccess manually approved by " + loginUser + " from ip " + serverRequest.getRemoteAddr() + ": Amount " + refundAmount + " " + currency + " - ";
                    // refund api call  
                    // transactionGet.setTransactionResponse("Refund Approved");
                }
            

                try
                {    
                    class StateHolder {
                        String systemNote;
                        TransactionAdditional additional;
                        StateHolder(String note, TransactionAdditional add) {
                            this.systemNote = note;
                            this.additional = add;
                        }
                    }
                    final StateHolder state = new StateHolder(systemNote, additional);
                    
                    Mono<ResponseEntity<Map<String, Object>>> serviceResponse = transactionService.updateTransStatusReactive(transID, null, Boolean.TRUE, Boolean.TRUE);
                    serviceResponse.subscribe(response -> {
                        if (response.getStatusCode() != HttpStatus.OK) {
                            log.error("Refund API call failed: {}", response.getBody());
                            System.out.println("Refund API call failed: " + response.getBody());
                        }

                        System.out.println("Refund API call successful: " + response.getBody());

                        Map<String, Object> responseBody = response.getBody();
                        if (responseBody != null && !responseBody.isEmpty()) {
                            String refundStatus = (String) responseBody.get("status");
                            String refundResponse = (String) responseBody.get("connector_response_msg");
                            state.systemNote = "Refund API call successful: " + refundStatus + " - " + refundResponse;
                            state.additional.setTransactionResponse(refundResponse);
                        } else {
                            log.error("Empty response from refund API call");
                            System.out.println("Empty response from refund API call");
                        }
                    });
                    
                    // Update the original variables
                    systemNote = state.systemNote;
                    additional = state.additional;
                }
                catch (Exception e)
                {
                    log.error("Error in refund api call: ", e);
                    System.out.println("Error in refund api call: " + e.getMessage());
                }

            }
            //chargeback
            else 
            {
                refundAmount = Double.parseDouble(billAmount);

                if(chargebackType !=null && "Chargeback".equals(chargebackType) )
                {
                    updateStatus = (short) 5;
                }
                else {
                    updateStatus = (short) 11;
                }

                systemNote = chargebackMessage + loginUser + " from ip " + serverRequest.getRemoteAddr();
                additional.setTransactionResponse(chargebackAlertMessage);
            }
            transactionGet.setTransactionStatus((short) 7);
            transactionGet.setRemarkStatus((short) 2); 
            Long transID_2 = generateUniqueTransID(transactionGet.getConnector().toString(), (long) 0); 
            Transaction transaction = new Transaction();
			TransactionAdditional transactionAdditional = new TransactionAdditional();
            transaction.setTransactionStatus((short) updateStatus);
            transaction.setBillAmount( Double.parseDouble("-" + billAmount));
            transaction.setBillCurrency(transactionGet.getBillCurrency());
            transaction.setBankProcessingAmount(Double.parseDouble("-" + refundAmount));
            transaction.setBankProcessingCurrency(transactionGet.getBankProcessingCurrency());
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setMerchantID(transactionGet.getMerchantID());
            transaction.setReference(transactionGet.getReference());
            transaction.setBearerToken(transactionGet.getBearerToken());
            transaction.setChannelType(transactionGet.getChannelType());
            transaction.setConnector(transactionGet.getConnector());
            transaction.setFeeId(transactionGet.getFeeId());
            transaction.setFullName(transactionGet.getFullName());
            transaction.setBillEmail(transactionGet.getBillEmail());
            transaction.setBillIP(transactionGet.getBillIP());
            transaction.setTerminalNumber(transactionGet.getTerminalNumber());
            transaction.setMethodOfPayment(transactionGet.getMethodOfPayment());
            transaction.setChannelType(transactionGet.getChannelType());
            transaction.setIntegrationType(transactionGet.getIntegrationType());
            transaction.setTransactionType(transactionGet.getTransactionType());
            transaction.setSettlementDelay(transactionGet.getSettlementDelay());
            transaction.setRollingDate(transactionGet.getRollingDate());
            transaction.setRollingDelay(transactionGet.getRollingDelay());
            transaction.setRiskRatio(transactionGet.getRiskRatio());
            transaction.setTransactionPeriod(transactionGet.getTransactionPeriod());
            if(transactionGet.getBuyMdrAmount() != null) {
                transaction.setBuyMdrAmount(Double.parseDouble("-" + transactionGet.getBuyMdrAmount()));
            }
            if(transactionGet.getSellMdrAmount() != null) {
                transaction.setSellMdrAmount(Double.parseDouble("-" + transactionGet.getSellMdrAmount()));
            }
            if(transactionGet.getBuyTxnFeeAmount() != null) {
                transaction.setSellMdrAmount(Double.parseDouble("-" + transactionGet.getSellTxnFeeAmount()));
            }
            if(transactionGet.getSellTxnFeeAmount() != null) {
                transaction.setSellTxnFeeAmount(Double.parseDouble("-" + transactionGet.getSellTxnFeeAmount()));
            }
            if(transactionGet.getGstAmount() != null) {
                transaction.setGstAmount(Double.parseDouble("-" + transactionGet.getGstAmount()));
            }
            if(transactionGet.getRollingAmount() != null) {
                transaction.setRollingAmount(Double.parseDouble("-" + transactionGet.getRollingAmount()));
            }
            if(transactionGet.getMdrCashbackAmount() != null) {
                transaction.setMdrCashbackAmount(Double.parseDouble("-" + transactionGet.getMdrCashbackAmount()));
            }
            if(transactionGet.getPayableTransactionAmount() != null) {
                transaction.setPayableTransactionAmount(Double.parseDouble("-" + transactionGet.getPayableTransactionAmount()));
            }
            if(transactionGet.getMdrRefundFeeAmount() != null) {
                transaction.setMdrRefundFeeAmount(Double.parseDouble("-" + transactionGet.getMdrRefundFeeAmount()));
            }
            if(transactionGet.getAvailableRolling() != null) {
                transaction.setAvailableRolling(Double.parseDouble("-" + transactionGet.getAvailableRolling()));
            }
           
            if(transactionGet.getMatureRollingFundAmount() != null) {
                transaction.setMatureRollingFundAmount(Double.parseDouble("-" + transactionGet.getMatureRollingFundAmount()));
            }
            if(transactionGet.getImmatureRollingFundAmount() != null) {
                transaction.setImmatureRollingFundAmount(Double.parseDouble("-" + transactionGet.getImmatureRollingFundAmount()));
            }
            
            transaction.setTransID(transID_2);
            transactionAdditional.setAuthUrl(additional.getAuthUrl());
            transactionAdditional.setAuthData(additional.getAuthData());
            transactionAdditional.setSourceUrl(additional.getSourceUrl());
            transactionAdditional.setWebhookUrl(additional.getWebhookUrl());
            transactionAdditional.setReturnUrl(additional.getReturnUrl());
            transactionAdditional.setUpa(additional.getUpa());
            transactionAdditional.setRrn(additional.getRrn());
            transactionAdditional.setConnectorRef(additional.getConnectorRef());
            transactionAdditional.setConnectorResponse(additional.getConnectorResponse());
            transactionAdditional.setDescriptor(additional.getDescriptor());
            transactionAdditional.setJsonValue(additional.getJsonValue());
            transactionAdditional.setConnectorJson(additional.getConnectorJson());
            transactionAdditional.setPayloadStage1(additional.getPayloadStage1());
            transactionAdditional.setConnectorCredsProcessingFinal(additional.getConnectorCredsProcessingFinal());
            transactionAdditional.setConnectorResponseStage1(additional.getConnectorResponseStage1());
            transactionAdditional.setConnectorResponseStage2(additional.getConnectorResponseStage2());
            transactionAdditional.setBinNumber(additional.getBinNumber());
            transactionAdditional.setCardNumber(additional.getCardNumber());
            transactionAdditional.setExpiryMonth(additional.getExpiryMonth());
            transactionAdditional.setExpiryYear(additional.getExpiryYear());
            transactionAdditional.setTransactionResponse(additional.getTransactionResponse());
            transactionAdditional.setBillingPhone(additional.getBillingPhone());
            transactionAdditional.setBillingAddress(additional.getBillingAddress());
            transactionAdditional.setBillingCity(additional.getBillingCity());
            transactionAdditional.setBillingState(additional.getBillingState());
            transactionAdditional.setBillingCountry(additional.getBillingCountry());
            transactionAdditional.setBillingZip(additional.getBillingZip());
            transactionAdditional.setProductName(additional.getProductName());
            if(systemNote != null)
            {
                transactionAdditional.setSystemNote(existingsystemNote + currentDateTime + " | " + systemNote);
            }
            String messageString = "";
            if (isRefund) 
            {
                if (refundAmount != null && originalAmount != null && !refundAmount.equals(originalAmount)) {
                    messageString = "Partial Refund Approved :- Transation has been Refunded " + loginUser + "from ip " + serverRequest.getRemoteAddr() + ": Amount " + refundAmount + " " + currency + " with your previous transID: " + transID + " - ";    
                } else {
                    messageString = "Refund Approved :- Transation has been Refunded " + loginUser + " from ip " + serverRequest.getRemoteAddr() + " with your previous transID: " + transID + " - ";   
                }
            }
            else {
                messageString = chargebackMessage + " with your previous transID: " + transID;
            }

            
            transactionAdditional.setSupportNote(existingNotes + currentDateTime  + " | " + messageString + refundReason);
            // Prepare additional transactionGet data
	        //transactionAdditional.setTransIDAd(transID_2);
	        //transactionAdditional.setId(savedTransaction.getId());
            
            // Save refund transactionGet and its additional data
            //Transaction savedRefundTransaction = transactionService.saveTransaction(transaction, transactionAdditional);
	        Transaction savedTransaction = transactionService.saves2sTrans(transaction);
	        transID_2 = generateUniqueTransID(transactionGet.getConnector().toString(), savedTransaction.getId());
	        savedTransaction.setTransID(transID_2);
	        transactionService.saves2sTrans(savedTransaction);
	        transID_2 = savedTransaction.getTransID();
	        transactionAdditional.setTransIDAd(transID_2);
	        transactionAdditional.setId(savedTransaction.getId());
            log.info("Created refund transaction with new ID: {}", transID_2);
	        transactionService.saves2sTransAdditional(transactionAdditional);
            if (isRefund) 
            {
                if (refundAmount != null && originalAmount != null && !refundAmount.equals(originalAmount)) {
                    messageString = "Partial Refund Approved :- Transation has been Refunded " + loginUser + "from ip " + serverRequest.getRemoteAddr() + ": Amount " + refundAmount + " " + currency + " with your new transID: R" + transID_2 + " - ";    
                } else {
                    messageString = "Refund Approved :- Transation has been Refunded " + loginUser + " from ip " + serverRequest.getRemoteAddr() + " with your new transID: R" + transID_2 + " - ";   
                }
            }
            else {
                messageString = chargebackMessage + " with your new transID: R" + transID_2;
            }

            
            additional.setSupportNote(existingNotes + currentDateTime  + " | " + messageString + refundReason);

            
            additional.setSystemNote(existingsystemNote + currentDateTime + " | " + systemNote);
            
            // Save updates to original transactionGet
            log.info("Saving transactionGet updates - New status: {}", transactionGet.getTransactionStatus());
            Transaction updatedTransaction = transactionService.updateTransaction(transID, transactionGet, additional);
            
            if (updatedTransaction == null) {
                log.error("Failed to update transactionGet");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update transactionGet");
            }

            log.info("Successfully processed refund approved for TransID: {}", transID);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("transactionGet", updatedTransaction);
            response.put("additional", additional);
            response.put("transaction", transaction);
            response.put("message", transID + " :: Refund approved submitted successfully");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            log.error("Invalid number format in refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid number format in request");
        } catch (Exception e) {
            log.error("Error processing refund request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error processing refund request: " + e.getMessage());
        }
    }

    @PostMapping("/webhook/send")
    public ResponseEntity<?> sendWebhook(@RequestBody Map<String, Object> webhookData, HttpServletRequest serverRequest) {
        try {
            log.info("Webhook send request received: {}", webhookData);
            
            String webhookUrl = (String) webhookData.get("webhookUrl");
            Object data = webhookData.get("data");
            
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                return ResponseEntity.badRequest().body("Webhook URL is required");
            }
            
            if (data == null) {
                return ResponseEntity.badRequest().body("Webhook data is required");
            }
            
            log.info("Sending webhook to: {}", webhookUrl);
            log.info("Webhook data: {}", data);
            Map<String, Object> webhookPayload;
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                webhookPayload = dataMap;
            } else {
                webhookPayload = new HashMap<>();
                webhookPayload.put("data", data);
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(webhookPayload);
            java.net.URL url = new java.net.URL(webhookUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "PGX-Webhook-Sender/1.0");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            String responseBody = "";
            try (java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()) {
                if (is != null) {
                    responseBody = new String(is.readAllBytes(), "utf-8");
                }
            }
            
            log.info("Webhook response status: {}", responseCode);
            log.info("Webhook response body: {}", responseBody);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", responseCode >= 200 && responseCode < 300);
            result.put("message", responseCode >= 200 && responseCode < 300 ? "Webhook sent successfully" : "Webhook failed");
            result.put("statusCode", responseCode);
            result.put("responseBody", responseBody);
            
            if (responseCode >= 200 && responseCode < 300) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.valueOf(responseCode)).body(result);
            }
            
        } catch (Exception e) {
            log.error("Error sending webhook: ", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to send webhook: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    @PostMapping("/retry")
    public ResponseEntity<?> retryTransaction(@RequestBody Map<String, Object> retryData, HttpServletRequest serverRequest) {
        try {
            log.info("Retry transaction request received: {}", retryData);
            
            // Extract transaction ID from request
            String transID = (String) retryData.get("transID");
            String loginUser = (String) retryData.get("loginUser");
            
            if (transID == null || transID.isEmpty()) {
                return ResponseEntity.badRequest().body("Transaction ID is required");
            }
            log.info("Retrying transaction {} by user {}", transID, loginUser);
            Long transIDLong = Long.parseLong(transID);
            Transaction transaction = transactionService.getTransactionByTransID(transIDLong);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            if (transaction.getTransactionStatus() == 1) { 
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction is already approved and cannot be retried");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transaction retry initiated successfully");
            response.put("transID", transID);
            response.put("status", "retry_initiated");
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            log.error("Invalid transaction ID format: ", e);
            return ResponseEntity.badRequest().body("Invalid transaction ID format");
        } catch (Exception e) {
            log.error("Error retrying transaction: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error retrying transaction: " + e.getMessage());
        }
    }

    @PutMapping("/update/{transID}")
    public ResponseEntity<?> updateTransaction(@PathVariable String transID, @RequestBody Map<String, Object> requestData) {
        try {
            if (transID == null || transID.isEmpty()) {
                return ResponseEntity.badRequest().body("Transaction ID cannot be null or empty");
            }

            log.debug("Updating transaction {}", transID);
            log.debug("Request data: {}", requestData);
            Transaction transaction = objectMapper.convertValue(requestData.get("transaction"), Transaction.class);
            TransactionAdditional additional = objectMapper.convertValue(requestData.get("additional"), TransactionAdditional.class);
            if (transaction == null) {
                return ResponseEntity.badRequest().body("Invalid transaction data");
            }

            Transaction updated = transactionService.updateTransaction(transID, transaction, additional);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            Long transIDLong = Long.parseLong(transID);
            TransactionAdditional updatedAdditional = transactionAdditionalRepo.findByTransIDAd(transIDLong);

            Map<String, Object> response = new HashMap<>();
            response.put("transaction", updated);
            response.put("additional", updatedAdditional); 
            response.put("message", "Transaction updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating transaction: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error updating transaction: " + e.getMessage());
        }
    }
  
    @GetMapping("/{transID}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long transID) {
        Transaction transaction = transactionService.getTransactionByTransID(transID);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        
        TransactionAdditional additional = transactionAdditionalRepo.findByTransIDAd(transID);
        Map<String, Object> response = new HashMap<>();
        response.put("transaction", transaction);
        response.put("additional", additional);
        
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) Long merchantID,
            @RequestParam(required = false) Short status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            Sort sort = Sort.by(sortDirection, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Map<String, Object>> results = transactionService.searchTransactionsWithPagination(
                reference, merchantID, status, pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", results.getContent());
            response.put("currentPage", results.getNumber());
            response.put("totalItems", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching transactions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error searching transactions"));
        }
    }
    
    private Long generateUniqueTransID(String connectorId, Long long1) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
			String datePart = dateFormat.format(new Date());
			long currentTimeMillis = System.currentTimeMillis();
			String microTimePart = String.format("%06d", currentTimeMillis % 1000000);
			String transIDStr = connectorId + long1 + datePart + microTimePart;
			if (transIDStr.length() > 18) {
				transIDStr = transIDStr.substring(0, 18);
			}
			return Long.parseLong(transIDStr);
    }


    @GetMapping("/fetch/{status}")
    public ResponseEntity<List<Map<String, Object>>> fetchLatest10ApprovedTransactions(@PathVariable Short status) throws Exception {
        Short status_id = Short.parseShort(status.toString());
        List<Map<String, Object>> transactions = transactionService.findLatest10ApprovedByTransactionDateDesc(status_id, null, 0, 10);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/countsw/{status}")
    public ResponseEntity<Long> getTransactionCountByStatus(@PathVariable short status) {
        long count = transactionService.countByTransactionStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/scount/all")
    public ResponseEntity<Map<Short, Long>> getTransactionCountsForStatuses() {
        short[] statuses = {1, 3, 5, 2};
        Map<Short, Long> counts = new HashMap<>();
        for (short status : statuses) {
            counts.put(status, transactionService.countByTransactionStatus(status));
        }
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/sbasum/all")
    public ResponseEntity<Map<Short, Double>> getTransactionSumForStatuses() {
        short[] statuses = {1, 3, 5, 2};
        Map<Short, Double> sums = new HashMap<>();
        for (short status : statuses) {
            double rawValue = transactionService.sumBillAmountByStatuses(status);
            double roundedValue = BigDecimal.valueOf(rawValue)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue();
            sums.put(status, roundedValue);
        }
        return ResponseEntity.ok(sums);
    }

    @GetMapping("/statuswise/merchant/{merchantID}/{status}")
    public ResponseEntity<List<Map<String, Object>>> fetchLatest10ApprovedTransactionsByMerchant(@PathVariable Short status, @PathVariable Long merchantID) throws Exception {
        Short status_id = Short.parseShort(status.toString());
        List<Map<String, Object>> transactions = transactionService.findLatest10ApprovedByTransactionDateDesc(status_id, merchantID, 0, 10);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/statuscount/merchant/{merchantID}")
    public ResponseEntity<?> getTransactionCountsForStatusesByMerchant(
        @PathVariable Long merchantID,
        @RequestParam(defaultValue = "1, 3, 5, 2") List<Short> statuses,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        HttpServletRequest httpRequest
    ) 
    {

        LocalDateTime parsedStartDate = parseFlexibleDate(startDate, true);
        LocalDateTime parsedEndDate = parseFlexibleDate(endDate, false);
        if (parsedStartDate == null && parsedEndDate == null) {
            parsedEndDate = LocalDateTime.now();
            parsedStartDate = parsedEndDate.minusDays(7);
        }
        log.info("getTransactionCountsForStatusesByMerchant=>  merchantID: {}, startDate: {}, endDate: {}", 
                    merchantID, parsedStartDate, parsedEndDate);
        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;
        String referer = httpRequest.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) 
        {
            log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }
        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }
        log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);
        if("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }
        if (session == null || session.getAttribute("USER_ID") == null) {
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }
       
        Map<Short, Long> counts = new HashMap<>();
        for (short status : statuses) {
            counts.put(status, transactionService.countByTransactionStatusAndMerchantIDAndDateRange(status, merchantID, parsedStartDate, parsedEndDate));
        }
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/billamtsum/merchant/{merchantID}")
    public ResponseEntity<?> getTransactionSumForStatusesByMerchant(
        @PathVariable Long merchantID,
        @RequestParam(defaultValue = "1, 3, 5, 2") List<Short> statuses,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        HttpServletRequest httpRequest
    ) 
    {

        LocalDateTime parsedStartDate = parseFlexibleDate(startDate, true);
        LocalDateTime parsedEndDate = parseFlexibleDate(endDate, false);
        if (parsedStartDate == null && parsedEndDate == null) {
            parsedEndDate = LocalDateTime.now();
            parsedStartDate = parsedEndDate.minusDays(7);
        }
        log.info("getTransactionSumForStatusesByMerchant=>  merchantID: {}, startDate: {}, endDate: {}", 
                 merchantID, parsedStartDate, parsedEndDate);
        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;
        String referer = httpRequest.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) 
        {
            log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }

        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }
        log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);
        if("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }

        if (session == null || session.getAttribute("USER_ID") == null) {
            
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }


        Map<Short, Double> sums = new HashMap<>();

        for (short status : statuses) {
            Double rawValue = transactionService.sumBillAmountByStatusesAndMerchantIDAndDateRange(status, merchantID, parsedStartDate, parsedEndDate);
            double safeValue = rawValue != null ? rawValue : 0.0;

            double roundedValue = BigDecimal.valueOf(safeValue)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue();

            sums.put(status, roundedValue);
        }

        return ResponseEntity.ok(sums);
    }

    @GetMapping("/successrate/merchant/{merchantID}")
    public ResponseEntity<?> getSuccessRateByMerchantIDAndDateRange(
        @PathVariable Long merchantID,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        HttpServletRequest httpRequest
    ) {
        LocalDateTime parsedStartDate = parseFlexibleDate(startDate, true);
        LocalDateTime parsedEndDate = parseFlexibleDate(endDate, false);
        if (parsedStartDate == null && parsedEndDate == null) {
            parsedEndDate = LocalDateTime.now();
            parsedStartDate = parsedEndDate.minusDays(7);
        }

        log.info("getSuccessRateByMerchantIDAndDateRange => merchantID: {}, startDate: {}, endDate: {}", 
                merchantID, parsedStartDate, parsedEndDate);

        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;
        String referer = httpRequest.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) 
        {
            log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }

        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }

        log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);

        if("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }

        if (session == null || session.getAttribute("USER_ID") == null) {
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }

        try {
            Map<String, Object> successRateData = transactionService.getSuccessRateByMerchantIDAndDateRange(
                merchantID, parsedStartDate, parsedEndDate);
            
            return ResponseEntity.ok(successRateData);
        } catch (Exception e) {
            log.error("Error fetching success rate for merchant {}: ", merchantID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching success rate statistics"));
        }
    }

    @GetMapping("/terminal/metrics/{merchantID}/{terminalNumber}")
    public ResponseEntity<?> getMetricsByTerminalNumber(
        @PathVariable Long merchantID,
        @PathVariable Long terminalNumber,
        HttpServletRequest httpRequest
    ) {
        
        log.info("getSuccessRateByMerchantIDAndDateRange => merchantID: {}, terNO: {}", 
                merchantID, terminalNumber);

        Long requestedMerchantId = merchantID != null ? merchantID : 0L;
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;

        // Check for referer URL to ensure the request is coming from the expected source
        String referer = httpRequest.getHeader("Referer");

        if (referer == null || !isRefererAllowed(referer)) 
        {
            log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }

        // Check for valid session and appropriate role/access
        if (session == null || userId == null) {
            log.error("No valid session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }

        log.info("Access Session. Role: {}, UserId: {}, RequestedMerchantId: {}", userRole, userId, requestedMerchantId);

        // Not matching merchantID and userId then access denied
        if(("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) && (userId != null && !userId.isEmpty() && !userId.equals("0") && !userId.equals("null") 
            && !userId.equals(String.valueOf(merchantID)))) {
            log.error("Access denied - UserId does not match merchantID. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - UserId does not match merchantID"));
            
        }

        if (session == null || session.getAttribute("USER_ID") == null) {
            log.error("Access denied - Insufficient privileges. Role: {}, UserId: {}, RequestedMerchantId: {}", 
                userRole, userId, merchantID);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied - Insufficient privileges"));
        }

        try {
            Map<String, Object> successRateData = transactionService.getMetricsByTerminalNumber(
                terminalNumber);
            
            return ResponseEntity.ok(successRateData);
        } catch (Exception e) {
            log.error("Error fetching success rate for merchant {}, terminalNumber {}: ", merchantID, terminalNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching success rate statistics"));
        }
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<?> getUnifiedAnalytics(
        @RequestParam(required = false) Long merchantID,
        @RequestParam(defaultValue = "today") String period,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String groupType,
        @RequestParam(required = false) String previous, // New parameter for comparison period
        @RequestParam(defaultValue = "1") int responseType,
        HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("LOGIN_ROLE") : null;
        String userId = session != null ? (String) session.getAttribute("USER_ID") : null;

        String referer = httpRequest.getHeader("Referer");
        if (referer == null || !isRefererAllowed(referer)) {
            log.warn("Unauthorized access attempt. Referer URL: {}, Role: {}, UserId: {}", referer, userRole, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized access attempt. Please access this endpoint from the authorized web application."));
        }

        if (session == null || userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated or session expired"));
        }

        if ("CUSTOMER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
            merchantID = Long.parseLong(userId);
        }

        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            if ("custom".equalsIgnoreCase(period)) {
                if (startDate != null && endDate != null) {
                    try {
                        start = parseFlexibleDate(startDate, true);
                        end = parseFlexibleDate(endDate, false);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Invalid startDate/endDate format for custom period"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "startDate and endDate are required for custom period"));
                }
            }

            
            Map<String, Object> data = transactionService.getUnifiedAnalytics(merchantID, period, start, end, groupType, responseType, previous);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching unified analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching analytics"));
        }
    }

    //Get master analytics for graph as MerchantID and Date Range 
    //

    
    private LocalDateTime parseFlexibleDate(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateStr.trim();

        // Handle date-only format by appending default time
        if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            trimmed += isStart ? " 00:00:00.000000" : " 23:59:59.999999";
        } else if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
            trimmed += isStart ? ".000000" : ".999999";
        } else if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) {
            trimmed += isStart ? "000" : "999";  // Pad to 6-digit microseconds
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        try {
            return LocalDateTime.parse(trimmed, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, or yyyy-MM-dd HH:mm:ss.SSSSSS");
        }
    }

    private boolean isRefererAllowed(String referer) {
        if (referer == null) return false;
        for (String origin : corsAllowedOrigins.split(",")) {
            if (referer.startsWith(origin.trim())) {
                return true;
            }
        }
        return false;
    }

    private String maskCardNumber(String ccno) {
	    if (ccno == null || ccno.length() < 4) {
	        return ccno;
	    }
	    return ccno.substring(0, 6) + "******" + ccno.substring(ccno.length() - 4);
	}  

}
