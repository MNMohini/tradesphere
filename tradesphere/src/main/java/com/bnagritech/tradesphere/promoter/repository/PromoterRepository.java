package com.bnagritech.tradesphere.promoter.repository;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.promoter.model.Promoter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoterRepository extends MongoRepository<Promoter, String> {

    Optional<Promoter> findByPromoterId(String promoterId);
    Optional<Promoter> findByPhoneNumber(long phoneNumber);
    Optional<Promoter> findByEmail(String email);

    List<Promoter> findByTerritoryId(String territoryId);
    List<Promoter> findByStatus(UserStatus status);

    boolean existsByPromoterId(String promoterId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmail(String email);

}
