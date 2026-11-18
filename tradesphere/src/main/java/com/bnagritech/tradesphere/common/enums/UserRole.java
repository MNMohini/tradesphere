package com.bnagritech.tradesphere.common.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum UserRole {
    ADMIN(0),
    NATIONAL_HEAD(1),
    REGIONAL_MANAGER(2),
    AREA_MANAGER(3),
    SALES_OFFICER(4),
    SUPERVISOR(5),
    SALES_REPRESENTATIVE(6),
    PROMOTER(6);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

    public boolean isHigherThan(UserRole other) {
        return this.level < other.level;
    }

    public boolean isFieldLevel() {
        return this == SALES_REPRESENTATIVE || this == PROMOTER;
    }

    /** Roles that may report directly to a user with this role. This is the one place that encodes the hierarchy. */
    public Set<UserRole> allowedChildren() {
        return switch (this) {
            case ADMIN -> EnumSet.of(NATIONAL_HEAD);
            case NATIONAL_HEAD -> EnumSet.of(REGIONAL_MANAGER);
            case REGIONAL_MANAGER -> EnumSet.of(AREA_MANAGER);
            case AREA_MANAGER -> EnumSet.of(SALES_OFFICER);
            case SALES_OFFICER -> EnumSet.of(SUPERVISOR,SALES_REPRESENTATIVE);
            case SUPERVISOR -> EnumSet.of(PROMOTER);
            case SALES_REPRESENTATIVE, PROMOTER -> EnumSet.noneOf(UserRole.class);
        };
    }
}