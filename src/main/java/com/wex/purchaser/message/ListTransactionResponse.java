package com.wex.purchaser.message;

import java.util.List;


import com.wex.purchaser.dto.TransactionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListTransactionResponse extends AbstractResponse{

	private List<TransactionDTO> transactionList;
}
