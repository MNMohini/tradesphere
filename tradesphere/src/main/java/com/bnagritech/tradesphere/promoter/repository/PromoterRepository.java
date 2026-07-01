package com.bnagritech.tradesphere.promoter.repository;

import com.bnagritech.tradesphere.promoter.model.Promoter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PromoterRepository extends MongoRepository<Promoter, String> {

    Optional<Promoter> findByPromoterId(String promoterId);
    Optional<Promoter> findByPhoneNumber(long phoneNumber);
    Optional<?> findByEmail(String email);

    List<Promoter> findByTerritoryId(String territoryId);
    List<Promoter> findByStatus(String status);
    List<Promoter> findByState(String state);

}
