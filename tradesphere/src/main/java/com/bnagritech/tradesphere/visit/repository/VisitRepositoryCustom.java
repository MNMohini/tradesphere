package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.visit.model.Visit;

import java.util.List;

public interface VisitRepositoryCustom {

    /** Visits matching the filter, newest day first. */
    List<Visit> search(VisitFilter filter);
}