package com.blacklist.controller;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.blacklist.entity.BlackList;
import com.blacklist.service.BlackListService;
@RestController
@RequestMapping("/api/blacklist")
public class BlackListController {

    private final BlackListService blackListService;

    public BlackListController(BlackListService blackListService) {
        this.blackListService = blackListService;
    }
    
    public record ApiResponse<T>(T data, String message, boolean success) {}

    @PostMapping
    public ResponseEntity<ApiResponse<BlackList>> createBlacklist( @RequestBody BlackList blackList) {
        boolean isDuplicate = blackListService.findByClientId(blackList.getClientId()).stream()
            .anyMatch(existing ->
                Objects.equals(existing.getBlacklistType(), blackList.getBlacklistType()) &&
                Objects.equals(existing.getCondition(), blackList.getCondition()) &&
                existing.getStatus() == 1 && blackList.getStatus() == 1
            );

        if (isDuplicate) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,
                            "A blacklist entry with the same Client ID, Type and Condition already exists",
                            false));
        }

        BlackList saved = blackListService.save(blackList);
        return ResponseEntity.ok(new ApiResponse<>(saved, "Blacklist created successfully", true));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlackList>> updateBlacklist(@PathVariable Integer id,
             @RequestBody BlackList updatedBlacklist) {
        BlackList existing = blackListService.findById(id);
        if (existing == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Record not found with id: " + id, false));
        }

        existing.setClientId(updatedBlacklist.getClientId());
        existing.setBlacklistType(updatedBlacklist.getBlacklistType());
        existing.setBlacklistValue(updatedBlacklist.getBlacklistValue());
        existing.setCondition(updatedBlacklist.getCondition());
        existing.setConnectorId(updatedBlacklist.getConnectorId());
        existing.setRemarks(updatedBlacklist.getRemarks());
        existing.setStatus(updatedBlacklist.getStatus());

        BlackList saved = blackListService.save(existing);
        return ResponseEntity.ok(new ApiResponse<>(saved, "Record updated successfully", true));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlacklist(@PathVariable Integer id) {
        blackListService.delete(id); 
        return ResponseEntity.ok(new ApiResponse<>(null, "Record deleted successfully", true));
     }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BlackList>>> getAllBlacklists() {
        List<BlackList> all = blackListService.findAllByOrderByIdDesc();
        return ResponseEntity.ok(new ApiResponse<>(all, "Fetched all blacklists", true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlackList>> getBlacklistById(@PathVariable Integer id) {
        BlackList blackList = blackListService.findById(id);
        if (blackList == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Blacklist not found with id: " + id, false));
        }
        return ResponseEntity.ok(new ApiResponse<>(blackList, "Blacklist fetched successfully", true));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<BlackList>>> getBlacklistsByClientId(@PathVariable Integer clientId) {
        List<BlackList> blacklists = blackListService.findByClientId(clientId);
        return ResponseEntity.ok(new ApiResponse<>(blacklists, "Fetched blacklists for clientId: " + clientId, true));
    }

    @GetMapping("/clients")
    public ResponseEntity<ApiResponse<List<BlackList>>> getBlacklistsByClientIds(@RequestParam String clientIds) {
        List<BlackList> allBlacklists = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(idStr -> {
                    try {
                        return Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(clientId -> blackListService.findByClientId(clientId).stream())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(allBlacklists, "Fetched blacklists for multiple clientIds", true));
    }
 
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkBlacklist(@RequestParam Integer clientId,
            @RequestParam String type, @RequestParam String value) {
        boolean isBlacklisted = blackListService.isBlacklisted(clientId, type, value);
        return ResponseEntity.ok(new ApiResponse<>(isBlacklisted, "Blacklist check performed", true));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BlackList>>> searchBlacklists(@RequestParam(required = false) String type,@RequestParam(required = false) String value, @RequestParam(required = false) String condition) {
        List<BlackList> results = blackListService.findByTypeAndValue(type, value, condition);
        return ResponseEntity.ok(new ApiResponse<>(results, "Search completed", true));
    }
    
    
    
}
