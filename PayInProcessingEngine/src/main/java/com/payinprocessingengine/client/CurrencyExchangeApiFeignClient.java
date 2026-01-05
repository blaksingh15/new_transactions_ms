package com.payinprocessingengine.client;
import org.springframework.cloud.openfeign.FeignClient;
@FeignClient(name = "currencyexchangeapi-service")
public interface CurrencyExchangeApiFeignClient {

}
