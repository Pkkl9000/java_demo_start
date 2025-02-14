package ru.t1.java.demo.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.t1.java.demo.entity.enums.TransactionStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("transaction_time")
    private LocalDateTime transactionTime;

    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("transaction_id")
    private String transactionId;
}

