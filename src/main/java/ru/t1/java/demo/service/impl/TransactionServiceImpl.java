package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.entity.Transaction;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.mapper.DemoMapper;
import ru.t1.java.demo.service.TransactionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;
    private final DemoMapper demoMapper;

    @Override
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        Transaction transaction = demoMapper.transactionDtoToTransaction(transactionDto);
        transaction.setTransactionTime(LocalDateTime.now()); // Устанавливаем текущее время
        return demoMapper.transactionToTransactionDto(transactionRepository.save(transaction));
    }

    @Override
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(demoMapper::transactionToTransactionDto)
                .toList();
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(demoMapper::transactionToTransactionDto)
                .orElseThrow(() -> new EntityNotFoundException("Транзакция с ID " + id + " не найдена"));
    }

    @Override
    public TransactionDto updateTransaction(Long id, TransactionDto transactionDto) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Транзакция с ID " + id + " не найдена"));

        existingTransaction.setAccountId(transactionDto.getAccountId());
        existingTransaction.setAmount(transactionDto.getAmount());
        existingTransaction.setTransactionTime(transactionDto.getTransactionTime());

        return demoMapper.transactionToTransactionDto(transactionRepository.save(existingTransaction));
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new EntityNotFoundException("Транзакция с ID " + id + " не найдена");
        }
        transactionRepository.deleteById(id);
    }
}

