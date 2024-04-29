package com.wex.purchaser.fiscalapi;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wex.purchaser.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * Fiscal API for obtain exchange rate from FiscalData
 * @author    Ray Cheng
 */
@Slf4j
@Component
public class FiscalApi {

	//https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange
	//?fields=country_currency_desc,exchange_rate,record_date
	//&filter=country_currency_desc:in:(Canada-Dollar)
	//,record_date:gte:2020-01-01
	final static String EXCHANGE_RATE_ENDPOINT="https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc,exchange_rate,effective_date,record_date&filter=country_currency_desc:in:(%s),effective_date:gte:%s";

	   /**
	   * This method is call fiscal API with current and date
	   * , then parse the exchange rate list from the response
	   * @param currency The currency for the exchange rate
	   * @param date This is the earliest date. string and in yyyy-MM-dd format
	   * @return exchangeRateList It return the exchange rate list return from the fisal API
	   */
	public List<ExchangeRate> parseExchangeRate( String currency, String date) throws ServiceException {
		 
		log.info("currency: "+ currency+ " date: " + date);
		
		RestTemplate template=new RestTemplate();
		String url=String.format(EXCHANGE_RATE_ENDPOINT, currency, date);

		ResponseEntity<ApiResponse> response=template.getForEntity(url, ApiResponse.class);		
		
		System.out.println("response: " + response);
		
		System.out.println("response.getBody().: " + response);
		
		List<ExchangeRate> rateList=response.getBody().getData();
		
		if (rateList.size()==0) {
			throw new ServiceException("Exchange Rate not found");
		}
		
		return rateList;
	}
	
	public static void main(String[] avg) throws ServiceException {
		FiscalApi api=new FiscalApi();
		
		api.parseExchangeRate("Canada-Dollar", "2023-01-01");
	}
}
