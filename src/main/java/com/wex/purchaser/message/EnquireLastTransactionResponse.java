package com.wex.purchaser.message;

import com.wex.purchaser.dto.TransactionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnquireLastTransactionResponse extends AbstractResponse{

	private TransactionDTO transaction;
	
}
