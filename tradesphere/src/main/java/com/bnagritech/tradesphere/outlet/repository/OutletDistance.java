package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.outlet.model.Outlet;

/** An outlet together with its straight-line distance from a point, in meters. */
public record OutletDistance(Outlet outlet, double distanceMeters) {
}
