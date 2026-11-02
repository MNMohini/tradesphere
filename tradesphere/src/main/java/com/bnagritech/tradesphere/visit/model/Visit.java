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
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visits")
@CompoundIndexes({
        @CompoundIndex(name = "user_day",   def = "{'userId': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "path_day",   def = "{'userPath': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "outlet_day", def = "{'outletId': 1, 'visitDay': -1}"),
        @CompoundIndex(name = "status_day", def = "{'status': 1, 'visitDay': -1}")
})
public class Visit {

    @Id
    private String id;

    @Version
    private Long version;

    // UUID from the mobile app; makes a retried check-in safe
    @Indexed(unique = true, sparse = true)
    private String clientVisitId;

    // ---- Who ----
    private String userId;
    private String userName;             // snapshot
    private List<String> userPath;       // the user's ancestors, for manager-level queries

    // ---- Where ----
    private String outletId;
    private String outletName;           // snapshot
    private GeoJsonPoint outletLocation; // snapshot, used for the distance check
    private String beatId;

    // ---- When ----
    // Calendar day in IST as "yyyy-MM-dd": the planned day, or the check-in day for unplanned visits.
    // Stored as text so it never shifts with time zones, and still sorts and range-queries correctly.
    private String visitDay;

    // ---- State ----
    private boolean planned;             // false = the rep walked in without a plan
    private VisitStatus status;

    private VisitPunch checkIn;
    private VisitPunch checkOut;
    private Long durationMinutes;

    private VisitOutcome outcome;
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