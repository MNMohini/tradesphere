package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.visit.model.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/** The name must be the repository interface name + "Impl" so Spring Data finds it. */
@RequiredArgsConstructor
public class VisitRepositoryImpl implements VisitRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Visit> search(VisitFilter f) {
        return mongoTemplate.find(sorted(buildQuery(f)), Visit.class);
    }

    @Override
    public List<Visit> search(VisitFilter f, long offset, int limit) {
        Query query = sorted(buildQuery(f)).skip(offset).limit(limit);
        return mongoTemplate.find(query, Visit.class);
    }

    @Override
    public long count(VisitFilter f) {
        return mongoTemplate.count(buildQuery(f), Visit.class);
    }

    @Override
    public List<Visit> searchAttendance(VisitFilter f, long offset, int limit) {
        Query query = sorted(buildQuery(f, true)).skip(offset).limit(limit);
        return mongoTemplate.find(query, Visit.class);
    }

    @Override
    public long countAttendance(VisitFilter f) {
        return mongoTemplate.count(buildQuery(f, true), Visit.class);
    }

    private Query buildQuery(VisitFilter f) {
        return buildQuery(f, false);
    }

    private Query buildQuery(VisitFilter f, boolean attendanceOnly) {
        List<Criteria> conditions = new ArrayList<>();

        if (attendanceOnly) {
            conditions.add(new Criteria().orOperator(
                    Criteria.where("checkIn.time").exists(true),
                    Criteria.where("outletVisits.checkIn.time").exists(true)));
        }

        // Hierarchy visibility: own visits, or visits of anyone below the viewer
        if (f.viewerUserId() != null) {
            conditions.add(new Criteria().orOperator(
                    Criteria.where("userId").is(f.viewerUserId()),
                    Criteria.where("userPath").is(f.viewerUserId())));
        }
        if (f.userId() != null) {
            conditions.add(Criteria.where("userId").is(f.userId()));
        }
        if (f.outletCode() != null) {
            // outletCodes is a list: this matches visits that contain the outlet
            conditions.add(Criteria.where("outletCodes").is(f.outletCode()));
        }
        if (f.beatId() != null) {
            conditions.add(Criteria.where("beatId").is(f.beatId()));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (f.fromDay() != null || f.toDay() != null) {
            Criteria day = Criteria.where("visitDay");
            if (f.fromDay() != null) {
                day = day.gte(f.fromDay());
            }
            if (f.toDay() != null) {
                day = day.lte(f.toDay());
            }
            conditions.add(day);
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }
        return query;
    }

    private Query sorted(Query query) {
        return query.with(Sort.by(Sort.Direction.DESC, "visitDay").and(Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
