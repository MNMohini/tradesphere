package com.bnagritech.tradesphere.retailer.repository;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RetailerRepository extends MongoRepository<Retailer, String> {

    // single retailer search

    Optional<Retailer> findByRetailerId(String retailerId);
    Optional<Retailer> findByPhoneNumber(long phoneNumber);
    Optional<Retailer> findByEmail(String email);

    //Duplicate checks
    boolean existsByRetailerId(String retailerId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(long phoneNumber);
    boolean existsByShopNameAndCityAndState(
            String shopName,
            String city,
            String state
    );

    //location

    List<Retailer>findByCity(String city);
    List<Retailer>findByState(String state);

    //territory management

    List<Retailer>findByTerritoryId(String territoryId);
    List<Retailer>findRetailerByRetailerStatus(RetailerStatus retailerStatus);

    //Search

    List<Retailer> findByShopNameContainingIgnoreCase(String shopName);
    List<Retailer> findByOwnerNameContainingIgnoreCase(String shopName);
    List<Retailer> findRetailerByEmployeeId(String employeeId);

}
