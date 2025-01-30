package ru.t1.java.demo.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendAccount(String accountJson) {
        kafkaTemplate.send("t1_demo_accounts", UUID.randomUUID().toString(), accountJson);
    }

    public void sendTransaction(String transactionJson) {
        kafkaTemplate.send("t1_demo_transactions", UUID.randomUUID().toString(), transactionJson);
    }
}