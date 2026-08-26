package com.bnagritech.tradesphere.roleAndPermssion.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class CreateRoleRequest {
    @Id
    private String roleId;
    private String roleName;
    private String description;
    private String employeeId;

}
