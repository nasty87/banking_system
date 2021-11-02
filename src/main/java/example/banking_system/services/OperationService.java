package example.banking_system.services;

import example.banking_system.models.OperationDao;
import example.banking_system.models.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private OperationDao operationDao;
}
