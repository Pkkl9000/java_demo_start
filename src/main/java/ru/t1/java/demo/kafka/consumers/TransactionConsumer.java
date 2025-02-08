package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "t1_demo_transactions", groupId = "t1-demo")
    public void listenTransaction(ConsumerRecord<String, String> record) {
        try {
            // Десериализация сообщения в TransactionDto
            TransactionDto transactionDto = objectMapper.readValue(record.value(), TransactionDto.class);

            // Генерация случайного UUID для id, если он не указан
            if (transactionDto.getId() == null) {
                transactionDto.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            }

            // Сохранение транзакции в БД через сервис
            transactionService.createTransaction(transactionDto);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке сообщения из топика t1_demo_transactions: " + e.getMessage());
        }
    }
}
