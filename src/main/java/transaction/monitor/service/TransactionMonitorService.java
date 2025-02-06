package transaction.monitor.service;

import org.springframework.beans.factory.annotation.Value;
import transaction.monitor.entity.Transaction;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionMonitorService {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final int maxCount;
    private final long timeWindow;

    private final Map<String, Map<Instant, Integer>> transactionCounts = new HashMap<>();

    public TransactionMonitorService(KafkaTemplate<String, Transaction> kafkaTemplate,
                                     @Value("${t1.transaction.max-count}") int maxCount,
                                     @Value("${t1.transaction.time-window}") long timeWindow) {
        this.kafkaTemplate = kafkaTemplate;
        this.maxCount = maxCount;
        this.timeWindow = timeWindow;
    }

    @KafkaListener(topics = "t1_demo_transaction_accept")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void listen(ConsumerRecord<String, Transaction> record,
                       Acknowledgment acknowledgment,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Transaction transaction = record.value();
            String clientId = String.valueOf(transaction.getClientId());
            String accountId = String.valueOf(transaction.getAccountId());
            String key = clientId + ":" + accountId;

            if (transaction.getAmount() > getAccountBalance(accountId)) {
                transaction.setStatus(Transaction.TransactionStatus.valueOf("REJECTED"));
                sendToResultTopic(transaction);
                acknowledgment.acknowledge();
                return;
            }

            Map<Instant, Integer> counts = transactionCounts.computeIfAbsent(key, k -> new HashMap<>());
            int count = counts.getOrDefault(transaction.getTimestamp(), 0) + 1;
            counts.put(transaction.getTimestamp(), count);

            if (count > maxCount && isWithinTimeWindow(transaction.getTimestamp())) {
                transaction.setStatus(Transaction.TransactionStatus.valueOf("BLOCKED"));
            } else {
                transaction.setStatus(Transaction.TransactionStatus.valueOf("ACCEPTED"));
            }

            sendToResultTopic(transaction);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            System.err.println("Ошибка при обработке транзакции: " + e.getMessage());
            throw e;
        }
    }

    private void sendToResultTopic(Transaction transaction) {
        try {
            kafkaTemplate.send("t1_demo_transaction_result", transaction);
        } catch (Exception e) {
            System.err.println("Ошибка при отправке сообщения в Kafka: " + e.getMessage());
            throw e;
        }
    }

    private boolean isWithinTimeWindow(Instant timestamp) {
        Instant now = Instant.now();
        return timestamp.isAfter(now.minus(timeWindow, ChronoUnit.MILLIS));
    }

    private double getAccountBalance(String accountId) {
        return 1000.0;
    }
}