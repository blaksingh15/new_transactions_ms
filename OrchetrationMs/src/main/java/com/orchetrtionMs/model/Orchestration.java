package com.orchetrtionMs.model;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orchestration_rules")
public class Orchestration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", unique = true, nullable = false)
    private String ruleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "conditions", length = 1000, nullable = false)
    private String conditions;

    @Column(name = "actions", length = 1000, nullable = false)
    private String actions;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrchestrationStatus status;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_modified")
    private String lastModified;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "visible_to_merchant")
    @JsonProperty("visible_to_merchant")
    private Boolean visibleToMerchant = true;

    @Column(name = "merchant_access")
    @JsonProperty("merchant_access")
    private String merchantAccess;

    @Column(name = "force_enabled")
    @JsonProperty("force_enabled")
    private Boolean forceEnabled = false;

    @Column(name = "admin_notes")
    @JsonProperty("admin_notes")
    private String adminNotes;
    
    @Column(name = "regulatory_request_field")
    @JsonProperty("regulatory_request_field")
    private String regulatoryRequestField;
    
    @Column(name = "volume_regulatory_period", length = 50)
    @JsonProperty("volume_regulatory_period")
    private String volumeRegulatoryPeriod;
    
    @Column(name = "max_successful_volume_amount")
    @JsonProperty("max_successful_volume_amount")
    private String maxSuccessfulVolumeAmount;
    
    @Column(name = "total_success_count")
    @JsonProperty("total_success_count")
    private String totalSuccessCount;
    
    @Column(name = "total_failed_count")
    @JsonProperty("total_failed_count")
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