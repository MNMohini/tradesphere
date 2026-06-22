package com.bnagritech.tradesphere.roleAndPermssion.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Role")
public class Role {
   private String roleId;
   private String roleName;
   private String description;
}
