package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class EmployeeRequest {

    private String employeeId;
    private String employeeName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private String territoryId;
    private LocalDateTime joiningDate;
}
