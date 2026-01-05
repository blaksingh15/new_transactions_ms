package com.connector.controller;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.connector.apiresponse.CommonApiResponse;
import com.connector.connectorresponse.ConnectorResponse;
import com.connector.entity.Connector;
import com.connector.service.IConnectorService;
@RestController
@RequestMapping("/api/connectors/")
@CrossOrigin
public class ConnectorController {
    
    @Autowired
    private IConnectorService connectorService;
    
    @PostMapping("/add")
    public ResponseEntity<CommonApiResponse> addConnector(@RequestBody Connector connector) {
        CommonApiResponse response = new CommonApiResponse();
        try {
            @SuppressWarnings("unused")
			Connector savedConnector = connectorService.addOrUpdateConnector(connector);
            response.setSuccess(true);
            if (connector.getId() == null) {
                response.setResponseMessage("Connector added successfully");
            } else {
                response.setResponseMessage("Connector updated successfully");
            }

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setResponseMessage("Failed to add/update connector: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ConnectorResponse> getAllConnectors() {
        ConnectorResponse response = new ConnectorResponse();
        try {
            List<Connector> connectors = connectorService.getAllConnectors();
            response.setConnectors(connectors);
            response.setSuccess(true);
            response.setResponseMessage("Connectors fetched successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setResponseMessage("Failed to fetch connectors: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<Connector> getConnectors(@PathVariable Long id) {
        try {
            Connector connector = connectorService.getConnectorsById(id);
            if (connector != null) {
                return ResponseEntity.ok(connector); 
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
        }
    }


    @GetMapping("/fetch/dropdown/list")
    public ResponseEntity<ConnectorResponse> getDropdownConnectors() {
        ConnectorResponse response = new ConnectorResponse();
        try {
            List<Connector> connectors= connectorService.getDropdownConnectors();
            response.setConnectors(connectors);
            response.setSuccess(true);
            response.setResponseMessage("Active and Common connectors fetched successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setResponseMessage("Failed to fetch connectors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/update")
    public ResponseEntity<CommonApiResponse> updateConnector(
            @RequestParam String originalCode,
            @RequestBody Connector connector) {
            CommonApiResponse response = new CommonApiResponse();
        try {
            boolean updated=connectorService.updateConnector(originalCode, connector);
            if (!updated) {
                response.setSuccess(false);
                response.setResponseMessage("Connector not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.setSuccess(true);
            response.setResponseMessage("Connector updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setResponseMessage("Failed to update connector: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonApiResponse> deleteConnector(@PathVariable Long id) {
        CommonApiResponse response = new CommonApiResponse();
        try {
            boolean deleted = connectorService.deleteConnector(id);

            if (!deleted) {
                response.setSuccess(false);
                response.setResponseMessage("Connector not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.setSuccess(true);
            response.setResponseMessage("Connector deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setResponseMessage("Failed to delete connector: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/list/get")
    public ResponseEntity<List<Connector>> getConnectors(@RequestParam List<Long> ids) {
        try {
            List<Connector> connectors = new ArrayList<>();
            for (Long id : ids) {
                Connector connector = connectorService.getConnectorsById(id);
                if (connector != null) {
                    connectors.add(connector);
                }
            }
            if (connectors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(connectors);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/getByNumber")
    public ResponseEntity<Connector> getConnectorByNumber(@RequestParam String name) {
        try {
            Connector connector = connectorService.findByConnectorNumber(name);
            if (connector != null) {
                return ResponseEntity.ok(connector); 
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
        }
    }

    
}
