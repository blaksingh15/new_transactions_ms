package com.transactions.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transactions.entity.TransactionAdditional;

public interface TransactionAdditionalRepository extends JpaRepository<TransactionAdditional, Integer>{
	 TransactionAdditional findByTransIDAd(Long transIDAd);
	 List<TransactionAdditional> findByTransIDAdIn(java.util.Collection<Long> transIDAds);

}
