package com.wex.purchaser.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

	private long transactionId;
	private String description;
	private BigDecimal amountInUSD;
	private String currency;
	private BigDecimal amount;
	private BigDecimal exchangeRate;
	private String exchangeRateEfferctiveDate;
	private String transactionDate;
	
}
