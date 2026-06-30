package com.bnagritech.tradesphere.promoter.repository;

import com.bnagritech.tradesphere.promoter.model.Promoter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PromoterRepository extends MongoRepository<Promoter, String> {


}
