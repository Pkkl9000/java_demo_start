package ru.t1.java.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.aop.annotation.Metric;
import ru.t1.java.demo.entity.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @LogDataSourceError
    @Metric(value = 100)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @LogDataSourceError
    @Metric(value = 100)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account with ID " + id + " not found"));
    }

    @Override
    @LogDataSourceError
    @Metric(value = 100)
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    @LogDataSourceError
    @Metric(value = 100)
    public Account updateAccount(Long id, Account accountDetails) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setClientId(accountDetails.getClientId());
        account.setAccountType(accountDetails.getAccountType());
        account.setBalance(accountDetails.getBalance());
        return accountRepository.save(account);
    }

    @Override
    @LogDataSourceError
    @Metric(value = 100)
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public void importAccountsFromCsv(File file) throws IOException {
        List<Account> accounts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    Account account = new Account();
                    account.setClientId(Long.parseLong(values[0]));
                    account.setAccountType(Account.AccountType.valueOf(values[1]));
                    account.setBalance(Double.parseDouble(values[2]));
                    accounts.add(account);
                }
            }
        }

        accountRepository.saveAll(accounts);
    }
}

