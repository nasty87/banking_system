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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableAsync
public class MultiThreadTest {
    @Autowired
    private UserService userService;
    @Autowired
    private OperationService operationService;

    private Authentication authentication;

    private boolean withdrawSuccess = false;

    protected Role getBankRole() {
        Role bankRole = new Role();
        bankRole.setId(2L);
        bankRole.setName(Role.BankRoleName);
        return bankRole;
    }

    protected UserEntity getBank() {
        UserEntity bankEntity = new UserEntity();
        bankEntity.setId(1L);
        bankEntity.setName("bank");
        bankEntity.setLogin("bank");
        bankEntity.setPassword("12345");
        bankEntity.setRole(getBankRole());
        return bankEntity;
    }

    @Before
    public void setup() {
        try {
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

            userService.addUser(bankUser);
            userService.addUser(clientUser1);
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
        operationDto.setFromAccountNumber("00000000000000000000");
        operationDto.setToAccountNumber("11111111111111111111");
        operationDto.setSum(new BigDecimal(1));
        operationDto.setDateTime(new Date());
        do {
            try {
                operationService.addOperation(OperationService.OperationType.BankPut, operationDto, getBank());
                return;
            }
            catch (InvalidParameterException e) {
                return;
            }
            catch (NotAllowedException e) {
                return;
            }
            catch (Exception e) {

            }
        }
        while(true);
    }

    public void addOperationWithdraw(int i) {
        OperationDto operationDto = new OperationDto();
        operationDto.setFromAccountNumber("11111111111111111111");
        operationDto.setToAccountNumber("00000000000000000000");
        operationDto.setSum(new BigDecimal(30000));
        operationDto.setDateTime(new Date());
        do {
            try {
                operationService.addOperation(OperationService.OperationType.BankWithdraw, operationDto, getBank());
                return;
            }
            catch (InvalidParameterException e) {
                withdrawSuccess = true;
                return;
            }
            catch (NotAllowedException e) {
                return;
            }
            catch (Exception e) {

            }
        }
        while(true);
    }

    @Test
    public void testPut() throws Exception{
        final int threadCount = 5;
        final BigDecimal sum1 = operationService.getBalance("11111111111111111111", getBank());
        authentication = SecurityContextHolder.getContext().getAuthentication();
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addOperationPut);
        BigDecimal sum2 = operationService.getBalance("11111111111111111111", getBank());
        assert (sum2.equals(sum1.add(new BigDecimal(threadCount))));
    }

    @Test
    public void testWithdraw() throws Exception{
        final int threadCount = 5;
        authentication = SecurityContextHolder.getContext().getAuthentication();
        IntStream.range(0, threadCount)
                .parallel()
                .forEach(this::addOperationWithdraw);
        assert(withdrawSuccess);
    }

}
