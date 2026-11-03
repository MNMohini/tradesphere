package com.bnagritech.tradesphere.outlet.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.outlet.dto.*;

import java.util.List;

/**
 * Outlet lifecycle:  PENDING_APPROVAL -> ACTIVE | REJECTED
 *                    ACTIVE <-> INACTIVE
 *                    REJECTED -> PENDING_APPROVAL (the creator fixes it and resubmits)
 * Only ACTIVE outlets can be visited or receive orders.
 */
public interface OutletService {

    /** Creates an outlet. Retrying with the same clientOutletId returns the outlet already created. */
    OutletResponse createOutlet(CreateOutletRequest request, User currentUser);

    /** Changes the fields that are sent. Some fields are manager-only. */
    OutletResponse updateOutlet(String outletId, UpdateOutletRequest request, User currentUser);

    /** Approve, reject, deactivate, reactivate or resubmit. */
    OutletResponse updateOutletStatus(String outletId, UpdateOutletStatusRequest request, User currentUser);

    /** One outlet, if the caller may see it. */
    OutletResponse getOutletById(String outletId, User currentUser);

    /** One outlet found by its code (for example OUT-000123), if the caller may see it. */
    OutletResponse getOutletByCode(String outletCode, User currentUser);

    /** Outlets the caller may see, narrowed by the filters, sorted by name. */
    List<OutletSummaryResponse> searchOutlets(OutletSearchRequest request, User currentUser);

    /** ACTIVE outlets around a point, nearest first. */
    List<NearByOutletResponse> findNearbyOutlets(NearByOutletRequest request, User currentUser);

    /** ACTIVE outlets of a beat, sorted by name (used when planning visits). */
    List<OutletSummaryResponse> getOutletsByBeat(String beatId, User currentUser);
}
