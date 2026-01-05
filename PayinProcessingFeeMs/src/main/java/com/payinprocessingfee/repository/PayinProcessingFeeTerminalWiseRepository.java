package com.payinprocessingfee.repository;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.payinprocessingfee.model.PayinProcessingFeeTerminalWise;

public interface PayinProcessingFeeTerminalWiseRepository extends JpaRepository<PayinProcessingFeeTerminalWise, Integer> {
	
	List<PayinProcessingFeeTerminalWise> findAll(Specification<PayinProcessingFeeTerminalWise> spec);

    List<PayinProcessingFeeTerminalWise> findAllByOrderById();

    List<PayinProcessingFeeTerminalWise> findByMeridOrderById(Integer merid);
    
    

}
