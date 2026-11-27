package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.visit.model.Visit;

import java.util.List;

public interface VisitRepositoryCustom {

    /** Visits matching the filter, newest day first. */
    List<Visit> search(VisitFilter filter);

    /** One page of matching visits, newest day first. */
    List<Visit> search(VisitFilter filter, long offset, int limit);

    /** Number of visits matching the filter. */
    long count(VisitFilter filter);

    /** One page of actual attendance records (visits with a check-in). */
    List<Visit> searchAttendance(VisitFilter filter, long offset, int limit);

    /** Number of actual attendance records matching the filter. */
    long countAttendance(VisitFilter filter);
}
