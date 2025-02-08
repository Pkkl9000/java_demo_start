package ru.t1.java.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.entity.Account;
import ru.t1.java.demo.entity.Transaction;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DemoMapper {

    @Mapping(target = "accountType", source = "accountType")
    AccountDto accountToAccountDto(Account account);

    @Mapping(target = "accountType", source = "accountType")
    Account accountDtoToAccount(AccountDto accountDto);

    List<AccountDto> accountsToAccountDtos(List<Account> accounts);

    Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    TransactionDto transactionToTransactionDto(Transaction save);
}
