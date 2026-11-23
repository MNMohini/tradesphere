package com.bnagritech.tradesphere.scheme.repository;

import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import com.bnagritech.tradesphere.product.model.Product;
import com.bnagritech.tradesphere.scheme.model.Scheme;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SchemeRepository extends MongoRepository<Scheme, String> {

    boolean existsBySchemeCode(String schemeCode);

    boolean existsBySchemeCodeAndIdNot(String schemeCode, String id);

    List<Scheme> findByStatus(SchemeStatus status);
    List<Scheme> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            SchemeStatus status,
            LocalDate startDate,
            LocalDate endDate
    );
}