package ru.t1.java.demo.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionAcceptMessage {

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("transaction_amount")
    private Double transactionAmount;

    @JsonProperty("account_balance")
    private BigDecimal accountBalance;
}


