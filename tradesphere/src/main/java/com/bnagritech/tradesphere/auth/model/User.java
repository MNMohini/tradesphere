package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collation = "User")
public class User {

    @Id
    private String id;
    private String userName;
    private String password;
    private String employeeId;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updateBy;


}
