package ru.t1.java.demo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.java.demo.entity.enums.AccountStatus;
import ru.t1.java.demo.entity.enums.AccountType;

import java.math.BigDecimal;


@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "frozen_amount", nullable = false)
    private BigDecimal frozenAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;
}
