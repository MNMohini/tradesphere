package com.bnagritech.tradesphere.outlet.repository;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutletRepository extends MongoRepository<Outlet, String> {

    Optional<Outlet> findByOutletId(String outletId);

    Optional<Outlet> findByMobileNumber(String mobileNumber);

    Optional<Outlet> findByEmail(String email);

    //Duplicate checks
    boolean existsByOutletId(String outletId);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    //Search
    List<Outlet> findByOutletNameContainingIgnoreCase(String outletName);

    List<Outlet> findOutletByOutletType(OutletType outletType);

    List<Outlet> findByCityContainingIgnoreCase(String city);

    List<Outlet> findByStateContainingIgnoreCase(String state);

    List<Outlet> findByPinCodeContainingIgnoreCase(String pinCode);

    List<Outlet> findByTerritoryId(String territoryId);

    List<Outlet> findOutletByOutletStatus(OutletStatus outletStatus);
}