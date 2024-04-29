package com.wex.purchaser.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
* Hero is the main entity we'll be using for purchase transaction
* 
* @author Ray Cheng
* 
*/

@Entity
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="transactions")
public class Transaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="transaction_id")
	private long transactionId;

	@Column(name="amount", nullable = false)
	private BigDecimal amount;
	
	@Column(name="description", length=50, nullable = false)
	private String description;
	
	@Column(name="created_at", nullable = false)
	private Timestamp createdAt;
		
}
