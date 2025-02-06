package ru.t1.java.demo.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.entity.Account;
import ru.t1.java.demo.entity.Transaction;
import ru.t1.java.demo.kafka.KafkaProducerService;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionConsumer {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "t1_demo_transactions", groupId = "demo-group")
    @Transactional
    public void listenTransaction(String message) {
        try {
            Transaction transaction = objectMapper.readValue(message, Transaction.class);

            Optional<Account> optionalAccount = accountRepository.findByAccountId(transaction.getAccountId());
            if (optionalAccount.isEmpty() || !optionalAccount.get().getStatus().equals(Account.AccountStatus.OPEN)) {
                System.err.println("Счет закрыт или заблокирован. Транзакция отклонена.");
                return;
            }

            Account account = optionalAccount.get();

            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setStatus(Transaction.TransactionStatus.REQUESTED);
            transactionRepository.save(transaction);

            Double newBalance = account.getBalance() + transaction.getAmount();
            account.setBalance(newBalance);
            accountRepository.save(account);

            Map<String, Object> transactionAcceptMessage = Map.of(
                    "clientId", account.getClientId(),
                    "accountId", account.getAccountId(),
                    "transactionId", transaction.getTransactionId(),
                    "timestamp", transaction.getTransactionTime().toString(),
                    "amount", transaction.getAmount(),
                    "balance", account.getBalance()
            );

            kafkaProducerService.sendTransaction(objectMapper.writeValueAsString(transactionAcceptMessage));
            System.out.println("Транзакция успешно обработана и отправлена в топик t1_demo_transaction_accept.");

        } catch (Exception e) {
            System.err.println("Ошибка при обработке транзакции: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "t1_demo_transaction_result", groupId = "demo-group")
    @Transactional
    public void listenTransactionResult(String message) {
        try {
            Transaction transactionResult = objectMapper.readValue(message, Transaction.class);

            Optional<Transaction> optionalTransaction = transactionRepository.findById(Long.valueOf(transactionResult.getTransactionId()));
            if (optionalTransaction.isEmpty()) {
                System.err.println("Транзакция не найдена: " + transactionResult.getTransactionId());
                return;
            }

            Transaction transaction = optionalTransaction.get();
            Account account = accountRepository.findByAccountId(transaction.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Счет не найден: " + transaction.getAccountId()));

            switch (transactionResult.getStatus()) {
                case ACCEPTED:
                    transaction.setStatus(Transaction.TransactionStatus.ACCEPTED);
                    transactionRepository.save(transaction);
                    System.out.println("Транзакция ACCEPTED: " + transaction.getTransactionId());
                    break;

                case BLOCKED:
                    transaction.setStatus(Transaction.TransactionStatus.BLOCKED);
                    transactionRepository.save(transaction);

                    // Блокировка счета и заморозка суммы
                    account.setStatus(Account.AccountStatus.BLOCKED);
                    account.setFrozenAmount(account.getFrozenAmount() + transaction.getAmount());
                    accountRepository.save(account);
                    System.out.println("Транзакция BLOCKED: " + transaction.getTransactionId());
                    break;

                case REJECTED:
                    transaction.setStatus(Transaction.TransactionStatus.REJECTED);
                    transactionRepository.save(transaction);

                    // Корректировка баланса счета
                    account.setBalance(account.getBalance() - transaction.getAmount());
                    accountRepository.save(account);
                    System.out.println("Транзакция REJECTED: " + transaction.getTransactionId());
                    break;

                default:
                    System.err.println("Неизвестный статус транзакции: " + transactionResult.getStatus());
                    break;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обработке результата транзакции: " + e.getMessage());
            e.printStackTrace();
        }
    }
}