package com.bnagritech.tradesphere.common;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


public enum UserRole {
    ADMIN,
    NATIONAL_HEAD,
    REGIONAL_MANAGER,
    STATE_MANAGER,
    AREA_MANAGER,
    DISTRICT_MANAGER,
    PROMOTER
}
