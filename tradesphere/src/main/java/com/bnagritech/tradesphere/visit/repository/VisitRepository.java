package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.visit.model.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String>, VisitRepositoryCustom {

    /** Retry safety for check-in from the mobile app. */
    Optional<Visit> findByClientVisitId(String clientVisitId);

    /** The visit a user is currently checked in to (a user may have only one). */
    Optional<Visit> findFirstByUserIdAndStatus(String userId, VisitStatus status);

    /** Visits of one user on one day for any of the given outlets (stops the same outlet being planned twice). */
    List<Visit> findByUserIdAndVisitDayAndOutletCodeInAndStatusIn(
            String userId, String visitDay, Collection<String> outletCodes, Collection<VisitStatus> statuses);

    /** A user's visits for one day, in the order they were created. */
    List<Visit> findByUserIdAndVisitDayOrderByCreatedAtAsc(String userId, String visitDay);

    /** Used by the nightly job: planned visits whose day has passed. */
    List<Visit> findByStatusAndVisitDayLessThan(VisitStatus status, String visitDay);

    /** A check-in photo may be used for one visit only. */
    boolean existsByCheckInPhotoFileId(String fileId);
}
