package com.transactions.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
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
import reactor.core.publisher.Mono;

@Service
public class TransactionServiceImplWebFlux implements TransactionServiceWebFlux {

  
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepo;
    
    @Autowired
    private TransactionAdditionalRepository transactionAdditionalRepo;
    
    @Autowired
    private  ConnectorFeignClient connectorClient;
    
    @Autowired
    private  TerminalFeignClient terminalClient;

    @Override
    public Mono<ResponseEntity<Map<String, Object>>> updateTransStatusReactive(String transID, ServerWebExchange exchange, Boolean isWebhookBoolean,Boolean isRefundBoolean) {
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
                                                transactionRepo.save(transaction); 
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
                                String decryptedCCNO = additional.getCcnoDecrypted(); 
                                String statusDescription = getStatusDes(orderStatus); 
                                response.put("transID", transaction.getTransID().toString());
                                response.put("order_status", orderStatus);
                                response.put("status", statusDescription);
                                response.put("bill_amt", transaction.getBillAmount().toString());
                                String formattedDate = dateFormat.format(transaction.getTransactionDate());
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
                                                throw new IllegalArgumentException("Public key is missing for webhook notification");
                                            }
                                            String queryString = response.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
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
                                    transactionAdditionalRepo.save(additional); // Save the updated additional data
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
    public Map<String, Object> getTransactionDetails(String transID) {
		Map<String, Object> response = new HashMap<>();
		try {

			Long transIDLong = Long.parseLong(transID); // Convert String to Long
			Optional<Transaction> transactionOpt = Optional
					.ofNullable(transactionRepo.findByTransID(transIDLong));
			Optional<TransactionAdditional> additionalOpt = Optional
					.ofNullable(transactionAdditionalRepo.findByTransIDAd(transIDLong));

			if (transactionOpt.isPresent()) {
				Transaction transaction = transactionOpt.get();
				TransactionAdditional additional = additionalOpt.get();
				String decryptedCCNO = additional.getCcnoDecrypted(); 
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
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String formattedDate = dateFormat.format(transaction.getTransactionDate());
				response.put("tdate", formattedDate);
				response.put("bill_currency", transaction.getBillCurrency());
				response.put("response", additional.getTransactionResponse() == null ? "Payment is pending"
						: additional.getTransactionResponse());
				response.put("reference", transaction.getReference());
				response.put("mop", transaction.getMethodOfPayment());
				if (additionalOpt.isPresent()) {

					response.put("descriptor", additional.getDescriptor());
					response.put("upa", additional.getUpa());
					response.put("rrn", additional.getRrn());
					
					if(decryptedCCNO != null) {
						response.put("ccno", maskCardNumber(decryptedCCNO));
					}
					if(orderStatus.equals("0") || orderStatus.equals("27")) 
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
	        return "Invalid Card";
	    }
	    return ccno.substring(0, 6) + "XXXXXX" + ccno.substring(ccno.length() - 4);
	}
	
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
	        System.err.println("Error parsing JSON: " + e.getMessage());
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
	        System.err.println("Error parsing JSON: " + e.getMessage());
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
    public Optional<Transaction> getTransactionById(Long id) {
        log.info("Fetching transaction from database for id: {}", id);
        return transactionRepo.findById(id);
    }

}