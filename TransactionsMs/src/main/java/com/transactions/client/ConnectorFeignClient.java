package com.transactions.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.transactions.entity.Connector;

@FeignClient(name = "connector-service")
public interface ConnectorFeignClient {
	 @GetMapping("/api/connectors/getByNumber")
	  Connector findByConnectorNumber(@RequestParam("name") String name);
	       
}
