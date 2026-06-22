package com.bnagritech.tradesphere.roleAndPermssion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
@Document(collection = "role_Permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    private String permissionId;
    private String roleId;
    private Set<Permission> permissions;

}
