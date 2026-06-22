package com.bnagritech.tradesphere.roleAndPermssion.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Sharded;

@Document(collection = "Role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
   private String roleId;
   private String roleName;
   private String description;
   private String employeeId;
}
