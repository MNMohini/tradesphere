package com.bnagritech.tradesphere.roleAndPermssion.dto;

import lombok.Data;

@Data
public class CreateRoleRequest {

    private String roleName;
    private String description;
    private String employeeId;
}
