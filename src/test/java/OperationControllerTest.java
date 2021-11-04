import example.banking_system.Application;
import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.controllers.NotAllowedException;
import example.banking_system.models.*;

import example.banking_system.services.OperationService;
import example.banking_system.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class OperationControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private OperationService operationService;

    protected Role getBankRole() {
        Role bankRole = new Role();
        bankRole.setId(2L);
        bankRole.setName(Role.BankRoleName);
        return bankRole;
    }

    protected Role getClientRole() {
        Role clientRole = new Role();
        clientRole.setId(3L);
        clientRole.setName(Role.ClientRoleName);
        return clientRole;
    }

    protected UserEntity getBank() {
        UserEntity bankEntity = new UserEntity();
        bankEntity.setId(2L);
        bankEntity.setName("bank");
        bankEntity.setLogin("bank");
        bankEntity.setPassword("12345");
        bankEntity.setRole(getBankRole());
        return bankEntity;
    }

    protected UserEntity getClient1() {
        UserEntity client1 = new UserEntity();
        client1.setId(3L);
        client1.setName("client1");
        client1.setLogin("client1");
        client1.setPassword("12345");
        client1.setRole(getClientRole());
        return client1;
    }

    protected UserEntity getClient3() {
        UserEntity client3 = new UserEntity();
        client3.setId(5L);
        client3.setName("client3");
        client3.setLogin("client3");
        client3.setPassword("12345");
        client3.setRole(getClientRole());
        return client3;
    }

    @Before
    public void setup() {
        try {
            UserDto adminUser = new UserDto();
            adminUser.setName("admin");
            adminUser.setLogin("admin");
            adminUser.setPassword("12345");
            adminUser.setRoleName(Role.AdminRoleName);

            UserDto bankUser = new UserDto();
            bankUser.setName("bank");
            bankUser.setLogin("bank");
            bankUser.setPassword("12345");
            bankUser.setRoleName(Role.BankRoleName);

            AccountDto bankAccount = new AccountDto();
            bankAccount.setAccountNumber("00000000000000000000");
            bankAccount.setBalance(new BigDecimal(0));
            bankAccount.setCreationDate(new Date());
            bankUser.getAccounts().add(bankAccount);

            UserDto clientUser1 = new UserDto();
            clientUser1.setName("client1");
            clientUser1.setLogin("client1");
            clientUser1.setPassword("12345");
            clientUser1.setRoleName(Role.ClientRoleName);

            AccountDto account1 = new AccountDto();
            account1.setAccountNumber("11111111111111111111");
            account1.setBalance(new BigDecimal(100000));
            account1.setCreationDate(new Date());
            clientUser1.getAccounts().add(account1);

            UserDto clientUser2 = new UserDto();
            clientUser2.setName("client2");
            clientUser2.setLogin("client2");
            clientUser2.setPassword("12345");
            clientUser2.setRoleName(Role.ClientRoleName);

            AccountDto account2 = new AccountDto();
            account2.setAccountNumber("22222222222222222222");
            account2.setBalance(new BigDecimal(200000));
            account2.setCreationDate(new Date());
            clientUser2.getAccounts().add(account2);

            UserDto clientUser3 = new UserDto();
            clientUser3.setName("client3");
            clientUser3.setLogin("client3");
            clientUser3.setPassword("12345");
            clientUser3.setRoleName(Role.ClientRoleName);

            AccountDto account3 = new AccountDto();
            account3.setAccountNumber("33333333333333333333");
            account3.setBalance(new BigDecimal(300000));
            account3.setCreationDate(new Date());
            clientUser3.getAccounts().add(account3);

            userService.addUser(adminUser);
            userService.addUser(bankUser);
            userService.addUser(clientUser1);
            userService.addUser(clientUser2);
            userService.addUser(clientUser3);
        }
        catch (Exception e) {
            /// do nothing
        }
    }

    @Test
    public void addValidOperationTest() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(100));

        operationService.addOperation(OperationService.OperationType.ClientOperation, operation, getClient1());
    }

    @Test
    public void addOperationWithTooBigSumTest() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(1000000000));

        boolean testPassed = false;
        try {
            operationService.addOperation(OperationService.OperationType.ClientOperation, operation, getClient1());
        }
        catch (InvalidParameterException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @Test
    public void addOperationWithOtherUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(10000));

        boolean testPassed = false;
        try {
            operationService.addOperation(OperationService.OperationType.ClientOperation, operation, getClient3());
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @Test
    public void getBalanceTest() throws Exception {
        operationService.getBalance("33333333333333333333", getClient3());
    }

    @Test
    public void getBalanceByBankTest() throws Exception {
        operationService.getBalance("33333333333333333333", getBank());
    }

    @Test
    public void getBalanceByWrongUserTest() throws Exception {
        boolean testPassed = false;
        try {
            operationService.getBalance("33333333333333333333", getClient1());
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);
    }

    @Test
    public void getFullHistoryTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", -1, 0, getClient1());
    }

    @Test
    public void getFullHistoryByBankTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", -1, 0, getBank());
    }

    @Test
    public void getFullHistoryByWrongUserTest() throws Exception {
        boolean testPassed = false;
        try {
            operationService.getHistoryPage("11111111111111111111", -1, 0, getClient3());
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @Test
    public void getPageHistoryTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", 1, 2, getClient1());
    }

    @Test
    public void putMoney() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("00000000000000000000");
        operation.setToAccountNumber("11111111111111111111");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());
        operationService.addOperation(OperationService.OperationType.BankPut, operation, getBank());
    }

    @Test
    public void putMoneyWrongUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("00000000000000000000");
        operation.setToAccountNumber("11111111111111111111");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        boolean testPassed = false;
        try {
            operationService.addOperation(OperationService.OperationType.BankPut, operation, getClient1());
        }
        catch (NotAllowedException e) {
            testPassed = true;
        }
        assert (testPassed);
    }

    @Test
    public void withdrawMoney() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("00000000000000000000");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        operationService.addOperation(OperationService.OperationType.BankWithdraw, operation, getBank());
    }

    @Test
    public void withdrawMoneyWrongUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("00000000000000000000");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        boolean testPassed = false;
        try {
            operationService.addOperation(OperationService.OperationType.BankWithdraw, operation, getClient1());
        }
        catch (NotAllowedException e) {
            testPassed = true;
        }
        assert (testPassed);
    }
}
