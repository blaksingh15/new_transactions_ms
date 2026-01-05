package com.payinprocessingfee.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity 
@Table( name = "payin_processing_fee_terminal_wise")
public class PayinProcessingFeeTerminalWise {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id", nullable = false)
    private Integer id;
    private Integer merid = 0;
    @Column(name = "connector_id")
    private String connectorId;
    private Integer connectorProcessingMode;
    private Double mdrRate = 0.00;
    private Double txnFeeAprroved = 0.00;
    private Double txnFeeFailed = 0.00;
    private Double taxOnMdrRate = 0.00;
    private String reserveRate = "";
    private String reserveDelay;
    private Double monthlyFee = 0.00;
    private Double chargeBackFee1 = 0.00;
    private Double chargeBackFee2 = 0.00;
    private Double chargeBackFee3 = 0.00;
    private Double predisputFee = 0.00;
    private Double refundFee = 0.00;
    private Double specialMdrRate = 0.00;
    private Double specialMdrRangeAmount = 0.00;
    private Double specialMdrRangeAmountApply = 0.00;
    private String settelementDelay;
    private String gatewayName = "";
    private String secondaryGatewayName = "";
    
    @Transient
    private Connector connector;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMerid() {
        return merid;
    }

    public void setMerid(Integer merid) {
        this.merid = merid;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public Integer getConnectorProcessingMode() {
        return connectorProcessingMode;
    }

    public void setConnectorProcessingMode(Integer connectorProcessingMode) {
        this.connectorProcessingMode = connectorProcessingMode;
    }

    public Double getMdrRate() {
        return mdrRate;
    }

    public void setMdrRate(Double mdrRate) {
        this.mdrRate = mdrRate;
    }

    public Double getTxnFeeAprroved() {
        return txnFeeAprroved;
    }

    public void setTxnFeeAprroved(Double txnFeeAprroved) {
        this.txnFeeAprroved = txnFeeAprroved;
    }

    public Double getTxnFeeFailed() {
        return txnFeeFailed;
    }

    public void setTxnFeeFailed(Double txnFeeFailed) {
        this.txnFeeFailed = txnFeeFailed;
    }

    public Double getTaxOnMdrRate() {
        return taxOnMdrRate;
    }

    public void setTaxOnMdrRate(Double taxOnMdrRate) {
        this.taxOnMdrRate = taxOnMdrRate;
    }

    public String getReserveRate() {
        return reserveRate;
    }

    public void setReserveRate(String reserveRate) {
        this.reserveRate = reserveRate;
    }

    public String getReserveDelay() {
        return reserveDelay;
    }

    public void setReserveDelay(String reserveDelay) {
        this.reserveDelay = reserveDelay;
    }

    public Double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(Double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public Double getChargeBackFee1() {
        return chargeBackFee1;
    }

    public void setChargeBackFee1(Double chargeBackFee1) {
        this.chargeBackFee1 = chargeBackFee1;
    }

    public Double getChargeBackFee2() {
        return chargeBackFee2;
    }

    public void setChargeBackFee2(Double chargeBackFee2) {
        this.chargeBackFee2 = chargeBackFee2;
    }

    public Double getChargeBackFee3() {
        return chargeBackFee3;
    }

    public void setChargeBackFee3(Double chargeBackFee3) {
        this.chargeBackFee3 = chargeBackFee3;
    }

    public Double getPredisputFee() {
        return predisputFee;
    }

    public void setPredisputFee(Double predisputFee) {
        this.predisputFee = predisputFee;
    }

    public Double getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(Double refundFee) {
        this.refundFee = refundFee;
    }

    public Double getSpecialMdrRate() {
        return specialMdrRate;
    }

    public void setSpecialMdrRate(Double specialMdrRate) {
        this.specialMdrRate = specialMdrRate;
    }

    public Double getSpecialMdrRangeAmount() {
        return specialMdrRangeAmount;
    }

    public void setSpecialMdrRangeAmount(Double specialMdrRangeAmount) {
        this.specialMdrRangeAmount = specialMdrRangeAmount;
    }

    public Double getSpecialMdrRangeAmountApply() {
        return specialMdrRangeAmountApply;
    }

    public void setSpecialMdrRangeAmountApply(Double specialMdrRangeAmountApply) {
        this.specialMdrRangeAmountApply = specialMdrRangeAmountApply;
    }

    public String getSettelementDelay() {
        return settelementDelay;
    }

    public void setSettelementDelay(String settelementDelay) {
        this.settelementDelay = settelementDelay;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getSecondaryGatewayName() {
        return secondaryGatewayName;
    }

    public void setSecondaryGatewayName(String secondaryGatewayName) {
        this.secondaryGatewayName = secondaryGatewayName;
    }
}
