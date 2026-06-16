package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User , String> {

    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);





}
