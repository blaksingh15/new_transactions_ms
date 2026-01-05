package com.orchetrtionMs.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.orchetrtionMs.model.Orchestration;

public interface OrchestrationRepository extends JpaRepository<Orchestration, Long> {
    Optional<Orchestration> findByRuleId(String ruleId);
    List<Orchestration> findByMerchantId(String merchantId);
    List<Orchestration> findByStatus(Orchestration.OrchestrationStatus status);
    List<Orchestration> findByMerchantIdAndStatus(String merchantId, Orchestration.OrchestrationStatus status);
    List<Orchestration> findByMerchantIdAndIsActiveTrue(String merchantId);
    List<Orchestration> findByMerchantIdOrderByPriorityAsc(String merchantId);
    List<Orchestration> findByMerchantIdAndStatusOrderByPriorityAsc(String merchantId, Orchestration.OrchestrationStatus status);
    boolean existsByRuleId(String ruleId);
    void deleteByRuleId(String ruleId);
    @Modifying
    @Query("UPDATE Orchestration o SET o.status = :status, o.isActive = :isActive, o.updatedAt = CURRENT_TIMESTAMP WHERE o.ruleId = :ruleId")
    void updateStatusByRuleId(@Param("ruleId") String ruleId, @Param("isActive") Boolean isActive, @Param("status") Orchestration.OrchestrationStatus status);
    
    @Modifying
    @Query("UPDATE Orchestration o SET o.lastModified = :lastModified, o.updatedAt = CURRENT_TIMESTAMP WHERE o.ruleId = :ruleId")
    void updateLastModifiedByRuleId(@Param("ruleId") String ruleId, @Param("lastModified") String lastModified);
    List<Orchestration> findByMerchantIdAndNameContainingIgnoreCase(String merchantId, String name);
    List<Orchestration> findByMerchantIdAndDescriptionContainingIgnoreCase(String merchantId, String description);
    long countByMerchantId(String merchantId);
    long countByMerchantIdAndStatus(String merchantId, Orchestration.OrchestrationStatus status);
    @Query("SELECT COALESCE(MAX(o.priority), 0) + 1 FROM Orchestration o WHERE o.merchantId = :merchantId")
    Integer findNextPriorityForMerchant(@Param("merchantId") String merchantId);
    List<Orchestration> findByMerchantIdAndVolumeRegulatoryPeriodIsNotNull(String merchantId);

}
