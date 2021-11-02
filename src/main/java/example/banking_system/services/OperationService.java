package example.banking_system.services;

import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.controllers.OperationInfo;
import example.banking_system.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OperationService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private OperationDao operationDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private UserService userService;

    @Transactional
    public void addClientOperation(@NotNull OperationDto operation, @NotNull UserEntity currentUser)
            throws InvalidParameterException, NotAllowedException {
        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getToAccountNumber());

        if (fromAccountDb == null || toAccountDb == null) {
            throw new InvalidParameterException();
        }

        if (fromAccountDb.getUser().getId() != currentUser.getId()) {
            throw new NotAllowedException();
        }

        if (fromAccountDb.getBalance().compareTo(operation.getSum()) < 0) {
            throw new InvalidParameterException();
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(new Date());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());

        fromAccountDb.setBalance(fromAccountDb.getBalance().subtract(operation.getSum()));
        toAccountDb.setBalance(toAccountDb.getBalance().add(operation.getSum()));

        accountDao.saveAccount(fromAccountDb);
        accountDao.saveAccount(toAccountDb);
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public void addBankOperationPut(@NotNull OperationDto operation, @NotNull UserEntity currentUser)
        throws InvalidParameterException, NotAllowedException {
        if (!userService.userHasBankRole(currentUser)) {
            throw new NotAllowedException();
        }

        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getToAccountNumber());

        if (fromAccountDb == null || toAccountDb == null) {
            throw new InvalidParameterException();
        }

        if (fromAccountDb.getUser().getId() != currentUser.getId()) {
            throw new InvalidParameterException();
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(new Date());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());

        toAccountDb.setBalance(toAccountDb.getBalance().add(operation.getSum()));

        accountDao.saveAccount(toAccountDb);
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public void addBankOperationWithdraw(@NotNull OperationDto operation, @NotNull UserEntity currentUser)
            throws InvalidParameterException, NotAllowedException {
        if (!userService.userHasBankRole(currentUser)) {
            throw new NotAllowedException();
        }

        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getToAccountNumber());

        if (fromAccountDb == null || toAccountDb == null) {
            throw new InvalidParameterException();
        }

        if (toAccountDb.getUser().getId() != currentUser.getId()) {
            throw new InvalidParameterException();
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(new Date());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());

        fromAccountDb.setBalance(fromAccountDb.getBalance().subtract(operation.getSum()));

        accountDao.saveAccount(fromAccountDb);
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public String getBalance(String accountNumber, UserEntity currentUser)
        throws InvalidParameterException, NotAllowedException {
        AccountEntity account = checkCanGetAccountInformation(accountNumber, currentUser);
        return "$" + account.getBalance().toString();
    }

    @Transactional
    public List<OperationInfo> getHistoryPage(String accountNumber, int pageNumber, int pageSize, UserEntity currentUser)
            throws InvalidParameterException, NotAllowedException {
        AccountEntity account = checkCanGetAccountInformation(accountNumber, currentUser);
        return toOperationInfoList(operationDao.getOperationHistoryPage(account, pageNumber, pageSize));
    }

    protected AccountEntity checkCanGetAccountInformation(String accountNumber, UserEntity currentUser)
            throws InvalidParameterException, NotAllowedException {
        AccountEntity accountFromDb = accountDao.findByAccountNumber(accountNumber);
        if (accountFromDb == null) {
            throw new InvalidParameterException();
        }
        UserEntity accountUser = accountFromDb.getUser();
        if (accountUser == null) {
            throw new InvalidParameterException();
        }
        if (!userService.userHasBankRole(currentUser) &&
            currentUser.getId() != accountUser.getId()) {
            throw new NotAllowedException();
        }
        return  accountFromDb;
    }
    protected List<OperationInfo> toOperationInfoList(List<OperationEntity> operations) {
        List<OperationInfo> res = new ArrayList<>();
        for (OperationEntity operation: operations) {
            OperationInfo operationInfo = new OperationInfo();
            operationInfo.setDate(operation.getDateTime());
            operationInfo.setFromAccount(operation.getFromAccount().getAccountNumber());
            operationInfo.setToAccount(operation.getToAccount().getAccountNumber());
            operationInfo.setSum(operation.getSum());
            res.add(operationInfo);
        }
        return res;
    }
}
