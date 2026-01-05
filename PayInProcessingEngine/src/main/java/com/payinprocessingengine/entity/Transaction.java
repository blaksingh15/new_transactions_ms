package com.payinprocessingengine.entity;
import java.time.LocalDateTime;


public class Transaction {
    private Long id;
    private Long transID;  
    private String reference;
    private Long bearerToken;
    private LocalDateTime transactionDate;
    private Double billAmount;
    private String billCurrency;
    private Double transactionAmount;
    private String transactionCurrency;
    private Long connector;
    private Short transactionStatus;
    private Short transactionStatusStep;
    private Long merchantID;
    private Integer feeId;
    private String transactionFlag;
    private String fullName;
    private String billEmail;
    private String billIP;
    private Long terminalNumber;
    private String methodOfPayment;
    private String mopName;
    private Integer channelType;
    private String integrationType;
    private Double buyMdrAmount;
    private Double sellMdrAmount;
    private Double buyTxnFeeAmount;
    private Double sellTxnFeeAmount;
    private Double gstAmount;
    private Double rollingAmount;
    private Double mdrCashbackAmount;
    private LocalDateTime feeUpdateTimestamp;
    private Short remarkStatus;
    private Integer transactionType;
    private LocalDateTime settlementDate;
    private Integer settlementDelay;
    private LocalDateTime rollingDate;
    private Integer rollingDelay;
    private String riskRatio;
    private String transactionPeriod;
    private Double bankProcessingAmount;
    private String bankProcessingCurrency;
    private LocalDateTime createdDate = LocalDateTime.now();
    private String relatedTransactionID;
    private Double payableTransactionAmount;
    private Double mdrCashback1Amount;
    private Double mdrRefundFeeAmount;
    private Double availableRolling;
    private Double availableBalance;

    private Integer runtime;

    private Double remainingBalanceAmount;

    private Double matureRollingFundAmount;

    private Double immatureRollingFundAmount;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    public Long getTransID() {
        return transID;
    }
    public void setTransID(Long transID) {
        this.transID = transID;
    }
    
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(Long bearerToken) {
        this.bearerToken = bearerToken;
    }

    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(Double billAmount) {
        this.billAmount = billAmount;
    }

    public String getBillCurrency() {
        return billCurrency;
    }

    public void setBillCurrency(String billCurrency) {
        this.billCurrency = billCurrency;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public Long getConnector() {
        return connector;
    }

    public void setConnector(Long connector) {
        this.connector = connector;
    }

    public Short getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(Short transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Short getTransactionStatusStep() {
		return transactionStatusStep;
	}

	public void setTransactionStatusStep(Short transactionStatusStep) {
		this.transactionStatusStep = transactionStatusStep;
	}

	public Long getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(Long merchantID) {
        this.merchantID = merchantID;
    }

    public String getTransactionFlag() {
        return transactionFlag;
    }

    public void setTransactionFlag(String transactionFlag) {
        this.transactionFlag = transactionFlag;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBillEmail() {
        return billEmail;
    }

    public void setBillEmail(String billEmail) {
        this.billEmail = billEmail;
    }

    public String getBillIP() {
        return billIP;
    }

    public void setBillIP(String billIP) {
        this.billIP = billIP;
    }

    public String getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
    }

    public Long getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(Long terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public String getMethodOfPayment() {
        return methodOfPayment;
    }

    public void setMethodOfPayment(String methodOfPayment) {
        this.methodOfPayment = methodOfPayment;
    }

    public String getMopName() {
		return mopName;
	}

	public void setMopName(String mopName) {
		this.mopName = mopName;
	}

	public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    public Double getBuyMdrAmount() {
        return buyMdrAmount;
    }

    public void setBuyMdrAmount(Double buyMdrAmount) {
        this.buyMdrAmount = buyMdrAmount;
    }

    public Double getSellMdrAmount() {
        return sellMdrAmount;
    }

    public void setSellMdrAmount(Double sellMdrAmount) {
        this.sellMdrAmount = sellMdrAmount;
    }

    public Double getBuyTxnFeeAmount() {
        return buyTxnFeeAmount;
    }

    public void setBuyTxnFeeAmount(Double buyTxnFeeAmount) {
        this.buyTxnFeeAmount = buyTxnFeeAmount;
    }

    public Double getSellTxnFeeAmount() {
        return sellTxnFeeAmount;
    }

    public void setSellTxnFeeAmount(Double sellTxnFeeAmount) {
        this.sellTxnFeeAmount = sellTxnFeeAmount;
    }

    public Double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(Double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public Double getRollingAmount() {
        return rollingAmount;
    }

    public void setRollingAmount(Double rollingAmount) {
        this.rollingAmount = rollingAmount;
    }

    public Double getMdrCashbackAmount() {
        return mdrCashbackAmount;
    }

    public void setMdrCashbackAmount(Double mdrCashbackAmount) {
        this.mdrCashbackAmount = mdrCashbackAmount;
    }

    public LocalDateTime getFeeUpdateTimestamp() {
        return feeUpdateTimestamp;
    }

    public void setFeeUpdateTimestamp(LocalDateTime feeUpdateTimestamp) {
        this.feeUpdateTimestamp = feeUpdateTimestamp;
    }

    public Short getRemarkStatus() {
        return remarkStatus;
    }

    public void setRemarkStatus(Short remarkStatus) {
        this.remarkStatus = remarkStatus;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Integer getSettlementDelay() {
        return settlementDelay;
    }

    public void setSettlementDelay(Integer settlementDelay) {
        this.settlementDelay = settlementDelay;
    }

    public LocalDateTime getRollingDate() {
        return rollingDate;
    }

    public void setRollingDate(LocalDateTime rollingDate) {
        this.rollingDate = rollingDate;
    }

    public Integer getRollingDelay() {
        return rollingDelay;
    }

    public void setRollingDelay(Integer rollingDelay) {
        this.rollingDelay = rollingDelay;
    }

    public String getRiskRatio() {
        return riskRatio;
    }

    public void setRiskRatio(String riskRatio) {
        this.riskRatio = riskRatio;
    }

    public String getTransactionPeriod() {
        return transactionPeriod;
    }

    public void setTransactionPeriod(String transactionPeriod) {
        this.transactionPeriod = transactionPeriod;
    }

    public Double getBankProcessingAmount() {
        return bankProcessingAmount;
    }

    public void setBankProcessingAmount(Double bankProcessingAmount) {
        this.bankProcessingAmount = bankProcessingAmount;
    }

    public String getBankProcessingCurrency() {
        return bankProcessingCurrency;
    }

    public void setBankProcessingCurrency(String bankProcessingCurrency) {
        this.bankProcessingCurrency = bankProcessingCurrency;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getRelatedTransactionID() {
        return relatedTransactionID;
    }

    public void setRelatedTransactionID(String relatedTransactionID) {
        this.relatedTransactionID = relatedTransactionID;
    }

    public Double getPayableTransactionAmount() {
        return payableTransactionAmount;
    }

    public void setPayableTransactionAmount(Double payableTransactionAmount) {
        this.payableTransactionAmount = payableTransactionAmount;
    }

    public Double getMdrCashback1Amount() {
        return mdrCashback1Amount;
    }

    public void setMdrCashback1Amount(Double mdrCashback1Amount) {
        this.mdrCashback1Amount = mdrCashback1Amount;
    }

    public Double getMdrRefundFeeAmount() {
        return mdrRefundFeeAmount;
    }

    public void setMdrRefundFeeAmount(Double mdrRefundFeeAmount) {
        this.mdrRefundFeeAmount = mdrRefundFeeAmount;
    }

    public Double getAvailableRolling() {
        return availableRolling;
    }

    public void setAvailableRolling(Double availableRolling) {
        this.availableRolling = availableRolling;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Integer getFeeId() {
        return feeId;
    }

    public void setFeeId(Integer feeId) {
        this.feeId = feeId;
    }

    public Double getRemainingBalanceAmount() {
        return remainingBalanceAmount;
    }

    public void setRemainingBalanceAmount(Double remainingBalanceAmount) {
        this.remainingBalanceAmount = remainingBalanceAmount;
    }

    public Double getMatureRollingFundAmount() {
        return matureRollingFundAmount;
    }

    public void setMatureRollingFundAmount(Double matureRollingFundAmount) {
        this.matureRollingFundAmount = matureRollingFundAmount;
    }

    public Double getImmatureRollingFundAmount() {
        return immatureRollingFundAmount;
    }

    public void setImmatureRollingFundAmount(Double immatureRollingFundAmount) {
        this.immatureRollingFundAmount = immatureRollingFundAmount;
    }

    


}
