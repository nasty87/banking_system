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
    public String addOperation(@NotNull @RequestBody Operation operation) throws BusinessException {
        User currentUser = getCurrentUser();
        boolean currentUserBankRole = currentUser.getRole().getName().equals("ROLE_BANK");

        Account fromAccountDb = operation.getFromAccount() == null ? null : accountDao.findByAccountNumber(operation.getFromAccount().getAccountNumber());
        Account toAccountDb = operation.getToAccount() == null ? null : accountDao.findByAccountNumber(operation.getToAccount().getAccountNumber());
        if (!currentUserBankRole) {
            if (fromAccountDb == null)
                throw new BusinessException("Operation error: from account required!", HttpStatus.BAD_REQUEST);

            if (toAccountDb == null)
                throw new BusinessException("Operation error: to account required!", HttpStatus.BAD_REQUEST);

            User user = fromAccountDb.getUser();
            if (currentUser.getId() != user.getId())
                throw new BusinessException("Operation error: operation not permitted!", HttpStatus.FORBIDDEN);
        }

        if (fromAccountDb != null && fromAccountDb.getBalance().compareTo(operation.getSum()) < 0)
            throw new BusinessException("Operation error: not enough money!", HttpStatus.FORBIDDEN);

        Operation newOperation = new Operation();
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
        Account accountFromDb = checkCanGetAccountInformation(accountNumber);
        return "$" + accountFromDb.getBalance().toString();
    }

    @Transactional
    @GetMapping(path = "/account/history")
    public List<OperationInfo> getHistoryPage(@RequestParam (value = "accountNumber", defaultValue = "") String accountNumber,
                                              @RequestParam (value = "offset", defaultValue = "-1") int offset,
                                              @RequestParam (value = "pageSize", defaultValue = "0") int pageSize) throws BusinessException {
        Account accountFromDb = checkCanGetAccountInformation(accountNumber);
        return toOperationInfoList(operationDao.getOperationHistoryPage(accountFromDb, offset, pageSize));
    }

    protected User getCurrentUser() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDao.findUserByLogin(currentUserName);
    }

    protected Account checkCanGetAccountInformation(String accountNumber) throws BusinessException{
        if (accountNumber.isEmpty())
            throw new BusinessException("Get balance error: no account set!", HttpStatus.BAD_REQUEST);

        Account accountFromDb = accountDao.findByAccountNumber(accountNumber);
        if (accountFromDb == null)
            throw new BusinessException("Get balance error: account not found!", HttpStatus.BAD_REQUEST);

        User accountUser = accountFromDb.getUser();
        if (accountUser == null)
            throw new BusinessException("Get balance error: user for account not found!", HttpStatus.BAD_REQUEST);

        User currentUser = getCurrentUser();
        if (!currentUser.getRole().getName().equals("ROLE_BANK") &&
                currentUser.getLogin() != accountUser.getLogin())
            throw new BusinessException("Get balance error: access forbidden!", HttpStatus.FORBIDDEN);

        return  accountFromDb;
    }

    protected List<OperationInfo> toOperationInfoList(List<Operation> operations) {
        List<OperationInfo> res = new ArrayList<>();
        for (Operation operation: operations) {
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
