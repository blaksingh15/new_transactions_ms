package com.connector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
@Entity
@Table(name = "connectors",  uniqueConstraints = {  @UniqueConstraint(columnNames = "connector_number") },
    indexes = {@Index(name = "idx_connector_number", columnList = "connector_number"),  @Index(name = "idx_connector_status", columnList = "connector_status") })
public class Connector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "connector_number", columnDefinition = "varchar(255)") 
    private String connectorNumber;
    
    @Column(name = "ecommerce_cruises_json", columnDefinition = "TEXT")
    private String ecommerceCruisesJson;
    
    @Column(name = "connector_name")
    private String connectorName;
    
    @Column(name = "channel_type")
    private String channelType;
    
    @Column(name = "connector_status")
    private String connectorStatus;
    
    @Column(name = "mcc_code")
    private String mccCode;
    
    @Column(name = "connector_prod_mode")
    private String connectorProdMode;
    
    @Column(name = "default_connector")
    private String defaultConnector;
    
    @Column(name = "connection_method")
    private String connectionMethod;
    
    @Column(name = "connector_base_url")
    private String connectorBaseUrl;
    
    @Column(name = "connector_prod_url")
    private String connectorProdUrl;
    
    @Column(name = "connector_uat_url")
    private String connectorUatUrl;
    
    @Column(name = "connector_status_url")
    private String connectorStatusUrl;
    
    @Column(name = "connector_refund_url")
    private String connectorRefundUrl;
    
    @Column(name = "connector_refund_policy")
    private String connectorRefundPolicy;
    
    @Column(name = "connector_descriptor")
    private String connectorDescriptor;
    
    @Column(name = "trans_auto_expired")
    private String transAutoExpired;
    
    @Column(name = "trans_auto_refund")
    private String transAutoRefund;
    
    @Column(name = "connector_wl_ip")
    private String connectorWlIp;
    
    @Column(name = "connector_wl_domain")
    private String connectorWlDomain;
    
    @Column(name = "connector_dev_api_url")
    private String connectorDevApiUrl;
    
    @Column(name = "connector_login_creds")
    private String connectorLoginCreds;
    
    @Column(name = "connector_processing_currency")
    private String connectorProcessingCurrency;
    
    @Column(name = "processing_currency_markup")
    private String processingCurrencyMarkup;
    
    @Column(name = "mop_web")
    private String mopWeb;
    
    @Column(name = "mop_mobile")
    private String mopMobile;
    
    @Column(name = "tech_comments_text", columnDefinition = "TEXT")
    private String techCommentsText;
    
    @Column(name = "hard_code_payment_url")
    private String hardCodePaymentUrl;
    
    @Column(name = "hard_code_status_url")
    private String hardCodeStatusUrl;
    
    @Column(name = "hard_code_refund_url")
    private String hardCodeRefundUrl;
    
    @Column(name = "skip_checkout_validation")
    private String skipCheckoutValidation;
    
    @Column(name = "redirect_popup_msg_web")
    private String redirectPopupMsgWeb;
    
    @Column(name = "redirect_popup_msg_mobile")
    private String redirectPopupMsgMobile;
    
    @Column(name = "checkout_label_name_web")
    private String checkoutLabelNameWeb;
    
    @Column(name = "checkout_label_name_mobile")
    private String checkoutLabelNameMobile;
    
    @Column(name = "checkout_sub_label_name_web")
    private String checkoutSubLabelNameWeb;
    
    @Column(name = "checkout_sub_label_name_mobile")
    private String checkoutSubLabelNameMobile;

    @Column(name = "checkout_ui_version")
    private String checkoutUiVersion;
    
    @Column(name = "checkout_ui_theme")
    private String checkoutUiTheme;
    
    @Column(name = "checkout_ui_language")
    private String checkoutUiLanguage;
    
    @Column(name = "connector_processing_creds_json", columnDefinition = "TEXT")
    private String connectorProcessingCredsJson;
    
    @Column(name = "mer_setting_json", columnDefinition = "TEXT")
    private String merSettingJson;
    
    @Column(name = "connector_label_json", columnDefinition = "TEXT")
    private String connectorLabelJson;
    
    @Column(name = "processing_countries_json", columnDefinition = "TEXT")
    private String processingCountriesJson;
    
    @Column(name = "block_countries_json", columnDefinition = "TEXT")
    private String blockCountriesJson;
    
    @Column(name = "notification_email")
    private String notificationEmail;
    
    @Column(name = "webhook_notification")
    private String webhookNotification;
    
    @Column(name = "notification_count")
    private Integer notificationCount;
    
    @Column(name = "auto_status_fetch")
    private String autoStatusFetch;
    
    @Column(name = "auto_status_start_time")
    private String autoStatusStartTime;
    
    @Column(name = "auto_status_interval_time")
    private String autoStatusIntervalTime;
    
    @Column(name = "cron_bank_status_response")
    private String cronBankStatusResponse;


    @Column(name = "pending_1x", columnDefinition = "smallint")
    private Short pending1x;

    @Column(name = "approved_2x", columnDefinition = "smallint")
    private Short approved2x;

    @Column(name = "refund_3x", columnDefinition = "smallint")
    private Short refund3x;

    @Column(name = "failed_4x", columnDefinition = "smallint")
    private Short failed4x;

    @Column(name = "dispute_5x", columnDefinition = "smallint")
    private Short dispute5x;

    @Column(name = "cancelled_6x", columnDefinition = "smallint")
    private Short cancelled6x;

    @Column(name = "payout_7x", columnDefinition = "smallint")
    private Short payout7x;

    @Column(name = "technical_8x", columnDefinition = "smallint")
    private Short technical8x;

    @Column(name = "compliance_9x", columnDefinition = "smallint")
    private Short compliance9x;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Short getPending1x() {
        return pending1x;
    }

    public void setPending1x(Short pending1x) {
        this.pending1x = pending1x;
    }

    public Short getApproved2x() {
        return approved2x;
    }

    public void setApproved2x(Short approved2x) {
        this.approved2x = approved2x;
    }

    public Short getRefund3x() {
        return refund3x;
    }

    public void setRefund3x(Short refund3x) {
        this.refund3x = refund3x;
    }

    public Short getFailed4x() {
        return failed4x;
    }

    public void setFailed4x(Short failed4x) {
        this.failed4x = failed4x;
    }

    public Short getDispute5x() {
        return dispute5x;
    }

    public void setDispute5x(Short dispute5x) {
        this.dispute5x = dispute5x;
    }

    public Short getCancelled6x() {
        return cancelled6x;
    }

    public void setCancelled6x(Short cancelled6x) {
        this.cancelled6x = cancelled6x;
    }

    public Short getPayout7x() {
        return payout7x;
    }

    public void setPayout7x(Short payout7x) {
        this.payout7x = payout7x;
    }

    public Short getTechnical8x() {
        return technical8x;
    }

    public void setTechnical8x(Short technical8x) {
        this.technical8x = technical8x;
    }

    public Short getCompliance9x() {
        return compliance9x;
    }

    public void setCompliance9x(Short compliance9x) {
        this.compliance9x = compliance9x;
    }
    public String getConnectorNumber() {
        return connectorNumber;
    }

    public void setConnectorNumber(String connectorNumber) {
        this.connectorNumber = connectorNumber;
    }

    public String getEcommerceCruisesJson() {
        return ecommerceCruisesJson;
    }

    public void setEcommerceCruisesJson(String ecommerceCruisesJson) {
        this.ecommerceCruisesJson = ecommerceCruisesJson;
    }

    public String getConnectorName() { return connectorName; }
    public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
    
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }

    public String getConnectorStatus() { return connectorStatus; }
    public void setConnectorStatus(String connectorStatus) { this.connectorStatus = connectorStatus; }

    public String getMccCode() { return mccCode; }
    public void setMccCode(String mccCode) { this.mccCode = mccCode; }

    public String getConnectorProdMode() { return connectorProdMode; }
    public void setConnectorProdMode(String connectorProdMode) { this.connectorProdMode = connectorProdMode; }

    public String getDefaultConnector() { return defaultConnector; }
    public void setDefaultConnector(String defaultConnector) { this.defaultConnector = defaultConnector; }

    public String getConnectionMethod() { return connectionMethod; }
    public void setConnectionMethod(String connectionMethod) { this.connectionMethod = connectionMethod; }

    public String getConnectorBaseUrl() { return connectorBaseUrl; }
    public void setConnectorBaseUrl(String connectorBaseUrl) { this.connectorBaseUrl = connectorBaseUrl; }

    public String getConnectorProdUrl() { return connectorProdUrl; }
    public void setConnectorProdUrl(String connectorProdUrl) { this.connectorProdUrl = connectorProdUrl; }

    public String getConnectorUatUrl() { return connectorUatUrl; }
    public void setConnectorUatUrl(String connectorUatUrl) { this.connectorUatUrl = connectorUatUrl; }

    public String getConnectorStatusUrl() { return connectorStatusUrl; }
    public void setConnectorStatusUrl(String connectorStatusUrl) { this.connectorStatusUrl = connectorStatusUrl; }

    public String getConnectorRefundUrl() { return connectorRefundUrl; }
    public void setConnectorRefundUrl(String connectorRefundUrl) { this.connectorRefundUrl = connectorRefundUrl; }

    public String getConnectorRefundPolicy() { return connectorRefundPolicy; }
    public void setConnectorRefundPolicy(String connectorRefundPolicy) { this.connectorRefundPolicy = connectorRefundPolicy; }

    public String getConnectorDescriptor() { return connectorDescriptor; }
    public void setConnectorDescriptor(String connectorDescriptor) { this.connectorDescriptor = connectorDescriptor; }

    public String getTransAutoExpired() { return transAutoExpired; }
    public void setTransAutoExpired(String transAutoExpired) { this.transAutoExpired = transAutoExpired; }

    public String getTransAutoRefund() { return transAutoRefund; }
    public void setTransAutoRefund(String transAutoRefund) { this.transAutoRefund = transAutoRefund; }

    public String getConnectorWlIp() { return connectorWlIp; }
    public void setConnectorWlIp(String connectorWlIp) { this.connectorWlIp = connectorWlIp; }

    public String getConnectorWlDomain() { return connectorWlDomain; }
    public void setConnectorWlDomain(String connectorWlDomain) { this.connectorWlDomain = connectorWlDomain; }

    public String getConnectorDevApiUrl() { return connectorDevApiUrl; }
    public void setConnectorDevApiUrl(String connectorDevApiUrl) { this.connectorDevApiUrl = connectorDevApiUrl; }

    public String getConnectorLoginCreds() { return connectorLoginCreds; }
    public void setConnectorLoginCreds(String connectorLoginCreds) { this.connectorLoginCreds = connectorLoginCreds; }

    public String getConnectorProcessingCurrency() { return connectorProcessingCurrency; }
    public void setConnectorProcessingCurrency(String connectorProcessingCurrency) { this.connectorProcessingCurrency = connectorProcessingCurrency; }

    public String getProcessingCurrencyMarkup() { return processingCurrencyMarkup; }
    public void setProcessingCurrencyMarkup(String processingCurrencyMarkup) { this.processingCurrencyMarkup = processingCurrencyMarkup; }

    public String getMopWeb() { return mopWeb; }
    public void setMopWeb(String mopWeb) { this.mopWeb = mopWeb; }

    public String getMopMobile() { return mopMobile; }
    public void setMopMobile(String mopMobile) { this.mopMobile = mopMobile; }

    public String getTechCommentsText() { return techCommentsText; }
    public void setTechCommentsText(String techCommentsText) { this.techCommentsText = techCommentsText; }

    public String getHardCodePaymentUrl() { return hardCodePaymentUrl; }
    public void setHardCodePaymentUrl(String hardCodePaymentUrl) { this.hardCodePaymentUrl = hardCodePaymentUrl; }

    public String getHardCodeStatusUrl() { return hardCodeStatusUrl; }
    public void setHardCodeStatusUrl(String hardCodeStatusUrl) { this.hardCodeStatusUrl = hardCodeStatusUrl; }

    public String getHardCodeRefundUrl() { return hardCodeRefundUrl; }
    public void setHardCodeRefundUrl(String hardCodeRefundUrl) { this.hardCodeRefundUrl = hardCodeRefundUrl; }

    public String getSkipCheckoutValidation() { return skipCheckoutValidation; }
    public void setSkipCheckoutValidation(String skipCheckoutValidation) { this.skipCheckoutValidation = skipCheckoutValidation; }

    public String getRedirectPopupMsgWeb() { return redirectPopupMsgWeb; }
    public void setRedirectPopupMsgWeb(String redirectPopupMsgWeb) { this.redirectPopupMsgWeb = redirectPopupMsgWeb; }

    public String getRedirectPopupMsgMobile() { return redirectPopupMsgMobile; }
    public void setRedirectPopupMsgMobile(String redirectPopupMsgMobile) { this.redirectPopupMsgMobile = redirectPopupMsgMobile; }

    public String getCheckoutLabelNameWeb() { return checkoutLabelNameWeb; }
    public void setCheckoutLabelNameWeb(String checkoutLabelNameWeb) { this.checkoutLabelNameWeb = checkoutLabelNameWeb; }

    public String getCheckoutLabelNameMobile() { return checkoutLabelNameMobile; }
    public void setCheckoutLabelNameMobile(String checkoutLabelNameMobile) { this.checkoutLabelNameMobile = checkoutLabelNameMobile; }

    public String getCheckoutSubLabelNameWeb() { return checkoutSubLabelNameWeb; }
    public void setCheckoutSubLabelNameWeb(String checkoutSubLabelNameWeb) { this.checkoutSubLabelNameWeb = checkoutSubLabelNameWeb; }

    public String getCheckoutSubLabelNameMobile() { return checkoutSubLabelNameMobile; }
    public void setCheckoutSubLabelNameMobile(String checkoutSubLabelNameMobile) { this.checkoutSubLabelNameMobile = checkoutSubLabelNameMobile; }

    public String getCheckoutUiVersion() { return checkoutUiVersion; }
    public void setCheckoutUiVersion(String checkoutUiVersion) { this.checkoutUiVersion = checkoutUiVersion; }
    
    public String getCheckoutUiTheme() { return checkoutUiTheme; }
    public void setCheckoutUiTheme(String checkoutUiTheme) { this.checkoutUiTheme = checkoutUiTheme; }
    
    public String getCheckoutUiLanguage() { return checkoutUiLanguage; }
    public void setCheckoutUiLanguage(String checkoutUiLanguage) { this.checkoutUiLanguage = checkoutUiLanguage; }
    
    public String getConnectorProcessingCredsJson() { return connectorProcessingCredsJson; }
    public void setConnectorProcessingCredsJson(String connectorProcessingCredsJson) { this.connectorProcessingCredsJson = connectorProcessingCredsJson; }

    public String getMerSettingJson() { return merSettingJson; }
    public void setMerSettingJson(String merSettingJson) { this.merSettingJson = merSettingJson; }

    public String getConnectorLabelJson() { return connectorLabelJson; }
    public void setConnectorLabelJson(String connectorLabelJson) { this.connectorLabelJson = connectorLabelJson; }

    public String getProcessingCountriesJson() { return processingCountriesJson; }
    public void setProcessingCountriesJson(String processingCountriesJson) { this.processingCountriesJson = processingCountriesJson; }

    public String getBlockCountriesJson() { return blockCountriesJson; }
    public void setBlockCountriesJson(String blockCountriesJson) { this.blockCountriesJson = blockCountriesJson; }

    public String getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }

    

    public Integer getNotificationCount() { return notificationCount; }
    public void setNotificationCount(Integer notificationCount) { this.notificationCount = notificationCount; }

    public String getAutoStatusFetch() { return autoStatusFetch; }
    public void setAutoStatusFetch(String autoStatusFetch) { this.autoStatusFetch = autoStatusFetch; }

    public String getAutoStatusStartTime() { return autoStatusStartTime; }
    public void setAutoStatusStartTime(String autoStatusStartTime) { this.autoStatusStartTime = autoStatusStartTime; }

    public String getAutoStatusIntervalTime() { return autoStatusIntervalTime; }
    public void setAutoStatusIntervalTime(String autoStatusIntervalTime) { this.autoStatusIntervalTime = autoStatusIntervalTime; }

    public String getCronBankStatusResponse() { return cronBankStatusResponse; }
    public void setCronBankStatusResponse(String cronBankStatusResponse) { this.cronBankStatusResponse = cronBankStatusResponse; }
    
    public String getWebhookNotification() {
        return webhookNotification;
    }
    public void setWebhookNotification(String webhookNotification) {
        this.webhookNotification = webhookNotification;
    }
   

}