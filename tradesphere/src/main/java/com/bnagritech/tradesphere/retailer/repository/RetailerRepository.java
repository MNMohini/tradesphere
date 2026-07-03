package com.bnagritech.tradesphere.retailer.repository;

import com.bnagritech.tradesphere.retailer.model.Retailer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RetailerRepository extends MongoRepository<Retailer, String> {

    Optional<Retailer> findByRetailerId(String retailerId);
    Optional<Retailer> findByPhoneNumber(long phoneNumber);
    Optional<Retailer> findByEmail(String email);

    boolean existsByPromoterId(String promoterId);
    boolean existsByEmail(String email);
}
