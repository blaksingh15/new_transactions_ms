package com.transactions.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import com.transactions.entity.Transaction;
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>  {
	    @NonNull
	    List<Transaction> findAll();
	    List<Transaction> findAllByOrderByTransactionDateDesc();
	    List<Transaction> findByMerchantIDOrderByTransactionDateDesc(Long merchantID);
	    Transaction findByTransID(Long transID);
	    Transaction findByReferenceAndMerchantID(String reference, Long merchantID);
	    long countByTransactionStatus(short status);
	    Long countByTransactionStatusAndMerchantID(Short transactionStatus, Long merchantID);
	    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionStatus = :transactionStatus AND t.merchantID = :merchantID AND t.transactionDate BETWEEN :startDate AND :endDate")
	    Long countByTransactionStatusAndMerchantIDAndDateRange(@Param("transactionStatus") Short transactionStatus,@Param("merchantID") Long merchantID,@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate );
	    Page<Transaction> findAll(Pageable pageable);
	    Page<Transaction> findByMerchantID(Long merchantID, Pageable pageable);
	    Page<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	    Page<Transaction> findByMerchantIDAndTransactionDateBetween(Long merchantID, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	    @Query("SELECT t FROM Transaction t WHERE " + "(:reference IS NULL OR t.reference = :reference) AND " +"(:merchantID IS NULL OR t.merchantID = :merchantID) AND " +"(:status IS NULL OR t.transactionStatus = :status)")
	    Page<Transaction> searchTransactions( @Param("reference") String reference,   @Param("merchantID") Long merchantID,  @Param("status") Short status, Pageable pageable);
	    @Query("SELECT t FROM Transaction t WHERE t.transactionStatus = :status AND t.merchantID = :merchantID ORDER BY t.transactionDate DESC")
	    List<Transaction> findLatest10ApprovedByStatusAndMerchantId(@Param("status") Short status, @Param("merchantID") Long merchantID,org.springframework.data.domain.Pageable pageable);
	    @Query("SELECT t FROM Transaction t WHERE (t.transactionStatus = :status) ORDER BY t.transactionDate DESC")
	    List<Transaction> findLatest10ApprovedByTransactionDateDesc(@Param("status") Short status, org.springframework.data.domain.Pageable pageable);
	    @Query("SELECT SUM(t.billAmount) FROM Transaction t WHERE t.transactionStatus IN :statuses")
	    Double sumBillAmountByStatuses(@Param("statuses") Short statuses);
	    @Query("SELECT SUM(t.billAmount) FROM Transaction t WHERE t.transactionStatus = :status AND t.merchantID = :merchantID")
	    Double sumBillAmountByStatusesAndMerchantID(@Param("status") Short status, @Param("merchantID") Long merchantID);
	    @Query("SELECT SUM(t.billAmount) FROM Transaction t WHERE t.transactionStatus = :status AND t.merchantID = :merchantID AND t.transactionDate BETWEEN :startDate AND :endDate")
	    Double sumBillAmountByStatusesAndMerchantIDAndDateRange(@Param("status") Short status, @Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
	    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionStatus IN (1, 7) AND t.merchantID = :merchantID AND t.transactionDate BETWEEN :startDate AND :endDate")
	    Long countSuccessTransactionsByMerchantIDAndDateRange( @Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate );
	    @Query("SELECT DISTINCT t.billEmail FROM Transaction t WHERE t.transactionStatus IN (1, 7) AND t.merchantID = :merchantID AND t.transactionDate BETWEEN :startDate AND :endDate")
	    List<String> findSuccessEmailsByMerchantIDAndDateRange(
	        @Param("merchantID") Long merchantID,
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDate
	    );

	    @Query("SELECT DISTINCT t.billEmail FROM Transaction t WHERE t.transactionStatus IN (2, 23, 24) AND t.merchantID = :merchantID AND t.transactionDate BETWEEN :startDate AND :endDate")
	    List<String> findFailedEmailsByMerchantIDAndDateRange(
	        @Param("merchantID") Long merchantID,
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDate
	    );



	    // Success Rate queries as per terNO
	    @Query("SELECT COUNT(t), SUM(t.billAmount) FROM Transaction t WHERE t.transactionStatus IN (1, 7) AND t.terminalNumber = :terminalNumber")
	    Object[] countSuccessTransactionsByTerminalNumber(@Param("terminalNumber") Long terminalNumber);


	    @Query("SELECT DISTINCT t.billEmail FROM Transaction t WHERE t.transactionStatus IN (1, 7) AND t.terminalNumber = :terminalNumber")
	    List<String> findSuccessEmailsByTerminalNumber(
	        @Param("terminalNumber") Long terminalNumber
	    );

	    @Query("SELECT DISTINCT t.billEmail FROM Transaction t WHERE t.transactionStatus IN (2, 23, 24) AND t.terminalNumber = :terminalNumber")
	    List<String> findFailedEmailsByTerminalNumber(
	        @Param("terminalNumber") Long terminalNumber
	    );

	    @Query("SELECT t.transactionStatus, COUNT(t), COALESCE(SUM(t.billAmount), 0) FROM Transaction t " +
	           "WHERE (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	           "AND (:billIp IS NULL OR LOWER(t.billIP) = LOWER(:billIp)) " +
	           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
	           "AND t.transactionStatus IN (1, 2, 7, 22, 23, 24) " +
	           "GROUP BY t.transactionStatus")
	    List<Object[]> getStatusSummaryByBillIp(
	        @Param("merchantID") Long merchantID,
	        @Param("billIp") String billIp,
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDate
	    );

	    @Query("SELECT t.transactionStatus, COUNT(t), COALESCE(SUM(t.billAmount), 0) FROM Transaction t " +
	           "WHERE (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	           "AND (:terminalNumber IS NULL OR t.terminalNumber = :terminalNumber) " +
	           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
	           "AND t.transactionStatus IN (1, 2, 7, 22, 23, 24) " +
	           "GROUP BY t.transactionStatus")
	    List<Object[]> getStatusSummaryByTerminal(
	        @Param("merchantID") Long merchantID,
	        @Param("terminalNumber") Long terminalNumber,
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDate
	    );


	    Optional<Transaction> findByTransactionStatus(Short transactionId);
	    //List<Transaction> findByUserId(Long userId);
	    //List<Transaction> findByTransactionStatus(String transactionStatus);

	    // THIS METHOD SHOULD *NOT* HAVE A @Query ANNOTATION
	    // Its implementation will be provided by JpaSpecificationExecutor
	    // The previous complex @Query content should be completely removed.
	    // It should look like this:
	    // Page<Transaction> findAllWithFilters(Specification<Transaction> spec, Pageable pageable); // This exact method is not needed as JpaSpecificationExecutor provides findAll(Specification, Pageable)
	    // You only need the interface method in TransactionService and the implementation in TransactionServiceImpl

	    



	    //Anayalic
	    @Query("SELECT COUNT(t), SUM(t.billAmount), AVG(t.billAmount) FROM Transaction t " +
	    "WHERE t.transactionDate BETWEEN :start AND :end AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Object[] getAggregateStats(LocalDateTime start, LocalDateTime end, String merchantID);

	    @Query("SELECT t.transactionStatus, COUNT(t) FROM Transaction t " +
	        "WHERE t.transactionDate BETWEEN :start AND :end " +
	        "AND (:merchantID IS NULL OR t.merchantID = :merchantID) GROUP BY t.transactionStatus")
	    List<Object[]> getStatusCounts(LocalDateTime start, LocalDateTime end, String merchantID);

	    @Query("SELECT FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m-%d'), COUNT(t) FROM Transaction t " +
	        "WHERE t.transactionDate BETWEEN :start AND :end AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	        "GROUP BY FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m-%d')")
	    List<Object[]> getDailyTrends(LocalDateTime start, LocalDateTime end, String merchantID);

	    @Query("SELECT FUNCTION('HOUR', t.transactionDate), COUNT(t) FROM Transaction t " +
	        "WHERE t.transactionDate BETWEEN :start AND :end AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	        "GROUP BY FUNCTION('HOUR', t.transactionDate)")
	    List<Object[]> getHourlyTrends(LocalDateTime start, LocalDateTime end, String merchantID);

	    @Query("SELECT t.methodOfPayment, COUNT(t), SUM(t.billAmount) FROM Transaction t " +
	        "WHERE t.transactionDate BETWEEN :start AND :end AND t.merchantID = :merchantID " +
	        "GROUP BY t.methodOfPayment")
	    List<Object[]> getPaymentMethods(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("merchantID") Long merchantID);

	   
	    //@Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end AND t.type = :type")
	    //List<Transaction> getFraudScores(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type);

	    

	    //check-regulatory 
	    @Query("SELECT SUM(t.billAmount), COUNT(t) FROM Transaction t " +
	           "WHERE t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
	           "AND t.merchantID = :merchantID " +
	           "AND t.billEmail = :billEmail " +
	           "AND t.transactionStatus = 1")
	    List<Object[]> findSuccessfulVolumeAndCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("merchantID") Long merchantID, @Param("billEmail") String billEmail);

	    @Query("SELECT COUNT(t) FROM Transaction t " +
	           "WHERE t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
	           "AND t.merchantID = :merchantID " +
	           "AND t.billEmail = :billEmail " +
	           "AND t.transactionStatus IN (2, 23, 24)")
	    Long countFailedTransactions(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("merchantID") Long merchantID, @Param("billEmail") String billEmail);


	   
	   
	   
	    // ==== Unified analytics helpers ====
	    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Long countAllByMerchantAndDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionStatus IN (1,7) AND t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Long countSuccessByMerchantAndDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	    @Query("SELECT SUM(t.billAmount) FROM Transaction t WHERE t.transactionStatus IN (1,7) AND t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Double sumSuccessfulBillAmountByMerchantAndDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	    @Query("SELECT AVG(t.billAmount) FROM Transaction t WHERE t.transactionStatus IN (1,7) AND t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Double avgSuccessfulTransactionAmount(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionStatus IN (5,11) AND t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Long countChargebacksByMerchantAndDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	    @Query("SELECT DISTINCT t.billEmail FROM Transaction t WHERE t.transactionStatus IN (1, 7) AND t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    List<String> findSuccessEmailsByDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
	    


	    // Average fraud score from text column risk_ratio; use native for casting
	    @Query("SELECT AVG(CAST(t.riskRatio AS double)) FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate AND (:merchantID IS NULL OR t.merchantID = :merchantID)")
	    Double avgFraudScoreByMerchantAndDateRange(@Param("merchantID") Long merchantID, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	     // Card type breakdown joined with TransactionAdditional (by transID to transIDAd)
	    @Query("SELECT COALESCE(ta.cardType, 'Unknown') as cardType, COUNT(t) as count, SUM(t.billAmount) as totalAmount " +
	           "FROM Transaction t JOIN com.webapp.entity.TransactionAdditional ta ON ta.transIDAd = t.transID " +
	           "WHERE t.transactionDate BETWEEN :start AND :end AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	           "GROUP BY ta.cardType")
	    List<Object[]> getCardTypeBreakdown(@Param("start") LocalDateTime start,
	                                        @Param("end") LocalDateTime end,
	                                        @Param("merchantID") Long merchantID);

	    @Query("SELECT t.terminalNumber, COUNT(t), SUM(t.billAmount), SUM(CASE WHEN t.transactionStatus = 1 THEN 1 ELSE 0 END) FROM Transaction t " +
	        "WHERE t.transactionDate BETWEEN :start AND :end AND (:merchantID IS NULL OR t.merchantID = :merchantID) " +
	        "GROUP BY t.terminalNumber")
	    List<Object[]> getTerminalPerformance(LocalDateTime start, LocalDateTime end, String merchantID);





	    // START : responseType 11 is Customer Intelligence Hub
	    @Query(value = "SELECT " +
	            "COALESCE(NULLIF(ta.bill_country, ''), 'Unknown') AS country, " +
	            "COUNT(t.*) AS transactionCount, " +
	            "ROUND(COUNT(t.id) * 100.0 / denom.total_count, 2) AS percentage, " +
	            "SUM(t.bill_amt) AS totalAmount " +
	            "FROM master_trans_table_3 t " +
	            "LEFT JOIN master_trans_additional_3 ta ON ta.transID_ad = t.transid " +
	            "JOIN (SELECT COUNT(*) AS total_count " +
	            "      FROM master_trans_table_3 t2 " +
	            "      WHERE t2.trans_status IN (1, 7) " +
	            "      AND t2.merid = :merchantID " +
	            "      AND t2.tdate BETWEEN :startDate AND :endDate) denom ON 1=1 " +
	            "WHERE t.trans_status IN (1, 7) " +
	            "AND t.merid = :merchantID " +
	            "AND t.tdate BETWEEN :startDate AND :endDate " +
	            "GROUP BY country, denom.total_count " +
	            "ORDER BY transactionCount DESC", nativeQuery = true)
	    List<Object[]> getGeographicDistribution(
	            @Param("merchantID") Long merchantID,
	            @Param("startDate") LocalDateTime startDate,
	            @Param("endDate") LocalDateTime endDate);

	    @Query(value = "SELECT " +
	            "COALESCE(NULLIF(ta.card_brand, ''), 'Unknown') as method, " +
	            "COUNT(t.*) as count, " +
	            "ROUND(COUNT(t.id) * 100.0 / denom.total_count, 2) as percentage " +
	            "FROM master_trans_table_3 t " +
	            "LEFT JOIN master_trans_additional_3 ta ON ta.transID_ad = t.transid " +
	            "JOIN (SELECT COUNT(*) AS total_count " +
	            "      FROM master_trans_table_3 t2 " +
	            "      WHERE t2.merid = :merchantID " +
	            "      AND t2.tdate BETWEEN :startDate AND :endDate " +
	            "      AND t2.trans_status IN (1, 7)) denom ON 1 = 1 " +
	            "WHERE t.merid = :merchantID " +
	            "AND t.tdate BETWEEN :startDate AND :endDate " +
	            "AND t.trans_status IN (1, 7) " +
	            "AND (ta.card_brand IS NOT NULL AND ta.card_brand <> '' OR ta.card_type IS NOT NULL) " +
	            "GROUP BY method, denom.total_count " +
	            "ORDER BY count DESC", nativeQuery = true)
	    List<Object[]> getPaymentMethodDistribution(
	            @Param("merchantID") Long merchantID,
	            @Param("startDate") LocalDateTime startDate,
	            @Param("endDate") LocalDateTime endDate);

	    @Query(value = "SELECT t.bill_email, SUM(t.bill_amt) AS totalSpend, COUNT(*) AS transactionCount, " +
	            "MAX(t.tdate) AS lastTransactionDate, 'active' AS status, 0 AS revenue, 0 AS transactions, MIN(t.tdate) AS since "
	            +
	            "FROM master_trans_table_3 t " +
	            "WHERE t.merid = :merchantID AND t.tdate BETWEEN :startDate AND :endDate " +
	            "AND t.bill_email IS NOT NULL AND t.trans_status IN (1, 7) " +
	            "GROUP BY t.bill_email " +
	            "ORDER BY totalSpend DESC " +
	            "LIMIT :limit", nativeQuery = true)
	    List<Object[]> getTopPerformingCustomers(
	            @Param("merchantID") Long merchantID,
	            @Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate,@Param("limit") int limit);

	    

}
