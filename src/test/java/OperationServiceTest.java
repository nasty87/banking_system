import example.banking_system.Application;
import example.banking_system.controllers.InsufficientFundsException;
import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.models.*;

import example.banking_system.services.OperationService;
import example.banking_system.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RunWith(JUnitPlatform.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Log4j2
public class OperationServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private OperationService operationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OperationRepository operationRepository;

    @Transactional
    public void clearDataBase() {
        //TODO this is very much dangerous method. And we actually don't need it anywhere outside UT -> move to base UT class.
        // DONE
        operationRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    final static String bankAccountNumber = "00000000000000000000";
    final static String client1AccountNumber = "11111111111111111111";
    final static String client2AccountNumber = "22222222222222222222";

    final static String bankName = "bank";
    final static String client1Name = "client1";
    final static String client2Name = "client2";

    final static BigDecimal startBalance = new BigDecimal("10000.00");

    protected UserEntity getBank() {
        return userRepository.findUserByLogin(bankName);
    }

    protected UserEntity getClient1() {
        return userRepository.findUserByLogin(client1Name);
    }

    protected UserEntity getClient2() {
        return userRepository.findUserByLogin(client2Name);
    }

    @BeforeEach
    //TODO do we really want to recreate all the accs every test?
    // Yes, we want to be sure that data in db in the initial state
    public void setup() {
        try {
            clearDataBase();

            UserDto bankUser = new UserDto();
            bankUser.setName(bankName);
            bankUser.setLogin(bankName);
            bankUser.setPassword("12345");
            bankUser.setRoleName(Role.BankRoleName);

            AccountDto bankAccount = new AccountDto();
            bankAccount.setAccountNumber(bankAccountNumber);
            bankAccount.setBalance(BigDecimal.ZERO);
            bankAccount.setCreationDate(OffsetDateTime.now());
            bankUser.getAccounts().add(bankAccount);

            UserDto clientUser1 = new UserDto();
            clientUser1.setName(client1Name);
            clientUser1.setLogin(client1Name);
            clientUser1.setPassword("12345");
            clientUser1.setRoleName(Role.ClientRoleName);

            AccountDto account1 = new AccountDto();
            account1.setAccountNumber(client1AccountNumber);
            account1.setBalance(startBalance);
            account1.setCreationDate(OffsetDateTime.now());
            clientUser1.getAccounts().add(account1);

            UserDto clientUser2 = new UserDto();
            clientUser2.setName(client2Name);
            clientUser2.setLogin(client2Name);
            clientUser2.setPassword("12345");
            clientUser2.setRoleName(Role.ClientRoleName);

            AccountDto account2 = new AccountDto();
            account2.setAccountNumber(client2AccountNumber);
            account2.setBalance(startBalance);
            account2.setCreationDate(OffsetDateTime.now());
            clientUser2.getAccounts().add(account2);

            userService.addUser(bankUser);
            userService.addUser(clientUser1);
            userService.addUser(clientUser2);
        } catch (Exception e) {
            /// do nothing
        }
    }

    @Test
    public void testAllowAddOperation() throws Exception {
        OperationDto putOperation = new OperationDto();
        putOperation.setFromAccountNumber(bankAccountNumber);
        putOperation.setToAccountNumber(client1AccountNumber);
        putOperation.setSum(BigDecimal.ONE);
        putOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.BANK_PUT, putOperation, getBank());
        Assert.assertThrows("User with CLIENT role ",
                NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.BANK_PUT, putOperation, getClient1()));

        OperationDto withdrawOperation = new OperationDto();
        withdrawOperation.setFromAccountNumber(client1AccountNumber);
        withdrawOperation.setToAccountNumber(bankAccountNumber);
        withdrawOperation.setSum(BigDecimal.ONE);
        withdrawOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.BANK_WITHDRAW, withdrawOperation, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.BANK_WITHDRAW, withdrawOperation, getClient1()));

        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(BigDecimal.ONE);
        clientOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getClient1());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getBank()));
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getClient2()));
    }

    @Test
    public void testAllowGetBalance() throws Exception {
        operationService.getBalance(client1AccountNumber, getClient1());
        operationService.getBalance(client1AccountNumber, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.getBalance(client1AccountNumber, getClient2()));
    }

    @Test
    public void testAllowGetHistory() throws Exception {
        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(new BigDecimal(1));
        clientOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getClient1());

        operationService.getHistoryPage(client1AccountNumber, 1, 1, getClient1());
        operationService.getHistoryPage(client1AccountNumber, 1, 1, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.getHistoryPage(client1AccountNumber, 1, 1, getClient2()));
    }

    @Test
    public void testGetBalance() throws Exception {
        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()), startBalance);
        Assert.assertEquals(operationService.getBalance(client2AccountNumber, getClient2()), startBalance);
        //TODO don't confuse expected and actual arguments
    }

    @Test
    public void testClientOperation() throws Exception {
        final BigDecimal sum = BigDecimal.ONE;

        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(sum);
        clientOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getClient1());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()), startBalance.subtract(sum));
        Assert.assertEquals(operationService.getBalance(client2AccountNumber, getClient2()), startBalance.add(sum));
        //TODO where do we check that client2 received that sum? And why cast to bigint? What if we got 0.5 miss by the way?
        // DONE

        //TODO don't confuse expected and actual arguments
    }

    @Test
    public void testPutOperation() throws Exception {
        final BigDecimal sum = BigDecimal.ONE;

        OperationDto putOperation = new OperationDto();
        putOperation.setFromAccountNumber(bankAccountNumber);
        putOperation.setToAccountNumber(client1AccountNumber);
        putOperation.setSum(sum);
        putOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.BANK_PUT, putOperation, getBank());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()), startBalance.add(sum));
        //TODO don't confuse expected and actual arguments
    }

    @Test
    public void testWithdrawOperation() throws Exception {
        final BigDecimal sum = BigDecimal.ONE;

        OperationDto withdrawOperation = new OperationDto();
        withdrawOperation.setFromAccountNumber(client1AccountNumber);
        withdrawOperation.setToAccountNumber(bankAccountNumber);
        withdrawOperation.setSum(sum);
        withdrawOperation.setDateTime(OffsetDateTime.now());

        operationService.addOperation(OperationService.OperationType.BANK_WITHDRAW, withdrawOperation, getBank());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()), startBalance.subtract(sum));
        //TODO don't confuse expected and actual arguments
    }

    @Test
    public void testHistory() throws Exception {
        final int count = 10;
        List<OperationDto> etalonHistory = new ArrayList<>();
        for (int i = 1; i <= count; ++i) {
            OperationDto clientOperation = new OperationDto();
            clientOperation.setFromAccountNumber(client1AccountNumber);
            clientOperation.setToAccountNumber(client2AccountNumber);
            clientOperation.setSum(new BigDecimal(Integer.toString(i) + ".00"));
            clientOperation.setDateTime(OffsetDateTime.now());

            operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, clientOperation, getClient1());
            etalonHistory.add(clientOperation);
        }
        //TODO :> just check that lists equal each other. You may select ordered by date, or just sort them here.
        // don't need to check pagination, it's on spring's data behalf
        // DONE
        List<OperationDto> fullHistory = operationService.getHistoryPage(client1AccountNumber, -1, 0, getClient1());
        Assert.assertTrue(etalonHistory.containsAll(fullHistory) && fullHistory.containsAll(etalonHistory));
    }

    public void addOperationPut(int i) {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(bankAccountNumber);
        operationDto.setToAccountNumber(client1AccountNumber);
        operationDto.setSum(BigDecimal.ONE);
        operationDto.setDateTime(OffsetDateTime.now());

        //TODO duplicating with method in controller. Do not copy business logic from service, you need to test logic already there
        // DONE
        try {
            operationService.addOperation(OperationService.OperationType.BANK_PUT, operationDto, getBank());
        } catch (Exception e) {
            // nothing
        }
    }

    @Test
    public void testPut() {
        final int threadCount = 5;
        final BigDecimal sum1 = operationService.getBalance(client1AccountNumber, getBank());
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addOperationPut);
        BigDecimal sum2 = operationService.getBalance(client1AccountNumber, getBank());
        Assert.assertEquals(sum2, sum1.add(new BigDecimal(threadCount)));
        //TODO don't confuse expected and actual arguments
    }

    public void addOperationWithdraw(int i) {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(bankAccountNumber);
        operationDto.setSum(new BigDecimal(10000 * (i + 1)));
        operationDto.setDateTime(OffsetDateTime.now());

        //TODO duplication
        // DONE
        try {
            operationService.addOperation(OperationService.OperationType.BANK_WITHDRAW, operationDto, getBank());
        } catch (Exception e) {
            // nothing
        }
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
        operationDto.setSum(BigDecimal.ONE);
        operationDto.setDateTime(OffsetDateTime.now());
        Assert.assertThrows(InsufficientFundsException.class,
                () -> operationService.addOperation(OperationService.OperationType.BANK_WITHDRAW, operationDto, getBank()));
    }

    protected OperationDto create1to2Operation() {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(client2AccountNumber);
        operationDto.setSum(BigDecimal.ONE);
        operationDto.setDateTime(OffsetDateTime.now());
        return operationDto;
    }

    protected OperationDto create2o1Operation() {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client2AccountNumber);
        operationDto.setToAccountNumber(client1AccountNumber);
        operationDto.setSum(new BigDecimal("2"));
        operationDto.setDateTime(OffsetDateTime.now());
        return operationDto;
    }

    public void addClientOperation(int i) {
        boolean even = (i % 2 == 0);
        OperationDto operation = (even ? create1to2Operation() : create2o1Operation());
        try {
            operationService.addOperation(OperationService.OperationType.CLIENT_OPERATION, operation, even ? getClient1() : getClient2());
            log.info("Added operation:" + operation);
        } catch (Exception e) {
            // nothing
        }
    }

    private BigDecimal calculateSumFromHistory(List<OperationDto> history, String accountNumber, BigDecimal startSum) {
        BigDecimal sum = startSum;
        for (OperationDto operation : history) {
            if (operation.getFromAccountNumber().equals(accountNumber)) {
                sum = sum.subtract(operation.getSum());
            } else if (operation.getToAccountNumber().equals(accountNumber)) {
                sum = sum.add(operation.getSum());
            }
        }
        return sum;
    }

    @Test
    public void testClientOperations() throws Exception {
        //TODO these are not threadCount, but tasks
        //TODO check with this amount(10 or 100) please, and 1-2 retries -> it fails then. When 20 attempts, ok, just increase amount of threadCount to 50, and it will take all the time of earth to finish

        // TODO look https://www.postgresql.org/docs/9.4/explicit-locking.html 13.3.4 Deadlocks
        // you get deadlocks here, when A to B and B to A in the same time.
        // DONE: now update to rows on one request
        final int taskCount = 100;
        IntStream.range(0, taskCount)
                .parallel()
                .forEach(this::addClientOperation);

        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber(client1AccountNumber);
        operationDto.setToAccountNumber(bankAccountNumber);
        operationDto.setSum(BigDecimal.ONE);
        operationDto.setDateTime(OffsetDateTime.now());
        //TODO this logic why you get this number is unclear. You need to get that from 4 methods
        // threadCount must be even,
        // there are 1 and 2 sums A to B and B to A
        // and the result differs threadCount*(2-1)
        // Isn't there a simpler solution which is easier to read and follow?
        // DONE: now it does not matter whether all requests are succeeded or not

        List<OperationDto> history1 = operationService.getHistoryPage(client1AccountNumber, -1, 0, getClient1());
        List<OperationDto> history2 = operationService.getHistoryPage(client2AccountNumber, -1, 0, getClient2());

        //TODO вообще непонятно, что тут тогда тестируется. почему тут проверяется история, когда она проверяется в другом месте.
        Assert.assertEquals(BigDecimal.valueOf(startBalance.longValue() + taskCount/2), operationService.getBalance(client1AccountNumber, getClient1()));
        //TODO а еще мы бомбардируем базу запросами, не возвращая пользователю ответ, успешно ли - такое апи не годится
//        Assert.assertEquals(calculateSumFromHistory(history2, client2AccountNumber, startBalance), operationService.getBalance(client2AccountNumber, getClient2()));

        //TODO don't confuse expected and actual arguments
        // DONE
    }
}
