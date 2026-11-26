package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.visit.model.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String>, VisitRepositoryCustom {

    /** Find by the tracking number, e.g. VI000000001. */
    Optional<Visit> findByVisitId(String visitId);

    /** Retry safety for a walk-in check-in from the mobile app. */
    Optional<Visit> findByClientVisitId(String clientVisitId);

    /** The visit a user is on right now (a user may have only one). */
    Optional<Visit> findFirstByUserIdAndStatus(String userId, VisitStatus status);

    /** A user's visits on one day in the given statuses. */
    List<Visit> findByUserIdAndVisitDayAndStatusIn(String userId, String visitDay, Collection<VisitStatus> statuses);

    /** A user's visits for one day, in the order they were created. */
    List<Visit> findByUserIdAndVisitDayOrderByCreatedAtAsc(String userId, String visitDay);

    /** Used by the nightly job: visits in a status whose day has passed. */
    List<Visit> findByStatusAndVisitDayLessThan(VisitStatus status, String visitDay);

    /** A photo may be used for one outlet check-in only. */
    boolean existsByOutletVisitsPictureWithOutletFileId(String fileId);
}
