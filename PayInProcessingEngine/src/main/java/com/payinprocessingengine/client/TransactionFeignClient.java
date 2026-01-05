package com.payinprocessingengine.client;
import org.springframework.cloud.openfeign.FeignClient;
@FeignClient(name = "transactions-service")
public interface TransactionFeignClient {

}
