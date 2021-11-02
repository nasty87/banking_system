import com.google.gson.Gson;
import example.banking_system.Application;
import example.banking_system.models.Account;
import example.banking_system.models.Operation;

import example.banking_system.models.Role;
import example.banking_system.models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class OperationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @WithMockUser(roles="ADMIN")
    @Test
    public void addUserTest() throws Exception {
        User user = new User();
        user.setName("test");
        user.setLogin(UUID.randomUUID().toString());
        user.setPassword("12345");
        Role adminRole = new Role();
        adminRole.setName("ROLE_CLIENT");
        user.setRole(adminRole);
        this.mockMvc.perform(post("/users/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(user)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @WithMockUser(roles="ADMIN")
    @Test
    public void addExistedUserTest() throws Exception {
        User user = new User();
        user.setName("test");
        user.setLogin("admin");
        user.setPassword("12345");
        Role adminRole = new Role();
        adminRole.setName("ROLE_CLIENT");
        user.setRole(adminRole);
        this.mockMvc.perform(post("/users/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(user)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles="BANK")
    @Test
    public void addUserTestForNotAdmin() throws Exception {
        User user = new User();
        user.setName("admin");
        user.setLogin("admin");
        user.setPassword("12345");
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        user.setRole(adminRole);
        this.mockMvc.perform(post("/users/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(user)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "client1")
    @Test
    public void addValidOperationTest() throws Exception {
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("111111");

        Account toAccount = new Account();
        toAccount.setAccountNumber("222222");

        Operation operation = new Operation();
        operation.setFromAccount(fromAccount);
        operation.setToAccount(toAccount);
        operation.setSum(new BigDecimal(100));

        System.out.println(new Gson().toJson(operation));

        this.mockMvc.perform(post("/operations/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(operation)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @WithUserDetails(value = "client1")
    @Test
    public void addOperationWithTooBigSumTest() throws Exception {
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("111111");

        Account toAccount = new Account();
        toAccount.setAccountNumber("222222");

        Operation operation = new Operation();
        operation.setFromAccount(fromAccount);
        operation.setToAccount(toAccount);
        operation.setSum(new BigDecimal(1000000000));

        System.out.println(new Gson().toJson(operation));

        this.mockMvc.perform(post("/operations/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(operation)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @WithUserDetails(value = "client3")
    @Test
    public void addOperationWithOtherUser() throws Exception {
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("111111");

        Account toAccount = new Account();
        toAccount.setAccountNumber("222222");

        Operation operation = new Operation();
        operation.setFromAccount(fromAccount);
        operation.setToAccount(toAccount);
        operation.setSum(new BigDecimal(10000));

        System.out.println(new Gson().toJson(operation));

        this.mockMvc.perform(post("/operations/add")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(operation)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @WithUserDetails(value = "client3")
    @Test
    public void getBalanceTest() throws Exception {
        this.mockMvc.perform(get("/account/balance")
                        .param("accountNumber", "333333"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @WithUserDetails(value = "bank")
    @Test
    public void getBalanceByBankTest() throws Exception {
        Account account = new Account();
        account.setAccountNumber("333333");

        this.mockMvc.perform(get("/account/balance")
                        .param("accountNumber", "333333"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @WithUserDetails(value = "client1")
    @Test
    public void getBalanceByWrongUserTest() throws Exception {
        Account account = new Account();
        account.setAccountNumber("333333");

        this.mockMvc.perform(get("/account/balance")
                        .param("accountNumber", "333333"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @WithUserDetails(value = "client1")
    @Test
    public void getFullHistoryTest() throws Exception {
        this.mockMvc.perform(get("/account/history")
                        .param("accountNumber", "111111"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @WithUserDetails(value = "bank")
    @Test
    public void getFullHistoryByBankTest() throws Exception {
        this.mockMvc.perform(get("/account/history")
                        .param("accountNumber", "111111"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

    @WithUserDetails(value = "client3")
    @Test
    public void getFullHistoryByWrongUserTest() throws Exception {
        this.mockMvc.perform(get("/account/history")
                        .param("accountNumber", "111111"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

    }

    @WithUserDetails(value = "client1")
    @Test
    public void getPageHistoryTest() throws Exception {
        this.mockMvc.perform(get("/account/history")
                        .param("accountNumber", "111111")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }
}
