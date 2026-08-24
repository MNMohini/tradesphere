package com.bnagritech.tradesphere.territory.repository;

import com.bnagritech.tradesphere.promoter.model.Promoter;
import com.bnagritech.tradesphere.territory.model.Territory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TerritoryRepository extends MongoRepository<Territory,String> {

    boolean existsTerritoryByTerritoryId(String territoryId);
    Optional<Territory> findByTerritoryId(String territoryId);
    Optional<Territory> findByTerritoryNameIgnoreCase(String territoryName);
    List<Territory> findByStateIgnoreCase(String state);
    List<Territory> findByCityIgnoreCase(String city);

}
