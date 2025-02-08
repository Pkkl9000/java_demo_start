package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;
import java.util.List;

public interface AccountService {

    List<AccountDto> getAllAccounts();

    // Получение аккаунта по ID
    AccountDto getAccountById(Long id);

    // Создание нового аккаунта
    AccountDto createAccount(AccountDto accountDto);

    AccountDto updateAccount(Long id, AccountDto accountDetails);

    void deleteAccount(Long id);
}
