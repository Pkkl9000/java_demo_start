package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.entity.Account;
import ru.t1.java.demo.repository.AccountRepository;

@Service
public class AccountConsumer {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "t1_demo_accounts", groupId = "demo-group")
    @Transactional
    public void listenAccount(String message) {
        try {
            Account account = objectMapper.readValue(message, Account.class);

            accountRepository.save(account);

            System.out.println("Счет успешно сохранен: " + account);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке сообщения: " + e.getMessage());
        }
    }
}