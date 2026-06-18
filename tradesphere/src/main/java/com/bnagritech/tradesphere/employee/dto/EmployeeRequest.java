package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequest {

    private String employeeId;
    private String employeeName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private String territoryId;
}
