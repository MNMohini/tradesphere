package com.bnagritech.tradesphere.auth;

import java.time.LocalDateTime;
import java.util.List;

public class Role {
    private int roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private List<String> permissionIds;
    private boolean active;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
