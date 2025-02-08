package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

@Service
@RequiredArgsConstructor
public class AccountConsumer {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "t1_demo_accounts", groupId = "t1-demo")
    public void listenAccount(ConsumerRecord<String, String> record) {
        try {
            // Десериализация сообщения в AccountDto
            AccountDto accountDto = objectMapper.readValue(record.value(), AccountDto.class);

            // Сохранение счета в БД через сервис
            accountService.createAccount(accountDto);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке сообщения из топика t1_demo_accounts: " + e.getMessage());
        }
    }
}