package com.bnagritech.tradesphere.employee.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
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
@Document(collection = "employees")
public class Employee {
    @Id
    private String id;
    private String employeeId;
    private String userName;
    private String employeeName;
    private UserRole role;
    private String phoneNumber;
    private String profileImageUrl;
    private String email;
    private String territoryId;
    private String joiningDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

