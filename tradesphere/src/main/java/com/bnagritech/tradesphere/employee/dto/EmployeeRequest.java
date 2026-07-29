package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeId;
    private String employeeName;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private String territoryId;
    private String dateOfBirth;
    private String designation;
    private String joiningDate;
    private String managerName1;
    private String managerName2;


}
