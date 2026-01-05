package com.terminal.controller;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.terminal.entity.Terminal;
import com.terminal.service.TerminalService;
import com.terminal.utility.AES256Util;

@RestController
@RequestMapping("/api/terminals")
public class TerminalController {

    @Autowired
    private TerminalService terminalService;
    
    private static final SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat("yyMMddHHmmss");
    private static final SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("MMddHHmmss");
    
    @PostMapping("/create")
    public ResponseEntity<Terminal> createTerminal(@RequestBody Terminal terminal) {
        try {
            if (terminal.getActive() == null) {
                terminal.setActive((short) 4);
            }

            Terminal savedTerminal = terminalService.saveTerminal(terminal);
            String timestamp = DATE_FORMAT_FULL.format(new Date());
            String publicKey = AES256Util.encrypt(savedTerminal.getId() + "_" + savedTerminal.getMerid() + "_" + timestamp);
            String privateKey = AES256Util.encrypt(savedTerminal.getMerid() + "_" + savedTerminal.getId() + "_" + timestamp);
            savedTerminal.setPublicKey(publicKey);
            savedTerminal.setPrivateKey(privateKey);
            savedTerminal = terminalService.saveTerminal(savedTerminal);
            return ResponseEntity.ok(savedTerminal);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<Terminal> updateTerminal(@PathVariable int id, @RequestBody Terminal terminal) {
        try {
            terminal.setId(id);
            Terminal updatedTerminal = terminalService.saveTerminal(terminal);
            return ResponseEntity.ok(updatedTerminal);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTerminal(@PathVariable int id) {
        try {
            terminalService.deleteTerminal(id);
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Terminal deleted successfully",
                "id", id
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = Map.of(
                "success", false,
                "message", "Error deleting terminal: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }


    @GetMapping({"", "/list"})
    public List<Terminal> getAllTerminals() {
        return terminalService.getAllTerminals();
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Terminal> getTerminalById(@PathVariable int id) {
        try {
            Terminal terminal = terminalService.getTerminalById(id);
            return terminal != null ? ResponseEntity.ok(terminal) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/key/{public_key}")
    public ResponseEntity<Map<String, Object>> getPublicKey(@PathVariable String public_key) {
        Map<String, Object> response = terminalService.getPublicKey(public_key);
        if (response.containsKey("error_number")) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Terminal>> searchTerminals(@RequestParam(required = false) Boolean active,@RequestParam(required = false) String publicKey,@RequestParam(required = false) String merId,@RequestParam(required = false) String businessUrl,@RequestParam(required = false) String terName, @RequestParam(required = false) String dbaBrandName,@RequestParam(required = false) String connectorids) {
        List<Terminal> results = terminalService.searchTerminals(active, publicKey, merId, businessUrl, terName, dbaBrandName, connectorids);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/merid/{merId}")
    public ResponseEntity<List<Terminal>> searchTerminalsByMerid(@PathVariable String merId, @RequestParam(required = false) Boolean active,@RequestParam(required = false) String publicKey,@RequestParam(required = false) String businessUrl,@RequestParam(required = false) String terName, @RequestParam(required = false) String dbaBrandName,  @RequestParam(required = false) String connectorids) {
        if (merId == null || merId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<Terminal> terminals = terminalService.searchTerminalsByMerid(
                    merId, active, publicKey, businessUrl, terName, dbaBrandName, connectorids);
            return ResponseEntity.ok(terminals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/generatePublicKey/{id}")
    public ResponseEntity<String> generatePublicKey(@PathVariable int id) {
        Terminal terminal = terminalService.getTerminalById(id);
        if (terminal == null) return ResponseEntity.notFound().build();
        try {
            String publicKey = AES256Util.encrypt(
                    terminal.getId() + "_" + terminal.getMerid() + "_" + DATE_FORMAT_FULL.format(new Date()));
            terminal.setPublicKey(publicKey);
            terminalService.saveTerminal(terminal);
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/generatePrivateKey/{id}")
    public ResponseEntity<String> generatePrivateKey(@PathVariable int id) {
        Terminal terminal = terminalService.getTerminalById(id);
        if (terminal == null) return ResponseEntity.notFound().build();
        try {
            String privateKey = AES256Util.encrypt(
                    terminal.getMerid() + "_" + terminal.getId() + "_" + DATE_FORMAT_FULL.format(new Date()));
            terminal.setPrivateKey(privateKey);
            terminalService.saveTerminal(terminal);
            return ResponseEntity.ok(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/generateWebhookSecretApiKey/{id}")
    public ResponseEntity<String> generateWebhookSecretApiKey(@PathVariable int id) {
        Terminal terminal = terminalService.getTerminalById(id);
        if (terminal == null) return ResponseEntity.notFound().build();
        try {
            String webhookSecretApiKey = terminal.getMerid() + "_" + terminal.getId() + "_" + DATE_FORMAT_SHORT.format(new Date());
            terminal.setWebhookSecretApiKey(webhookSecretApiKey);
            terminalService.saveTerminal(terminal);
            return ResponseEntity.ok(webhookSecretApiKey);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/fields")
    public ResponseEntity<List<Map<String, String>>> getTerminalFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        for (Field field : Terminal.class.getDeclaredFields()) {
            Map<String, String> fieldInfo = new java.util.HashMap<>();
            fieldInfo.put("name", field.getName());
            fieldInfo.put("type", field.getType().getSimpleName());
            String label = field.getName().replaceAll("([A-Z])", " $1").replace("_", " ").trim();
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            fieldInfo.put("label", label);
            fields.add(fieldInfo);
        }
        return ResponseEntity.ok(fields);
    }
}
