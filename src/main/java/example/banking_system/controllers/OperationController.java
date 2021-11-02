package example.banking_system.controllers;

import com.sun.istack.NotNull;
import example.banking_system.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class OperationController {
    @Autowired
    private UserDao userDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private OperationDao operationDao;
    
    @Transactional
    @PostMapping(path = "/operations/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String addOperation(@NotNull @RequestBody OperationDto operation) throws BusinessException {
        UserEntity currentUser = getCurrentUser();
        boolean currentUserBankRole = currentUser.getRole().getName().equals("ROLE_BANK");

        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountDao.findByAccountNumber(operation.getToAccountNumber());
        if (!currentUserBankRole) {
            if (fromAccountDb == null) {
                throw new BusinessException("Operation error: from account required!", HttpStatus.BAD_REQUEST);
            }

            if (toAccountDb == null) {
                throw new BusinessException("Operation error: to account required!", HttpStatus.BAD_REQUEST);
            }

            UserEntity user = fromAccountDb.getUser();
            if (currentUser.getId() != user.getId()) {
                throw new BusinessException("Operation error: operation not permitted!", HttpStatus.FORBIDDEN);
            }
        }

        if (fromAccountDb != null && fromAccountDb.getBalance().compareTo(operation.getSum()) < 0) {
            throw new BusinessException("Operation error: not enough money!", HttpStatus.FORBIDDEN);
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(new Date());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());
        operationDao.addOperation(newOperation);
        return "Operation successfully added!";
    }

    @Transactional
    @GetMapping(path = "/account/balance")
    public String getBalance(@RequestParam (value = "accountNumber", defaultValue = "") String accountNumber) throws BusinessException {
        AccountEntity accountFromDb = checkCanGetAccountInformation(accountNumber);
        return "$" + accountFromDb.getBalance().toString();
    }

    @Transactional
    @GetMapping(path = "/account/history")
    public List<OperationInfo> getHistoryPage(@RequestParam (value = "accountNumber", defaultValue = "") String accountNumber,
                                              @RequestParam (value = "pageNumber", defaultValue = "-1") int pageNumber,
                                              @RequestParam (value = "pageSize", defaultValue = "0") int pageSize) throws BusinessException {
        AccountEntity accountFromDb = checkCanGetAccountInformation(accountNumber);
        return toOperationInfoList(operationDao.getOperationHistoryPage(accountFromDb, pageNumber, pageSize));
    }

    protected UserEntity getCurrentUser() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDao.findUserByLogin(currentUserName);
    }

    protected AccountEntity checkCanGetAccountInformation(String accountNumber) throws BusinessException{
        if (accountNumber.isEmpty()) {
            throw new BusinessException("Get balance error: no account set!", HttpStatus.BAD_REQUEST);
        }

        AccountEntity accountFromDb = accountDao.findByAccountNumber(accountNumber);
        if (accountFromDb == null) {
            throw new BusinessException("Get balance error: account not found!", HttpStatus.BAD_REQUEST);
        }

        UserEntity accountUser = accountFromDb.getUser();
        if (accountUser == null) {
            throw new BusinessException("Get balance error: user for account not found!", HttpStatus.BAD_REQUEST);
        }

        UserEntity currentUser = getCurrentUser();
        if (!currentUser.getRole().getName().equals("ROLE_BANK") &&
                currentUser.getLogin() != accountUser.getLogin()) {
            throw new BusinessException("Get balance error: access forbidden!", HttpStatus.FORBIDDEN);
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
