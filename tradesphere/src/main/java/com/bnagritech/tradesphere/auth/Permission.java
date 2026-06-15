package com.bnagritech.tradesphere.auth;

import java.time.LocalDateTime;

public class Permission {
    private String permissionId;
    private String permissionCode;
    private String permissionName;
    private String module;
    private String action;
    private String description;
    private Boolean active;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
