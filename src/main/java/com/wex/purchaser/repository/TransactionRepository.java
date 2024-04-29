package com.wex.purchaser.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wex.purchaser.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long>{

	public Transaction findFirstByOrderByCreatedAt();
}
