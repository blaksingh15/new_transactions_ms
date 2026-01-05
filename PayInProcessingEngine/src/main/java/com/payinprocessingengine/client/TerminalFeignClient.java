package com.payinprocessingengine.client;
import org.springframework.cloud.openfeign.FeignClient;
@FeignClient(name = "terminal-service")
public interface TerminalFeignClient {

}
