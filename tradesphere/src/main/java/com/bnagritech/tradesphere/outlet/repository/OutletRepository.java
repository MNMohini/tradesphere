package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OutletRepository extends MongoRepository<Outlet, String>, OutletRepositoryCustom {

    Optional<Outlet> findByOutletCode(String outletCode);

    /** Retry safety for "create outlet" from the mobile app. */
    Optional<Outlet> findByClientOutletId(String clientOutletId);

    /** ERP or distributor codes must not repeat. */
    boolean existsByExternalCode(String externalCode);

    /** Outlets of a beat, used when planning visits. */
    List<Outlet> findByBeatIdAndStatusOrderByNameAsc(String beatId, OutletStatus status);
}
