package example.banking_system.services;

import example.banking_system.controllers.*;
import example.banking_system.models.*;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class OperationService {
    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationContext applicationContext;
    private OperationService operationService;

    @PostConstruct
    private void init() {
        operationService = applicationContext.getBean(OperationService.class);
    }


    public enum OperationType {
        //TODO please, check https://google.github.io/styleguide/javaguide.html#s4.8.1-enum-classes
        // And read all that codestyle, which is mostly a standard, and try to follow
        // DONE
        CLIENT_OPERATION,
        BANK_PUT,
        BANK_WITHDRAW
    }

    //TODO => What is this business logic doing in controller again?!
    // DONE
    public void addOperation(OperationService.OperationType operationType, OperationDto operation, UserEntity currentUser) {
        int attemptsCount = 10;
        do {
            try {
                --attemptsCount;
                operationService.addOperationLogic(operationType, operation, currentUser);
                return;
            }
            catch (InvalidParameterException | NotAllowedException | InsufficientFundsException | EntityNotFoundException e ) {
                throw e;
            }
            catch (Exception e) {
                // nothing
            }
        }
        while(attemptsCount != 0);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void addOperationLogic(OperationType operationType, OperationDto operation, UserEntity currentUser) {

        AccountEntity fromAccountDb = operation.getFromAccountNumber().isEmpty() ? null : accountRepository.findByAccountNumber(operation.getFromAccountNumber());
        AccountEntity toAccountDb = operation.getToAccountNumber().isEmpty() ? null : accountRepository.findByAccountNumber(operation.getToAccountNumber());

        if (fromAccountDb == null || toAccountDb == null) {
            throw new EntityNotFoundException();
        }

        if ((operationType != OperationType.BANK_WITHDRAW
                //TODO check comparisons != vs equals
                // DONE
                && !fromAccountDb.getUser().getId().equals(currentUser.getId()))
                || (operationType == OperationType.BANK_WITHDRAW
                && !toAccountDb.getUser().getId().equals(currentUser.getId()))) {
            throw new NotAllowedException();
        }

        if (operationType != OperationType.BANK_PUT
                && fromAccountDb.getBalance().compareTo(operation.getSum()) < 0) {
            //TODO this is definitely not a InvalidParameter, but rather InsufficientFundsException
            // DONE
            throw new InsufficientFundsException();
        }

        OperationEntity newOperation = new OperationEntity();
        newOperation.setDateTime(operation.getDateTime());
        newOperation.setFromAccount(fromAccountDb);
        newOperation.setToAccount(toAccountDb);
        newOperation.setSum(operation.getSum());
            //TODO What's the point of separating selectForUpdate logic by this bool config param, while this method is anyway in repeatable_read?
            // you need to do it then outside of repeatable_read
            // and make selects for update
            // DONE I wanted to leave both logic, this parameter is for "conditional compilation", but i found no way to use it for turning off repeatable_read
            // Now it can be removed

            //You could use programmatical TXs, not needed to use annotation. Or you could create separate method
        if (operationType == OperationType.BANK_WITHDRAW) {
            fromAccountDb.setBalance(fromAccountDb.getBalance().subtract(operation.getSum()));
        }
        else if (operationType == OperationType.BANK_PUT) {
            toAccountDb.setBalance(toAccountDb.getBalance().add(operation.getSum()));
        }
        else {
            accountRepository.updateBalancesForTwoAccount(fromAccountDb.getId(), fromAccountDb.getBalance().subtract(operation.getSum()),
                    toAccountDb.getId(), toAccountDb.getBalance().add(operation.getSum()));
        }
        operationRepository.save(newOperation);
    }

    @Transactional
    public BigDecimal getBalance(String accountNumber, UserEntity currentUser){
        AccountEntity account = checkCanGetAccountInformation(accountNumber, currentUser);
        return account.getBalance();
    }

    @Transactional
    public List<OperationDto> getHistoryPage(String accountNumber, int pageNumber, int pageSize, UserEntity currentUser) {
        AccountEntity account = checkCanGetAccountInformation(accountNumber, currentUser);
        if (pageNumber >= 0) {
            return toOperationDtoList(operationRepository.getOperationHistory(account.getId(), PageRequest.of(pageNumber, pageSize)));
        }
        else {
            return toOperationDtoList(operationRepository.getOperationHistory(account.getId(), Pageable.unpaged()));
        }
    }

    //TODO ideally we need an interface for service. And this protected seems superfluous. We aren't really going to extend this, private is fine.
    // make private, if used only inside this class
    // DONE
    private AccountEntity checkCanGetAccountInformation(String accountNumber, UserEntity currentUser) {
        AccountEntity accountFromDb = accountRepository.findByAccountNumber(accountNumber);
        if (accountFromDb == null) {
            //TODO Rather entity not found exception here
            // DONE
            throw new EntityNotFoundException();
        }
        UserEntity accountUser = accountFromDb.getUser();
        if (!userService.userHasBankRole(currentUser) &&
                //TODO Please, refresh what is != vs .equals() in Java and how we do comparisons
                // (*)And try to find out why this check actually worked in your UT
                // DONE
                // (*) This is because Java maintains a constant pool for instances of Long between -128 and 127. (https://www.baeldung.com/java-compare-long-values)
                !currentUser.getId().equals(accountUser.getId())) {
            throw new NotAllowedException();
        }
        return accountFromDb;
    }

    protected List<OperationDto> toOperationDtoList(List<OperationEntity> operations) {
        List<OperationDto> res = new ArrayList<>();
        for (OperationEntity operation : operations) {
            //TODO you might create an interface for these two, OperationDTO, and put in it
            // public static final from(Operation op){
            // newOp = new OperationDTO();
            // ...newOp.set(op.get...);
            // }
            // DONE
            res.add(OperationDto.fromOperation(operation));
        }
        return res;
    }




}
