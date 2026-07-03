package com.bnagritech.tradesphere.roleAndPermssion.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
   private String roleId;
   private UserRole roleName;
   private String description;
   private String employeeId;
}
