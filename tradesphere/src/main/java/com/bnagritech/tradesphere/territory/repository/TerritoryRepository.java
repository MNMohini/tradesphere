package com.bnagritech.tradesphere.territory.repository;

import com.bnagritech.tradesphere.territory.model.Territory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TerritoryRepository extends MongoRepository<Territory,String> {

    boolean existsTerritoryByTerritoryId(String territoryId);

    boolean existsTerritoryByTerritoryNameAndCityAndState(
            String territoryName, String city, String state);
    List<Territory> findByStateIgnoreCase(String state);
    List<Territory> findByCityIgnoreCase(String city);

}
