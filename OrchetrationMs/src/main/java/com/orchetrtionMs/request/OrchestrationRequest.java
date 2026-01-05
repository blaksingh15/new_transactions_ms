package com.orchetrtionMs.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orchetrtionMs.model.Orchestration.OrchestrationStatus;
public class OrchestrationRequest {
	    private String ruleId;
	    private String name;
	    private String description;
	    private String conditions;
	    private String actions;
	    private Integer priority;
	    private OrchestrationStatus status;
	    private String merchantId;
	    @JsonProperty("visible_to_merchant")
	    private Boolean visibleToMerchant = true;
	    @JsonProperty("merchant_access")
	    private String merchantAccess = "editable"; 
	    @JsonProperty("force_enabled")
	    private Boolean forceEnabled = false;
	    @JsonProperty("admin_notes")
	    private String adminNotes;
	    @JsonProperty("regulatory_request_field")
	    private String regulatoryRequestField;
	    @JsonProperty("volume_regulatory_period")
	    private String volumeRegulatoryPeriod = "1 Day";
	    @JsonProperty("max_successful_volume_amount")
	    private String maxSuccessfulVolumeAmount;
	    @JsonProperty("total_success_count")
	    private String totalSuccessCount;
	    @JsonProperty("total_failed_count")
	    private String totalFailedCount;
	    public OrchestrationRequest() {}
	    public OrchestrationRequest(String name, String description, String conditions, String actions, Integer priority, String merchantId) {
	        this.name = name;
	        this.description = description;
	        this.conditions = conditions;
	        this.actions = actions;
	        this.priority = priority;
	        this.merchantId = merchantId;
	        this.status = OrchestrationStatus.ENABLED;
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


}
