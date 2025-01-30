package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.entity.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;

import java.time.LocalDateTime;

@Service
public class TransactionConsumer {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "t1_demo_transactions", groupId = "demo-group")
    @Transactional
    public void listenTransaction(String message) {
        try {
            Transaction transaction = objectMapper.readValue(message, Transaction.class);

            transaction.setTransactionTime(LocalDateTime.now());

            transactionRepository.save(transaction);

            System.out.println("Транзакция успешно сохранена: " + transaction);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке сообщения: " + e.getMessage());
        }
    }
}