package com.payinprocessingengine.entity;
 public class TransactionAdditional {
    private Long id;
    private Long transIDAd;

    private String authUrl;

    private String authData;

    private String sourceUrl;

    private String webhookUrl;

    private String returnUrl;

    private String upa;

    private String rrn;

    private String connectorRef;

    private String connectorResponse;

    private String descriptor;

    private String merchantNote;

    private String supportNote;

    private String systemNote;

    private String jsonValue;

    private String connectorJson;

    private String jsonLogHistory;

    private String payloadStage1;

    private String connectorCredsProcessingFinal;

    private String connectorResponseStage1;

    private String connectorResponseStage2;

    private Integer binNumber;

    private String cardNumber;

    private String expiryMonth;

    private String expiryYear;

    private String transactionResponse;

    private String billingPhone;

    private String billingAddress;

    private String billingCity;

    private String billingState;

    private String billingCountry;

    private String billingZip;

    private String productName;

    private String cardType; // credit / debit / prepaid / unknown

    private String cardBrand; // visa / mastercard / amex / rupay

    private String cardTier; // platinum / gold / classic / signature

    private String gateway; // stripe / checkout / razorpay / authorize_net

    private String routingChannel; // stripe_credit_lane / razorpay_debit_lane / fallback / manual_review

    private String issuingBank; // HDFC / ICICI / SBI / Axis

    private String issuingCountry; // IN / US / GB / SG

    private String customerDeviceType; // desktop / mobile / tablet / unknown

    private String authenticationType; // 3ds / otp / biometric / none

    // Add getters and setters for new fields

    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long long1) {
        this.id = long1;
    }
    
    public String getAuthUrl() {
        return authUrl;
    }
    
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
    
    public String getAuthData() {
        return authData;
    }
    
    public void setAuthData(String authData) {
        this.authData = authData;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getUpa() {
        return upa;
    }
    
    public void setUpa(String upa) {
        this.upa = upa;
    }
    
    public String getRrn() {
        return rrn;
    }
    
    public void setRrn(String rrn) {
        this.rrn = rrn;
    }
    
    public String getConnectorRef() {
        return connectorRef;
    }
    
    public void setConnectorRef(String connectorRef) {
        this.connectorRef = connectorRef;
    }
    
    public String getConnectorResponse() {
        return connectorResponse;
    }
    
    public void setConnectorResponse(String connectorResponse) {
        this.connectorResponse = connectorResponse;
    }
    
    public String getDescriptor() {
        return descriptor;
    }
    
    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
    
    public String getMerchantNote() {
        return merchantNote;
    }
    
    public void setMerchantNote(String merchantNote) {
        this.merchantNote = merchantNote;
    }
    
    public String getSupportNote() {
        return supportNote;
    }
    
    public void setSupportNote(String supportNote) {
        this.supportNote = supportNote;
    }
    
    public String getSystemNote() {
        return systemNote;
    }
    
    public void setSystemNote(String systemNote) {
        this.systemNote = systemNote;
    }
    
    public String getJsonValue() {
        return jsonValue;
    }
    
    public void setJsonValue(String jsonValue) {
        this.jsonValue = jsonValue;
    }
    
    public String getConnectorJson() {
        return connectorJson;
    }
    
    public void setConnectorJson(String connectorJson) {
        this.connectorJson = connectorJson;
    }
    
    public String getJsonLogHistory() {
        return jsonLogHistory;
    }
    
    public void setJsonLogHistory(String jsonLogHistory) {
        this.jsonLogHistory = jsonLogHistory;
    }
    
    public String getPayloadStage1() {
        return payloadStage1;
    }
    
    public void setPayloadStage1(String payloadStage1) {
        this.payloadStage1 = payloadStage1;
    }
    
    public String getConnectorCredsProcessingFinal() {
        return connectorCredsProcessingFinal;
    }
    
    public void setConnectorCredsProcessingFinal(String connectorCredsProcessingFinal) {
        this.connectorCredsProcessingFinal = connectorCredsProcessingFinal;
    }
    
    public String getConnectorResponseStage1() {
        return connectorResponseStage1;
    }
    
    public void setConnectorResponseStage1(String connectorResponseStage1) {
        this.connectorResponseStage1 = connectorResponseStage1;
    }
    
    public String getConnectorResponseStage2() {
        return connectorResponseStage2;
    }
    
    public void setConnectorResponseStage2(String connectorResponseStage2) {
        this.connectorResponseStage2 = connectorResponseStage2;
    }
    
    public Integer getBinNumber() {
        return binNumber;
    }
    
    public void setBinNumber(Integer binNumber) {
        this.binNumber = binNumber;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getExpiryMonth() {
        return expiryMonth;
    }
    
    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    
    public String getExpiryYear() {
        return expiryYear;
    }
    
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }
    
    public String getTransactionResponse() {
        return transactionResponse;
    }
    
    public void setTransactionResponse(String transactionResponse) {
        this.transactionResponse = transactionResponse;
    }
    
    public String getBillingPhone() {
        return billingPhone;
    }
    
    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }
    
    public String getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public String getBillingCity() {
        return billingCity;
    }
    
    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }
    
    public String getBillingState() {
        return billingState;
    }
    
    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }
    
    public String getBillingCountry() {
        return billingCountry;
    }
    
    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }
    
    public String getBillingZip() {
        return billingZip;
    }
    
    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Long getTransIDAd() {
        return transIDAd;
    }

    public void setTransIDAd(Long transIDAd) {
        this.transIDAd = transIDAd;
    }



  
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardBrand() {
        return cardBrand;
    }
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getCardTier() {
        return cardTier;
    }
    public void setCardTier(String cardTier) {
        this.cardTier = cardTier;
    }

    public String getGateway() {
        return gateway;
    }
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getRoutingChannel() {
        return routingChannel;
    }
    public void setRoutingChannel(String routingChannel) {
        this.routingChannel = routingChannel;
    }

    public String getIssuingBank() {
        return issuingBank;
    }
    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }
    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    public String getCustomerDeviceType() {
        return customerDeviceType;
    }
    public void setCustomerDeviceType(String customerDeviceType) {
        this.customerDeviceType = customerDeviceType;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
    
    

}
