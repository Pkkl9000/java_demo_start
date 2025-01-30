package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.aop.annotation.Metric;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class MetricAspect {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_NAME = "t1_demo_metrics";
    private static final String ERROR_TYPE = "METRICS";

    @Around("@annotation(ru.t1.java.demo.aop.annotation.Metric) && execution(* *(..))")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Получаем аннотацию @Metric
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Metric metricAnnotation = signature.getMethod().getAnnotation(Metric.class);
        long threshold = metricAnnotation.value(); // Пороговое значение времени

        // Засекаем время начала выполнения метода
        long startTime = System.currentTimeMillis();

        // Выполняем метод
        Object result = joinPoint.proceed();

        // Засекаем время окончания выполнения метода
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Если время выполнения превышает порог, отправляем сообщение в Kafka
        if (executionTime > threshold) {
            sendMetricToKafka(joinPoint, executionTime);
        }

        return result;
    }

    private void sendMetricToKafka(ProceedingJoinPoint joinPoint, long executionTime) {
        // Получаем информацию о методе
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.toShortString();
        Object[] methodArgs = joinPoint.getArgs();

        // Формируем сообщение для Kafka
        Map<String, Object> metricDetails = new HashMap<>();
        metricDetails.put("errorType", ERROR_TYPE);
        metricDetails.put("methodName", methodName);
        metricDetails.put("executionTime", executionTime);
        metricDetails.put("methodArgs", methodArgs);

        String kafkaMessage = convertMapToJson(metricDetails);

        // Отправляем сообщение в Kafka
        Message<String> message = MessageBuilder.withPayload(kafkaMessage)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .setHeader("errorType", ERROR_TYPE)
                .build();

        kafkaTemplate.send(message);
    }

    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(",");
        }
        json.deleteCharAt(json.length() - 1); // Удаляем последнюю запятую
        json.append("}");
        return json.toString();
    }
}
