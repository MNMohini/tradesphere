package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String id;
    private String userName;
    private UserRole role;
    private String employeeId;
    private Boolean active;

}
