package ru.t1.java.monitor.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.t1.java.demo.entity.enums.TransactionStatus;

@Getter
@AllArgsConstructor
public class TransactionResultMessage {

    @JsonProperty("status")
    private TransactionStatus status; // ACCEPTED, REJECTED, BLOCKED

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("transaction_id")
    private Long transactionId;
}