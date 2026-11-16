package com.bnagritech.tradesphere.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN(0),
    NATIONAL_HEAD(1),
    REGIONAL_MANAGER(2),
    AREA_MANAGER(3),
    SALES_OFFICER(4),
    SUPERVISOR(5),
    SALES_REPRESENTATIVE(6),
    PROMOTER(6);   // same level as sales rep

    private final int level;

    UserRole(int level) { this.level = level; }

    public boolean isHigherThan(UserRole other) {
        return this.level < other.level;
    }

    public boolean isFieldLevel() {
        return this == SALES_REPRESENTATIVE || this == PROMOTER;
    }
}
