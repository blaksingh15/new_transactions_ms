package com.payinprocessingfee.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.payinprocessingfee.dto.PayinProcessingFeeDropdownDTO;
import com.payinprocessingfee.model.PayinProcessingFeeTerminalWise;
import com.payinprocessingfee.service.PayinProcessingFeeTerminalWiseService;

@RestController
@RequestMapping("/api/payin-processing-fee")
public class PayinProcessingFeeTerminalWiseController {
	
    private final PayinProcessingFeeTerminalWiseService feeService;
    public PayinProcessingFeeTerminalWiseController(PayinProcessingFeeTerminalWiseService feeService) {
        this.feeService = feeService;
    }
   
    @PostMapping("/create")
    public ResponseEntity<PayinProcessingFeeTerminalWise> createFee(@RequestBody PayinProcessingFeeTerminalWise fee) {
        if (fee.getId() != null && fee.getId() > 0) {
            return updateFee(fee.getId(), fee);
        }
        fee.setId(null);
        return ResponseEntity.ok(feeService.saveFee(fee));
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<PayinProcessingFeeTerminalWise> updateFee(@PathVariable int id, @RequestBody PayinProcessingFeeTerminalWise fee) {
        try {
            fee.setId(id);
            PayinProcessingFeeTerminalWise updatedFee = feeService.saveFee(fee);
            return ResponseEntity.ok(updatedFee);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/update")
    public ResponseEntity<PayinProcessingFeeTerminalWise> updateFeeWithoutPath(@RequestBody PayinProcessingFeeTerminalWise fee) {
        if (fee.getId() == null || fee.getId() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            PayinProcessingFeeTerminalWise updatedFee = feeService.saveFee(fee);
            return ResponseEntity.ok(updatedFee);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PayinProcessingFeeTerminalWise> updateFeeSimple(@PathVariable int id, @RequestBody PayinProcessingFeeTerminalWise fee) {
        return updateFee(id, fee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFee(@PathVariable Integer id) {
        feeService.deleteFee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PayinProcessingFeeTerminalWise> getFeeById(@PathVariable Integer id) {
        return ResponseEntity.ok(feeService.getFeeById(id));
    }
    
    @GetMapping({"/list"}) 
    public ResponseEntity<List<PayinProcessingFeeTerminalWise>> getAllFees() {
        return ResponseEntity.ok(feeService.getAllFees());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PayinProcessingFeeTerminalWise>> searchPayinProcessingFeeTerminalWise(
            @RequestParam(required = false) Boolean connectorProcessingMode,
            @RequestParam(required = false) String connectorId,
            @RequestParam(required = false) String merid,
            @RequestParam(required = false) String mdrRate,
            @RequestParam(required = false) String monthlyFee) {
        try {
            List<PayinProcessingFeeTerminalWise> results = feeService.searchPayinProcessingFeeTerminalWise(
                    connectorProcessingMode, connectorId, merid, mdrRate, monthlyFee);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    
    @GetMapping("/dropdown/gatewayname/{merid}")
    public ResponseEntity<List<PayinProcessingFeeTerminalWise>> fetchDropdownListByMerid(@PathVariable Integer merid) {
        List<PayinProcessingFeeTerminalWise> dropdownList = feeService.fetchDropdownListByMerid(merid, false);
        return ResponseEntity.ok(dropdownList);
    } 

    @GetMapping("/dropdown/gatewayname/all")
    public ResponseEntity<List<PayinProcessingFeeTerminalWise>> fetchDropdownListByAll() {
        List<PayinProcessingFeeTerminalWise> dropdownList = feeService.fetchDropdownListByMerid(0, true);
        return ResponseEntity.ok(dropdownList);
    } 

    @GetMapping("/dropdown/list/{merid}")
    public ResponseEntity<List<PayinProcessingFeeDropdownDTO>> fetchDropdownList(@PathVariable Integer merid) {
        List<PayinProcessingFeeDropdownDTO> dropdownList = feeService.getDropdownListByMerid(merid);
        return ResponseEntity.ok(dropdownList);
    }
}
