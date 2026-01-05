package com.currencyexchangeapi.service;
import java.util.List;
import java.util.Map;
import com.currencyexchangeapi.entity.CurrencyExchangeApi;
public interface CurrencyExchangeApiService {
	 List<CurrencyExchangeApi> getAllCurrencyExchanges();
	    CurrencyExchangeApi getCurrencyExchangeById(Integer id);
	    CurrencyExchangeApi saveCurrencyExchange(CurrencyExchangeApi currencyExchange);
	    void deleteCurrencyExchange(Integer id);
	    Map<String, Object> currencyConverter(String fromCurrency, String toCurrency, double amount);
	    List<CurrencyExchangeApi> getAllCurrencyExchangesSortedByIdDesc();
	    CurrencyExchangeApi getLatestCurrencyExchange(String currencyFrom, String currencyTo);
	    CurrencyExchangeApi findTopByCurrencyFromAndCurrencyToOrderByIdDesc(String currencyFrom, String currencyTo);


}
