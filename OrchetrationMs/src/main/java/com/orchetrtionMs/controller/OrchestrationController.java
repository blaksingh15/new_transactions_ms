package com.orchetrtionMs.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.orchetrtionMs.model.Orchestration.OrchestrationStatus;
import com.orchetrtionMs.request.OrchestrationRequest;
import com.orchetrtionMs.response.OrchestrationResponse;
import com.orchetrtionMs.service.OrchestrationService;

@RestController
@RequestMapping("/api/orchestration/rules")
public class OrchestrationController {
	
	    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrchestrationController.class);
	    @Autowired
	    private OrchestrationService orchestrationService;
	    
	    @PostMapping
	    public ResponseEntity<Map<String, Object>> createRule( @RequestBody OrchestrationRequest request) {
	        log.info("Create rule request by user: {}");
	        OrchestrationResponse createdRule = orchestrationService.createRule(request);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", createdRule);
	        response.put("message", "Orchestration rule created successfully");
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    }
	    
	    @PostMapping("/test-conditions")
	    public ResponseEntity<Map<String, Object>> testRuleConditions( @RequestBody Map<String, Object> request) {
	        log.info("Test rule conditions request by user: {}");
	        String conditions = (String) request.get("conditions");
	        @SuppressWarnings("unchecked")
	        Map<String, Object> testData = (Map<String, Object>) request.get("testData");
	        Map<String, Object> result = orchestrationService.testRuleConditions(conditions, testData);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", result);
	        response.put("message", "Rule conditions tested successfully");
	        return ResponseEntity.ok(response);
	    }

	    @PostMapping("/validate")
	    public ResponseEntity<Map<String, Object>> validateRule(@RequestBody OrchestrationRequest request) {
	        log.info("Validate rule request by user: {}");
	        boolean isValid = orchestrationService.validateRuleConditions(request.getConditions());
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", Map.of("valid", isValid));
	        response.put("message", isValid ? "Rule validation successful" : "Rule validation failed");
	        return ResponseEntity.ok(response);
	    }

	    
	    @DeleteMapping("/{ruleId}")
	    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable String ruleId) {
	        log.info("Delete rule request by user: {} for ruleId: {}", ruleId);
	        orchestrationService.deleteRule(ruleId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "Orchestration rule deleted successfully");
	        return ResponseEntity.ok(response);
	    }
	    
	    @DeleteMapping("/bulk")
	    public ResponseEntity<Map<String, Object>> bulkDeleteRules( @RequestBody List<String> ruleIds) {
	        log.info("Bulk delete request by user: {}");
	        orchestrationService.bulkDeleteRules(ruleIds);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "Bulk delete completed successfully");
	        return ResponseEntity.ok(response);
	    }
	    
	    @PutMapping("/{ruleId}")
	    public ResponseEntity<Map<String, Object>> updateRule( @PathVariable String ruleId,@RequestBody OrchestrationRequest request) {
	        log.info("Update rule request by user: {} for ruleId: {}", ruleId);
	        OrchestrationResponse updatedRule=orchestrationService.updateRule(ruleId, request);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", updatedRule);
	        response.put( "message",updatedRule.getName() + " (" + updatedRule.getRuleId() + ") updated successfully.");
	        return ResponseEntity.ok(response);
	    }
	    
	    @PutMapping("/bulk")
	    public ResponseEntity<Map<String, Object>> bulkUpdateRules( @RequestBody List<OrchestrationRequest> requests) {
	        log.info("Bulk update request by user: {}");
	        List<OrchestrationResponse> updatedRules = orchestrationService.bulkUpdateRules(requests);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", updatedRules);
	        response.put("message", "Bulk update completed successfully");
	        return ResponseEntity.ok(response);
	    }
	    
	    
	    @PatchMapping("/{ruleId}/toggle")
	    public ResponseEntity<Map<String, Object>> toggleRuleStatus( @PathVariable String ruleId) {
	        log.info("Toggle rule status request by user: {} for ruleId: {}", ruleId);
	        OrchestrationResponse updatedRule = orchestrationService.toggleRuleStatus(ruleId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", updatedRule);
	        response.put("message", "Orchestration rule status toggled successfully");
	        return ResponseEntity.ok(response);
	    }

	    @PatchMapping("/{ruleId}/enable")
	    public ResponseEntity<Map<String, Object>> enableRule( @PathVariable String ruleId) {
	        log.info("Enable rule request by user: {} for ruleId: {}", ruleId);
	        OrchestrationResponse updatedRule =orchestrationService.enableRule(ruleId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", updatedRule);
	        response.put("message", "Orchestration rule enabled successfully");
	        return ResponseEntity.ok(response);
	    }
	    
	    @PatchMapping("/{ruleId}/disable")
	    public ResponseEntity<Map<String, Object>> disableRule( @PathVariable String ruleId) {
	        log.info("Disable rule request by user: {} for ruleId: {}", ruleId);
	        OrchestrationResponse updatedRule = orchestrationService.disableRule(ruleId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", updatedRule);
	        response.put("message", "Orchestration rule disabled successfully");
	        return ResponseEntity.ok(response);
	    }


	   
	    @GetMapping("/all")
	    public ResponseEntity<Map<String, Object>> getAllRules() {
	        log.info("Access request by user: {}");
	        List<OrchestrationResponse> rules = orchestrationService.getAllRules();
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", rules);
	        response.put("message", "Orchestration rules retrieved successfully");
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping("/merchant/{merchantId}")
	    public ResponseEntity<Map<String, Object>> getRulesByMerchantId(@PathVariable String merchantId) {
	        log.info("Access request by user: {} for merchant: {}", merchantId);
	        List<OrchestrationResponse> rules = orchestrationService.getRulesByMerchantId(merchantId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", rules);
	        response.put( "message","Orchestration rules retrieved successfully for merchant: " + merchantId);
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping("/{ruleId}")
	    public ResponseEntity<Map<String, Object>> getRuleByRuleId(@PathVariable String ruleId) {
	        log.info("Access request by user: {} for ruleId: {}", ruleId);
	        OrchestrationResponse rule = orchestrationService.getRuleByRuleId(ruleId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", rule);
	        response.put("message", "Orchestration rule retrieved successfully");
	        return ResponseEntity.ok(response);
	    }

	   
	  
	    @GetMapping("/merchant/{merchantId}/search")
	    public ResponseEntity<Map<String, Object>> searchRules( @PathVariable String merchantId, @RequestParam String searchTerm) {
	        log.info("Search rules request by user: {} for merchantId: {}, searchTerm: {}", merchantId, searchTerm);
	        List<OrchestrationResponse> rules = orchestrationService.searchRulesByMerchantId(merchantId, searchTerm);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", rules);
	        response.put("message", "Orchestration rules search completed successfully");
	        return ResponseEntity.ok(response);
	    }


	    @GetMapping("/merchant/{merchantId}/status/{status}")
	    public ResponseEntity<Map<String, Object>> getRulesByStatus( @PathVariable String merchantId,@PathVariable String status) {
	        log.info("Get rules by status request by user: {} for merchantId: {}, status: {}", merchantId, status);
	        OrchestrationStatus orchestrationStatus = OrchestrationStatus.valueOf(status.toUpperCase());
	        List<OrchestrationResponse> rules = orchestrationService.getRulesByStatus(merchantId, orchestrationStatus);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", rules);
	        response.put("message", "Orchestration rules retrieved by status successfully");
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping("/merchant/{merchantId}/stats")
	    public ResponseEntity<Map<String, Object>> getRuleStatistics( @PathVariable String merchantId) {
	        log.info("Get rule statistics request by user: {} for merchantId: {}", merchantId);
	        Map<String, Object> stats = orchestrationService.getRuleStatistics(merchantId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", stats);
	        response.put("message", "Orchestration rule statistics retrieved successfully");
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping("/test/volume-regulatory")
	    public ResponseEntity<Map<String, Object>> testVolumeRegulatoryFields() {
	        log.info("Testing volume regulatory fields request by user: {}");
	        OrchestrationRequest testRequest = new OrchestrationRequest();
	        testRequest.setName("Test Volume Regulatory Rule");
	        testRequest.setDescription("Test rule for volume regulatory fields");
	        testRequest.setConditions("bill_amt > 100");
	        testRequest.setActions("block: Test Block");
	        testRequest.setPriority(1);
	        testRequest.setMerchantId("test-merchant");
	        testRequest.setVolumeRegulatoryPeriod("7 Days");
	        testRequest.setMaxSuccessfulVolumeAmount("10000");
	        testRequest.setTotalSuccessCount("25");
	        testRequest.setTotalFailedCount("5");
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "Volume regulatory fields test completed");
	        response.put("testRequest", testRequest);
	        return ResponseEntity.ok(response);
	    }
 
}
