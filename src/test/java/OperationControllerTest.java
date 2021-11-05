import example.banking_system.Application;
import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.controllers.OperationInfo;
import example.banking_system.models.*;

import example.banking_system.services.OperationService;
import example.banking_system.services.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(JUnitPlatform.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class OperationControllerTest {
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

    final static BigDecimal startBalance = new BigDecimal(10000.00);

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
            account1.setBalance(startBalance);
            account1.setCreationDate(new Date());
            clientUser1.getAccounts().add(account1);

            UserDto clientUser2 = new UserDto();
            clientUser2.setName(client2Name);
            clientUser2.setLogin(client2Name);
            clientUser2.setPassword("12345");
            clientUser2.setRoleName(Role.ClientRoleName);

            AccountDto account2 = new AccountDto();
            account2.setAccountNumber(client2AccountNumber);
            account2.setBalance(startBalance);
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

    @Test
    public void testAllowAddOperation() throws Exception{
        OperationDto putOperation = new OperationDto();
        putOperation.setFromAccountNumber(bankAccountNumber);
        putOperation.setToAccountNumber(client1AccountNumber);
        putOperation.setSum(new BigDecimal(1));
        putOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.BankPut, putOperation, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.BankPut, putOperation, getClient1()));

        OperationDto withdrawOperation = new OperationDto();
        withdrawOperation.setFromAccountNumber(client1AccountNumber);
        withdrawOperation.setToAccountNumber(bankAccountNumber);
        withdrawOperation.setSum(new BigDecimal(1));
        withdrawOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.BankWithdraw, withdrawOperation, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.BankWithdraw, withdrawOperation, getClient1()));

        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(new BigDecimal(1));
        clientOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getClient1());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getBank()));
        Assert.assertThrows(NotAllowedException.class, () -> operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getClient2()));
    }

    @Test
    public void testAllowGetBalance() throws Exception{
        operationService.getBalance(client1AccountNumber, getClient1());
        operationService.getBalance(client1AccountNumber, getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.getBalance(client1AccountNumber, getClient2()));
    }

    @Test
    public void testAllowGetHistory() throws Exception{
        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(new BigDecimal(1));
        clientOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getClient1());

        operationService.getHistoryPage(client1AccountNumber, 1, 1, getClient1());
        operationService.getHistoryPage(client1AccountNumber, 1, 1,  getBank());
        Assert.assertThrows(NotAllowedException.class, () -> operationService.getHistoryPage(client1AccountNumber, 1, 1, getClient2()));
    }

    @Test
    public void testGetBalance() throws Exception {
        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()).toBigInteger(), startBalance.toBigInteger());
        Assert.assertEquals(operationService.getBalance(client2AccountNumber, getClient2()).toBigInteger(), startBalance.toBigInteger());
    }

    @Test
    public void testClientOperation() throws Exception {
        final BigDecimal sum = new BigDecimal(1.00);

        OperationDto clientOperation = new OperationDto();
        clientOperation.setFromAccountNumber(client1AccountNumber);
        clientOperation.setToAccountNumber(client2AccountNumber);
        clientOperation.setSum(sum);
        clientOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getClient1());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()).toBigInteger(), startBalance.subtract(sum).toBigInteger());
    }

    @Test
    public void testPutOperation() throws Exception {
        final BigDecimal sum = new BigDecimal(1.00);

        OperationDto putOperation = new OperationDto();
        putOperation.setFromAccountNumber(bankAccountNumber);
        putOperation.setToAccountNumber(client1AccountNumber);
        putOperation.setSum(sum);
        putOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.BankPut, putOperation, getBank());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()).toBigInteger(), startBalance.add(sum).toBigInteger());
    }

    @Test
    public void testWithdrawOperation() throws Exception {
        final BigDecimal sum = new BigDecimal(1.00);

        OperationDto withdrawOperation = new OperationDto();
        withdrawOperation.setFromAccountNumber(client1AccountNumber);
        withdrawOperation.setToAccountNumber(bankAccountNumber);
        withdrawOperation.setSum(sum);
        withdrawOperation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.BankWithdraw, withdrawOperation, getBank());

        Assert.assertEquals(operationService.getBalance(client1AccountNumber, getClient1()).toBigInteger(), startBalance.subtract(sum).toBigInteger());
    }

    @Test
    public void testHistory() throws Exception {
        final int count = 10;
        for (int i = 1; i <= count; ++i) {
            OperationDto clientOperation = new OperationDto();
            clientOperation.setFromAccountNumber(client1AccountNumber);
            clientOperation.setToAccountNumber(client2AccountNumber);
            clientOperation.setSum(new BigDecimal(i));
            clientOperation.setDateTime(new Date());

            operationService.addOperation(OperationService.OperationType.ClientOperation, clientOperation, getClient1());
        }
        List<OperationInfo> fullHistory = operationService.getHistoryPage(client1AccountNumber, -1, 0, getClient1());
        Assert.assertEquals(fullHistory.size(), count);
        List<OperationInfo> pageHistory = operationService.getHistoryPage(client1AccountNumber, 3, 1, getClient1());
        Assert.assertEquals(pageHistory.size(), 1);
        Optional<OperationInfo> optional = pageHistory.stream().findFirst();
        Assert.assertFalse(optional.isEmpty());
        Assert.assertEquals(optional.get().getSum().toBigInteger(), new BigDecimal(4.00).toBigInteger());
    }
}
