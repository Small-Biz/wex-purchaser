package com.wex.purchaser.fiscalapi;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
* This object is the exchange rate return from fiscaldata
* 
* @author Ray Cheng
* 
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate implements Serializable {

	@JsonProperty("country_currency_desc")
	private String countryCurrencyDesc;
	
	@JsonProperty("exchange_rate")
	private BigDecimal exchangeRate;
	
	@JsonProperty("record_date")
	private String recordDate;
	
	@JsonProperty("effective_date")
	private String effectiveDate;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(effectiveDate);
		builder.append("]");
		return builder.toString();
	}
	
}
