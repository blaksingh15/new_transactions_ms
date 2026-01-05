package com.orchetrtionMs.service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orchetrtionMs.model.Orchestration;
import com.orchetrtionMs.model.Orchestration.OrchestrationStatus;
import com.orchetrtionMs.repository.OrchestrationRepository;
import com.orchetrtionMs.request.OrchestrationRequest;
import com.orchetrtionMs.response.OrchestrationResponse;
@Service
@Transactional
public class OrchestrationServiceImpl implements OrchestrationService {
	 private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrchestrationServiceImpl.class);

	    @Autowired
	    private OrchestrationRepository orchestrationDao;

	    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	    private static final Set<String> generatedIds = ConcurrentHashMap.newKeySet();
	    private static final Random random = new Random();

	    @Override
	    public List<OrchestrationResponse> getAllRules() {
	        try {
	            List<Orchestration> rules = orchestrationDao.findAll();
	            return rules.stream().map(this::convertToResponse).collect(Collectors.toList());
	        } catch (Exception e) {
	            log.error("Error fetching all rules: {}", e.getMessage());
	            throw new RuntimeException("Failed to fetch orchestration rules", e);
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> getRulesByMerchantId(String merchantId) {
	        try {
	            List<Orchestration> rules = orchestrationDao.findByMerchantIdOrderByPriorityAsc(merchantId);
	            return rules.stream().map(this::convertToResponse).collect(Collectors.toList());
	        } catch (Exception e) {
	            log.error("Error fetching rules for merchant {}: {}", merchantId, e.getMessage());
	            throw new RuntimeException("Failed to fetch orchestration rules for merchant", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse getRuleByRuleId(String ruleId) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (rule.isPresent()) {
	                return convertToResponse(rule.get());
	            } else {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	        } catch (Exception e) {
	            log.error("Error fetching rule with ID {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to fetch orchestration rule", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse createRule(OrchestrationRequest request) {
	        try {
	            log.info("=== CREATE RULE REQUEST DEBUG ===");
	            validateOrchestrationRequest(request);
	            String ruleId = request.getRuleId();
	            if (ruleId == null || ruleId.trim().isEmpty()) {
	                ruleId = generateRuleId();
	            }
	            if (orchestrationDao.existsByRuleId(ruleId)) {
	                throw new RuntimeException("Rule ID already exists: " + ruleId);
	            }
	            if (request.getPriority() == null) {
	                request.setPriority(getNextPriorityForMerchant(request.getMerchantId()));
	            }
	            Orchestration orchestration = new Orchestration();
	            orchestration.setRuleId(ruleId);
	            orchestration.setName(request.getName());
	            orchestration.setDescription(request.getDescription());
	            orchestration.setConditions(request.getConditions());
	            orchestration.setActions(request.getActions());
	            orchestration.setPriority(request.getPriority());
	            orchestration.setStatus(request.getStatus() != null ? request.getStatus() : OrchestrationStatus.ENABLED);
	            orchestration.setMerchantId(request.getMerchantId());
	            // Set admin rule management fields
	            orchestration.setVisibleToMerchant(request.getVisibleToMerchant() != null ? request.getVisibleToMerchant() : true);
	            orchestration.setMerchantAccess(request.getMerchantAccess() != null ? request.getMerchantAccess() : "editable");
	            orchestration.setForceEnabled(request.getForceEnabled() != null ? request.getForceEnabled() : false);
	            orchestration.setAdminNotes(request.getAdminNotes());
	            // Set volume regulatory fields
	            orchestration.setRegulatoryRequestField(request.getRegulatoryRequestField() != null && !request.getRegulatoryRequestField().trim().isEmpty() && !"null".equals(request.getRegulatoryRequestField()) ? request.getRegulatoryRequestField() : "bill_email");
	            orchestration.setVolumeRegulatoryPeriod(request.getVolumeRegulatoryPeriod() != null && !request.getVolumeRegulatoryPeriod().trim().isEmpty() && !"null".equals(request.getVolumeRegulatoryPeriod()) ? request.getVolumeRegulatoryPeriod() : "1 Day");
	            orchestration.setMaxSuccessfulVolumeAmount(request.getMaxSuccessfulVolumeAmount() != null && !request.getMaxSuccessfulVolumeAmount().trim().isEmpty() && !"null".equals(request.getMaxSuccessfulVolumeAmount()) ? request.getMaxSuccessfulVolumeAmount() : null);
	            orchestration.setTotalSuccessCount(request.getTotalSuccessCount() != null && !request.getTotalSuccessCount().trim().isEmpty() && !"null".equals(request.getTotalSuccessCount()) ? request.getTotalSuccessCount() : null);
	            orchestration.setTotalFailedCount(request.getTotalFailedCount() != null && !request.getTotalFailedCount().trim().isEmpty() && !"null".equals(request.getTotalFailedCount()) ? request.getTotalFailedCount() : null);
	            // Debug logging
	            log.info("Creating rule with admin fields: visibleToMerchant={}, merchantAccess={}, forceEnabled={}, adminNotes={}", 
	            request.getVisibleToMerchant(), request.getMerchantAccess(), request.getForceEnabled(), request.getAdminNotes());
	            log.info("Creating rule with volume regulatory fields: regulatoryRequestField={}, volumeRegulatoryPeriod={}, maxSuccessfulVolumeAmount={}, totalSuccessCount={}, totalFailedCount={}", 
	            request.getRegulatoryRequestField(), request.getVolumeRegulatoryPeriod(), request.getMaxSuccessfulVolumeAmount(), request.getTotalSuccessCount(), request.getTotalFailedCount());
	            orchestration.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            orchestration.setIsActive(true);
	            Orchestration savedRule = orchestrationDao.save(orchestration);
	            log.info("Created orchestration rule with ID: {}", savedRule.getRuleId());
	            return convertToResponse(savedRule);
	        } catch (Exception e) {
	            log.error("Error creating orchestration rule: {}", e.getMessage());
	            throw new RuntimeException("Failed to create orchestration rule", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse updateRule(String ruleId, OrchestrationRequest request) {
	        try {
	            log.info("=== UPDATE RULE REQUEST DEBUG ===");	            
	            Optional<Orchestration> existingRule = orchestrationDao.findByRuleId(ruleId);
	            if (!existingRule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            Orchestration rule = existingRule.get();
	            if (request.getName() != null) rule.setName(request.getName());
	            if (request.getDescription() != null) rule.setDescription(request.getDescription());
	            if (request.getConditions() != null) rule.setConditions(request.getConditions());
	            if (request.getActions() != null) rule.setActions(request.getActions());
	            if (request.getPriority() != null) rule.setPriority(request.getPriority());
	            if (request.getStatus() != null) rule.setStatus(request.getStatus());
	            if (request.getMerchantId() != null) rule.setMerchantId(request.getMerchantId());
	            rule.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            rule.setUpdatedAt(LocalDateTime.now());
	            rule.setVisibleToMerchant(request.getVisibleToMerchant() != null ? request.getVisibleToMerchant() : true);
	            rule.setMerchantAccess(request.getMerchantAccess() != null ? request.getMerchantAccess() : "editable");
	            rule.setForceEnabled(request.getForceEnabled() != null ? request.getForceEnabled() : false);
	            rule.setAdminNotes(request.getAdminNotes());
	            String regulatoryField = request.getRegulatoryRequestField();
	            if (regulatoryField == null || regulatoryField.trim().isEmpty() || "null".equals(regulatoryField)) {
	                rule.setRegulatoryRequestField(null);
	            } else {
	                rule.setRegulatoryRequestField(regulatoryField);
	            }
	            String volumePeriod = request.getVolumeRegulatoryPeriod();
	            if (volumePeriod == null || volumePeriod.trim().isEmpty() || "null".equals(volumePeriod) || "0".equals(volumePeriod) || "1 Day".equals(volumePeriod)) {
	                rule.setVolumeRegulatoryPeriod(null);
	            } else {
	                rule.setVolumeRegulatoryPeriod(volumePeriod);
	            }
	            String maxVolume = request.getMaxSuccessfulVolumeAmount();
	            if (maxVolume == null || maxVolume.trim().isEmpty() || "null".equals(maxVolume)) {
	                rule.setMaxSuccessfulVolumeAmount(null);
	            } else {
	                rule.setMaxSuccessfulVolumeAmount(maxVolume);
	            }
	            String successCount = request.getTotalSuccessCount();
	            if (successCount == null || successCount.trim().isEmpty() || "null".equals(successCount)) {
	                rule.setTotalSuccessCount(null);
	            } else {
	                rule.setTotalSuccessCount(successCount);
	            }
	            String failedCount = request.getTotalFailedCount();
	            if (failedCount == null || failedCount.trim().isEmpty() || "null".equals(failedCount)) {
	                rule.setTotalFailedCount(null);
	            } else {
	                rule.setTotalFailedCount(failedCount);
	            }
	            log.info("Updating admin fields for rule {}: visibleToMerchant={}, merchantAccess={}, forceEnabled={}, adminNotes={}", 
	            ruleId, request.getVisibleToMerchant(), request.getMerchantAccess(), request.getForceEnabled(), request.getAdminNotes());
	            log.info("Updating volume regulatory fields for rule {}: regulatoryRequestField={}, volumeRegulatoryPeriod={}, maxSuccessfulVolumeAmount={}, totalSuccessCount={}, totalFailedCount={}", 
	            ruleId, request.getRegulatoryRequestField(), request.getVolumeRegulatoryPeriod(), request.getMaxSuccessfulVolumeAmount(), request.getTotalSuccessCount(), request.getTotalFailedCount());
	            Orchestration updatedRule = orchestrationDao.save(rule);
	            log.info("Updated orchestration rule with ID: {}", updatedRule.getRuleId());
	            return convertToResponse(updatedRule);
	        } catch (Exception e) {
	            log.error("Error updating orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to update orchestration rule", e);
	        }
	    }

	    @Override
	    public void deleteRule(String ruleId) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (!rule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            orchestrationDao.deleteByRuleId(ruleId);
	            log.info("Deleted orchestration rule with ID: {}", ruleId);
	        } catch (Exception e) {
	            log.error("Error deleting orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to delete orchestration rule", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse toggleRuleStatus(String ruleId) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (!rule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            Orchestration orchestration = rule.get();
	            OrchestrationStatus newStatus = orchestration.getStatus() == OrchestrationStatus.ENABLED ? OrchestrationStatus.DISABLED : OrchestrationStatus.ENABLED;
	            Boolean isActive = "ENABLED".equals(newStatus.toString())? true : false;
	            orchestrationDao.updateStatusByRuleId(ruleId, isActive, newStatus);
	            orchestration.setIsActive(isActive);
	            orchestration.setStatus(newStatus);
	            orchestration.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            log.info("Toggled status for orchestration rule {} to {}", ruleId, newStatus);
	            return convertToResponse(orchestration);
	        } catch (Exception e) {
	            log.error("Error toggling status for orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to toggle orchestration rule status", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse enableRule(String ruleId) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (!rule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            Boolean isActive = true;
	            orchestrationDao.updateStatusByRuleId(ruleId, isActive, OrchestrationStatus.ENABLED);
	            Orchestration orchestration = rule.get();
	            orchestration.setStatus(OrchestrationStatus.ENABLED);
	            orchestration.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            log.info("Enabled orchestration rule with ID: {}", ruleId);
	            return convertToResponse(orchestration);
	        } catch (Exception e) {
	            log.error("Error enabling orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to enable orchestration rule", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse disableRule(String ruleId) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (!rule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            Boolean isActive = false;
	            orchestrationDao.updateStatusByRuleId(ruleId, isActive, OrchestrationStatus.DISABLED);
	            Orchestration orchestration = rule.get();
	            orchestration.setStatus(OrchestrationStatus.DISABLED);
	            orchestration.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            log.info("Disabled orchestration rule with ID: {}", ruleId);
	            return convertToResponse(orchestration);
	        } catch (Exception e) {
	            log.error("Error disabling orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to disable orchestration rule", e);
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> searchRulesByMerchantId(String merchantId, String searchTerm) {
	        try {
	            List<Orchestration> rules = new ArrayList<>();
	            rules.addAll(orchestrationDao.findByMerchantIdAndNameContainingIgnoreCase(merchantId, searchTerm));
	     	    rules.addAll(orchestrationDao.findByMerchantIdAndDescriptionContainingIgnoreCase(merchantId, searchTerm));
	            return rules.stream().distinct().sorted((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority())).map(this::convertToResponse).collect(Collectors.toList());
	        } catch (Exception e) {
	            log.error("Error searching rules for merchant {} with term {}: {}", merchantId, searchTerm, e.getMessage());
	            throw new RuntimeException("Failed to search orchestration rules", e);
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> getRulesByStatus(String merchantId, OrchestrationStatus status) {
	        try {
	            List<Orchestration> rules = orchestrationDao.findByMerchantIdAndStatusOrderByPriorityAsc(merchantId, status);
	            return rules.stream().map(this::convertToResponse).collect(Collectors.toList());
	        } catch (Exception e) {
	            log.error("Error fetching rules for merchant {} with status {}: {}", merchantId, status, e.getMessage());
	            throw new RuntimeException("Failed to fetch orchestration rules by status", e);
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> getActiveRulesByMerchantId(String merchantId) {
	        try {
	            List<Orchestration> rules = orchestrationDao.findByMerchantIdAndIsActiveTrue(merchantId);
	            return rules.stream().map(this::convertToResponse).collect(Collectors.toList());
	        } catch (Exception e) {
	            log.error("Error fetching active rules for merchant {}: {}", merchantId, e.getMessage());
	            throw new RuntimeException("Failed to fetch active orchestration rules", e);
	        }
	    }

	    @Override
	    public OrchestrationResponse updateRulePriority(String ruleId, Integer newPriority) {
	        try {
	            Optional<Orchestration> rule = orchestrationDao.findByRuleId(ruleId);
	            if (!rule.isPresent()) {
	                throw new RuntimeException("Orchestration rule not found with ID: " + ruleId);
	            }
	            Orchestration orchestration = rule.get();
	            orchestration.setPriority(newPriority);
	            orchestration.setLastModified(LocalDateTime.now().format(DATE_FORMATTER));
	            orchestration.setUpdatedAt(LocalDateTime.now());
	            Orchestration updatedRule = orchestrationDao.save(orchestration);
	            log.info("Updated priority for orchestration rule {} to {}", ruleId, newPriority);
	            return convertToResponse(updatedRule);
	        } catch (Exception e) {
	            log.error("Error updating priority for orchestration rule {}: {}", ruleId, e.getMessage());
	            throw new RuntimeException("Failed to update orchestration rule priority", e);
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> reorderPriorities(String merchantId, List<String> ruleIds) {
	        try {
	            List<OrchestrationResponse> updatedRules = new ArrayList<>();
	            for (int i = 0; i < ruleIds.size(); i++) {
	                String ruleId = ruleIds.get(i);
	                int newPriority = i + 1;
	                OrchestrationResponse updatedRule = updateRulePriority(ruleId, newPriority);
	                updatedRules.add(updatedRule);
	            }
	            log.info("Reordered priorities for {} rules for merchant {}", ruleIds.size(), merchantId);
	            return updatedRules;
	        } catch (Exception e) {
	            log.error("Error reordering priorities for merchant {}: {}", merchantId, e.getMessage());
	            throw new RuntimeException("Failed to reorder orchestration rule priorities", e);
	        }
	    }

	    @Override
	    public Map<String, Object> getRuleStatistics(String merchantId) {
	        try {
	            Map<String, Object> stats = new HashMap<>();
	            long totalRules = orchestrationDao.countByMerchantId(merchantId);
	            long enabledRules = orchestrationDao.countByMerchantIdAndStatus(merchantId, OrchestrationStatus.ENABLED);
	            long disabledRules = orchestrationDao.countByMerchantIdAndStatus(merchantId, OrchestrationStatus.DISABLED);
	            stats.put("total", totalRules);
	            stats.put("enabled", enabledRules);
	            stats.put("disabled", disabledRules);
	            stats.put("activePercentage", totalRules > 0 ? (double) enabledRules / totalRules * 100 : 0);
	            return stats;
	        } catch (Exception e) {
	            log.error("Error getting statistics for merchant {}: {}", merchantId, e.getMessage());
	            throw new RuntimeException("Failed to get orchestration rule statistics", e);
	        }
	    }

	    @Override
	    public long getRuleCountByMerchantId(String merchantId) {
	        try {
	            return orchestrationDao.countByMerchantId(merchantId);
	        } catch (Exception e) {
	            log.error("Error counting rules for merchant {}: {}", merchantId, e.getMessage());
	            throw new RuntimeException("Failed to count orchestration rules", e);
	        }
	    }

	    @Override
	    public long getRuleCountByStatus(String merchantId, OrchestrationStatus status) {
	        try {
	            return orchestrationDao.countByMerchantIdAndStatus(merchantId, status);
	        } catch (Exception e) {
	            log.error("Error counting rules for merchant {} with status {}: {}", merchantId, status, e.getMessage());
	            throw new RuntimeException("Failed to count orchestration rules by status", e);
	        }
	    }

	    @Override
	    public boolean validateRuleConditions(String conditions) {
	        try {
	            if (conditions == null || conditions.trim().isEmpty()) {
	                return false;
	            }
	            return true;
	        } catch (Exception e) {
	            log.error("Error validating rule conditions: {}", e.getMessage());
	            return false;
	        }
	    }

	    @Override
	    public Map<String, Object> testRuleConditions(String conditions, Map<String, Object> testData) {
	        try {
	            Map<String, Object> result = new HashMap<>();
	            boolean matches = validateRuleConditions(conditions);
	            result.put("matches", matches);
	            result.put("details", matches ? "Conditions are valid" : "Conditions are invalid");
	            result.put("testData", testData);
	            return result;
	        } catch (Exception e) {
	            log.error("Error testing rule conditions: {}", e.getMessage());
	            Map<String, Object> result = new HashMap<>();
	            result.put("matches", false);
	            result.put("details", "Error testing conditions: " + e.getMessage());
	            return result;
	        }
	    }

	    @Override
	    public List<OrchestrationResponse> bulkUpdateRules(List<OrchestrationRequest> requests) {
	        try {
	            List<OrchestrationResponse> updatedRules = new ArrayList<>();
	            
	            for (OrchestrationRequest request : requests) {
	                if (request.getRuleId() != null) {
	                    OrchestrationResponse updatedRule = updateRule(request.getRuleId(), request);
	                    updatedRules.add(updatedRule);
	                } else {
	                    OrchestrationResponse createdRule = createRule(request);
	                    updatedRules.add(createdRule);
	                }
	            }
	            
	            log.info("Bulk updated {} orchestration rules", updatedRules.size());
	            return updatedRules;
	        } catch (Exception e) {
	            log.error("Error bulk updating orchestration rules: {}", e.getMessage());
	            throw new RuntimeException("Failed to bulk update orchestration rules", e);
	        }
	    }

	    @Override
	    public void bulkDeleteRules(List<String> ruleIds) {
	        try {
	            for (String ruleId : ruleIds) {
	                deleteRule(ruleId);
	            }
	            
	            log.info("Bulk deleted {} orchestration rules", ruleIds.size());
	        } catch (Exception e) {
	            log.error("Error bulk deleting orchestration rules: {}", e.getMessage());
	            throw new RuntimeException("Failed to bulk delete orchestration rules", e);
	        }
	    }

	    @Override
	    public String generateRuleId() {
	        String ruleId;
	        do {
	            int number = random.nextInt(10000); 
	            String formattedNumber = String.format("%04d", number); 
	            ruleId = "ORG-" + formattedNumber;
	        } while (!generatedIds.add(ruleId));
	        return ruleId;
	    }

	    @Override
	    public Integer getNextPriorityForMerchant(String merchantId) {
	        try {
	            Integer nextPriority = orchestrationDao.findNextPriorityForMerchant(merchantId);
	            return nextPriority != null ? nextPriority : 1;
	        } catch (Exception e) {
	            log.error("Error getting next priority for merchant {}: {}", merchantId, e.getMessage());
	            return 1;
	        }
	    }

	    @Override
	    public boolean isRuleIdExists(String ruleId) {
	        try {
	            return orchestrationDao.existsByRuleId(ruleId);
	        } catch (Exception e) {
	            log.error("Error checking if rule ID exists {}: {}", ruleId, e.getMessage());
	            return false;
	        }
	    }

	    private OrchestrationResponse convertToResponse(Orchestration orchestration) {
	        OrchestrationResponse response = new OrchestrationResponse(
	            orchestration.getId(),
	            orchestration.getRuleId(),
	            orchestration.getName(),
	            orchestration.getDescription(),
	            orchestration.getConditions(),
	            orchestration.getActions(),
	            orchestration.getPriority(),
	            orchestration.getStatus(),
	            orchestration.getMerchantId(),
	            orchestration.getCreatedAt(),
	            orchestration.getUpdatedAt(),
	            orchestration.getLastModified(),
	            orchestration.getIsActive(),
	            orchestration.getVisibleToMerchant(),
	            orchestration.getMerchantAccess(),
	            orchestration.getForceEnabled(),
	            orchestration.getAdminNotes(),
	            orchestration.getRegulatoryRequestField(),
	            orchestration.getVolumeRegulatoryPeriod(),
	            orchestration.getMaxSuccessfulVolumeAmount(),
	            orchestration.getTotalSuccessCount(),
	            orchestration.getTotalFailedCount()
	        );
	        	        
	        return response;
	    }

	    private void validateOrchestrationRequest(OrchestrationRequest request) {
	        if (request.getName() == null || request.getName().trim().isEmpty()) {
	            throw new RuntimeException("Rule name is required");
	        }
	        
	        if (request.getConditions() == null || request.getConditions().trim().isEmpty()) {
	            throw new RuntimeException("Rule conditions are required");
	        }
	        
	        if (request.getActions() == null || request.getActions().trim().isEmpty()) {
	            throw new RuntimeException("Rule actions are required");
	        }
	        
	        if (request.getMerchantId() == null || request.getMerchantId().trim().isEmpty()) {
	            throw new RuntimeException("Merchant ID is required");
	        }
	    }
	

}
