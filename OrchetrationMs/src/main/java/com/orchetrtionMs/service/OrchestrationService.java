package com.orchetrtionMs.service;
import java.util.List;
import java.util.Map;
import com.orchetrtionMs.model.Orchestration.OrchestrationStatus;
import com.orchetrtionMs.request.OrchestrationRequest;
import com.orchetrtionMs.response.OrchestrationResponse;
public interface OrchestrationService {
    List<OrchestrationResponse> getAllRules();
    List<OrchestrationResponse> getRulesByMerchantId(String merchantId);
    OrchestrationResponse getRuleByRuleId(String ruleId);
    OrchestrationResponse createRule(OrchestrationRequest request);
    OrchestrationResponse updateRule(String ruleId, OrchestrationRequest request);
    void deleteRule(String ruleId);
    OrchestrationResponse toggleRuleStatus(String ruleId);
    OrchestrationResponse enableRule(String ruleId);
    OrchestrationResponse disableRule(String ruleId);
    List<OrchestrationResponse> searchRulesByMerchantId(String merchantId, String searchTerm);
    List<OrchestrationResponse> getRulesByStatus(String merchantId, OrchestrationStatus status);
    List<OrchestrationResponse> getActiveRulesByMerchantId(String merchantId);
    OrchestrationResponse updateRulePriority(String ruleId, Integer newPriority);
    List<OrchestrationResponse> reorderPriorities(String merchantId, List<String> ruleIds);
    Map<String, Object> getRuleStatistics(String merchantId);
    long getRuleCountByMerchantId(String merchantId);
    long getRuleCountByStatus(String merchantId, OrchestrationStatus status);
    boolean validateRuleConditions(String conditions);
    Map<String, Object> testRuleConditions(String conditions, Map<String, Object> testData);
    List<OrchestrationResponse> bulkUpdateRules(List<OrchestrationRequest> requests);
    void bulkDeleteRules(List<String> ruleIds);
    String generateRuleId();
    Integer getNextPriorityForMerchant(String merchantId);
    boolean isRuleIdExists(String ruleId);

}
