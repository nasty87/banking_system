import example.banking_system.Application;
import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.models.*;
import example.banking_system.services.OperationService;
import example.banking_system.services.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.IntStream;

@RunWith(JUnitPlatform.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableAsync
public class MultiThreadTest {
    @Autowired
    private UserService userService;
    @Autowired
    private OperationService operationService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    final static String bankAccountNumber = "00000000000000000000";
    final static String client1AccountNumber = "11111111111111111111";
    final static String client2AccountNumber = "22222222222222222222";

    final static String bankName = "bank";
    final static String client1Name = "client1";
    final static String client2Name = "client2";

    protected Role getBankRole() {
        return roleDao.getRoleByName(Role.BankRoleName);
    }

    protected UserEntity getBank() {
        return userDao.findUserByLogin(bankName);
    }

    protected UserEntity getClient1() {
        return userDao.findUserByLogin(client1Name);
    }

    protected UserEntity getClient2() {
        return userDao.findUserByLogin(client2Name);
    }

    @BeforeEach
    public void setup() {
        try {
            operationService.clearDataBase();

            UserDto bankUser = new UserDto();
            bankUser.setName(bankName);
            bankUser.setLogin(bankName);
            bankUser.setPassword("12345");
            bankUser.setRoleName(Role.BankRoleName);

            AccountDto bankAccount = new AccountDto();
            bankAccount.setAccountNumber(bankAccountNumber);
            bankAccount.setBalance(new BigDecimal(0));
            bankAccount.setCreationDate(new Date());
            bankUser.getAccounts().add(bankAccount);

            UserDto clientUser1 = new UserDto();
            clientUser1.setName(client1Name);
            clientUser1.setLogin(client1Name);
            clientUser1.setPassword("12345");
            clientUser1.setRoleName(Role.ClientRoleName);

            AccountDto account1 = new AccountDto();
            account1.setAccountNumber(client1AccountNumber);
            account1.setBalance(new BigDecimal(100000));
            account1.setCreationDate(new Date());
            clientUser1.getAccounts().add(account1);

            UserDto clientUser2 = new UserDto();
            clientUser2.setName(client2Name);
            clientUser2.setLogin(client2Name);
            clientUser2.setPassword("12345");
            clientUser2.setRoleName(Role.ClientRoleName);

            AccountDto account2 = new AccountDto();
            account2.setAccountNumber(client2AccountNumber);
            account2.setBalance(new BigDecimal(100000));
            account2.setCreationDate(new Date());
            clientUser2.getAccounts().add(account2);

            userService.addUser(bankUser);
            userService.addUser(clientUser1);
            userService.addUser(clientUser2);
        }
        catch (Exception e) {
            /// do nothing
        }
    }

    @PostConstruct
    void setGlobalSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    public void addOperationPut(int i) {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(bankAccountNumber);
        operationDto.setToAccountNumber(client1AccountNumber);
        operationDto.setSum(new BigDecimal(1));
        operationDto.setDateTime(new Date());

        int attemptsCount = 10;
        do {
            try {
                --attemptsCount;
                operationService.addOperation(OperationService.OperationType.BankPut, operationDto, getBank());
                return;
            }
            catch (InvalidParameterException | NotAllowedException e) {
                return;
            }
            catch (Exception e) {
                // nothing
            }
        }
        while(attemptsCount != 0);
    }

    @Test
    public void testPut() throws Exception {
        final int threadCount = 5;
        final BigDecimal sum1 = operationService.getBalance(client1AccountNumber, getBank());
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addOperationPut);
        BigDecimal sum2 = operationService.getBalance(client1AccountNumber, getBank());
        Assert.assertEquals(sum2, sum1.add(new BigDecimal(threadCount)));
    }

    public void addOperationWithdraw(int i) {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(bankAccountNumber);
        operationDto.setSum(new BigDecimal(10000 * (i + 1)));
        operationDto.setDateTime(new Date());

        int attemptsCount = 10;
        do {
            try {
                --attemptsCount;
                operationService.addOperation(OperationService.OperationType.BankWithdraw, operationDto, getBank());
                return;
            }
            catch (InvalidParameterException | NotAllowedException e) {
                return;
            }
            catch (Exception e) {
                // nothing
            }
        }
        while(attemptsCount != 0);
    }

    @Test
    public void testWithdraw() {
        final int threadCount = 4;
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addOperationWithdraw);

        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(bankAccountNumber);
        operationDto.setSum(new BigDecimal(1));
        operationDto.setDateTime(new Date());
        Assert.assertThrows(InvalidParameterException.class,
                () -> operationService.addOperation(OperationService.OperationType.BankWithdraw, operationDto, getBank()));
    }

    protected OperationDto create1to2Operation() {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(client2AccountNumber);
        operationDto.setSum(new BigDecimal(1));
        operationDto.setDateTime(new Date());
        return operationDto;
    }

    protected OperationDto create2o1Operation() {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client2AccountNumber);
        operationDto.setToAccountNumber(client1AccountNumber);
        operationDto.setSum(new BigDecimal(2));
        operationDto.setDateTime(new Date());
        return operationDto;
    }

    public void addClientOperation(int i) {
        boolean even = (i % 2 == 0);
        OperationDto operation = (even ? create1to2Operation() : create2o1Operation());

        int attemptsCount = 20;
        do {
            try {
                --attemptsCount;
                operationService.addOperation(OperationService.OperationType.ClientOperation, operation, even ? getClient1() : getClient2());
                return;
            }
            catch (InvalidParameterException | NotAllowedException e) {
                return;
            }
            catch (Exception e) {
                // nothing
            }
        }
        while(attemptsCount != 0);
    }

    @Test
    public void testClientOperations() throws Exception{
        final int threadCount = 6;
        assert (threadCount % 2 == 0);
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addClientOperation);

        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(bankAccountNumber);
        operationDto.setSum(new BigDecimal(1));
        operationDto.setDateTime(new Date());
        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()),
                operationService.getBalance(client2AccountNumber, getClient2()).add(new BigDecimal(threadCount)));
    }

}
