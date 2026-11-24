package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;

/**
 * Parameters for searching users. Every field except limit is optional.
 *
 * viewerUserId: the user who is asking; results are limited to themselves and everyone below them.
 *               Pass null ONLY for ADMIN and NATIONAL_HEAD (they see everyone).
 * keyword:      matched, ignoring case, against username, full name, employee id, phone and email.
 */
public record UserFilter(
        String viewerUserId,
        String keyword,
        UserRole role,
        UserStatus status,
        String managerId,
        String territoryId,
        int limit
) {
}
