package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;

/**
 * Parameters for searching outlets. Every field except limit is optional.
 *
 * viewerUserId: the user who is asking. ACTIVE outlets are visible to everyone; outlets in any other
 *               status are visible only to their creator and the creator's managers.
 *               Pass null ONLY for ADMIN and NATIONAL_HEAD (they see everything).
 * keyword:      matched, ignoring case, against name, outlet code, owner name and phone number.
 */
public record OutletFilter(
        String viewerUserId,
        String keyword,
        OutletStatus status,
        OutletType type,
        OutletGrade grade,
        String beatId,
        String territoryId,
        String city,
        String pinCode,
        int limit )
{
}
