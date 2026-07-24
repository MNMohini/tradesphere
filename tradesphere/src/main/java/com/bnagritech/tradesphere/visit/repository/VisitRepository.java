package com.bnagritech.tradesphere.visit.repository;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.visit.model.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends MongoRepository<Visit, String> {
    Optional<Visit> findByVisitCode(String visitCode);

   /* List<Visit> findByEmployeeId(String employeeId);
    List<Visit> findByRetailerId(String retailerId);
    List<Visit> findByBeatId(String beatId);
    List<Visit> findByTerritoryId(String territoryId);
    List<Visit> findByVisitStatus(VisitStatus visitStatus);
    List<Visit> findByPlannedDate(LocalDateTime plannedDate);
    boolean existsByVisitCode(String visitCode);
    long countByEmployeeId(String employeeId);
*/
}
