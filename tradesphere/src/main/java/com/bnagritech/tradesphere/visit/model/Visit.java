package com.bnagritech.tradesphere.visit.model;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One visit = one user's round of several outlets on one day, tracked by visitId (VI000000001).
 * Each outlet of the round is one entry in outletVisits, with its own check-in, check-out,
 * outcome and photo.
 *
 *  Visit status:   PLANNED -> IN_PROGRESS (first outlet check-in) -> COMPLETED (all outlets done, or ended)
 *                  PLANNED -> MISSED (nightly job) or CANCELLED
 *  Outlet status:  PLANNED -> IN_PROGRESS -> COMPLETED,  or PLANNED -> MISSED / CANCELLED
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visits")
@CompoundIndexes({
        @CompoundIndex(name = "user_day",   def = "{'userId': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "path_day",   def = "{'userPath': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "outlets_day", def = "{'outletCodes': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "status_day", def = "{'status': 1, 'visitDay': -1}"),
        // a user can be on only one visit at a time
        @CompoundIndex(name = "one_active_visit_per_user",
                def = "{'userId': 1}", unique = true,
                partialFilter = "{ 'status': 'IN_PROGRESS' }"),
        // a user has only one PLANNED visit per day (planning again adds outlets to it)
        @CompoundIndex(name = "one_planned_visit_per_day",
                def = "{'userId': 1, 'visitDay': 1}", unique = true,
                partialFilter = "{ 'status': 'PLANNED' }"),
        // a photo can be used only once
        @CompoundIndex(name = "outlet_photo", def = "{'outletVisits.pictureWithOutlet.fileId': 1}")
})
public class Visit {

    @Id
    private String id;

    // Tracking number shown to users: VI000000001
    @Indexed(unique = true)
    private String visitId;

    @Version
    private Long version;

    // UUID from the mobile app; makes a retried unplanned check-in safe
    @Indexed(unique = true, sparse = true)
    private String clientVisitId;

    // ---- Who ----
    private String userId;
    private String userName;             // snapshot
    private List<String> userPath;       // the user's ancestors, for manager-level queries

    // ---- Where ----
    @Builder.Default
    private List<String> outletCodes = new ArrayList<>();          // all outlets of this visit
    @Builder.Default
    private List<VisitOutlet> outletVisits = new ArrayList<>();    // one entry per outlet (same order)
    private String beatId;

    // ---- When ----
    // Calendar day in IST as "yyyy-MM-dd". Stored as text so it never shifts with time zones.
    private String visitDay;

    // ---- State ----
    private boolean planned;             // false = started with a walk-in, no plan
    private VisitStatus status;
    private VisitPunch checkIn;          // start of the visit (check-in at the first outlet)
    private VisitPunch checkOut;         // end of the visit
    private Long durationMinutes;        // whole visit
    private VisitOutcome outcome;        // summary of the outlets' outcomes
    private String outcomeRemarks;
    private String cancellationReason;

    // ---- Tracking ----
    @Builder.Default
    private List<TrackPoint> trackPoints = new ArrayList<>();

    @Builder.Default
    private List<VisitStatusHistory> statusHistory = new ArrayList<>();

    // ---- Audit ----
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
