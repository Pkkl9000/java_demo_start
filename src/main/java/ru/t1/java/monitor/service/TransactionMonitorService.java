package ru.t1.java.monitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.entity.enums.TransactionStatus;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;
import ru.t1.java.monitor.config.TransactionThresholdConfig;
import ru.t1.java.monitor.message.TransactionResultMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionMonitorService {

    private final KafkaTemplate<String, TransactionResultMessage> kafkaTemplate;
    private final TransactionThresholdConfig transactionThresholdConfig;
    private final Map<String, List<TransactionAcceptMessage>> transactionMap = new HashMap<>();

    @KafkaListener(topics = "t1_demo_transaction_accept", groupId = "transaction_group")
    public void listen(TransactionAcceptMessage message) {
        String key = message.getClientId() + "_" + message.getAccountId();
        transactionMap.putIfAbsent(key, new ArrayList<>());
        transactionMap.get(key).add(message);

        // Удаляем старые транзакции
        Duration timeWindow = Duration.ofSeconds(transactionThresholdConfig.getTimeWindowSeconds());
        transactionMap.get(key).removeIf(m -> Duration.between(m.getTimestamp(), LocalDateTime.now()).compareTo(timeWindow) > 0);

        // Проверяем количество транзакций
        if (transactionMap.get(key).size() > transactionThresholdConfig.getCount()) {
            blockTransactions(message);
        } else if (message.getTransactionAmount() > message.getAccountBalance().doubleValue()) {
            rejectTransaction(message);
        } else {
            acceptTransaction(message);
        }
    }

    private void blockTransactions(TransactionAcceptMessage message) {
        // Отправляем сообщение со статусом BLOCKED для каждой из N транзакций
        for (TransactionAcceptMessage acceptMessage : transactionMap.get(message.getClientId() + "_" + message.getAccountId())) {
            TransactionResultMessage result = new TransactionResultMessage(TransactionStatus.BLOCKED, acceptMessage.getAccountId(), acceptMessage.getTransactionId());
            kafkaTemplate.send("t1_demo_transaction_result", result);
        }
    }

    private void rejectTransaction(TransactionAcceptMessage message) {
        // Отправляем сообщение со статусом REJECTED
        TransactionResultMessage result = new TransactionResultMessage(TransactionStatus.REJECTED, message.getAccountId(), message.getTransactionId());
        kafkaTemplate.send("t1_demo_transaction_result", result);
    }

    private void acceptTransaction(TransactionAcceptMessage message) {
        // Отправляем сообщение со статусом ACCEPTED
        TransactionResultMessage result = new TransactionResultMessage(TransactionStatus.ACCEPTED, message.getAccountId(), message.getTransactionId());
        kafkaTemplate.send("t1_demo_transaction_result", result);
    }
}

