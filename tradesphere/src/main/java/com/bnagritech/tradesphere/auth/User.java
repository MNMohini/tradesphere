package com.bnagritech.tradesphere.auth;

import com.bnagritech.tradesphere.common.UserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "User")
public class User {
    @Id
    private String id;
    private String userName;
    private String password;
    private String employeeId;
    private UserRole role;
    private Boolean active;


}
