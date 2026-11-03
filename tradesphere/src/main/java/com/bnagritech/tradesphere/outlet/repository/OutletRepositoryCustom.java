package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.Collection;
import java.util.List;

public interface OutletRepositoryCustom {

    /** Outlets matching the filter, sorted by name, at most filter.limit(). */
    List<Outlet> search(OutletFilter filter);

    /**
     * Outlets within radiusMeters of the center, nearest first, at most limit.
     * statuses: only outlets in these statuses are returned (null or empty = any status).
     */
    List<OutletDistance> findNearby(GeoJsonPoint center, double radiusMeters, int limit,
                                    Collection<OutletStatus> statuses);
}
