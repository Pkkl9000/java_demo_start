package ru.t1.java.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "error_logs")
@Getter
@Setter
public class DataSourceErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stack_trace", nullable = false, columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "method_signature", nullable = false)
    private String methodSignature;
}
