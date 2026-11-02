package com.bnagritech.tradesphere.promoter.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromoterRequest {

    private String id;
    private String promoterId;
    private String promoterName;
    private String userName;
    private long phoneNumber;
    private String email;
    private String outletId;
    private String outletName;
    private String city;
}
