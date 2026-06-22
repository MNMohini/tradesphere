package com.bnagritech.tradesphere.roleAndPermssion.service.impl;

import com.bnagritech.tradesphere.roleAndPermssion.dto.CreateRoleRequest;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;
import com.bnagritech.tradesphere.roleAndPermssion.repository.RoleRepository;
import com.bnagritech.tradesphere.roleAndPermssion.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

     private  final RoleRepository roleRepository;

     public Role  createRole(CreateRoleRequest request){
         if (roleRepository.existsByEmployeeId(request.getEmployeeId())){
            throw new RuntimeException("This EmployeeId already Used");
         }

     }

}
