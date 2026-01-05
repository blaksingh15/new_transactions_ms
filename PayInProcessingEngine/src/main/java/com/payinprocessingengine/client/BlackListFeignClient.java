package com.payinprocessingengine.client;
import org.springframework.cloud.openfeign.FeignClient;
@FeignClient(name = "blackList-service")
public interface BlackListFeignClient {

}
