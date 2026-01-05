package com.terminal.entity;
import java.time.LocalDateTime;
import com.terminal.utility.AES256Util;
import jakarta.persistence.*;
@Entity
@Table(name = "terminal")
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer merid;
    private String publicKey;
    
    @Column(unique = true, nullable = true)
    private String privateKey;
    
    private String connectorids;
    private String checkoutConnectorids;
    private String bussinessUrl;
    private String terName;
    private String terminalType;
    private String businessDescription;
    private String businessNature;
    private Short active;
    private String tarnsAlertEmail;
    private String merTransAlertEmail;
    private String dbaBrandName;
    private String customerServiceNo;
    private String customerServiceEmail;
    private String merchantTermConditionUrl;
    private String merchantRefundPolicyUrl;
    private String merchantPrivacyPolicyUrl;
    private String merchantContactUsUrl;
    private String merchantLogoUrl;
    private String curlingAccessKey;
    
    @Column(name = "terno_json_value", columnDefinition = "TEXT")
    private String ternoJsonValue;
    private Integer selectTemplates;
    private String selectTemplatesLog;
    private String jsonLogHistory;
    private String deletedBussinessUrl;
    private String checkoutTheme;
    private String selectMcc;
    private String webhookUrl;
    private String returnUrl;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    // ==== Monthly & Business Info
    private String monthlyVolume;
    private String businessClassification;
    private String environment;

    // ==== Technical
    // API Configuration
    private String apiVersion;
    private Integer requestTimeoutSeconds;
    private Integer maxRetryAttempts;
    // Transaction Settings
    private Double minimumTransactionAmount;
    private Double maximumTransactionAmount;
    private Integer sessionTimeoutMinutes;
    private Boolean autoSettlement;
    private Integer settlementDelay;
    // Security Settings
    private String encryptionLevel;
    private Boolean requireIpWhitelisting;
    private Boolean enableRateLimiting;
    private Integer requestsPerMinute;
    // Supported Payment Methods
    @Column(columnDefinition = "TEXT")
    private String supportedPaymentMethods;

    // ==== URLs & Policies
    // Webhook Configuration
    private String webhookSecret;
    private String webhookSecretKey;
    private Integer webhookRetries;
    private Boolean webhookSignatureVerification;
    @Column(columnDefinition = "TEXT")
    private String webhookEvents;
    // Redirect URLs
    private String redirectRetrunUrl;
    private String redirectSuccessUrl;
    private String redirectFailureUrl;
    private String redirectCancelUrl;
    // Security Policies
    @Column(columnDefinition = "TEXT")
    private String allowedDomains;
    @Column(columnDefinition = "TEXT")
    private String ipWhitelist;
    private Boolean corsSsl;
    // URL Validation Status
    private String statusWebhookUrl;
    private String statusSuccessUrl;
    private String statusFailureUrl;
    private String statusCancelUrl;

    // ==== Notifications
    // Notification Channels
    private Boolean emailNotificationsEnabled;
    private Boolean smsNotifications;
    private String smsNotificationsPhoneNumber;
    private Boolean webhookNotifications;
    private Boolean pushNotifications;
    // Notification Preferences
    private String notificationFrequency;
    private String notificationTimezone;
    private String quietHoursStart;
    private String quietHoursEnd;
    private String quietDays;
    // Notification Types
    @Column(columnDefinition = "TEXT")
    private String notificationTypes;
    //Advanced Notification Settings
    private Boolean batchNotifications;
    private Boolean smartFiltering;
    private Boolean priorityRouting;
    private Boolean deliveryConfirmation;

    // ==== Email Reports
    // Email Report Configuration
    private String senderName;
    private String senderEmail;
    private String reportTimezone;
    private String reportFormat;
    // Report Types & Scheduling
    @Column(columnDefinition = "TEXT")
    private String reportTypesScheduling;
    //not used
    private String dailySummaryTime;
    private String weeklyReportTime;
    private String dayOfWeek;
    private String monthlyAnalyticsTime;
    private String dayOfMonth;
    private String settlementReportTime;
    private String chargebackReportTime;
    private String reconciliationReportTime;
    @Column(columnDefinition = "TEXT")
    private String recipientsJson;
    // Email Template Customization
    private String emailSubjectTemplate;
    private String emailFooter;
    private String customBranding;

    // ==== API Keys
    // API Keys & Credentials 
    private String webhookSecretApiKey;

    // Getters and Setters
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getConnectorids() {
        return connectorids;
    }

    public void setConnectorids(String connectorids) {
        this.connectorids = connectorids;
    }

    public String getCheckoutConnectorids() {
		return checkoutConnectorids;
	}

	public void setCheckoutConnectorids(String checkoutConnectorids) {
		this.checkoutConnectorids = checkoutConnectorids;
	}

	public String getBussinessUrl() {
        return bussinessUrl;
    }

    public void setBussinessUrl(String bussinessUrl) {
        this.bussinessUrl = bussinessUrl;
    }

    public String getTerName() {
        return terName;
    }

    public void setTerName(String terName) {
        this.terName = terName;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getBusinessNature() {
        return businessNature;
    }

    public void setBusinessNature(String businessNature) {
        this.businessNature = businessNature;
    }

    public Short getActive() {
        return active;
    }

    public void setActive(Short active) {
        this.active = active;
    }

    public String getTarnsAlertEmail() {
        return tarnsAlertEmail;
    }

    public void setTarnsAlertEmail(String tarnsAlertEmail) {
        this.tarnsAlertEmail = tarnsAlertEmail;
    }

    public String getMerTransAlertEmail() {
        return merTransAlertEmail;
    }

    public void setMerTransAlertEmail(String merTransAlertEmail) {
        this.merTransAlertEmail = merTransAlertEmail;
    }

    public String getDbaBrandName() {
        return dbaBrandName;
    }

    public void setDbaBrandName(String dbaBrandName) {
        this.dbaBrandName = dbaBrandName;
    }

    public String getCustomerServiceNo() {
        return customerServiceNo;
    }

    public void setCustomerServiceNo(String customerServiceNo) {
        this.customerServiceNo = customerServiceNo;
    }

    public String getCustomerServiceEmail() {
        return customerServiceEmail;
    }

    public void setCustomerServiceEmail(String customerServiceEmail) {
        this.customerServiceEmail = customerServiceEmail;
    }

    public String getMerchantTermConditionUrl() {
        return merchantTermConditionUrl;
    }

    public void setMerchantTermConditionUrl(String merchantTermConditionUrl) {
        this.merchantTermConditionUrl = merchantTermConditionUrl;
    }

    public String getMerchantRefundPolicyUrl() {
        return merchantRefundPolicyUrl;
    }

    public void setMerchantRefundPolicyUrl(String merchantRefundPolicyUrl) {
        this.merchantRefundPolicyUrl = merchantRefundPolicyUrl;
    }

    public String getMerchantPrivacyPolicyUrl() {
        return merchantPrivacyPolicyUrl;
    }

    public void setMerchantPrivacyPolicyUrl(String merchantPrivacyPolicyUrl) {
        this.merchantPrivacyPolicyUrl = merchantPrivacyPolicyUrl;
    }

    public String getMerchantContactUsUrl() {
        return merchantContactUsUrl;
    }

    public void setMerchantContactUsUrl(String merchantContactUsUrl) {
        this.merchantContactUsUrl = merchantContactUsUrl;
    }

    public String getMerchantLogoUrl() {
        return merchantLogoUrl;
    }

    public void setMerchantLogoUrl(String merchantLogoUrl) {
        this.merchantLogoUrl = merchantLogoUrl;
    }

    public String getCurlingAccessKey() {
        return curlingAccessKey;
    }

    public void setCurlingAccessKey(String curlingAccessKey) {
        this.curlingAccessKey = curlingAccessKey;
    }

    public String getTernoJsonValue() {
        return ternoJsonValue;
    }

    public void setTernoJsonValue(String ternoJsonValue) {
        this.ternoJsonValue = ternoJsonValue;
    }

    public Integer getSelectTemplates() {
        return selectTemplates;
    }

    public void setSelectTemplates(Integer selectTemplates) {
        this.selectTemplates = selectTemplates;
    }

    public String getSelectTemplatesLog() {
        return selectTemplatesLog;
    }

    public void setSelectTemplatesLog(String selectTemplatesLog) {
        this.selectTemplatesLog = selectTemplatesLog;
    }

    public String getJsonLogHistory() {
        return jsonLogHistory;
    }

    public void setJsonLogHistory(String jsonLogHistory) {
        this.jsonLogHistory = jsonLogHistory;
    }

    public String getDeletedBussinessUrl() {
        return deletedBussinessUrl;
    }

    public void setDeletedBussinessUrl(String deletedBussinessUrl) {
        this.deletedBussinessUrl = deletedBussinessUrl;
    }

    public String getCheckoutTheme() {
        return checkoutTheme;
    }

    public void setCheckoutTheme(String checkoutTheme) {
        this.checkoutTheme = checkoutTheme;
    }

    public String getSelectMcc() {
        return selectMcc;
    }

    public void setSelectMcc(String selectMcc) {
        this.selectMcc = selectMcc;
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
    
    public String getPrivateKey() {
		String result = privateKey != null ? privateKey.replace("-", "").replace("_", "") : privateKey;
		if (result != null && result.length() > 0) {
			char lastChar = result.charAt(result.length() - 1);
			if (lastChar == '=' || lastChar == '+' || lastChar == '-' || lastChar == '_') {
				return result.substring(0, result.length() - 1);
			}
		}
		return result;
	}

	public void setPrivateKey(String privateKey) {
		if (privateKey != null) {
			privateKey = privateKey.replace("-", "").replace("_", "");
		}
		if (privateKey != null && privateKey.length() > 0) {
			char lastChar = privateKey.charAt(privateKey.length() - 1);
			if (lastChar == '=' || lastChar == '+' || lastChar == '-' || lastChar == '_') {
				privateKey = privateKey.substring(0, privateKey.length() - 1);
			}
		}
		this.privateKey = privateKey;
	}
    
    public void setPublicKeyEn(String publicKey) {
        try {
            String cleanedKey = publicKey != null ? publicKey.replace("-", "").replace("_", "") : publicKey;
            if (cleanedKey != null && cleanedKey.length() > 0) {
                char lastChar = cleanedKey.charAt(cleanedKey.length() - 1);
                if (lastChar == '=' || lastChar == '+' || lastChar == '-' || lastChar == '_') {
                    cleanedKey = cleanedKey.substring(0, cleanedKey.length() - 1);
                }
            }
            this.publicKey = AES256Util.encrypt(cleanedKey); // Encrypt before storing
        } catch (Exception e) {
            e.printStackTrace();
            this.publicKey = null;
        }
    }

    public String getPublicKeyDe() {
        try {
            String decryptedKey = AES256Util.decrypt(this.publicKey); // Decrypt when retrieving
            decryptedKey = decryptedKey != null ? decryptedKey.replace("-", "").replace("_", "") : decryptedKey;
            if (decryptedKey != null && decryptedKey.length() > 0) {
                char lastChar = decryptedKey.charAt(decryptedKey.length() - 1);
                if (lastChar == '=' || lastChar == '+' || lastChar == '-' || lastChar == '_') {
                    decryptedKey = decryptedKey.substring(0, decryptedKey.length() - 1);
                }
            }
            return decryptedKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ==== Monthly & Business Info
    public String getMonthlyVolume() { return monthlyVolume; }
    public void setMonthlyVolume(String monthlyVolume) { this.monthlyVolume = monthlyVolume; }
    public String getBusinessClassification() { return businessClassification; }
    public void setBusinessClassification(String businessClassification) { this.businessClassification = businessClassification; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    // ==== Technical
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    public Integer getRequestTimeoutSeconds() { return requestTimeoutSeconds; }
    public void setRequestTimeoutSeconds(Integer requestTimeoutSeconds) { this.requestTimeoutSeconds = requestTimeoutSeconds; }
    public Integer getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(Integer maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    public Double getMinimumTransactionAmount() { return minimumTransactionAmount; }
    public void setMinimumTransactionAmount(Double minimumTransactionAmount) { this.minimumTransactionAmount = minimumTransactionAmount; }
    public Double getMaximumTransactionAmount() { return maximumTransactionAmount; }
    public void setMaximumTransactionAmount(Double maximumTransactionAmount) { this.maximumTransactionAmount = maximumTransactionAmount; }
    public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
    public Boolean getAutoSettlement() { return autoSettlement; }
    public void setAutoSettlement(Boolean autoSettlement) { this.autoSettlement = autoSettlement; }
    public Integer getSettlementDelay() {return settlementDelay;}
    public void setSettlementDelay(Integer settlementDelay) {this.settlementDelay = settlementDelay;}
    public String getEncryptionLevel() { return encryptionLevel; }
    public void setEncryptionLevel(String encryptionLevel) { this.encryptionLevel = encryptionLevel; }
    public Boolean getRequireIpWhitelisting() { return requireIpWhitelisting; }
    public void setRequireIpWhitelisting(Boolean requireIpWhitelisting) { this.requireIpWhitelisting = requireIpWhitelisting; }
    public Boolean getEnableRateLimiting() { return enableRateLimiting; }
    public void setEnableRateLimiting(Boolean enableRateLimiting) { this.enableRateLimiting = enableRateLimiting; }
    public Integer getRequestsPerMinute() {return requestsPerMinute;}
    public void setRequestsPerMinute(Integer requestsPerMinute) {this.requestsPerMinute = requestsPerMinute;}
    public String getSupportedPaymentMethods() { return supportedPaymentMethods; }
    public void setSupportedPaymentMethods(String supportedPaymentMethods) { this.supportedPaymentMethods = supportedPaymentMethods; }
    // ==== URLs & Policies
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getWebhookSecretKey() { return webhookSecretKey; }
    public void setWebhookSecretKey(String webhookSecretKey) { this.webhookSecretKey = webhookSecretKey; }
    public Integer getWebhookRetries() { return webhookRetries; }
    public void setWebhookRetries(Integer webhookRetries) { this.webhookRetries = webhookRetries; }
    public Boolean getWebhookSignatureVerification() { return webhookSignatureVerification; }
    public void setWebhookSignatureVerification(Boolean webhookSignatureVerification) { this.webhookSignatureVerification = webhookSignatureVerification; }
    public String getWebhookEvents() { return webhookEvents; }
    public void setWebhookEvents(String webhookEvents) { this.webhookEvents = webhookEvents; }
    public String getRedirectSuccessUrl() { return redirectSuccessUrl; }
    public void setRedirectSuccessUrl(String redirectSuccessUrl) { this.redirectSuccessUrl = redirectSuccessUrl; }
    public String getRedirectRetrunUrl() {return redirectRetrunUrl;}
    public void setRedirectRetrunUrl(String redirectRetrunUrl) {this.redirectRetrunUrl = redirectRetrunUrl;}
    public String getRedirectFailureUrl() { return redirectFailureUrl; }
    public void setRedirectFailureUrl(String redirectFailureUrl) { this.redirectFailureUrl = redirectFailureUrl; }
    public String getRedirectCancelUrl() { return redirectCancelUrl; }
    public void setRedirectCancelUrl(String redirectCancelUrl) { this.redirectCancelUrl = redirectCancelUrl; }
    public String getAllowedDomains() { return allowedDomains; }
    public void setAllowedDomains(String allowedDomains) { this.allowedDomains = allowedDomains; }
    public String getIpWhitelist() { return ipWhitelist; }
    public void setIpWhitelist(String ipWhitelist) { this.ipWhitelist = ipWhitelist; }
    public Boolean getCorsSsl() { return corsSsl; }
    public void setCorsSsl(Boolean corsSsl) { this.corsSsl = corsSsl; }
    public String getStatusWebhookUrl() { return statusWebhookUrl; }
    public void setStatusWebhookUrl(String statusWebhookUrl) { this.statusWebhookUrl = statusWebhookUrl; }
    public String getStatusSuccessUrl() { return statusSuccessUrl; }
    public void setStatusSuccessUrl(String statusSuccessUrl) { this.statusSuccessUrl = statusSuccessUrl; }
    public String getStatusFailureUrl() { return statusFailureUrl; }
    public void setStatusFailureUrl(String statusFailureUrl) { this.statusFailureUrl = statusFailureUrl; }
    public String getStatusCancelUrl() { return statusCancelUrl; }
    public void setStatusCancelUrl(String statusCancelUrl) { this.statusCancelUrl = statusCancelUrl; }
    // ==== Notifications
    public Boolean getEmailNotificationsEnabled() {return emailNotificationsEnabled;}
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {this.emailNotificationsEnabled = emailNotificationsEnabled;}
    public Boolean getSmsNotifications() { return smsNotifications; }
    public void setSmsNotifications(Boolean smsNotifications) { this.smsNotifications = smsNotifications; }
    public String getSmsNotificationsPhoneNumber() { return smsNotificationsPhoneNumber; }
    public void setSmsNotificationsPhoneNumber(String smsNotificationsPhoneNumber) { this.smsNotificationsPhoneNumber = smsNotificationsPhoneNumber; }
    public Boolean getWebhookNotifications() { return webhookNotifications; }
    public void setWebhookNotifications(Boolean webhookNotifications) { this.webhookNotifications = webhookNotifications; }
    public Boolean getPushNotifications() { return pushNotifications; }
    public void setPushNotifications(Boolean pushNotifications) { this.pushNotifications = pushNotifications; }
    public String getNotificationFrequency() { return notificationFrequency; }
    public void setNotificationFrequency(String notificationFrequency) { this.notificationFrequency = notificationFrequency; }
    public String getNotificationTimezone() { return notificationTimezone; }
    public void setNotificationTimezone(String notificationTimezone) { this.notificationTimezone = notificationTimezone; }
    public String getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }
    public String getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    public String getQuietDays() { return quietDays; }
    public void setQuietDays(String quietDays) { this.quietDays = quietDays; }
    public String getNotificationTypes() { return notificationTypes; }
    public void setNotificationTypes(String notificationTypes) { this.notificationTypes = notificationTypes; }
    public Boolean getBatchNotifications() {return batchNotifications;}
    public void setBatchNotifications(Boolean batchNotifications) {this.batchNotifications = batchNotifications;}
    public Boolean getSmartFiltering() {return smartFiltering;}
    public void setSmartFiltering(Boolean smartFiltering) {this.smartFiltering = smartFiltering;}
    public Boolean getPriorityRouting() {return priorityRouting;}
    public void setPriorityRouting(Boolean priorityRouting) {this.priorityRouting = priorityRouting;}
    public Boolean getDeliveryConfirmation() {return deliveryConfirmation;}
    public void setDeliveryConfirmation(Boolean deliveryConfirmation) {this.deliveryConfirmation = deliveryConfirmation;}
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public String getReportTimezone() { return reportTimezone; }
    public void setReportTimezone(String reportTimezone) { this.reportTimezone = reportTimezone; }
    public String getReportFormat() { return reportFormat; }
    public void setReportFormat(String reportFormat) { this.reportFormat = reportFormat; }
    public String getReportTypesScheduling() {return reportTypesScheduling;}
    public void setReportTypesScheduling(String reportTypesScheduling) {this.reportTypesScheduling = reportTypesScheduling;}
    public String getDailySummaryTime() { return dailySummaryTime; }
    public void setDailySummaryTime(String dailySummaryTime) { this.dailySummaryTime = dailySummaryTime; }
    public String getWeeklyReportTime() { return weeklyReportTime; }
    public void setWeeklyReportTime(String weeklyReportTime) { this.weeklyReportTime = weeklyReportTime; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getMonthlyAnalyticsTime() { return monthlyAnalyticsTime; }
    public void setMonthlyAnalyticsTime(String monthlyAnalyticsTime) { this.monthlyAnalyticsTime = monthlyAnalyticsTime; }
    public String getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(String dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public String getSettlementReportTime() { return settlementReportTime; }
    public void setSettlementReportTime(String settlementReportTime) { this.settlementReportTime = settlementReportTime; }
    public String getChargebackReportTime() { return chargebackReportTime; }
    public void setChargebackReportTime(String chargebackReportTime) { this.chargebackReportTime = chargebackReportTime; }
    public String getReconciliationReportTime() { return reconciliationReportTime; }
    public void setReconciliationReportTime(String reconciliationReportTime) { this.reconciliationReportTime = reconciliationReportTime; }
    public String getRecipientsJson() { return recipientsJson; }
    public void setRecipientsJson(String recipientsJson) { this.recipientsJson = recipientsJson; }
    public String getEmailSubjectTemplate() { return emailSubjectTemplate; }
    public void setEmailSubjectTemplate(String emailSubjectTemplate) { this.emailSubjectTemplate = emailSubjectTemplate; }
    public String getEmailFooter() { return emailFooter; }
    public void setEmailFooter(String emailFooter) { this.emailFooter = emailFooter; }
    public String getCustomBranding() { return customBranding; }
    public void setCustomBranding(String customBranding) { this.customBranding = customBranding; }
    public LocalDateTime getCreatedDate() {return createdDate;}
    public void setCreatedDate(LocalDateTime createdDate) {this.createdDate = createdDate;}
    public LocalDateTime getUpdatedDate() {return updatedDate;}
    public void setUpdatedDate(LocalDateTime updatedDate) {this.updatedDate = updatedDate;}
    public String getWebhookSecretApiKey() {return webhookSecretApiKey;}
    public void setWebhookSecretApiKey(String webhookSecretApiKey) {this.webhookSecretApiKey = webhookSecretApiKey;}

    
}
