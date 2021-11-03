package example.banking_system.services;

import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.controllers.OperationInfo;
import example.banking_system.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableTransactionManagement
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
    public void addClientOperation(OperationDto operation)
            throws InvalidParameterException, NotAllowedException {
        UserEntity currentUser = userService.getCurrentUser();
        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getToAccountNumber());

        if (fromAccountDb == null || toAccountDb == null) {
            throw new InvalidParameterException();
        }

        if (fromAccountDb.getUser().getId() != currentUser.getId()) {
            throw new NotAllowedException();
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(new Date());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());

        executeOperation(fromAccountDb.getId(), toAccountDb.getId(), operation.getSum());
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public void addBankOperationPut(OperationDto operation)
        throws InvalidParameterException, NotAllowedException {
        UserEntity currentUser = userService.getCurrentUser();
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

        executeOperation(null, toAccountDb.getId(), operation.getSum());
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public void addBankOperationWithdraw(@NotNull OperationDto operation)
            throws InvalidParameterException, NotAllowedException {
        UserEntity currentUser = userService.getCurrentUser();

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

        executeOperation(fromAccountDb.getId(), null, operation.getSum());
        operationDao.addOperation(newOperation);
    }

    @Transactional
    public BigDecimal getBalance(String accountNumber)
        throws InvalidParameterException, NotAllowedException {
        AccountEntity account = checkCanGetAccountInformation(accountNumber, userService.getCurrentUser());
        return account.getBalance();
    }

    @Transactional
    public List<OperationInfo> getHistoryPage(String accountNumber, int pageNumber, int pageSize)
            throws InvalidParameterException, NotAllowedException {
        AccountEntity account = checkCanGetAccountInformation(accountNumber, userService.getCurrentUser());
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

    public void executeOperation(Long fromAccountId, Long toAccountId, BigDecimal sum) throws InvalidParameterException {
        System.out.println("executeOperation");
        BigDecimal fromBalance = null;
        BigDecimal toBalance = null;
        if (fromAccountId != null) {
            fromBalance = accountDao.getBalanceForUpdate(fromAccountId);
            if (fromBalance.compareTo(sum) < 0) {
                throw new InvalidParameterException();
            }
        }
        if (toAccountId != null) {
            toBalance = accountDao.getBalanceForUpdate(toAccountId);
        }
        if (fromBalance != null) {
            accountDao.setBalance(fromAccountId, fromBalance.subtract(sum));
        }
        if (toBalance != null) {
            System.out.println("old balance: " + toBalance);
            System.out.println("new balance: " + toBalance.add(sum));
            accountDao.setBalance(toAccountId, toBalance.add(sum));
        }
    }
}
