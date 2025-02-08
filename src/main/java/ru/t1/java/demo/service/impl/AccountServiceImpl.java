package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.mapper.DemoMapper;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final DemoMapper demoMapper;

    @Override
    @LogDataSourceError
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(demoMapper::accountToAccountDto)
                .toList();
    }

    @Override
    @LogDataSourceError
    public AccountDto getAccountById(Long id) {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + id));
        return demoMapper.accountToAccountDto(account);
    }

    @Override
    @LogDataSourceError
    public AccountDto createAccount(AccountDto accountDto) {
        var savedAccount = accountRepository.save(demoMapper.accountDtoToAccount(accountDto));
        return demoMapper.accountToAccountDto(savedAccount);
    }

    @Override
    @LogDataSourceError
    public AccountDto updateAccount(Long id, AccountDto accountDetails) {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + id));
        account.setClientId(accountDetails.getClientId());
        account.setAccountType(accountDetails.getAccountType());
        account.setBalance(accountDetails.getBalance());
        var savedAccount = accountRepository.save(account);
        return demoMapper.accountToAccountDto(savedAccount);
    }

    @Override
    @LogDataSourceError
    public void deleteAccount(Long id) {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + id));
        accountRepository.delete(account);
    }

}

