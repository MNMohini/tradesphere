package com.bnagritech.tradesphere.roleAndPermssion.service;

import com.bnagritech.tradesphere.roleAndPermssion.dto.CreateRoleRequest;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;

import java.util.List;

public interface RoleService {

        Role createRole(CreateRoleRequest request);
        List<Role> getAllRolls();
}
