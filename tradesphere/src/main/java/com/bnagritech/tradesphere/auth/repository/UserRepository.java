package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByUserName(String userName);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    /** True if at least one user has this role (used to create the first admin only once). */
    boolean existsByRole(UserRole role);

    /** Direct reports of a manager. */
    List<User> findByManagerId(String managerId);

    /** Used to stop a manager being deactivated while active people still report to them. */
    boolean existsByManagerIdAndStatus(String managerId, UserStatus status);

    /** Everyone below a user in the hierarchy: "path" is an array, so this matches arrays that contain the id. */
    List<User> findByPath(String userId);
}
