package com.transactions.entity;
public class Connector {
    private Long id;
    private String connectorNumber;
    private String ecommerceCruisesJson;
    private String connectorName;
    private String channelType;
    private String connectorStatus;
    private String mccCode;
    private String connectorProdMode;
    private String defaultConnector;
    private String connectionMethod;
    private String connectorBaseUrl;
    private String connectorProdUrl;
    private String connectorUatUrl;
    private String connectorStatusUrl;
    private String connectorRefundUrl;
    private String connectorRefundPolicy;
    private String connectorDescriptor;
    private String transAutoExpired;
    private String transAutoRefund;
    private String connectorWlIp;
    private String connectorWlDomain;
    private String connectorDevApiUrl;
    private String connectorLoginCreds;
    private String connectorProcessingCurrency;
    private String processingCurrencyMarkup;
    private String mopWeb;
    private String mopMobile;
    private String techCommentsText;
    private String hardCodePaymentUrl;
    private String hardCodeStatusUrl;
    private String hardCodeRefundUrl;
    private String skipCheckoutValidation;
    private String redirectPopupMsgWeb;
    private String redirectPopupMsgMobile;
    private String checkoutLabelNameWeb;
    private String checkoutLabelNameMobile;
    private String checkoutSubLabelNameWeb;
    private String checkoutSubLabelNameMobile;
    private String checkoutUiVersion;
    private String checkoutUiTheme;
    private String checkoutUiLanguage;
    private String connectorProcessingCredsJson;
    private String merSettingJson;
    private String connectorLabelJson;
    private String processingCountriesJson;
    private String blockCountriesJson;
    private String notificationEmail;
    private String webhookNotification;
    private Integer notificationCount;
    private String autoStatusFetch;
    private String autoStatusStartTime;
    private String autoStatusIntervalTime;
    private String cronBankStatusResponse;
    private Short pending1x;
    private Short approved2x;
    private Short refund3x;
    private Short failed4x;
    private Short dispute5x;
    private Short cancelled6x;
    private Short payout7x;
    private Short technical8x;
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
