package example.banking_system.controllers;

import example.banking_system.models.*;
import example.banking_system.services.OperationService;
import example.banking_system.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
public class OperationController {
    @Autowired
    OperationService  operationService;
    @Autowired
    UserService userService;

    @Secured(Role.ClientRoleName)
    @PostMapping(path = "/clients/operations/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addClientOperation(@NotNull @RequestBody OperationDto operation)
            throws InvalidParameterException, NotAllowedException {
        operationService.addClientOperation(operation, userService.getCurrentUser());
    }

    @Secured(Role.BankRoleName)
    @PostMapping(path = "/bank/operations/put", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addBankOperationPut(@NotNull @RequestBody OperationDto operation)
            throws InvalidParameterException, NotAllowedException {
        operationService.addBankOperationPut(operation, userService.getCurrentUser());
    }

    @Secured(Role.BankRoleName)
    @PostMapping(path = "/bank/operations/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addBankOperationWithdraw(@NotNull @RequestBody OperationDto operation)
            throws InvalidParameterException, NotAllowedException {
        operationService.addBankOperationWithdraw(operation, userService.getCurrentUser());
    }

    @Transactional
    @GetMapping(path = "/account/balance")
    public String getBalance(@Size(min = 20, max = 20) @RequestParam (value = "accountNumber") String accountNumber)
            throws InvalidParameterException, NotAllowedException {
        return operationService.getBalance(accountNumber, userService.getCurrentUser());
    }

    @Transactional
    @GetMapping(path = "/account/history")
    public List<OperationInfo> getHistoryPage(@Size(min = 20, max = 20) @RequestParam (value = "accountNumber", defaultValue = "") String accountNumber,
                                              @RequestParam (value = "pageNumber", defaultValue = "-1") int pageNumber,
                                              @RequestParam (value = "pageSize", defaultValue = "0") int pageSize)
            throws InvalidParameterException, NotAllowedException {
        return operationService.getHistoryPage(accountNumber, pageNumber, pageSize, userService.getCurrentUser());
    }




}
