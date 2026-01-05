package com.payinprocessingfee.service;
import java.util.List;
import com.payinprocessingfee.dto.PayinProcessingFeeDropdownDTO;
import com.payinprocessingfee.model.PayinProcessingFeeTerminalWise;
public interface PayinProcessingFeeTerminalWiseService {
    List<PayinProcessingFeeTerminalWise> getAllFees();
    PayinProcessingFeeTerminalWise getFeeById(Integer id);
    PayinProcessingFeeTerminalWise saveFee(PayinProcessingFeeTerminalWise fee);
    void deleteFee(Integer id);
    List<PayinProcessingFeeTerminalWise> searchPayinProcessingFeeTerminalWise( Boolean connectorProcessingMode,String connectorId,String merid,String mdrRate,String monthlyFee);
    List<PayinProcessingFeeDropdownDTO> getDropdownList();
    List<PayinProcessingFeeDropdownDTO> getDropdownListByMerid(Integer merid);
    List<PayinProcessingFeeTerminalWise> fetchDropdownListByMerid(Integer merid, Boolean isAll); 
}

