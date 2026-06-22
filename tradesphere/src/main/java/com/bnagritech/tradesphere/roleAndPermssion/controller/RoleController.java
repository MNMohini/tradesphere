package com.bnagritech.tradesphere.roleAndPermssion.controller;

import com.bnagritech.tradesphere.roleAndPermssion.dto.CreateRoleRequest;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;
import com.bnagritech.tradesphere.roleAndPermssion.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/create")
    public Role createRole(@RequestBody CreateRoleRequest request)
    {
        return roleService.createRole(request);
    }

    @GetMapping("/allRoles")
    public List<Role> getAllRoles(){
        return roleService.getAllRolls();
    }

}
