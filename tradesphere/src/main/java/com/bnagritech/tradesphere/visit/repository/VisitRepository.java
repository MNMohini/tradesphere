package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.visit.model.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String>, VisitRepositoryCustom {

    /** Retry safety for check-in from the mobile app. */
    Optional<Visit> findByClientVisitId(String clientVisitId);

    /** The visit a user is currently checked in to (a user may have only one). */
    Optional<Visit> findFirstByUserIdAndStatus(String userId, VisitStatus status);

    /** Stops the same outlet being planned twice for the same user and day. */
    boolean existsByUserIdAndOutletIdAndVisitDayAndStatus(
            String userId, String outletId, String visitDay, VisitStatus status);

    /** A user's visits for one day, in the order they were created. */
    List<Visit> findByUserIdAndVisitDayOrderByCreatedAtAsc(String userId, String visitDay);

    /** Used by the nightly job: planned visits whose day has passed. */
    List<Visit> findByStatusAndVisitDayLessThan(VisitStatus status, String visitDay);
}