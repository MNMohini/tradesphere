package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.Data;


import java.time.LocalDate;

@Data
public class EmployeeRequest {

    private String employeeId;
    private String employeeName;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private String territoryId;
    private String joiningDate;
}
