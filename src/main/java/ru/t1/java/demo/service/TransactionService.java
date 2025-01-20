package ru.t1.java.demo.service;

import ru.t1.java.demo.entity.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(Long accountId, Double amount);

    List<Transaction> getAllTransactions();

    List<Transaction> getTransactionsByAccountId(Long accountId);

    Transaction getTransactionById(Long id);

    void deleteTransaction(Long id);
}
