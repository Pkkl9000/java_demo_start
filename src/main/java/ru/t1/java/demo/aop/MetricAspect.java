package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.aop.annotation.Metric;
import ru.t1.java.demo.kafka.KafkaProducerService;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final KafkaProducerService kafkaProducerService;
    private static final String TOPIC_NAME = "t1_demo_metrics";
    private static final String ERROR_TYPE = "METRICS";

    @Around("@annotation(metric)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed(); // Выполнение целевого метода
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            if (executionTime > metric.timeout()) { // Проверка превышения допустимого времени
                sendMetricToKafka(joinPoint, executionTime);
            }
        }
    }

    private void sendMetricToKafka(ProceedingJoinPoint joinPoint, long executionTime) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Формирование данных для отправки в Kafka
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("executionTime", executionTime);
        metrics.put("methodName", methodName);
        if (args != null && args.length > 0) {
            metrics.put("arguments", args);
        }

        String messageBody = convertMapToJson(metrics);

        // Отправка сообщения в Kafka
        kafkaProducerService.sendErrorMessage(TOPIC_NAME, ERROR_TYPE, messageBody);
    }

    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1); // Удаляем последнюю запятую
        }
        json.append("}");
        return json.toString();
    }
}