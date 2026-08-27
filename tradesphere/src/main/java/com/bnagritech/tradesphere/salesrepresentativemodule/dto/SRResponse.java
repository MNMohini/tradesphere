package com.bnagritech.tradesphere.salesrepresentativemodule.dto;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SRResponse {
    private String srId;
    private String srName;
    private String userName;
    private String phoneNumber;
    private String email;
    private String territoryId;
    private String city;
    private String state;
    private UserStatus status;
}
