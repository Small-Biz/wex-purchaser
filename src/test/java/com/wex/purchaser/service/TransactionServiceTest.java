package com.wex.purchaser.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



import com.wex.purchaser.dto.TransactionDTO;
import com.wex.purchaser.entity.Transaction;
import com.wex.purchaser.exception.ServiceException;
import com.wex.purchaser.fiscalapi.ExchangeRate;
import com.wex.purchaser.fiscalapi.FiscalApi;
import com.wex.purchaser.message.CreateTransactionRequest;
import com.wex.purchaser.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
	
	@InjectMocks
	private TransactionService service;
	
	@Mock
	private TransactionRepository repository;
	
	@Mock
	private FiscalApi api;
	
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	//insert success
	//insert with null description
	//insert with null amount
	//insert with negative amount
	//insert with too long description
	//insert with amount with dp more than 2
	
	//enquire success
	//enquire with null currency
	//enquire with no related exchange rate
	//enquire with related exchange rate which is longer than 6 months before
	
	//list success
	//list with null currency
	//list with no related exchange rate
	//list with related exchange rate which is longer than 6 months before
	
	@BeforeEach
	public void setup() {
	}
	
	@DisplayName("Transaction-Insert success")
	@Test
	public void createTransaction_Normal() throws ServiceException {
		
		Transaction transaction=Transaction.builder()
				.description("Transaction Description")
				.amount(new BigDecimal(123))
				.createdAt(new Timestamp( System.currentTimeMillis() ))
				.build();	
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription("Transaction Description");
		request.setAmount(new BigDecimal(123));		
		
		when(repository.save(Mockito.any(Transaction.class)))
        .thenAnswer(i -> i.getArguments()[0]);
		
		Transaction savedData=service.createTransaction(request);
		
		assertEquals("Transaction Description", savedData.getDescription());
		assertEquals(new BigDecimal(123), savedData.getAmount());
	}
	
	@DisplayName("Transaction-Insert with null description")
	@Test
	public void createTransaction_NullDescription(){
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription(null);
		request.setAmount(new BigDecimal(123));		
		
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.createTransaction(request);
		});
		
		String expectedMessage = "Description must not be null";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Insert with null amount")
	@Test
	public void createTransaction_nullAmount() throws ServiceException {
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription("Transaction Description");
		request.setAmount(null);		
		
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.createTransaction(request);
		});
		
		String expectedMessage = "Amount must not be null";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Insert with negative amount")
	@Test
	public void createTransaction_negativeAmount() throws ServiceException {
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription("Transaction Description");
		request.setAmount(new BigDecimal(-123));		
		
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.createTransaction(request);
		});
		
		String expectedMessage = "Amount should be positive";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Insert with too long description")
	@Test
	public void createTransaction_tooLongDescription() throws ServiceException {
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription("Transaction Description Transaction Description dekj Transaction Description dekj Transaction Description dekj Transaction Description dekj Transaction Description dekj Transaction Description dekj ");
		request.setAmount(new BigDecimal(123));		
		
		Exception exception=assertThrows(ServiceException.class, ()->{
				service.createTransaction(request);
		});
		
		String expectedMessage = "Description is too longht ( max. 50 )";
	    String actualMessage = exception.getMessage();

	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Insert with amount's dp more than 2")
	@Test
	public void createTransaction_AmountDpMoreThan2() throws ServiceException {
		
		CreateTransactionRequest request=new CreateTransactionRequest();
		request.setDescription("Transaction Description");
		request.setAmount(new BigDecimal(123.122));		
		
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.createTransaction(request);
		});
		
		String expectedMessage = "Amount's decimal place should less than or equal to 2 only";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Enquire success")
	@Test
	public void enquireTransactions_success() throws Exception {
		
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.831))
				.effectiveDate("2024-03-31")
				.build();
		ExchangeRate rate2=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.821))
				.effectiveDate("2023-12-31")
				.build();
		ExchangeRate rate3=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.841))
				.effectiveDate("2023-09-30")
				.build();
		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		rates.add(rate2);
		rates.add(rate3);
		
		when(repository.findFirstByOrderByCreatedAt()).thenReturn(transaction1);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
		
		TransactionDTO transaction=service.enquireLastTransaction("Hong Kong-Dollar");
		
		assertEquals("Transaction 1 Description", transaction.getDescription());
		assertEquals(0, new BigDecimal(7831).compareTo(transaction.getAmount()));
		assertEquals(0, new BigDecimal(1000).compareTo(transaction.getAmountInUSD()));
		assertEquals(0, new BigDecimal(7.831).compareTo(transaction.getExchangeRate()));
		assertEquals("2024-04-28", transaction.getTransactionDate());
		assertEquals("Hong Kong-Dollar", transaction.getCurrency());
				
	}
	
	@DisplayName("Transaction-Enquire success 2dp scale ")
	@Test
	public void enquireTransactions_success_2DpScale() throws Exception {
		
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(10))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.8356))
				.effectiveDate("2024-03-31")
				.build();
		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		
		when(repository.findFirstByOrderByCreatedAt()).thenReturn(transaction1);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
		
		TransactionDTO transaction=service.enquireLastTransaction("Hong Kong-Dollar");
		
		assertEquals("Transaction 1 Description", transaction.getDescription());
		assertEquals(0, new BigDecimal(78.36).setScale(2, RoundingMode.HALF_UP).compareTo(transaction.getAmount()));
		assertEquals(0, new BigDecimal(10).compareTo(transaction.getAmountInUSD()));
		assertEquals(0, new BigDecimal(7.8356).compareTo(transaction.getExchangeRate()));
		assertEquals("2024-04-28", transaction.getTransactionDate());
		assertEquals("Hong Kong-Dollar", transaction.getCurrency());
				
	}
	
	@DisplayName("Transaction-Enquire with null currency")
	@Test
	public void enquireTransaction_nullCurrency() throws Exception {
			
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.enquireLastTransaction(null);
		});
		
		String expectedMessage = "Currency must not be null";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-Enquire without exchange rate")
	@Test
	public void enquireTransactions_returnNoneExchangeRate() throws Exception {
		
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.841))
				.effectiveDate("2023-09-30")
				.build();
		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		
		when(repository.findFirstByOrderByCreatedAt()).thenReturn(transaction1);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
				
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.enquireLastTransaction("Hong Kong-Dollar");
		});
		
		String expectedMessage = "Exchange rate not found";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@DisplayName("Transaction-Enquire with exchange rate over 6 months")
	@Test
	public void enquireTransactions_returnExchangeRateOver6Months() throws Exception {
		
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		List<ExchangeRate> rates= new ArrayList<>();
		
		when(repository.findFirstByOrderByCreatedAt()).thenReturn(transaction1);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
				
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.enquireLastTransaction("Hong Kong-Dollar");
		});
		
		String expectedMessage = "Exchange rate not found";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
	}
	
	
	
	@DisplayName("Transaction-List success")
	@Test
	public void listTransactions_success() throws Exception {
		
		List<Transaction> list=new ArrayList<>();
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		Transaction transaction2=Transaction.builder()
				.transactionId(2)
				.description("Transaction 2 Description")
				.amount(new BigDecimal(2000))
				.createdAt(new Timestamp( df.parse("2023-12-28").getTime() ))
				.build();
		
		list.add(transaction1);
		list.add(transaction2);
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.831))
				.effectiveDate("2024-03-31")
				.build();
		ExchangeRate rate2=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.821))
				.effectiveDate("2023-12-31")
				.build();
		ExchangeRate rate3=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.841))
				.effectiveDate("2023-09-30")
				.build();
		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		rates.add(rate2);
		rates.add(rate3);
		
		when(repository.findAll()).thenReturn(list);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-06-28"))
		.thenReturn(rates);
		
		List<TransactionDTO> transactions=service.listTransaction("Hong Kong-Dollar");
		
		assertEquals(2, transactions.size());
		assertEquals("Transaction 1 Description", transactions.get(0).getDescription());
		assertEquals(0, new BigDecimal(7831).compareTo(transactions.get(0).getAmount()));
		assertEquals(0, new BigDecimal(1000).compareTo(transactions.get(0).getAmountInUSD()));
		assertEquals(0, new BigDecimal(7.831).compareTo(transactions.get(0).getExchangeRate()));
		assertEquals("2024-04-28", transactions.get(0).getTransactionDate());
		assertEquals("Hong Kong-Dollar", transactions.get(0).getCurrency());
		
		assertEquals("Transaction 2 Description", transactions.get(1).getDescription());
		assertEquals(0, new BigDecimal(15682).compareTo(transactions.get(1).getAmount()));
		assertEquals(0, new BigDecimal(2000).compareTo(transactions.get(1).getAmountInUSD()));
		assertEquals(0, new BigDecimal(7.841).compareTo(transactions.get(1).getExchangeRate()));
		assertEquals("2023-12-28", transactions.get(1).getTransactionDate());
		assertEquals("Hong Kong-Dollar", transactions.get(1).getCurrency());
		
	}
	
	@DisplayName("Transaction-List success 2dp scale")
	@Test
	public void listTransactions_success_2DpScale() throws Exception {
		
		List<Transaction> list=new ArrayList<>();
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(10))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		list.add(transaction1);
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.8356))
				.effectiveDate("2024-03-31")
				.build();

		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		
		when(repository.findAll()).thenReturn(list);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
		
		List<TransactionDTO> transactions=service.listTransaction("Hong Kong-Dollar");
		
		assertEquals(1, transactions.size());
		assertEquals("Transaction 1 Description", transactions.get(0).getDescription());
		assertEquals(0, new BigDecimal(78.36).setScale(2, RoundingMode.HALF_UP).compareTo(transactions.get(0).getAmount()));
		assertEquals(0, new BigDecimal(10).compareTo(transactions.get(0).getAmountInUSD()));
		assertEquals(0, new BigDecimal(7.8356).compareTo(transactions.get(0).getExchangeRate()));
		assertEquals("2024-04-28", transactions.get(0).getTransactionDate());
		assertEquals("Hong Kong-Dollar", transactions.get(0).getCurrency());
				
	}
	
	@DisplayName("Transaction-List with null currency")
	@Test
	public void listTransaction_nullCurrency() throws Exception {
			
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.listTransaction(null);
		});
		
		String expectedMessage = "Currency must not be null";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
		
	}
	
	@DisplayName("Transaction-List without exchange rate")
	@Test
	public void listTransactions_returnNoneExchangeRate() throws Exception {
		
		List<Transaction> list=new ArrayList<>();
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		list.add(transaction1);
		
		ExchangeRate rate1=ExchangeRate.builder()
				.countryCurrencyDesc("Hong Kong-Dollar")
				.exchangeRate(new BigDecimal(7.841))
				.effectiveDate("2023-09-30")
				.build();
		List<ExchangeRate> rates= new ArrayList<>();
		rates.add(rate1);
		
		when(repository.findAll()).thenReturn(list);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
				
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.listTransaction("Hong Kong-Dollar");
		});
		
		String expectedMessage = "Exchange rate not found";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@DisplayName("Transaction-List with exchange rate over 6 months")
	@Test
	public void listTransactions_returnExchangeRateOver6Months() throws Exception {
		
		List<Transaction> list=new ArrayList<>();
		Transaction transaction1=Transaction.builder()
				.transactionId(1)
				.description("Transaction 1 Description")
				.amount(new BigDecimal(1000))
				.createdAt(new Timestamp( df.parse("2024-04-28").getTime() ))
				.build();
		
		list.add(transaction1);
		
		List<ExchangeRate> rates= new ArrayList<>();
		
		when(repository.findAll()).thenReturn(list);

		when(api.parseExchangeRate("Hong Kong-Dollar", "2023-10-28"))
		.thenReturn(rates);
				
		Exception exception=assertThrows(ServiceException.class, ()->{
			service.listTransaction("Hong Kong-Dollar");
		});
		
		String expectedMessage = "Exchange rate not found";
	    String actualMessage = exception.getMessage();
	
	    assertTrue(actualMessage.contains(expectedMessage));
	}
	
}
