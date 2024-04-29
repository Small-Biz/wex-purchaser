package com.wex.purchaser.controller;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wex.purchaser.dto.TransactionDTO;
import com.wex.purchaser.exception.ServiceException;
import com.wex.purchaser.message.AbstractResponse;
import com.wex.purchaser.message.CreateTransactionRequest;
import com.wex.purchaser.message.CreateTransactionResponse;
import com.wex.purchaser.message.EnquireLastTransactionResponse;
import com.wex.purchaser.message.ListTransactionResponse;
import com.wex.purchaser.service.TransactionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin()
@RequestMapping(path = "/api/purchaser")
@Slf4j
public class TransactionController {

	@Autowired
	private TransactionService service;
	
	@PostMapping("/transaction")
	public ResponseEntity<AbstractResponse> create(@RequestBody CreateTransactionRequest request) throws ServiceException{

		log.info("create transaction");
		try {
			service.createTransaction(request);
			CreateTransactionResponse response=new CreateTransactionResponse();
			log.info("create transaction done");
			return ResponseEntity.ok(response);
		} catch (ServiceException e) {
			return ResponseEntity.ok( new AbstractResponse(e.getMessage()));			
		}
	}
	
	@GetMapping("/transaction")
	public ResponseEntity<AbstractResponse> enquireLastTransaction(@PathParam("currency") String currency){
				
		log.info("enquire transaction");
				
		try {
			TransactionDTO transaction = service.enquireLastTransaction(currency);
			log.info("last transaction: " + transaction);
			return ResponseEntity.ok( new EnquireLastTransactionResponse(transaction));
		} catch (ServiceException e) {
			return ResponseEntity.ok( new AbstractResponse(e.getMessage()));			
		}
	}
	
	@GetMapping("/transactions")
	public ResponseEntity<AbstractResponse> list(@PathParam("currency") String currency){
				
		log.info("list transaction");
		
		try {
			List<TransactionDTO> transactionList=service.listTransaction(currency);
			log.info("list transaction size: " + transactionList.size());
			return ResponseEntity.ok( new ListTransactionResponse(transactionList));			
		} catch (ServiceException e) {
			return ResponseEntity.ok( new AbstractResponse(e.getMessage()));			
		}
						
	}
	
}
