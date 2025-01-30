package ru.t1.java.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.service.DataSourceErrorLogService;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogDataSourceErrorAspect {

    @Autowired
    private DataSourceErrorLogService errorLogService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_NAME = "t1_demo_metrics";
    private static final String ERROR_TYPE = "DATA_SOURCE";

    @AfterThrowing(pointcut = "@annotation(ru.t1.java.demo.aop.annotation.LogDataSourceError)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String stackTrace = getStackTraceAsString(ex);
        String message = ex.getMessage();
        String methodName = methodSignature.toShortString();

        // Формируем сообщение для Kafka
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorType", ERROR_TYPE);
        errorDetails.put("methodName", methodName);
        errorDetails.put("message", message);
        errorDetails.put("stackTrace", stackTrace);

        String kafkaMessage = convertMapToJson(errorDetails);

        // Пытаемся отправить сообщение в Kafka
        try {
            System.out.println("kafka: " + TOPIC_NAME + " " + ERROR_TYPE + " " + kafkaMessage);
            sendMessageToKafka(TOPIC_NAME, ERROR_TYPE, kafkaMessage);
        } catch (Exception kafkaException) {
            // Если отправка в Kafka не удалась, записываем ошибку в БД
            errorLogService.logError(stackTrace, message, methodName);
        }
    }

    private void sendMessageToKafka(String topic, String errorType, String message) {
        // Создаем сообщение с заголовком
        Message<String> kafkaMessage = MessageBuilder.withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("errorType", errorType)
                .build();

        // Отправляем сообщение
        kafkaTemplate.send(kafkaMessage);
    }

    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        json.deleteCharAt(json.length() - 1); // Удаляем последнюю запятую
        json.append("}");
        return json.toString();
    }

    private String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}