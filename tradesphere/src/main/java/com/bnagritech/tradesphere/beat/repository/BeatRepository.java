package com.bnagritech.tradesphere.beat.repository;

import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BeatRepository extends MongoRepository<Beat, String> {
    // DUPLICATE VALIDATION
    boolean existsByTerritoryIdAndBeatId(String territoryId, String beatId);
    // Unique beat code validation
    boolean existsByBeatId(String beatId);
    // Get single active beat
    Optional<Beat> findByBeatId(String beatId);
    // TERRITORY BASED OPERATIONS
    List<Beat> findByTerritoryId(String territoryId);
    // LOCATION SEARCH
    List<Beat> findByCityIgnoreCase(String city);
    List<Beat> findByStateIgnoreCase(String state);
    // FILTERS FOR DASHBOARD
    List<Beat> findByBeatStatus(BeatStatus beatStatus);

}
