package com.currencyexchangeapi.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_exchange_table")
public class CurrencyExchangeApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "currency_from")
    private String currencyFrom;

    @Column(name = "currency_to")
    private String currencyTo;
    
    @Column(name = "conversion_rates")
    private String conversionRates;

    @Column(name = "amount_to_convert")
    private String amountToConvert;

    @Column(name = "converted_amount")
    private String convertedAmount;

	@Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "currency_josn", columnDefinition = "TEXT")
    private String currencyJson;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(String currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(String currencyTo) {
        this.currencyTo = currencyTo;
    }
    
    public String getConversionRates() {
		return conversionRates;
	}

	public void setConversionRates(String conversionRates) {
		this.conversionRates = conversionRates;
	}
	
    public String getAmountToConvert() {
		return amountToConvert;
	}

	public void setAmountToConvert(String amountToConvert) {
		this.amountToConvert = amountToConvert;
	}

	public String getConvertedAmount() {
		return convertedAmount;
	}

	public void setConvertedAmount(String convertedAmount) {
		this.convertedAmount = convertedAmount;
	}

    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.toString().replace("T", " ") : null;
    }
    public void setFormattedTimestamp(String formattedTimestamp) {
        if (formattedTimestamp != null) {
            this.timestamp = LocalDateTime.parse(formattedTimestamp.replace(" ", "T"));
        }
    }
    
	public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrencyJson() {
        return currencyJson;
    }

    public void setCurrencyJson(String currencyJson) {
        this.currencyJson = currencyJson;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
