package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Метод для отправки сообщения в Kafka с заголовками.
     *
     * @param topic       Имя топика.
     * @param errorType   Тип ошибки (заголовок).
     * @param messageBody Содержимое сообщения (JSON-строка).
     */
    public void sendErrorMessage(String topic, String errorType, String messageBody) {
        try {
            // Создаем сообщение с заголовками
            Message<String> kafkaMessage = MessageBuilder.withPayload(messageBody)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("errorType", errorType) // Добавляем заголовок "errorType"
                    .build();

            // Отправляем сообщение в Kafka
            kafkaTemplate.send(kafkaMessage);
            System.out.println("Сообщение успешно отправлено в топик: " + topic);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправке сообщения в Kafka", e);
        }
    }
}