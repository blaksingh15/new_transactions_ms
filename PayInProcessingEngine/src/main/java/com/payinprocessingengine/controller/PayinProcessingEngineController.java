package com.payinprocessingengine.controller;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payinprocessingengine.entity.BlackList;
import com.payinprocessingengine.entity.Connector;
import com.payinprocessingengine.entity.Orchestration;
import com.payinprocessingengine.entity.Terminal;
import com.payinprocessingengine.entity.Transaction;
import com.payinprocessingengine.entity.TransactionAdditional;
import com.payinprocessingengine.util.AES256Api;
import com.payinprocessingengine.util.AES256Util;
import com.payinprocessingengine.util.Base64Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api")
@CrossOrigin
public class PayinProcessingEngineController {
	
	private static final Logger log = LoggerFactory.getLogger(PayinProcessingEngineController.class);
	
//	@Autowired
//    private TransactionService transactionService;
	
//	@Autowired
//	private CurrencyExchangeApiService currencyExchangeApiService;
	
//	@Autowired
//    private BlackListService blackListService;
	
//	@Autowired
//	private OrchestrationValidationService orchestrationValidationService;
//	
//	@Autowired
//	private ConnectorDao connectorDao;

//	@Autowired
//	private TerminalDao terminalDao; 
	
//	@Autowired
//	private ApiCardInfoCashfreeUtility apiCardInfoCashfreeUtility;
	

	@Value("${production.domains}")
	private String productionDomains;
	@Value("${spring.web.otp-auth-url}")
	private String otpAuthUrl;
	private String getDynamicOtpAuthUrl(HttpServletRequest ser) {
		String origin = ser.getHeader("Origin");
		String referer = ser.getHeader("Referer");
		log.info("🔍 Determining OTP Auth URL - Origin: {}, Referer: {}", origin, referer);
		String requestSource = origin != null ? origin : referer;
		if (requestSource != null) {
			 if (requestSource.contains("54.74.33.191") || requestSource.contains("54.74.33.191:3002")) {
				log.info("✅ Using PGX OTP auth URL: http://54.74.33.191:3002");
				return "http://54.74.33.191:3002";
			}
			else if (requestSource.contains("localhost:3002") || requestSource.contains("localhost:3001") || requestSource.contains("localhost:3000")) {
				log.info("✅ Using localhost OTP auth URL: http://localhost:3002");
				return "http://localhost:3002";
			}
			else if (requestSource.contains("boxchrge.com")) {
				log.info("✅ Using BoxCharge OTP auth URL: https://checkout.boxchrge.com");
				return "https://checkout.boxchrge.com";
			}
			else if (requestSource.contains("i15.biz")) {
				log.info("✅ Using PGX OTP auth URL: https://checkout.i15.biz");
				return "https://checkout.i15.biz";
			}
			
		}
		
		String defaultUrl = otpAuthUrl.split(",")[0].trim();
		log.warn("⚠️ Using default OTP auth URL: {} (Origin: {}, Referer: {})", defaultUrl, origin, referer);
		return defaultUrl;
	}

	
	public static String connector_id = "";
	public static String auth_3ds = "";
	public static String return_status_url = "";
	public static String webhookhandler_url = "";
	public static String webhookhandler_transid_url = "";
	public static String webhookhandler_s2s_transid_url = "";
	@Value("${spring.public_key.length}")   
	private int publicKeyLength;
	@SuppressWarnings("unchecked")
	@CrossOrigin(origins = "*", allowCredentials = "false")
	@RequestMapping(
		value = {"/s2s","/s2s/encrypt"},
		method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS}
	)
	public ResponseEntity<Map<String, Object>> adds2s(HttpServletRequest ser) {
		Map<String, Object> request = new HashMap<>();
		
		if ("OPTIONS".equalsIgnoreCase(ser.getMethod())) {
			return ResponseEntity.ok().headers(createCorsHeaders()).body(Map.of("status", "CORS preflight OK"));
		}
		
		try {
			String contentType = ser.getContentType();
			if (contentType != null && contentType.contains("application/json")) {
				StringBuilder jsonBuilder = new StringBuilder();
				BufferedReader reader = ser.getReader();
				String line;
				while ((line = reader.readLine()) != null) {
					jsonBuilder.append(line);
				}
				String jsonString = jsonBuilder.toString();
				if (!jsonString.isEmpty()) {
					ObjectMapper objectMapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> jsonBody = objectMapper.readValue(jsonString, Map.class);
					request.putAll(jsonBody);
				}
			} else if (contentType != null && contentType.contains("multipart/form-data")) {
				Map<String, String[]> parameterMap = ser.getParameterMap();
				for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
					String key = entry.getKey();
					String[] values = entry.getValue();
					if (values != null && values.length > 0) {
						request.put(key, values[0].trim());
					}
				}
			} else {
				Map<String, String[]> parameterMap = ser.getParameterMap();
				for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
					String key = entry.getKey();
					String[] values = entry.getValue();
					if (values != null && values.length > 0) {
						request.put(key, values[0].trim());
					}
				}
			}

			if (ser.getMethod().equalsIgnoreCase("GET")) {
				String queryString = ser.getQueryString();
				if (queryString != null) {
					String[] pairs = queryString.split("&");
					for (String pair : pairs) {
						String[] keyValue = pair.split("=");
						if (keyValue.length == 2) {
							String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
							String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
							if ("public_key".equals(key)) {
								value = value.replace(' ', '+');
								if (pair.endsWith("=")) {
									value = value + "=";
								}
							}
							request.put(key, value.trim());
						}
					}
				}
			}
			Integer jqp = 0;
			if (request.containsKey("jqp") && request.get("jqp") != null) {
				jqp = Integer.parseInt(request.get("jqp").toString());
				if (jqp > 0) {
					System.out.println("JQP: " + jqp);
				}
			}
			
	        Transaction transaction = new Transaction();
			TransactionAdditional transactionAdditional = new TransactionAdditional();
			Map<String, Object> authDataMap = new HashMap<>();
			String connector_id = "";
			Long transID = null;	
			String feeId = "";
			String terNO = "";
			String connector_payin = "";
			String merID = "";
			String ccno = "";
			String privateKey = "";
			String encryptTransID = "";
			Map<String, Object> connectorkey = null;
			String redirect_auth = ""; 
			Map<String, Object> connectorData = new HashMap<>();
			Map<String, Object> credentials = new HashMap<>();
			Boolean isSaveAuthData = false;
			String payaddress = "";
			String response_action = "";
			String paytitle = "";
			String payamt = "";
			String paycurrency = "";
			Map<String, Object> authdata = new HashMap<>();
			if (request.containsKey("encrypted_data") && request.get("encrypted_data") != null && !request.get("encrypted_data").toString().isEmpty() && request.get("encrypted_data").toString().length() > publicKeyLength) {
				String encryptedPayload = request.get("encrypted_data").toString();
				request.put("public_key", encryptedPayload.substring(encryptedPayload.length() - publicKeyLength));
			}
			if (request.containsKey("public_key")) {
				try {
					String public_key = request.get("public_key").toString();
					Terminal terminal = terminalDao.findByPublicKey(public_key);
					if (terminal == null) {
						return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
								.body(Map.of("error", "Terminal not found"));
					}
					Map<String, Object> res_ter = new HashMap<>();
					if(request.containsKey("connector_id") && request.get("connector_id") != null && !request.get("connector_id").toString().isEmpty()) {
						connector_id = request.get("connector_id").toString();
					}else {
						if(terminal.getConnectorids() != null) {
							connector_id = terminal.getConnectorids();
						}
						
					}
					if(connector_id == null || connector_id.isEmpty()) {
						return ResponseEntity.badRequest().body(createErrorResponse("5015", "Error: connector_id is not map with terminal. contact support team."));
					}

					if(terminal.getTernoJsonValue() != null && connector_id != null && connector_id.length() > 0) {
						String ternoJsonValue = terminal.getTernoJsonValue();
						Map<String, Object> apc_json_ter_de = jsondecode(ternoJsonValue);
						@SuppressWarnings("unchecked")
						Map<String, Object> apc_json_ter = apc_json_ter_de.get(connector_id) != null 
							? (Map<String, Object>) apc_json_ter_de.get(connector_id) 
							: new HashMap<>();

						if (apc_json_ter.containsKey("feeId")) {
							feeId = apc_json_ter.get("feeId").toString();
							transaction.setFeeId(Integer.parseInt(feeId));
							res_ter.put("feeId", feeId);
						}

						if (terminal.getId() != null) {
							terNO = terminal.getId().toString();
							res_ter.put("terNO", terNO);
							request.put("terNO", terNO);
							
						}

						if (terminal.getMerid() != null) {
							merID = terminal.getMerid().toString();
							transaction.setMerchantID(Long.parseLong(merID));
							res_ter.put("merID", merID);
						}

						if (request.containsKey("unique_reference") && request.get("unique_reference") != null && !request.get("unique_reference").toString().isEmpty() && request.get("unique_reference").equals("Y") && merID != null) {
							String uniqueReference = request.get("reference").toString();
							Transaction existingTransaction = transactionService.findByReferenceAndMerchantID(uniqueReference, Long.parseLong(merID));
							if (existingTransaction != null) {
								return ResponseEntity.badRequest().body(createErrorResponse("5012", uniqueReference + " - Unique reference already exists"));
							}
						}
						if (apc_json_ter.containsKey("connectorkey")) {
							Object connectorkeyObj = apc_json_ter.get("connectorkey");
							if (connectorkeyObj instanceof Map) {
								connectorkey = (Map<String, Object>) connectorkeyObj;
							} else {
								connectorkey = new HashMap<>();
							}
							
							if (connectorkey != null && connectorkey.containsKey("redirect_auth")) {
								redirect_auth = connectorkey.get("redirect_auth").toString();
							}
							
							res_ter.put("connectorkey", connectorkey);
						}
					
					}

					if(terminal.getPrivateKey() != null) {
						privateKey = terminal.getPrivateKey();
						res_ter.put("private_key", privateKey);
					}
					res_ter.put("connectorids", connector_id);
					res_ter.put("merid", terminal.getMerid());
					res_ter.put("public_key", terminal.getPublicKey());
					res_ter.put("bussinessUrl", terminal.getBussinessUrl());
					res_ter.put("terName", terminal.getTerName());
					res_ter.put("dbaBrandName", terminal.getDbaBrandName());
					res_ter.put("ternoJsonValue", terminal.getTernoJsonValue());
					res_ter.put("selectMcc", terminal.getSelectMcc());
					res_ter.put("webhookUrl", terminal.getWebhookUrl());
					res_ter.put("returnUrl", terminal.getReturnUrl());
					res_ter.put("transID", transID);
					res_ter.put("status", "success");
					res_ter.put("message", "Terminal data fetched successfully");
					res_ter.put("response", "Terminal data fetched successfully");
					if (request.containsKey("jqp")) 
					{
						System.out.println("Fetch from terminal : " + res_ter);
					}

				} 
				catch (Exception e) {
					System.err.println("Error terminal : " + e.getMessage());
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(Map.of(
						"status", "error",
						"message", "Error Invalid public_key format",
						"error", e.getMessage()
					));
				}
			}
			if (request.containsKey("encrypted_data") && request.get("encrypted_data") != null && !request.get("encrypted_data").toString().isEmpty() && request.get("encrypted_data").toString().length() > publicKeyLength && privateKey != null && !privateKey.isEmpty()) {	
				try {
					String encryptedPayload = request.get("encrypted_data").toString();
					String encryptedPayloadEnd = encryptedPayload.substring(0, encryptedPayload.length() - publicKeyLength);
					String last44Chars_publicKey = "";

					if(request.containsKey("public_key") && request.get("public_key") != null && !request.get("public_key").toString().isEmpty()) {
						last44Chars_publicKey = request.get("public_key").toString();
					}else {
						last44Chars_publicKey = encryptedPayload.substring(encryptedPayload.length() - publicKeyLength);
					}
					String decryptedPayload = AES256Api.decrypt(encryptedPayloadEnd, privateKey, last44Chars_publicKey);
					if (decryptedPayload != null && !decryptedPayload.isEmpty()) {
						String[] pairs = decryptedPayload.split("&");
						for (String pair : pairs) {
							int idx = pair.indexOf("=");
							if (idx > 0 && idx < pair.length() - 1) {
								String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
								String value = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
								request.put(key, value);
							}
						}
					}

					if(terNO != null && !terNO.isEmpty()) {
						request.put("terNO", terNO);
					}
					

				} catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.badRequest().body(createErrorResponse("5001", "Error decrypting payload: " + e.getMessage()));
				}
			}

			LocalDateTime now = LocalDateTime.now();
			Timestamp timestamp = Timestamp.from(now.atZone(ZoneId.systemDefault()).toInstant());
			timestamp.setNanos(now.getNano()); 
			transaction.setTransactionDate(now); 
	        transaction.setTransactionStatus((short) 0);
			transaction.setTransactionType(11);
			String currentDateTime = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
			String supportNote = currentDateTime + " | " + "Pending transaction created by " + ser.getServerName() + " (From IP Address: " + ser.getRemoteAddr()+")";
			String systemNote = currentDateTime + " | " + " Transaction created by " + ser.getServerName() + " (From IP Address: " + ser.getRemoteAddr()+")" + " - " + request.get("integration-type");
			if (request.containsKey("integration_type")) {
				systemNote += currentDateTime + " - "+ request.get("integration_type");
			}
			else if (request.containsKey("integration-type")) {
				systemNote += currentDateTime + " - "+ request.get("integration-type");
			}
	        
	        String bill_currency = "";
	        String orderCurrency = "";
	        String bill_amt_str = "";
	        String total_payment_str = "";
	        Double total_payment = null;
			
	        // Check if bill_amt is provided
	        if (!request.containsKey("bill_amt")) {
	            return ResponseEntity.badRequest().body(createErrorResponse("1111", "Error: bill_amt is required."));
	        }
			total_payment_str = request.get("bill_amt").toString();
			bill_amt_str = total_payment_str;
			total_payment = Double.parseDouble(request.get("bill_amt").toString());
			if (total_payment <= 0) {
				return ResponseEntity.badRequest().body(createErrorResponse("1112", "Error: bill_amt not less than 0."));
			}
	        transaction.setBillAmount(total_payment);
	        if (request.containsKey("bill_currency") && request.get("bill_currency") != null && !request.get("bill_currency").toString().isEmpty()) {
				bill_currency = request.get("bill_currency").toString();
	            transaction.setBillCurrency(bill_currency);
				orderCurrency = bill_currency;
	        }
	        if (request.containsKey("reference")) {
	            transaction.setReference(request.get("reference").toString());
	        }
	        if (request.containsKey("bill_email")) {
	            transaction.setBillEmail(request.get("bill_email").toString());
	        }
	        
	        if (request.containsKey("bill_ip")) {
	            transaction.setBillIP(request.get("bill_ip").toString());
	        }

	        if (request.containsKey("fullname")) {
	            transaction.setFullName(request.get("fullname").toString());
	        }

	        if (request.containsKey("mop")) {
	            transaction.setMethodOfPayment(request.get("mop").toString());
	        }

	        if (request.containsKey("terNO")) {
	            transaction.setTerminalNumber(Long.parseLong(request.get("terNO").toString()));
	        }
	        
	        if (request.containsKey("integration_type")) {
	            transaction.setIntegrationType(request.get("integration_type").toString());
	        }
	        
	        else if (request.containsKey("integration-type")) {
				transaction.setIntegrationType(request.get("integration-type").toString());
			}
			else {
				transaction.setIntegrationType("s2s");
			}

	        if (request.containsKey("connector_id") || connector_id != null) {
	            try 
				{

	                String getConnectorId = "";
					if(connector_id != null) {
						getConnectorId = connector_id;
					}else {
						getConnectorId = request.get("connector_id").toString();
					}
					String connectorId = getConnectorId;
					if (request.containsKey("connector_id")) {
						connectorId=request.get("connector_id").toString();
						
					}
					
					transaction.setConnector(Long.parseLong(connectorId.toString()));
					 transID = generateUniqueTransID(connectorId, (long) 0);
					 transaction.setTransID(transID);

					Connector connector = connectorDao.findByConnectorNumber(getConnectorId);

	                if (connector != null) {
						connectorData.put("defaultConnector", connector.getDefaultConnector());
						connectorData.put("connectorProcessingCredsJson", connector.getConnectorProcessingCredsJson());
						connectorData.put("connectorNumber", connector.getConnectorNumber());
						connectorData.put("connectorName", connector.getConnectorName());
						connectorData.put("channelType", connector.getChannelType());
						connectorData.put("connectorStatus", connector.getConnectorStatus());
						connectorData.put("connectorBaseUrl", connector.getConnectorBaseUrl());
						connectorData.put("connectorLoginCreds", connector.getConnectorLoginCreds());
						connectorData.put("connectorProcessingCurrency", connector.getConnectorProcessingCurrency());
						connectorData.put("connectionMethod", connector.getConnectionMethod());
						connectorData.put("connectorProdUrl", connector.getConnectorProdUrl());
						connectorData.put("connectorUatUrl", connector.getConnectorUatUrl());
						connectorData.put("connectorStatusUrl", connector.getConnectorStatusUrl());
						connectorData.put("connectorDevApiUrl", connector.getConnectorDevApiUrl());
						connectorData.put("connectorWlDomain", connector.getConnectorWlDomain());
						connectorData.put("processingCurrencyMarkup", connector.getProcessingCurrencyMarkup());
						connectorData.put("techCommentsText", connector.getTechCommentsText());
						connectorData.put("connectorRefundPolicy", connector.getConnectorRefundPolicy());
						connectorData.put("connectorRefundUrl", connector.getConnectorRefundUrl());
						connectorData.put("mccCode", connector.getMccCode());
						connectorData.put("connectorProdMode", connector.getConnectorProdMode());
						connectorData.put("connectorDescriptor", connector.getConnectorDescriptor());
						connectorData.put("transAutoExpired", connector.getTransAutoExpired());
						connectorData.put("transAutoRefund", connector.getTransAutoRefund());
						connectorData.put("connectorWlIp", connector.getConnectorWlIp());
						connectorData.put("mopWeb", connector.getMopWeb());
						connectorData.put("mopMobile", connector.getMopMobile());
						connectorData.put("hardCodePaymentUrl", connector.getHardCodePaymentUrl());
						connectorData.put("hardCodeStatusUrl", connector.getHardCodeStatusUrl());
						connectorData.put("hardCodeRefundUrl", connector.getHardCodeRefundUrl());
						connectorData.put("skipCheckoutValidation", connector.getSkipCheckoutValidation());
						connectorData.put("redirectPopupMsgWeb", connector.getRedirectPopupMsgWeb());
						connectorData.put("redirectPopupMsgMobile", connector.getRedirectPopupMsgMobile());
						connectorData.put("checkoutLabelNameWeb", connector.getCheckoutLabelNameWeb());
						connectorData.put("checkoutLabelNameMobile", connector.getCheckoutLabelNameMobile());
						connectorData.put("checkoutSubLabelNameWeb", connector.getCheckoutSubLabelNameWeb());
						connectorData.put("checkoutSubLabelNameMobile", connector.getCheckoutSubLabelNameMobile());
						connectorData.put("ecommerceCruisesJson", connector.getEcommerceCruisesJson());
						connectorData.put("merSettingJson", connector.getMerSettingJson());
						connectorData.put("connectorLabelJson", connector.getConnectorLabelJson());
						connectorData.put("processingCountriesJson", connector.getProcessingCountriesJson());
						connectorData.put("blockCountriesJson", connector.getBlockCountriesJson());
						connectorData.put("notificationEmail", connector.getNotificationEmail());
						connectorData.put("notificationCount", connector.getNotificationCount());
						connectorData.put("autoStatusFetch", connector.getAutoStatusFetch());
						connectorData.put("autoStatusStartTime", connector.getAutoStatusStartTime());
						connectorData.put("autoStatusIntervalTime", connector.getAutoStatusIntervalTime());
						connectorData.put("cronBankStatusResponse", connector.getCronBankStatusResponse());
						Boolean currConverter = false;
						String connectorProcessingCurrency = connector.getConnectorProcessingCurrency();
						if (connectorProcessingCurrency != null) {
							transaction.setBankProcessingCurrency(connectorProcessingCurrency);
							currConverter = true;
						}
						if (connectorProcessingCurrency != null && currConverter && bill_currency != null && !bill_currency.isEmpty() && !bill_currency.equals(connectorProcessingCurrency) && total_payment != null && total_payment > 0) 
						{
					       request.put("start_currency", bill_currency);
						    request.put("start_on_total_payment", total_payment);
						    request.put("bank_processing_currency", connectorProcessingCurrency);
							orderCurrency = connectorProcessingCurrency;
						    try {
						        Map<String, Object> conversionResult = CurrencyExchangeApiController.commonDbCurrencyConverter(
						            currencyExchangeApiService, bill_currency, connectorProcessingCurrency, total_payment_str, "tr", "false");
						        if (conversionResult.get("error") != null) {
						            return ResponseEntity.badRequest().body(createErrorResponse("5003", "Error in currency converter: " + conversionResult.get("error")));
						        }

						        String convertedAmount = conversionResult.get("convertedAmount").toString();
						        String conversionRates = conversionResult.get("conversion_rates").toString();
						        transaction.setBankProcessingAmount(Double.parseDouble(convertedAmount));
						        request.put("bank_processing_amount", convertedAmount);
						        request.put("conversion_rates", conversionRates);
						        total_payment = Double.parseDouble(convertedAmount);
						        total_payment_str = convertedAmount;
								request.put("bill_amt", convertedAmount);

						    } catch (Exception e) {
						        e.printStackTrace();
						        return ResponseEntity.badRequest().body(Map.of("error", "Error in conversion result: " + e.getMessage()));
						    }
						}
						String apc_get = connector.getConnectorProcessingCredsJson();
						Map<String, Object> apc_json = jsondecode(apc_get);
						String connectorProdMode = connector.getConnectorProdMode();
						credentials = jsoncredentials(apc_json, connectorProdMode,connectorkey);
						String defaultConnector = connector.getDefaultConnector();
						if (defaultConnector != null) {
							connector_payin = defaultConnector;
							connectorData.put("defaultConnector", defaultConnector);
						}	
	                
	                } else {
	                    return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(Map.of("error", "Connector not found"));
	                }
	            } catch (Exception e) {
	                return ResponseEntity.badRequest().body(Map.of("error", "Invalid connector_id format"));
	            }
	        }



			if (request.containsKey("ccno")) 
			{
				ccno = request.get("ccno").toString();
				ccno = ccno.replaceAll("[\\s-]", "");

				if(ccno != null && ccno.length() > 0) 
				{
					transactionAdditional.setCcno(ccno);
					try {
						
						JSONObject cardInfoJson = apiCardInfoCashfreeUtility.cardBinF(ccno);

						if (cardInfoJson != null && !cardInfoJson.isEmpty()) {
							Map<String, Object> cardInfo = cardInfoJson.toMap();							
							if(cardInfo.get("card_type") != null && !cardInfo.get("card_type").toString().isEmpty()) {
								transactionAdditional.setCardType(cardInfo.get("card_type").toString());
							}

							if(cardInfo.get("card_brand") != null && !cardInfo.get("card_brand").toString().isEmpty()) {
								transactionAdditional.setCardBrand(cardInfo.get("card_brand").toString());
							}

							if(cardInfo.get("issuing_bank_name") != null && !cardInfo.get("issuing_bank_name").toString().isEmpty()) {
								transactionAdditional.setIssuingBank(cardInfo.get("issuing_bank_name").toString());
							}

							if(cardInfo.get("issuing_country") != null && !cardInfo.get("issuing_country").toString().isEmpty()) {
								transactionAdditional.setIssuingCountry(cardInfo.get("issuing_country").toString());
							}
							
							
						}


					} catch (Exception e) {
						System.err.println("Error fetching card info: " + e.getMessage());
						e.printStackTrace();
						return ResponseEntity.badRequest().body(createErrorResponse("5002", "Error fetching card info: " + e.getMessage()));

					}
				} 
			}

			Boolean isTestCardCheck = true;
			String isTestEnrollmentType = "";
			Boolean isBlackListCheck = true;
			String blacklistMessageType = "Transaction is Blocked";
			String blacklistPrintMessage = "";
			String blacklistTerm = "";
			String blacklistTermType = "";
			if(connector_id != null && !connector_id.isEmpty() && merID != null && !merID.isEmpty()) {
					
				try {

					String clientIdsAndConnectorids = "-10,-" + connector_id + "," + merID; 
					ResponseEntity<List<BlackList>> blackListResult = BlackListController.getBlacklistsInClientId(blackListService,clientIdsAndConnectorids,true);
					List<BlackList> blackList = blackListResult.getBody();
					if (blackList != null && !blackList.isEmpty()) {
						StringBuilder message = new StringBuilder();
						boolean allowed = BlacklistValidationService.isBlacklisted(blackList, request, message);
						if (!allowed) 
						{
							isBlackListCheck = false;
							blacklistPrintMessage = message.toString();
						}
					}
					
				} catch (Exception e) {
					isBlackListCheck = true;
					e.printStackTrace();
					return ResponseEntity.badRequest().body(Map.of("error", "Error parsing blacklist: " + e.getMessage()));
					
				}
				
				if(!isBlackListCheck && blacklistPrintMessage != null && !blacklistPrintMessage.isEmpty()) {
					request.put(blacklistTermType, blacklistTerm);
					request.put("blacklist_message", blacklistPrintMessage);
					transaction.setTransactionStatus((short) 10);
					transactionAdditional.setTransactionResponse(blacklistPrintMessage.toString());
					supportNote = currentDateTime + " | " + "Transaction " + blacklistPrintMessage;
				} 
			}

			Boolean isOrchestrationRules = false;
			StringBuilder messageOrchestration = new StringBuilder();
			Orchestration rule = null;
			try {
				rule = orchestrationValidationService.findMatchingRule(merID, feeId, request, messageOrchestration);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.badRequest().body(createErrorResponse("5007", "Error in orchestration validation: " + e.getMessage()));
			}


			if (rule != null && rule.getRuleId() != null && !rule.getRuleId().isEmpty()) {	
			
				String action = rule.getActions();
				if (action != null && !action.trim().isEmpty()) {
					String[] actionParts = action.split(";");
					for (String act : actionParts) {
						act = act.trim();
						if (act.startsWith("gateway_to:")) {
							String targetConnector = act.substring("gateway_to:".length()).trim();
							if (feeId != null && String.valueOf(feeId).equals(targetConnector)) {
								blacklistPrintMessage = messageOrchestration.toString();
								isBlackListCheck = false;
								isOrchestrationRules = true;
							} else {
    							return ResponseEntity.badRequest().body(createErrorResponse("5003", "Fee gateway conflict: FeeId does not match orchestration target."));
								isOrchestrationRules = true;
							}
						}
						else if (act.startsWith("block:")) {
							String reason = act.substring("block:".length()).trim();
							blacklistPrintMessage = messageOrchestration.toString();
							isBlackListCheck = false;
							isOrchestrationRules = true;
						}

						else if (act.startsWith("apply_fee:")) {
							String feeAmountStr = act.substring("apply_fee:".length()).trim();
							Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(feeAmountStr);
							double feeAmount = 0.0;

							try {
								if (matcher.find()) {
									feeAmount = Double.parseDouble(matcher.group(1));
								} else {
									throw new IllegalArgumentException("No valid fee amount found in: " + feeAmountStr);
								}
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}

						else if (act.startsWith("require_3ds:")) {
							String msg = act.substring("require_3ds:".length()).trim();
							request.put("enrollmentType", "3DS");
						}

						else if (act.startsWith("change_currency:")) {
							String newCurrency = act.substring("change_currency:".length()).trim();							
							if (newCurrency != null && !newCurrency.isEmpty() && orderCurrency != null && !orderCurrency.equals(newCurrency) && total_payment != null && total_payment > 0) 
							{
					            request.put("start_currency", orderCurrency);
							    request.put("start_on_total_payment", total_payment);
							    request.put("bank_processing_currency", newCurrency);							   
							    try {
							        Map<String, Object> conversionResult = CurrencyExchangeApiController.commonDbCurrencyConverter(
							            currencyExchangeApiService, orderCurrency, newCurrency, total_payment_str, "tr", "false");

							        if (conversionResult.get("error") != null) {
							            return ResponseEntity.badRequest().body(createErrorResponse("5005", "Error in currency converter: " + conversionResult.get("error")));
							        }

							        String convertedAmount = conversionResult.get("convertedAmount").toString();
							        String conversionRates = conversionResult.get("conversion_rates").toString();
							        transaction.setBankProcessingAmount(Double.parseDouble(convertedAmount));
							        request.put("bank_processing_amount", convertedAmount);
							        request.put("conversion_rates", conversionRates);
							        total_payment = Double.parseDouble(convertedAmount);
							        total_payment_str = convertedAmount;
									request.put("bill_amt", convertedAmount);
									blacklistPrintMessage = messageOrchestration.toString();

							    } catch (Exception e) {
							        e.printStackTrace();
							        return ResponseEntity.badRequest().body(Map.of("error", "Error in conversion result: " + e.getMessage()));
								}
							} 
																			        
						}

					}
				}


				if(isOrchestrationRules && !isBlackListCheck && blacklistPrintMessage != null && !blacklistPrintMessage.isEmpty()) {
					request.put("OrchestrationRulesId", rule.getRuleId());
					blacklistMessageType = "Transaction is Blocked - Orchestration Rule Violation";
					request.put("blacklist_message", blacklistPrintMessage);
					transaction.setTransactionStatus((short) 10);
					transactionAdditional.setTransactionResponse(blacklistPrintMessage.toString());
					supportNote = currentDateTime + " | " + "Transaction " + blacklistPrintMessage;
				}
			}

			Integer vqp = 0;
			if (request.containsKey("vqp") && request.get("vqp") != null) {
				vqp = Integer.parseInt(request.get("vqp").toString());
				
			}

			if (isBlackListCheck || !isOrchestrationRules) 
			{
				List<Map<String, Object>> regulatoryRules = null;
				try {
					regulatoryRules = orchestrationValidationService.getVolumeRegulatorySettingsCheckAll(merID, feeId);

				} catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.badRequest().body(createErrorResponse("5008", "Error in volume regulatory validation: " + e.getMessage()));
				}

				if (regulatoryRules != null && !regulatoryRules.isEmpty()) 
				{
					
					int regulatoryRulesSize = regulatoryRules.size();
					int countReg = 0;
					for (Map<String, Object> regulatorySettings : regulatoryRules) {
						countReg++;
						String ruleId = (String) regulatorySettings.getOrDefault("ruleId", null);
						String ruleName = (String) regulatorySettings.getOrDefault("ruleName", null);
						String ruleDescription = (String) regulatorySettings.getOrDefault("description", null);
						String regulatoryRequestField = (String) regulatorySettings.getOrDefault("regulatoryRequestField", null);
						String period = (String) regulatorySettings.getOrDefault("period", null);
						String maxVolumeStr = (String) regulatorySettings.getOrDefault("maxVolume", null);
						String successCountStr = (String) regulatorySettings.getOrDefault("successCount", null);
						String failCountStr = (String) regulatorySettings.getOrDefault("failCount", null);
						int regulatoryDays = 0;
						if (period != null) {
							Matcher m = Pattern.compile("(\\d+)\\s*Day[s]?").matcher(period);
							if (m.find()) {
								regulatoryDays = Integer.parseInt(m.group(1));
							} else if (period.matches("\\d+")) {
								regulatoryDays = Integer.parseInt(period);
							}
						}


						double volumeLimit = maxVolumeStr != null ? Double.parseDouble(maxVolumeStr) : 0.0;
						int successLimit = successCountStr != null ? Integer.parseInt(successCountStr) : 0;
						int failLimit = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
					
						Object fieldValue = request.getOrDefault(regulatoryRequestField, "");
						String fieldValueStr = fieldValue != null ? fieldValue.toString() : "";
						double billAmount = request.get("bill_amt") != null ? Double.parseDouble(request.get("bill_amt").toString()) : 0.0;
						if ((successLimit > 0 || failLimit > 0 || volumeLimit > 0) && regulatoryDays > 0 && regulatoryRequestField != null && fieldValueStr != null) {
							Map<String, Object> resVolume = transactionService.checkVolumeRegulatoryDynamic(
								Long.parseLong(merID), regulatoryRequestField, fieldValueStr, String.valueOf(regulatoryDays),
								volumeLimit, successLimit, failLimit, billAmount);
							boolean isBlocked = resVolume.get("isBlocked") instanceof Boolean && (Boolean) resVolume.get("isBlocked");
					
							if (isBlocked) {
								String msg_regulatory = ruleName + " - (Rule ID: " + ruleId + ") " + resVolume.get("message").toString() + " - " + ruleDescription;					
								blacklistMessageType = "Transaction is Blocked - Regulatory Violation";
								blacklistPrintMessage = msg_regulatory;
								isBlackListCheck = false;
								isOrchestrationRules = true;
								transaction.setTransactionStatus((short) 28);
								transactionAdditional.setTransactionResponse(blacklistPrintMessage);
								supportNote = currentDateTime + " | Transaction " + blacklistPrintMessage;
								request.put("OrchestrationRulesId", ruleId);
								request.put("blacklist_message", blacklistPrintMessage);
								break;
							}
						}
					}
					
				}
			}

			request.put("bill_amt", bill_amt_str);	
			if (request.containsKey("ccno") && request.get("ccno") != null && !request.get("ccno").toString().isEmpty()){
				ccno = request.get("ccno").toString();
				ccno = ccno.replaceAll("[\\s-]", "");
	        	transactionAdditional.setCcno(ccno);
				Integer binNumber = Integer.parseInt(ccno.substring(0, 6));
				transactionAdditional.setBinNumber(binNumber);

				if(ccno != null && ccno.length() > 0) {

					isSaveAuthData = true;
					
					Map<String, Object> cardValidate = CardValidatorUtils.validateAllCard(ccno);

					if(cardValidate != null) {
						String cardType = (String) cardValidate.get("cardType");
						if(cardType != null) {
							transaction.setMopName(cardType);
						}

						String cardNumber = (String) cardValidate.get("cardNumber");
						String enrollmentType = (String) cardValidate.get("enrollmentType");
						String cardMessage = (String) cardValidate.get("message");
						Boolean validLuhn = (Boolean) cardValidate.get("validLuhn");
						Boolean valid = (Boolean) cardValidate.get("valid");

						if(valid != null && valid && enrollmentType != null &&  (enrollmentType.equals("3DS") || enrollmentType.equals("25") || enrollmentType.equals("26") || enrollmentType.equals("9") )) {
							isTestCardCheck = false;
							
							if(enrollmentType.equals("3DS")){
								transaction.setTransactionStatus((short) 27);
								isTestEnrollmentType = "3DS";
							}else{
								transaction.setTransactionStatus(Short.parseShort(enrollmentType));
							}

							String testCardMessage = "Test Transaction "+blacklistPrintMessage+", we do not charge any fees for testing transaction";
							supportNote = currentDateTime + " | " + testCardMessage;
							transactionAdditional.setTransactionResponse(testCardMessage);

						}
						else if(!validLuhn && !valid && enrollmentType != null &&  (enrollmentType.equals("LIVE"))) {
							return ResponseEntity.badRequest().body(createErrorResponse("1113", "We are unable to validate the accuracy of your card. Would you like to try with another card or check the current card number please?"));
						}
						
						if(jqp == 6) {
							return ResponseEntity.ok(Map.of(
								"status", "success",
								"message", cardValidate
							));
						}
					}
					
				}

	        }

	        Transaction savedTransaction = transactionService.saves2sTrans(transaction);
	        transID = generateUniqueTransID(connector_id, savedTransaction.getId());
			savedTransaction.setTransID(transID);
			transactionService.updateTransactionID(savedTransaction.getId(), transID);
			authDataMap.put("transID", transID != null ? transID.toString() : null);
	        transID = savedTransaction.getTransID();
	        transactionAdditional.setTransIDAd(transID);
	        transactionAdditional.setId(savedTransaction.getId());
			if (request.containsKey("month")) {
	        	transactionAdditional.setExMonth(request.get("month").toString());
			}
			if (request.containsKey("year")) {
	        	transactionAdditional.setExYear(request.get("year").toString());
			}

	        if (credentials != null) {
	        	transactionAdditional.setConnectorCredsProcessingFinal(jsonen(credentials));
	        }
			String ccnoForConnector = request.containsKey("ccno") ? request.get("ccno").toString() : null;
			String ccvvForConnector = request.containsKey("ccvv") ? request.get("ccvv").toString() : null;
			String monthForConnector = request.containsKey("month") ? request.get("month").toString() : null;
			String yearForConnector = request.containsKey("year") ? request.get("year").toString() : null;
			if (request != null) {
	        	Map<String, Object> requestSet = (Map<String, Object>) request;
				if (request.containsKey("ccno")) requestSet.remove("ccno");
				if (request.containsKey("ccvv")) requestSet.remove("ccvv");
				if (request.containsKey("month")) requestSet.remove("month");
				if (request.containsKey("year")) requestSet.remove("year");
	        	transactionAdditional.setPayloadStage1(jsonen(requestSet));
	        }


	        if (request.containsKey("bill_address")) {
	        	transactionAdditional.setBillingAddress(request.get("bill_address").toString());
	        }

	        if (request.containsKey("bill_city")) {
	        	transactionAdditional.setBillingCity(request.get("bill_city").toString());
	        }

	        if (request.containsKey("bill_state")) {
	        	transactionAdditional.setBillingState(request.get("bill_state").toString());
	        }

	        if (request.containsKey("bill_zip")) {
	        	transactionAdditional.setBillingZip(request.get("bill_zip").toString());
	        }

	        if (request.containsKey("bill_country")) {
	        	transactionAdditional.setBillingCountry(request.get("bill_country").toString());
	        }

	        if (request.containsKey("bill_phone")) {
	        	transactionAdditional.setBillingPhone(request.get("bill_phone").toString());
	        }

	        if (request.containsKey("product_name")) {
	        	transactionAdditional.setProductName(request.get("product_name").toString());
	        }

	        if (request.containsKey("source_url")) {
	        	transactionAdditional.setSourceUrl(request.get("source_url").toString());
	        }

	        if (request.containsKey("return_url")) {
	        	transactionAdditional.setReturnUrl(request.get("return_url").toString());
	        }

	        if (request.containsKey("webhook_url")) {
	        	transactionAdditional.setWebhookUrl(request.get("webhook_url").toString());
	        }

	        String scheme = determineScheme(ser);
	        String baseUrl = scheme + "://" + ser.getServerName();
	        if (ser.getServerPort() != 80 && ser.getServerPort() != 443) {
	            baseUrl += ":" + ser.getServerPort();
	        }
	        baseUrl += "/api/";

	        String authurl = baseUrl + "authurl/" + savedTransaction.getTransID();
		   String dynamicOtpAuthUrl = getDynamicOtpAuthUrl(ser);
		   String test3dsecureauthentication = dynamicOtpAuthUrl + "/otp-auth?transID=" + savedTransaction.getTransID();
		   if(connector_payin != null && !connector_payin.isEmpty()) {
				webhookhandler_url = baseUrl + "status/webhook/" + connector_payin;
		   }
		   if(savedTransaction.getTransID() != null && !savedTransaction.getTransID().equals("") && connector_payin != null && !connector_payin.isEmpty()) {
				webhookhandler_transid_url = baseUrl + "status/webhook/" + connector_payin + "/transid/" + savedTransaction.getTransID();
		   }

		   
		   if(savedTransaction.getTransID() != null && !savedTransaction.getTransID().equals("") && connector_payin != null && !connector_payin.isEmpty()) {
			
				webhookhandler_s2s_transid_url = baseUrl + "status/webhook/s2s/transid/" + savedTransaction.getTransID();
		   }
		   
			if (request.containsKey("integration_type") && request.get("integration_type").toString().equals("checkout-s2s") && !isTestCardCheck && isTestEnrollmentType.equals("3DS")) {
				baseUrl = dynamicOtpAuthUrl + "/api/";
			}
		   

			if (request.containsKey("encryption_method") && request.get("encryption_method") != null && !request.get("encryption_method").toString().isEmpty() && request.get("encryption_method").equals("aes256")) {
				encryptTransID = AES256Util.encrypt(savedTransaction.getTransID().toString());
				authurl = baseUrl + "authurl/" + encryptTransID;
				test3dsecureauthentication = dynamicOtpAuthUrl + "/otp-auth?transID=" + encryptTransID;

				transaction.setIntegrationType(transaction.getIntegrationType().toString()+"_"+request.get("encryption_method").toString());
			}

	        transactionAdditional.setAuthUrl(authurl);
			transactionAdditional.setSystemNote(systemNote);
			transactionAdditional.setSupportNote(supportNote);
		    TransactionAdditional savedTransAdditional = transactionService.saves2sTransAdditional(transactionAdditional);
	   
			if ("s2s".equals(redirect_auth)) {
				if(encryptTransID != null && !encryptTransID.isEmpty()) {
					auth_3ds = baseUrl + "authurl/auth_3ds/" + encryptTransID;
					return_status_url = baseUrl + "status/transid/" + encryptTransID;
				} else {
					auth_3ds = baseUrl + "authurl/auth_3ds/" + savedTransaction.getTransID();
					return_status_url = baseUrl + "status/transid/" + savedTransaction.getTransID();
				}
			} else {
				if(encryptTransID != null && !encryptTransID.isEmpty()) {
					auth_3ds = dynamicOtpAuthUrl + "/apm-payment-cruise?transID=" + encryptTransID;
					return_status_url = baseUrl + "status/transid/" + encryptTransID;
				} else {
					auth_3ds = dynamicOtpAuthUrl + "/apm-payment-cruise?transID=" + savedTransaction.getTransID();
					return_status_url = baseUrl + "status/transid/" + savedTransaction.getTransID();
				}
			}

			
			if(!isTestCardCheck && isTestEnrollmentType.equals("3DS"))
			{
				payaddress = test3dsecureauthentication;
				authurl = auth_3ds = test3dsecureauthentication;
			}
			 request.put("total_payment", total_payment);
			 request.put("total_payment_str", total_payment_str);
			 request.put("orderCurrency", orderCurrency);
			 connectorData.put("total_payment", total_payment);
			 connectorData.put("total_payment_str", total_payment_str);
			 connectorData.put("orderCurrency", orderCurrency);
			 connectorData.put("return_status_url", return_status_url);
			 connectorData.put("webhookhandler_url", webhookhandler_url);
			 connectorData.put("webhookhandler_transid_url", webhookhandler_transid_url);
			 connectorData.put("webhookhandler_s2s_transid_url", webhookhandler_s2s_transid_url);
			if(!isBlackListCheck && blacklistPrintMessage != null && !blacklistPrintMessage.isEmpty()) {
				return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).body(Map.of(
					"status", "error",
					"transID", transID,
					"message", blacklistMessageType,
					"blacklist_message", blacklistPrintMessage
				));
			} 

			if(connector_payin != null && connectorData !=null && credentials !=null && isBlackListCheck && isTestCardCheck ) {
							
				try {
					String connectorClassName = "com.webapp.controller.payin.pay_" + connector_payin + ".Connector_" + connector_payin;
					Class<?> connectorClass = Class.forName(connectorClassName);
					Object connectorInstance = connectorClass.getDeclaredConstructor().newInstance();
					Method mapPayloadMethod = connectorClass.getMethod("mapPayload", Object.class);
					Map<String, Object> payload = new HashMap<>();
					payload.putAll(connectorData);
					payload.put("transID", transID);
					
					if (ccnoForConnector != null) {
						request.put("ccno", ccnoForConnector);
					}
					if (ccvvForConnector != null) {
						request.put("ccvv", ccvvForConnector);
					}
					if (monthForConnector != null) {
						request.put("month", monthForConnector);
					}
					if (yearForConnector != null) {
						request.put("year", yearForConnector);
					}
					
					payload.put("request", request);  // Add the request parameter to the payload
					payload.put("apc_get", credentials);  // Parsed JSON object as live or test from connectorProcessingCredsJson
					
					Object mappedPayload = mapPayloadMethod.invoke(connectorInstance, payload);		
					ser.setAttribute("mappedPayload", mappedPayload);  // Using request instead of ser

					Map<String, Object> authMap = new HashMap<>();
					@SuppressWarnings("unchecked")
					Map<String, Object> mappedResponseMap = (Map<String, Object>) mappedPayload;

					authMap.put("connector_authurl", auth_3ds); // set auth_3ds URL
					
					if (mappedResponseMap.containsKey("connector_ref")) {
						String connector_ref = mappedResponseMap.get("connector_ref").toString();
						transactionAdditional.setConnectorRef(connector_ref);
						authMap.put("connector_ref", connector_ref); 
					}

					if (mappedResponseMap.containsKey("connector_status_code")) {
						Object statusCodeObj = mappedResponseMap.get("connector_status_code");
						if (statusCodeObj != null && !statusCodeObj.toString().isEmpty()) {
							try {
								String connectorStatusCode = statusCodeObj.toString();
								short transactionStatus = (short) Integer.parseInt(connectorStatusCode);
								transaction.setTransactionStatus(transactionStatus);
								transactionService.saves2sTrans(transaction);
								if (mappedResponseMap.containsKey("connector_response_msg")) {
									Object responseMsgObj = mappedResponseMap.get("connector_response_msg");
									if (responseMsgObj != null && !responseMsgObj.toString().isEmpty()) {
										transactionAdditional.setTransactionResponse(responseMsgObj.toString());
									}
								}
								
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					}

					if (mappedResponseMap.containsKey("gateway_response")) {
						@SuppressWarnings("unchecked")
						String connector_response = Base64Util.encodeBase64(jsonen((Map<String, Object>) mappedResponseMap.get("gateway_response")));
						transactionAdditional.setConnectorResponse(connector_response);
						authMap.put("gateway_response", mappedResponseMap.get("gateway_response")); 
					}
					
					isSaveAuthData = true;
					if (mappedResponseMap.containsKey("connector_payaddress")) {
						payaddress = mappedResponseMap.get("connector_payaddress").toString(); //Extracting connector_payaddress
					}
					if (mappedResponseMap.containsKey("connector_authdata")) {
						authdata = (Map<String, Object>) mappedResponseMap.get("connector_authdata"); //Extracting connector_authdata
					}

					if (mappedResponseMap.containsKey("connector_coinName")) {
						paycurrency = mappedResponseMap.get("connector_coinName").toString(); //Extracting connector_coinName
					}

					if (mappedResponseMap.containsKey("connector_payamt")) {
						payamt = mappedResponseMap.get("connector_payamt").toString(); //Extracting connector_payamt
					}
					if (mappedResponseMap.containsKey("connector_paytitle")) {
						paytitle = mappedResponseMap.get("connector_paytitle").toString(); //Extracting connector_paytitle
					}
					if (mappedResponseMap.containsKey("connector_response_action")) {
						response_action = mappedResponseMap.get("connector_response_action").toString(); //Extracting connector_response_action
					}
					
					if (jqp == 1) {
						return ResponseEntity.ok().body(Map.of(
							"status", "success",
							"message", "Connector processed successfully",
							"transID", transID,
							"payload_res", mappedPayload
						));
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(Map.of(
						"status", "error",
						"message", "Connector not found: " + connector_payin,
						"error", e.getMessage()
					));
					
				} catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(Map.of(
						"status", "error",
						"message", "Error processing connector",
						"error", e.getMessage()
					));
				}
			}	
			
			if(isSaveAuthData) {
				if (response_action != null && !response_action.isEmpty()) {
					authDataMap.put("action", response_action);
				}
				else {
					authDataMap.put("action", "redirect");
				}
				if (payamt != null && !payamt.isEmpty()) {
					authDataMap.put("payamt", Double.parseDouble(payamt));
				} 
				else {
					authDataMap.put("payamt", Double.parseDouble(request.get("bill_amt").toString()));
				}
				//paycurrency
				if (paycurrency != null && !paycurrency.isEmpty()) {
					authDataMap.put("paycurrency", paycurrency);
				}
				else {
					authDataMap.put("paycurrency", request.get("bill_currency").toString());
				}
				//paytitle
				if (paytitle != null && !paytitle.isEmpty()) {
					authDataMap.put("paytitle", paytitle);
				}
				else {
					authDataMap.put("paytitle", request.get("product_name").toString());
				}
				
				
				if (payaddress != null) {
					authDataMap.put("payaddress", payaddress); //Extracting payaddress
				}
				if (authdata != null) {
					authDataMap.put("authdata", authdata); //Extracting connector_authdata
				}
	

				transactionAdditional.setAuthUrl(auth_3ds); // authurl is 3ds URL
				String authdata64 = Base64Util.encodeBase64(jsonen(authDataMap));
				transactionAdditional.setAuthData(authdata64); // authdata is base64 encoded JSON string of authDataMap
				
				transactionService.saves2sTransAdditional(transactionAdditional);
			}
		    Map<String, Object> response = createSuccessResponse(savedTransaction, savedTransAdditional, baseUrl, encryptTransID);
	        return ResponseEntity.ok().headers(createCorsHeaders()).body(response);

	    } catch (Exception e) {
	        return ResponseEntity.badRequest().headers(createCorsHeaders()).body(createErrorResponse("500", "Internal Server Error: " + e.getMessage()));
	    }
	}
	
	private HttpHeaders createCorsHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		headers.add("Access-Control-Allow-Headers", "*");
		headers.add("Access-Control-Max-Age", "3600");
		return headers;
	}
	
	    private Long generateUniqueTransID(String connectorId, Long savedTransactionId) {
			String connectorPart = connectorId != null && !connectorId.isEmpty() ? connectorId : "";
			String epochPart = String.valueOf(System.currentTimeMillis()); // 13 digits
			String randomPart = String.format("%03d", new Random().nextInt(1000));
			String combined = epochPart + randomPart; // 16 digits
			int remainingLength = 18 - connectorPart.length();
			if (remainingLength <= 0) {
				return Long.parseLong(connectorPart.substring(0, 18));
			}
			String uniquePart = combined.substring(combined.length() - remainingLength);
			String candidate = connectorPart + uniquePart;
			return Long.parseLong(candidate);
    }

    private Map<String, Object> createSuccessResponse(Transaction savedTransaction, TransactionAdditional transactionAdditional, String baseUrl, String encryptTransID) {
    	
    	String orderStatus = savedTransaction.getTransactionStatus() == null ? "0" : savedTransaction.getTransactionStatus().toString(); // Default to Pending
    	

    	String statusDescription = getStatusDes(orderStatus); // Convert code to text

		String transID = savedTransaction.getTransID() == null ? "" : savedTransaction.getTransID().toString();
		
		if(encryptTransID != null && !encryptTransID.isEmpty()) {
			transID = encryptTransID;
		}
    	
    	Map<String, Object> response = new HashMap<>();
    	response.put("transID", savedTransaction.getTransID().toString());
    	response.put("order_status", orderStatus);
    	response.put("status", statusDescription);
    	response.put("bill_amt", savedTransaction.getBillAmount() == null ? "0.00" : savedTransaction.getBillAmount().toString());

		if(transactionAdditional.getDescriptor() != null) {
    		response.put("descriptor", transactionAdditional.getDescriptor());
		}
		String formattedDate;
		try {
			LocalDateTime transactionDate = savedTransaction.getTransactionDate();
			if (transactionDate != null) {
				formattedDate = transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
			} else {
				formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
			}
		} catch (Exception e) {
			formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
		}
		response.put("tdate", formattedDate);
		

    	response.put("bill_currency", savedTransaction.getBillCurrency());
		response.put("response", transactionAdditional.getTransactionResponse() == null ? "Payment is pending" : transactionAdditional.getTransactionResponse());

    	response.put("reference", savedTransaction.getReference());
    	response.put("mop", savedTransaction.getMethodOfPayment());
    	
    	String decryptedCCNO = transactionAdditional.getCcnoDecrypted();
		String formattedCardNumber = transactionAdditional.getFormattedCardNumber(); 
    	if(decryptedCCNO != null) {
    		response.put("ccno", formattedCardNumber); 
    	}
    	
    	response.put("rrn", transactionAdditional.getRrn());
    	response.put("upa", transactionAdditional.getUpa() == null ? "null" : transactionAdditional.getUpa());
    	response.put("authstatus", baseUrl + "authurl/s2s/" + savedTransaction.getTransID());
    	response.put("authurl", baseUrl + "authurl/" + transID);
    	
		if(savedTransaction.getIntegrationType() != null && savedTransaction.getIntegrationType().equals("checkout-s2s")) {
			String authdata64 = transactionAdditional.getAuthData() == null ? "null" : transactionAdditional.getAuthData();
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
			else if(authDataMapDecode.containsKey("authdata") && authDataMapDecode.get("authdata") instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> nestedAuthData = (Map<String, Object>) authDataMapDecode.get("authdata");
				if(nestedAuthData.containsKey("payaddress") && nestedAuthData.get("payaddress") != null && !nestedAuthData.get("payaddress").toString().isEmpty()) {
					response.put("payaddress", nestedAuthData.get("payaddress"));
				}
			}

			if(authDataMapDecode.containsKey("action") && authDataMapDecode.get("action") != null && !authDataMapDecode.get("action").toString().isEmpty()) {
				response.put("action", authDataMapDecode.get("action"));
			}
			
			if(authDataMapDecode.containsKey("payamt") && authDataMapDecode.get("payamt") != null) {
				response.put("payamt", authDataMapDecode.get("payamt"));
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
		}
        return response;
    }

    private Map<String, Object> createErrorResponse(String errorNumber, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("error_number", errorNumber);
        response.put("error_message", errorMessage);
        response.put("status", "Error");
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
	        case "10": return "Scrubbed";
	        case "11": return "Predispute";
	        case "12": return "Partial Refund";
	        case "13": return "Withdraw Requested";
	        case "14": return "Withdraw Rolling";
	        case "20": return "Frozen Balance";
	        case "21": return "Frozen Rolling";
	        case "22": return "Expired";
	        case "23": return "Cancelled";
	        case "24": return "Failed";
	        default: return "Pending";
	    }
	}

	private String maskCardNumber(String ccno) {
        if (ccno == null || ccno.length() < 10) {
            return ccno;
        }
        return ccno.substring(0, 6) + "XXXXXX" + ccno.substring(ccno.length() - 4);
    }

	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsoncredentials(Map<String, Object> jsonConfig, String mode,Map<String, Object> connectorkey) {
	    Map<String, Object> credentials = new HashMap<>();
	    
	    if (jsonConfig == null) {
	        return credentials;
	    }
		    if ("1".equals(mode)) {
	        if (jsonConfig.containsKey("live")) {
	            credentials = (Map<String, Object>) jsonConfig.get("live");
	        } 
	    } 
	    else if ("0".equals(mode)) {
	        if (jsonConfig.containsKey("test")) {
	            credentials = (Map<String, Object>) jsonConfig.get("test");
	        } 
	    } 
	    
		if (connectorkey != null && !connectorkey.isEmpty()) {
			for (Map.Entry<String, Object> entry : connectorkey.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (credentials.containsKey(key)) {
					credentials.put(key, value);
				} else {
					credentials.put(key, value);
				}
			}
		}	

	    return credentials;
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
	                @SuppressWarnings("unchecked")
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
	        return new HashMap<>();
	    }
	}

	private String determineScheme(HttpServletRequest request) {
		String serverName = request.getServerName();
		if (serverName != null) {
			String lower = serverName.toLowerCase();
			if (lower.equals("localhost") || lower.equals("127.0.0.1") || lower.equals("0.0.0.0") || lower.equals("::1") || lower.endsWith(".local")) {
				return "http";
			}
		}

		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		if (forwardedProto != null && !forwardedProto.isEmpty()) {
			String proto = forwardedProto.split(",")[0].trim();
			if (!proto.isEmpty()) return proto.toLowerCase();
		}

		if ("https".equalsIgnoreCase(request.getScheme()) || request.getServerPort() == 443) {
			return "https";
		}

		if (serverName != null && isProductionDomain(serverName)) {
			return "https";
		}

		return request.getScheme();
	}


	private boolean isProductionDomain(String serverName) {
		if (serverName == null || productionDomains == null) {
			return false;
		}
		
		String[] domains = productionDomains.split(",");
		for (String domain : domains) {
			String trimmedDomain = domain.trim();
			if (serverName.contains(trimmedDomain)) {
				return true;
			}
		}
		return false;
	}
	
   
}
