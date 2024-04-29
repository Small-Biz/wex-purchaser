package com.wex.purchaser.fiscalapi;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wex.purchaser.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FiscalApi {

	//https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange
	//?fields=country_currency_desc,exchange_rate,record_date
	//&filter=country_currency_desc:in:(Canada-Dollar)
	//,record_date:gte:2020-01-01
	final static String EXCHANGE_RATE_ENDPOINT="https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc,exchange_rate,effective_date,record_date&filter=country_currency_desc:in:(%s),effective_date:gte:%s";

	//date: in yyyy-MM-dd format
	public List<ExchangeRate> parseExchangeRate( String toCurrency, String date) throws ServiceException {
		 
		log.info("toCurrency: "+ toCurrency+ " date: " + date);
		
		RestTemplate template=new RestTemplate();
		String url=String.format(EXCHANGE_RATE_ENDPOINT, toCurrency, date);

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
