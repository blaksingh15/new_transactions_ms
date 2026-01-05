package com.currencyexchangeapi.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.currencyexchangeapi.entity.CurrencyExchangeApi;
public interface CurrencyExchangeApiRepository extends JpaRepository<CurrencyExchangeApi, Integer> {
    List<CurrencyExchangeApi> findAllByOrderByIdDesc();
    CurrencyExchangeApi findTopByCurrencyFromAndCurrencyToOrderByIdDesc(String currencyFrom, String currencyTo);
}
