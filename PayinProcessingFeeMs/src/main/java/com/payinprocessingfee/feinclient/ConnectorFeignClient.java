package com.payinprocessingfee.feinclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.payinprocessingfee.model.Connector;

@FeignClient(name = "connector-service")
public interface ConnectorFeignClient {
    @GetMapping("/api/connectors/get/{id}")
    Connector getConnectorsDetails(@PathVariable("id") Long id);
    
    @GetMapping("/api/connectors/list/get")
    List<Connector> getListConnectors(@RequestParam("ids") List<Long> ids);
}
