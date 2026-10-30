package com.bnagritech.tradesphere.retailer.repository;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RetailerRepository extends MongoRepository<Retailer, String> {

    // single retailer search
    Optional<Retailer> findByRetailerNameContainingIgnoreCase(String retailerName);
    Optional<Retailer> findByRetailerId(String retailerId);
    boolean existsByRetailerNameAndRetailerId(String retailerName, String retailerId);
    boolean existsByRetailerId(String retailerId);


}
