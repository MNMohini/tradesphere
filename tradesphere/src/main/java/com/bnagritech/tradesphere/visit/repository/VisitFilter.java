package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.common.enums.VisitStatus;

/**
 * Parameters for searching visits. Every field except viewerUserId is optional.
 *
 * viewerUserId: the user who is asking; results are limited to their own visits and the
 *               visits of everyone below them. Pass null ONLY for ADMIN and NATIONAL_HEAD.
 * fromDay / toDay: inclusive, ISO text such as "2026-11-02".
 */
public record VisitFilter(
        String viewerUserId,
        String userId,
        String outletId,
        String beatId,
        VisitStatus status,
        String fromDay,
        String toDay
) {
}