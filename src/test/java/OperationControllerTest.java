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
import org.springframework.security.test.context.support.WithMockUser;
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

    @WithMockUser(username = "client1")
    @Test
    public void addValidOperationTest() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(100));

        operationService.addClientOperation(operation);
    }

    @WithMockUser(username = "client1")
    @Test
    public void addOperationWithTooBigSumTest() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(1000000000));

        boolean testPassed = false;
        try {
            operationService.addClientOperation(operation);
        }
        catch (InvalidParameterException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @WithMockUser(username = "client3")
    @Test
    public void addOperationWithOtherUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("22222222222222222222");
        operation.setSum(new BigDecimal(10000));

        boolean testPassed = false;
        try {
            operationService.addClientOperation(operation);
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @WithMockUser(username = "client3")
    @Test
    public void getBalanceTest() throws Exception {
        operationService.getBalance("33333333333333333333");
    }

    @WithMockUser(username = "bank")
    @Test
    public void getBalanceByBankTest() throws Exception {
        operationService.getBalance("33333333333333333333");
    }

    @WithMockUser(username = "client1")
    @Test
    public void getBalanceByWrongUserTest() throws Exception {
        boolean testPassed = false;
        try {
            operationService.getBalance("33333333333333333333");
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);
    }

    @WithMockUser(username = "client1")
    @Test
    public void getFullHistoryTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", -1, 0);
    }

    @WithMockUser(username = "bank")
    @Test
    public void getFullHistoryByBankTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", -1, 0);
    }

    @WithMockUser(username = "client3")
    @Test
    public void getFullHistoryByWrongUserTest() throws Exception {
        boolean testPassed = false;
        try {
            operationService.getHistoryPage("11111111111111111111", -1, 0);
        }
        catch (NotAllowedException e){
            testPassed = true;
        }
        assert(testPassed);

    }

    @WithMockUser(username = "client1")
    @Test
    public void getPageHistoryTest() throws Exception {
        operationService.getHistoryPage("11111111111111111111", 1, 2);
    }

    @WithMockUser(username = "bank")
    @Test
    public void putMoney() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("00000000000000000000");
        operation.setToAccountNumber("11111111111111111111");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());
        operationService.addBankOperationPut(operation);
    }

    @WithMockUser(username = "client1")
    @Test
    public void putMoneyWrongUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("00000000000000000000");
        operation.setToAccountNumber("11111111111111111111");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        boolean testPassed = false;
        try {
            operationService.addBankOperationPut(operation);
        }
        catch (NotAllowedException e) {
            testPassed = true;
        }
        assert (testPassed);
    }

    @WithMockUser(username = "bank")
    @Test
    public void withdrawMoney() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("00000000000000000000");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        operationService.addBankOperationWithdraw(operation);
    }

    @WithMockUser(username = "client1")
    @Test
    public void withdrawMoneyWrongUser() throws Exception {
        OperationDto operation = new OperationDto();
        operation.setFromAccountNumber("11111111111111111111");
        operation.setToAccountNumber("00000000000000000000");
        operation.setSum(new BigDecimal(100));
        operation.setDateTime(new Date());

        boolean testPassed = false;
        try {
            operationService.addBankOperationWithdraw(operation);
        }
        catch (NotAllowedException e) {
            testPassed = true;
        }
        assert (testPassed);
    }
}
