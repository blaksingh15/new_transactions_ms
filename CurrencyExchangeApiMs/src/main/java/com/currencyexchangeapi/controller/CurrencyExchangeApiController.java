package com.currencyexchangeapi.controller;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.currencyexchangeapi.entity.CurrencyExchangeApi;
import com.currencyexchangeapi.service.CurrencyExchangeApiService;

@RestController
@RequestMapping("/api/currency-exchange")
public class CurrencyExchangeApiController {

    @Autowired
    private CurrencyExchangeApiService currencyExchangeApiService;
    
    @PostMapping
    public CurrencyExchangeApi createCurrencyExchange(@RequestBody CurrencyExchangeApi currencyExchange) {
        String fromCurrency = currencyExchange.getCurrencyFrom();
        String toCurrency = currencyExchange.getCurrencyTo();
        double amount = 1.0; 
        amount = Double.parseDouble(currencyExchange.getAmountToConvert());
        try {
            Map<String, Object> conversionResult = currencyExchangeApiService.currencyConverter(fromCurrency, toCurrency, amount);
            String jsonResponse = "";
            if (conversionResult.get("success") != null && (boolean) conversionResult.get("success")) {
                 jsonResponse = conversionResult.get("data").toString();
                currencyExchange.setCurrencyJson(jsonResponse);
            } else {
                currencyExchange.setCurrencyJson("Conversion failed: " + conversionResult.get("message"));
            }
            currencyExchange.setTimestamp(LocalDateTime.now());
            return currencyExchange;
        } catch (Exception e) {
            currencyExchange.setCurrencyJson("Error during conversion: " + e.getMessage());
            return currencyExchangeApiService.saveCurrencyExchange(currencyExchange);
        }
    }


    @GetMapping
    public List<CurrencyExchangeApi> getAllCurrencyExchangesSortedByIdDesc() {
        return currencyExchangeApiService.getAllCurrencyExchangesSortedByIdDesc();
    }

    @GetMapping("/{id}")
    public CurrencyExchangeApi getCurrencyExchangeById(@PathVariable Integer id) {
        return currencyExchangeApiService.getCurrencyExchangeById(id);
    }

    @GetMapping("/{gateway}/{fromCurrency}/{toCurrency}/{getAmount}")
    public Map<String, Object> dbCurrencyConverter(@PathVariable String gateway, @PathVariable String fromCurrency, @PathVariable String toCurrency, @PathVariable String getAmount) {
        return commonDbCurrencyConverter(currencyExchangeApiService, fromCurrency, toCurrency, getAmount, gateway, "false");
    }

   
    public static Map<String, Object> commonDbCurrencyConverter(CurrencyExchangeApiService currencyExchangeApiService, String fromCurrency, String toCurrency, String getAmount, String gateway, String results) {
        Boolean qp = false;
        if (gateway != null && gateway.equals("qp" )) qp = true;
        if (results == null) {
            results = "false";
        }
        double amount = Double.parseDouble(getAmount.toString());
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("fromCurrency", fromCurrency);
        jsonResponse.put("toCurrency", toCurrency);
        jsonResponse.put("amountToConvert", getAmount);
        Boolean isInsertDb = false;
        Double conversion_rates = 0.0;
        double convertedAmount = 0.0;
        CurrencyExchangeApi currencyExchange = currencyExchangeApiService.findTopByCurrencyFromAndCurrencyToOrderByIdDesc(fromCurrency, toCurrency);
        if (currencyExchange != null) {
            if (currencyExchange.getTimestamp() != null && currencyExchange.getTimestamp().isAfter(LocalDateTime.now().minusHours(2))) {
                try {
                    conversion_rates = Double.parseDouble(currencyExchange.getConversionRates());
                    convertedAmount = (amount * conversion_rates);
                    jsonResponse.put("conversion_rates", conversion_rates);
                    jsonResponse.put("converted_amount_accurate", convertedAmount);
                    jsonResponse.put("response_from", "db");
                    if(qp) System.out.println("Using cached conversion rate from DB: " + conversion_rates);
                } catch (NumberFormatException e) {
                    if(qp) System.out.println("Error parsing conversion rate from DB: " + e.getMessage());
                    isInsertDb = true;
                }
            }
            else {
                isInsertDb = true;
            }
        }
        else {
            isInsertDb = true;
        }

        if(isInsertDb) 
        {
          
            Map<String, Object> conversionResult = currencyExchangeApiService.currencyConverter(fromCurrency, toCurrency, amount);
            if (conversionResult.get("success") != null && (boolean) conversionResult.get("success")) {
                if (conversionResult.get("conversionRates") != null) {
                    conversion_rates = Double.parseDouble(conversionResult.get("conversionRates").toString());
                } else {
                    if (conversionResult.get("convertedAmount") != null) {
                        double convertedAmountValue = Double.parseDouble(conversionResult.get("convertedAmount").toString());
                        conversion_rates = convertedAmountValue / amount;
                    } else {
                        return Map.of("error", "Conversion failed: Missing conversion rate data");
                    }
                }
                
                convertedAmount = (amount * conversion_rates);
                jsonResponse.put("conversion_rates", conversion_rates);
                jsonResponse.put("converted_amount_accurate", convertedAmount);
                jsonResponse.put("response_from", "api");
                
            } else {
                String errorMsg = "Conversion failed";
                if (conversionResult.get("message") != null) {
                    errorMsg += ": " + conversionResult.get("message");
                } else if (conversionResult.get("error") != null) {
                    errorMsg += ": " + conversionResult.get("error");
                }
                if(qp) System.out.println(errorMsg);
                return Map.of("error", errorMsg);
            }
        }
       
        if (gateway != null && ( gateway.equals("wd" ) || gateway.equals("wdresponse" ) )) { 
            convertedAmount -= (convertedAmount * 1.553 / 100);
            jsonResponse.put("cal_rate", "- 1.553%");
        }
        else{  
            convertedAmount += (convertedAmount * 2.676 / 100); 
            jsonResponse.put("cal_rate", "+ 2.676%");
        }
        convertedAmount = Math.round(convertedAmount * 100.0) / 100.0;       
        jsonResponse.put("convertedAmount", convertedAmount);
        if(qp) System.out.println("Final converted amount: " + convertedAmount);
        return jsonResponse;
    }

}
