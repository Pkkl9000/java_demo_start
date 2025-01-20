package ru.t1.java.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.entity.Account;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account accountDetails) {
        return ResponseEntity.ok(accountService.updateAccount(id, accountDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/csv")
//    public ResponseEntity<Void> doCsv() throws IOException {
//        File file = new File("src/main/resources/MOCK_DATA_ACCOUNTS.csv");
//        accountService.importAccountsFromCsv(file);
//        return ResponseEntity.noContent().build();
//    }
}