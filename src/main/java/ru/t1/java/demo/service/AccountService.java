package ru.t1.java.demo.service;

import ru.t1.java.demo.entity.Account;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AccountService {

    public List<Account> getAllAccounts();

    // Получение аккаунта по ID
    Account getAccountById(Long id);

    // Создание нового аккаунта
    Account createAccount(Account account);

    Account updateAccount(Long id, Account accountDetails);

    void deleteAccount(Long id);

    void importAccountsFromCsv(File file) throws IOException;
}
