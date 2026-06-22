package com.bnagritech.tradesphere.roleAndPermssion.repository;

import com.bnagritech.tradesphere.roleAndPermssion.model.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    Optional<RolePermission> findByRoleId(String roleId);
}
