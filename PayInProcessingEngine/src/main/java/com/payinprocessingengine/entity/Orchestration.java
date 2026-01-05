package com.payinprocessingengine.entity;
import java.time.LocalDateTime;
public class Orchestration {
    private Long id;
    private String ruleId;
    private String name;
    private String description;
    private String conditions;
    private String actions;
    private Integer priority;
    private OrchestrationStatus status;
    private String merchantId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String lastModified;
    private Boolean isActive = true;
    private Boolean visibleToMerchant = true;
    private String merchantAccess;
    private Boolean forceEnabled = false;
    private String adminNotes;
    private String regulatoryRequestField;
    private String volumeRegulatoryPeriod;
    private String maxSuccessfulVolumeAmount;
    private String totalSuccessCount;
    private String totalFailedCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public OrchestrationStatus getStatus() {
        return status;
    }

    public void setStatus(OrchestrationStatus status) {
        this.status = status;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getVisibleToMerchant() {
        return visibleToMerchant;
    }

    public void setVisibleToMerchant(Boolean visibleToMerchant) {
        this.visibleToMerchant = visibleToMerchant;
    }

    public String getMerchantAccess() {
        return merchantAccess;
    }

    public void setMerchantAccess(String merchantAccess) {
        this.merchantAccess = merchantAccess;
    }

    public Boolean getForceEnabled() {
        return forceEnabled;
    }

    public void setForceEnabled(Boolean forceEnabled) {
        this.forceEnabled = forceEnabled;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getRegulatoryRequestField() {
        return regulatoryRequestField;
    }

    public void setRegulatoryRequestField(String regulatoryRequestField) {
        this.regulatoryRequestField = regulatoryRequestField;
    }

    public String getVolumeRegulatoryPeriod() {
        return volumeRegulatoryPeriod;
    }

    public void setVolumeRegulatoryPeriod(String volumeRegulatoryPeriod) {
        this.volumeRegulatoryPeriod = volumeRegulatoryPeriod;
    }

    public String getMaxSuccessfulVolumeAmount() {
        return maxSuccessfulVolumeAmount;
    }

    public void setMaxSuccessfulVolumeAmount(String maxSuccessfulVolumeAmount) {
        this.maxSuccessfulVolumeAmount = maxSuccessfulVolumeAmount;
    }

    public String getTotalSuccessCount() {
        return totalSuccessCount;
    }

    public void setTotalSuccessCount(String totalSuccessCount) {
        this.totalSuccessCount = totalSuccessCount;
    }

    public String getTotalFailedCount() {
        return totalFailedCount;
    }

    public void setTotalFailedCount(String totalFailedCount) {
        this.totalFailedCount = totalFailedCount;
    }

    public enum OrchestrationStatus {
        ENABLED,
        DISABLED
    }

    
} 