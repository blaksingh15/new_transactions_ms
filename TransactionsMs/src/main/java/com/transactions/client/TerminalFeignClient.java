package com.transactions.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.transactions.entity.Terminal;
@FeignClient(name = "terminal-service")
public interface TerminalFeignClient {
    @GetMapping("/api/terminals/id/{id}")
    Terminal getTerminalById(@PathVariable("id") int id);
}
