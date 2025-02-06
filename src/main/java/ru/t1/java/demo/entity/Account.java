package ru.t1.java.demo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance", nullable = false)
    private Double balance;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId; // Уникальный идентификатор аккаунта

    @Column(name = "frozen_amount")
    private Double frozenAmount; // Сумма, замороженная на аккаунте

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status; // Статус аккаунта

    public enum AccountType {
        DEBIT,
        CREDIT
    }

    public enum AccountStatus {
        ARRESTED,
        BLOCKED,
        CLOSED,
        OPEN
    }
}
