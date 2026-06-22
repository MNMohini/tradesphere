package com.bnagritech.tradesphere.roleAndPermssion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
   private String roleId;
   private String roleName;
   private String description;
}
