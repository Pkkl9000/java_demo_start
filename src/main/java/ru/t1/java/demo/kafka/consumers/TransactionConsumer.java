package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.entity.enums.AccountStatus;
import ru.t1.java.demo.entity.enums.TransactionStatus;
import ru.t1.java.demo.kafka.KafkaProducerService;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.monitor.message.TransactionResultMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    /**
     * Метод для прослушивания топика t1_demo_transactions.
     */
    @KafkaListener(topics = "t1_demo_transactions", groupId = "transaction_group")
    public void listenTransaction(ConsumerRecord<String, String> record) {
        try {
            // Десериализация сообщения в TransactionDto
            TransactionDto transactionDto = objectMapper.readValue(record.value(), TransactionDto.class);

            // Генерация случайного UUID для transactionId, если он не указан
            if (transactionDto.getTransactionId() == null || transactionDto.getTransactionId().isEmpty()) {
                transactionDto.setTransactionId(UUID.randomUUID().toString());
            }

            // Получение информации о счете
            AccountDto accountDto = accountService.getAccountById(transactionDto.getAccountId());
            if (accountDto != null && accountDto.getStatus() == AccountStatus.OPEN) {

                // Установка статуса транзакции как REQUESTED
                transactionDto.setStatus(TransactionStatus.REQUESTED);
                // Сохранение транзакции в БД
                TransactionDto savedTransaction = transactionService.createTransaction(transactionDto);

                // Изменение баланса счета
                BigDecimal newBalance = accountDto.getBalance().add(BigDecimal.valueOf(transactionDto.getAmount()));
                accountService.updateAccountBalance(accountDto.getId(), newBalance);

                // Подготовка и отправка сообщения в топик t1_demo_transaction_accept
                TransactionAcceptMessage transactionAcceptMessage = new TransactionAcceptMessage(
                        accountDto.getClientId(),
                        accountDto.getAccountId(),
                        savedTransaction.getId(),
                        LocalDateTime.now(),
                        savedTransaction.getAmount(),
                        accountDto.getBalance()
                );

                // Отправка сообщения в топик t1_demo_transaction_accept
                kafkaProducerService.sendMessage("t1_demo_transaction_accept", objectMapper.writeValueAsString(transactionAcceptMessage));
            } else {
                log.error("Счет закрыт или заблокирован. Транзакция отклонена.");
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения из топика t1_demo_transactions: {}", e.getMessage(), e);
        }
    }

    /**
     * Метод для прослушивания топика t1_demo_transaction_result.
     */
    @KafkaListener(topics = "t1_demo_transaction_result", groupId = "transaction_group")
    public void listenTransactionResult(ConsumerRecord<String, String> record) {
        try {
            // Десериализация сообщения в TransactionResultMessage
            TransactionResultMessage resultMessage = objectMapper.readValue(record.value(), TransactionResultMessage.class);

            switch (resultMessage.getStatus()) {
                case ACCEPTED:
                    handleAcceptedTransaction(resultMessage);
                    break;
                case BLOCKED:
                    handleBlockedTransaction(resultMessage);
                    break;
                case REJECTED:
                    handleRejectedTransaction(resultMessage);
                    break;
                default:
                    log.warn("Неизвестный статус транзакции: {}", resultMessage.getStatus());
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения из топика t1_demo_transaction_result: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка транзакции со статусом ACCEPTED.
     */
    private void handleAcceptedTransaction(TransactionResultMessage resultMessage) {
        try {
            // Обновление статуса транзакции в БД
            TransactionDto transactionDto = transactionService.getTransactionById(resultMessage.getTransactionId());
            transactionDto.setStatus(TransactionStatus.ACCEPTED);
            transactionService.updateTransaction(resultMessage.getTransactionId(), transactionDto);
            log.info("Транзакция {} успешно обработана со статусом ACCEPTED.", resultMessage.getTransactionId());
        } catch (Exception e) {
            log.error("Ошибка при обновлении транзакции со статусом ACCEPTED: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка транзакции со статусом BLOCKED.
     */
    private void handleBlockedTransaction(TransactionResultMessage resultMessage) {
        try {
            // Обновление статуса транзакции в БД
            TransactionDto transactionDto = transactionService.getTransactionById(resultMessage.getTransactionId());
            transactionDto.setStatus(TransactionStatus.BLOCKED);

            transactionService.updateTransaction(resultMessage.getTransactionId(), transactionDto);

            // Обновление статуса счета и frozenAmount
            AccountDto accountDto = accountService.getAccountById(resultMessage.getAccountId());
            if (accountDto != null) {
                accountDto.setStatus(AccountStatus.BLOCKED); // Устанавливаем статус счета как BLOCKED
                accountDto.setFrozenAmount(accountDto.getFrozenAmount().add(BigDecimal.valueOf(transactionDto.getAmount())));

                accountService.updateAccount(resultMessage.getAccountId(), accountDto);
                log.info("Счет с ID {} успешно обновлен со статусом BLOCKED и frozenAmount увеличен.", resultMessage.getAccountId());
            } else {
                log.error("Счет с ID {} не найден.", resultMessage.getAccountId());
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке BLOCKED транзакции с ID {}: {}", resultMessage.getTransactionId(), e.getMessage(), e);
        }
    }

    /**
     * Обработка транзакции со статусом REJECTED.
     */
    private void handleRejectedTransaction(TransactionResultMessage resultMessage) {
        try {
            // Обновление статуса транзакции в БД
            TransactionDto transactionDto = new TransactionDto();
            transactionDto.setId(resultMessage.getTransactionId());
            transactionDto.setStatus(TransactionStatus.REJECTED);

            transactionService.updateTransaction(resultMessage.getTransactionId(), transactionDto);

            // Уменьшение баланса счета
            AccountDto accountDto = accountService.getAccountById(resultMessage.getAccountId());
            if (accountDto != null) {
                BigDecimal updatedBalance = accountDto.getBalance().subtract(BigDecimal.valueOf(transactionDto.getAmount()));

                accountDto.setBalance(updatedBalance);

                accountService.updateAccount(resultMessage.getAccountId(), accountDto);
                log.info("Счет с ID {} успешно обновлен после REJECTED транзакции.", resultMessage.getAccountId());
            } else {
                log.error("Счет с ID {} не найден.", resultMessage.getAccountId());
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке REJECTED транзакции с ID {}: {}", resultMessage.getTransactionId(), e.getMessage(), e);
        }
    }
}
