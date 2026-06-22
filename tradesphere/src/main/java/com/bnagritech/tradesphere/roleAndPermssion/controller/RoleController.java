package com.bnagritech.tradesphere.roleAndPermssion.controller;

import com.bnagritech.tradesphere.roleAndPermssion.dto.CreateRoleRequest;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;
import com.bnagritech.tradesphere.roleAndPermssion.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public Role createRole(@RequestBody CreateRoleRequest request)
    {
        return roleService.createRole(request);
    }

    @GetMapping("/all")
    public List<Role> getAllRoles(){
        return roleService.getAllRolls();
    }
}
