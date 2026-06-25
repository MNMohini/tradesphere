package com.bnagritech.tradesphere.territory.repository;

import com.bnagritech.tradesphere.territory.model.Territory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerritoryRepository extends MongoRepository<Territory,String> {

    boolean existsTerritoryByTerritoryId(String territoryId);

    Optional<Territory> findByState(String state);

}
