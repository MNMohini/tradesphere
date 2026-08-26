package com.bnagritech.tradesphere.roleAndPermssion.service.impl;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.roleAndPermssion.dto.CreateRoleRequest;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;
import com.bnagritech.tradesphere.roleAndPermssion.repository.RoleRepository;
import com.bnagritech.tradesphere.roleAndPermssion.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

     private  final RoleRepository roleRepository = null;
    @Override
     public Role  createRole(CreateRoleRequest request){
         if (roleRepository.existsByEmployeeId(request.getEmployeeId())){
            throw new RuntimeException("This EmployeeId already Used");
         }

         Role role = Role.builder()
                 .roleId(request.getRoleId())
                 .roleName(UserRole.valueOf(request.getRoleName()))
                 .description(request.getDescription())
                 .employeeId(request.getEmployeeId())
                 .build();
          return roleRepository.save(role);
     }

     @Override
     public List<Role> getAllRolls(){
         return roleRepository.findAll();
     }
}
