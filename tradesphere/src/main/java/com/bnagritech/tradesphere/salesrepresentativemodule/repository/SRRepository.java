package com.bnagritech.tradesphere.salesrepresentativemodule.repository;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.salesrepresentativemodule.model.SalesRepresentative;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SRRepository extends MongoRepository<SalesRepresentative, String> {
    Optional<SalesRepresentative> findBySRId(String SRId);
    Optional<SalesRepresentative> findByEmail(String email);
    Optional<SalesRepresentative> findByPhoneNumber(String phoneNumber);
    List<SalesRepresentative> findByTerritoryId(String  territoryId);
    List<SalesRepresentative> findByStatus(UserStatus status);
    boolean existsBySRId(String SRId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);


}
