package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends MongoRepository<User , String> {

    // login using username
    Optional<User> findByUserName(String userName);
    // forgot password
    Optional<User> findByEmail(String email);
    Optional<User> findByUserNameOrEmail(String UserName, String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmployeeId(String employeeId);
    // duplicate username check
    boolean existsByUserName(String userName);
    // duplicate email check
    boolean existsByEmail(String email);
    // duplicate phone number check
    boolean existsByPhoneNumber(String phoneNumber);
    // employee already has login or not
    boolean existsByEmployeeId(String employeeId);

}


