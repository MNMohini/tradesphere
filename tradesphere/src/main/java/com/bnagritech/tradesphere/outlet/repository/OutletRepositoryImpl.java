package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.util.GeoUtils;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** The name must be the repository interface name + "Impl" so Spring Data finds it. */
@RequiredArgsConstructor
public class OutletRepositoryImpl implements OutletRepositoryCustom {

    // Safety cap on how many candidates are read from the database for one nearby search
    private static final int MAX_NEARBY_CANDIDATES = 1000;

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Outlet> search(OutletFilter f) {
        List<Criteria> conditions = new ArrayList<>();

        // Visibility: ACTIVE outlets are visible to everyone; others only to the creator and managers
        if (f.viewerUserId() != null) {
            conditions.add(new Criteria().orOperator(
                    Criteria.where("status").is(OutletStatus.ACTIVE),
                    Criteria.where("createdBy").is(f.viewerUserId()),
                    Criteria.where("userPath").is(f.viewerUserId())));
        }

        if (hasText(f.keyword())) {
            // Pattern.quote makes the text literal, so characters like ( or * cannot break the query
            Pattern p = Pattern.compile(Pattern.quote(f.keyword().trim()), Pattern.CASE_INSENSITIVE);
            conditions.add(new Criteria().orOperator(
                    Criteria.where("name").regex(p),
                    Criteria.where("outletCode").regex(p),
                    Criteria.where("ownerName").regex(p),
                    Criteria.where("phoneNumber").regex(p)));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (f.type() != null) {
            conditions.add(Criteria.where("type").is(f.type()));
        }
        if (f.grade() != null) {
            conditions.add(Criteria.where("grade").is(f.grade()));
        }
        if (hasText(f.beatId())) {
            conditions.add(Criteria.where("beatId").is(f.beatId()));
        }
        if (hasText(f.territoryId())) {
            conditions.add(Criteria.where("territoryId").is(f.territoryId()));
        }
        if (hasText(f.city())) {
            conditions.add(Criteria.where("address.city").regex(
                    Pattern.compile("^" + Pattern.quote(f.city().trim()) + "$", Pattern.CASE_INSENSITIVE)));
        }
        if (hasText(f.pinCode())) {
            conditions.add(Criteria.where("address.pincode").is(f.pinCode().trim()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }
        query.with(Sort.by(Sort.Direction.ASC, "name")).limit(f.limit());

        return mongoTemplate.find(query, Outlet.class);
    }

    @Override
    public List<OutletDistance> findNearby(GeoJsonPoint center, double radiusMeters, int limit,
                                           Collection<OutletStatus> statuses) {
        // 1) Let MongoDB pick the outlets inside the circle ($geoWithin / $centerSphere).
        //    Circle takes (longitude, latitude), the radius is converted to radians by Spring Data.
        Circle circle = new Circle(new Point(center.getX(), center.getY()),
                new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS));

        Query query = new Query(Criteria.where("location").withinSphere(circle));
        if (statuses != null && !statuses.isEmpty()) {
            query.addCriteria(Criteria.where("status").in(statuses));
        }
        query.limit(MAX_NEARBY_CANDIDATES);

        List<Outlet> candidates = mongoTemplate.find(query, Outlet.class);

        // 2) Work out the exact distance ourselves, drop anything slightly outside, sort nearest first
        return candidates.stream()
                .filter(o -> o.getLocation() != null)
                .map(o -> new OutletDistance(o, GeoUtils.distanceMeters(
                        center.getY(), center.getX(), o.getLocation().getY(), o.getLocation().getX())))
                .filter(d -> d.distanceMeters() <= radiusMeters)
                .sorted(Comparator.comparingDouble(OutletDistance::distanceMeters))
                .limit(limit)
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
