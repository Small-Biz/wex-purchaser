package com.wex.purchaser.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wex.purchaser.dto.TransactionDTO;
import com.wex.purchaser.entity.Transaction;
import com.wex.purchaser.exception.ServiceException;
import com.wex.purchaser.fiscalapi.ExchangeRate;
import com.wex.purchaser.fiscalapi.FiscalApi;
import com.wex.purchaser.message.CreateTransactionRequest;
import com.wex.purchaser.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {
	
	@Autowired
	private TransactionRepository repository;
	
	@Autowired
	private FiscalApi fiscalApi;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	public Transaction createTransaction(CreateTransactionRequest request) throws ServiceException {
		
		if (request.getDescription() == null ) {
			throw new ServiceException("Description must not be null");
		}
		
		if (request.getAmount() == null) {
			throw new ServiceException("Amount must not be null");
		}
		
		if (request.getAmount().compareTo(BigDecimal.ZERO) < 0 ) {
			throw new ServiceException("Amount should be positive");
		}
		
		if (request.getAmount().scale() > 2) {
			throw new ServiceException("Amount's decimal place should less than or equal to 2 only");
		}
		
		if (request.getDescription().length() > 50 ) {
			throw new ServiceException("Description is too longht ( max. 50 )");
		}
		
		Transaction transaction=Transaction.builder()
				.description(request.getDescription())
				.amount(request.getAmount())
				.createdAt(new Timestamp(System.currentTimeMillis()))
				.build();
		
		return repository.save(transaction);
		
	}
	
	public TransactionDTO enquireLastTransaction(String currency) throws ServiceException {
		
		if (currency == null ) {
			throw new ServiceException("Currency must not be null");
		}
		
		Transaction transaction=repository.findFirstByOrderByCreatedAt();
		
		if (transaction == null ) {
			throw new ServiceException("No transaction record found");
		}
		
		ZonedDateTime zonedDateTime = transaction.getCreatedAt().toInstant().atZone(ZoneId.of("UTC"));
		Timestamp sixMonthBefore = Timestamp.from(zonedDateTime.minus(6, ChronoUnit.MONTHS).toInstant());
		String sixMonthBeforeDate=df.format( new Date(sixMonthBefore.getTime()));
		
		List<ExchangeRate> rateList=fiscalApi.parseExchangeRate(currency, sixMonthBeforeDate);
		
		if (rateList == null || rateList.size()== 0 ) {
			throw new ServiceException("Exchange rate not found");
		}
		
		try {
			return mapToDTO(transaction,rateList);
		}catch( NoSuchElementException e) {
			throw new ServiceException("Exchange rate not found");
		}
	}
	
	public List<TransactionDTO> listTransaction(String currency) throws ServiceException {
		
		if (currency == null ) {
			throw new ServiceException("Currency must not be null");
		}
		
		List<Transaction> transactions=repository.findAll();

		Timestamp earliestTimestamp=transactions.stream().map(Transaction::getCreatedAt).min(Comparator.naturalOrder()).get();
		ZonedDateTime zonedDateTime = earliestTimestamp.toInstant().atZone(ZoneId.of("UTC"));
		Timestamp sixMonthBefore = Timestamp.from(zonedDateTime.minus(6, ChronoUnit.MONTHS).toInstant());
		String sixMonthBeforeDate=df.format( new Date(sixMonthBefore.getTime()));
		
		List<ExchangeRate> rateList=fiscalApi.parseExchangeRate(currency, sixMonthBeforeDate);
		
		if (rateList == null || rateList.size()== 0 ) {
			throw new ServiceException("Exchange rate not found");
		}
		
		try {
			return transactions.stream().map(t->mapToDTO(t,rateList)).collect(Collectors.toList());
		}catch( NoSuchElementException e) {
			throw new ServiceException("Exchange rate not found");
		}
	}
	
	private TransactionDTO mapToDTO( Transaction transaction, List<ExchangeRate> rateList){
		if ( transaction==null) {
			return null;
		}
		
		//get related exchange rate
		
		ZonedDateTime zonedDateTime = transaction.getCreatedAt().toInstant().atZone(ZoneId.of("UTC"));
		Timestamp newTimestamp = Timestamp.from(zonedDateTime.minus(6, ChronoUnit.MONTHS).toInstant());

		String acceptableStartDate=df.format( new Date(newTimestamp.getTime()));
		String acceptableEndDate=df.format( new Date(transaction.getCreatedAt().getTime()));
		
		ExchangeRate rate=rateList.stream().sorted((r1,r2)->-r1.getEffectiveDate().compareTo(r2.getEffectiveDate())).filter(r-> checkExchangeDate(r.getEffectiveDate(), acceptableStartDate, acceptableEndDate )).findFirst().get();
		
		return TransactionDTO.builder()
				.transactionId( transaction.getTransactionId())
				.description( transaction.getDescription())
				.amountInUSD( transaction.getAmount())
				.currency(rate.getCountryCurrencyDesc())
				.exchangeRate( rate.getExchangeRate())
				.exchangeRateEfferctiveDate( rate.getEffectiveDate())
				.amount( transaction.getAmount().multiply(rate.getExchangeRate()).setScale(2,RoundingMode.HALF_UP))
				.transactionDate(df.format(new Date(transaction.getCreatedAt().getTime()))).build();
		
	}
	
	private boolean checkExchangeDate(String date, String startDate, String endDate) {
		int dateInt=exchangeDateToInt(date);
		int startDateInt=exchangeDateToInt(startDate);
		int endDateInt=exchangeDateToInt(endDate);
		if ( dateInt > startDateInt && dateInt < endDateInt ) {
			return true;
		}
		
		return false;
	}
	
	private int exchangeDateToInt( String date){
		
		return Integer.valueOf( date.replaceAll("-", ""));
	}
	

}
