package ru.t1.java.monitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "transaction.limit")
public class TransactionThresholdConfig {

    private int count; // Максимальное количество транзакций
    private int timeWindowSeconds; // Временной период в секундах
}