package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.enums.EmployeeStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeRequest {
    private String employeeId;
    private String userName;
    private String employeeName;
    private UserRole role;
    private String phoneNumber;
    private String profileImageUrl;
    private String email;
    private String territoryId;
    private EmployeeStatus employeeStatus;
    private String managerName1;
    private String dateOfBirth;
    private String managerName2;
    private String designation;
    private String joiningDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


}
