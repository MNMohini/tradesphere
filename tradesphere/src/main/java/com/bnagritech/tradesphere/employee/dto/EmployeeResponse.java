package com.bnagritech.tradesphere.employee.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String employeeId;
    private String userName;
    private String employeeName;
    private String email;
    private long phoneNumber;
    private UserRole role;
    private String territoryId;
    private Date joiningDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
