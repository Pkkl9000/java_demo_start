package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.t1.java.demo.entity.enums.AccountStatus;
import ru.t1.java.demo.entity.enums.AccountType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("account_type")
    private AccountType accountType;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("frozen_amount")
    private BigDecimal frozenAmount;

    @JsonProperty("status")
    private AccountStatus status;
}

