package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.java.demo.entity.Transaction;

import java.util.List;
//import ru.t1.java.demo.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
}