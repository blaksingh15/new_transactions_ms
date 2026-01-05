package com.payinprocessingfee.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.payinprocessingfee.dto.PayinProcessingFeeDropdownDTO;
import com.payinprocessingfee.feinclient.ConnectorFeignClient;
import com.payinprocessingfee.model.Connector;
import com.payinprocessingfee.model.PayinProcessingFeeTerminalWise;
import com.payinprocessingfee.repository.PayinProcessingFeeTerminalWiseRepository;
import jakarta.persistence.criteria.Predicate;
@Service
public class PayinProcessingFeeTerminalWiseServiceImpl implements PayinProcessingFeeTerminalWiseService {
	
    @Autowired
    private PayinProcessingFeeTerminalWiseRepository payinProcessingFeeTerminalWiseRepo;
  
    @Autowired
    private  ConnectorFeignClient connectorClient;
    
    @Override
    public List<PayinProcessingFeeTerminalWise> getAllFees() {
        List<PayinProcessingFeeTerminalWise> feeList =payinProcessingFeeTerminalWiseRepo.findAll();
        if (feeList.isEmpty()) {
            return feeList;
        }
        List<Long> connectorIds = collectConnectorIds(feeList);
        if (connectorIds.isEmpty()) {
            return feeList;
        }
        List<Connector> connectors = fetchConnectors(connectorIds);
        if (connectors.isEmpty()) {
            return feeList;
        }
        attachConnectorsToFees(feeList, connectors);
        return feeList;
    }


    @Override
    public PayinProcessingFeeTerminalWise getFeeById(Integer id) {
        PayinProcessingFeeTerminalWise fee = payinProcessingFeeTerminalWiseRepo.findById(id).orElse(null);
        if (fee != null) {
            String connectorIdStr = fee.getConnectorId();
            if (connectorIdStr != null && !connectorIdStr.isEmpty()) {
                try {
                    Long connectorId = Long.parseLong(connectorIdStr);
                    Connector connector = getConnector(connectorId); 
                    fee.setConnector(connector);
                } catch (NumberFormatException e) {
                    fee.setConnector(null);
                }
            }
        }

        return fee;
    }

    @Override
    public PayinProcessingFeeTerminalWise saveFee(PayinProcessingFeeTerminalWise fee) {
        return payinProcessingFeeTerminalWiseRepo.save(fee);
    }

    @Override
    public void deleteFee(Integer id) {
    	payinProcessingFeeTerminalWiseRepo.deleteById(id);
    }

    @Override
    public List<PayinProcessingFeeTerminalWise> searchPayinProcessingFeeTerminalWise(Boolean connectorProcessingMode, String connectorId, String merid, String mdrRate, String monthlyFee) {
        Specification<PayinProcessingFeeTerminalWise> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (connectorProcessingMode != null) {
                predicates.add(cb.equal(root.get("connectorProcessingMode"), connectorProcessingMode));
            }
            if (merid != null && !merid.isEmpty()) {
                predicates.add(cb.like(root.get("merid"), "%" + merid + "%"));
            }
            if (connectorId != null && !connectorId.isEmpty()) {
                predicates.add(cb.like(root.get("connector_id"), "%" + connectorId + "%"));
            }
            if (mdrRate != null && !mdrRate.isEmpty()) {
                predicates.add(cb.like(root.get("mdr_rate"), "%" + mdrRate + "%"));
            }
            if (monthlyFee != null && !monthlyFee.isEmpty()) {
                predicates.add(cb.like(root.get("monthly_fee"), "%" + monthlyFee + "%"));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        List<PayinProcessingFeeTerminalWise> feeList= payinProcessingFeeTerminalWiseRepo.findAll(spec);
        if (feeList.isEmpty()) {
            return feeList;
        }
        List<Long> connectorIds = collectConnectorIds(feeList);
        if (connectorIds.isEmpty()) {
            return feeList;
        }
        List<Connector> connectors = fetchConnectors(connectorIds);
        if (connectors.isEmpty()) {
            return feeList;
        }
        attachConnectorsToFees(feeList, connectors);
        return feeList;
    }

    @Override
    public List<PayinProcessingFeeDropdownDTO> getDropdownList() {
        List<PayinProcessingFeeTerminalWise> results = payinProcessingFeeTerminalWiseRepo.findAllByOrderById();
        return convertToDropdownDTO(results);
    }

    @Override
    public List<PayinProcessingFeeTerminalWise> fetchDropdownListByMerid(Integer merid, Boolean isAll) { 
        List<PayinProcessingFeeTerminalWise> results = null;
        if(isAll){
            results = payinProcessingFeeTerminalWiseRepo.findAllByOrderById();
        }
        else if(merid != null && merid != 0){
            results = payinProcessingFeeTerminalWiseRepo.findByMeridOrderById(merid);
        }
        
        List<PayinProcessingFeeTerminalWise> results1 = new ArrayList<>();
        for (PayinProcessingFeeTerminalWise result : results) {
            PayinProcessingFeeTerminalWise dto = new PayinProcessingFeeTerminalWise();
            dto.setId(result.getId());
            dto.setGatewayName(result.getGatewayName());
            dto.setSecondaryGatewayName(result.getSecondaryGatewayName());
            results1.add(dto);
        }
        if (results1.isEmpty()) {
            return results1;
        }
        List<Long> connectorIds = collectConnectorIds(results1);
        if (connectorIds.isEmpty()) {
            return results1;
        }
        List<Connector> connectors = fetchConnectors(connectorIds);
        if (connectors.isEmpty()) {
            return results1;
        }
        attachConnectorsToFees(results1, connectors);
        return results1;
        
    }

    @Override
    public List<PayinProcessingFeeDropdownDTO> getDropdownListByMerid(Integer merid) { 
        List<PayinProcessingFeeTerminalWise> results = payinProcessingFeeTerminalWiseRepo.findByMeridOrderById(merid);
        List<Long> connectorIds = collectConnectorIds(results);
        if (!connectorIds.isEmpty()) {
            List<Connector> connectors = fetchConnectors(connectorIds);
            if (!connectors.isEmpty()) {
                attachConnectorsToFees(results, connectors);
            }
        }
        return convertToDropdownDTO(results);
    }

    private List<PayinProcessingFeeDropdownDTO> convertToDropdownDTO(List<PayinProcessingFeeTerminalWise> results) {
        List<PayinProcessingFeeDropdownDTO> dtoList = new ArrayList<>();
        for (PayinProcessingFeeTerminalWise result : results) {
            PayinProcessingFeeDropdownDTO dto = new PayinProcessingFeeDropdownDTO();
            dto.setId(result.getId());
            dto.setConnectorId(result.getConnectorId());
            if (result.getConnector() != null) {
                Connector connector = result.getConnector();
                dto.setEcommerceCruisesJson(connector.getEcommerceCruisesJson());
                dto.setConnectorName(connector.getConnectorName());
                dto.setChannelType(connector.getChannelType());
                dto.setConnectorStatus(connector.getConnectorStatus());
                if (connector.getDefaultConnector() != null) {
                    dto.setDefaultConnector("true".equalsIgnoreCase(connector.getDefaultConnector()) || 
                                           "1".equals(connector.getDefaultConnector()) || 
                                           "yes".equalsIgnoreCase(connector.getDefaultConnector()));
                } else {
                    dto.setDefaultConnector(false);
                }
            }
            
            dtoList.add(dto);
        }
        return dtoList;
    }
    
    public Connector getConnector(Long connectorId) {
        return connectorClient.getConnectorsDetails(connectorId);
    }
    
    public List<Connector> fetchConnectors(List<Long> connectorIds) {
        return connectorClient.getListConnectors(connectorIds);
    }
    
    private List<Long> collectConnectorIds(List<PayinProcessingFeeTerminalWise> fees) {
        List<Long> connectorIds = new ArrayList<>();
        for (PayinProcessingFeeTerminalWise fee : fees) {
            String connectorIdStr = fee.getConnectorId();
            if (connectorIdStr != null && !connectorIdStr.isEmpty()) {
                try {
                    connectorIds.add(Long.parseLong(connectorIdStr));
                } catch (NumberFormatException e) {
                }
            }
        }
        return connectorIds;
    }

    private void attachConnectorsToFees(List<PayinProcessingFeeTerminalWise> fees,List<Connector> connectors) {
        Map<Long, Connector> connectorMap = new HashMap<>();
        for (Connector connector : connectors) {
            connectorMap.put(connector.getId(), connector);
        }
        for (PayinProcessingFeeTerminalWise fee : fees) {
            try {
                Long connectorId = Long.parseLong(fee.getConnectorId());
                Connector connector = connectorMap.get(connectorId);
                if (connector != null) {
                    fee.setConnector(connector);
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }

     
}