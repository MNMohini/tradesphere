package com.bnagritech.tradesphere.roleAndPermssion.repository;
import com.bnagritech.tradesphere.roleAndPermssion.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

    @Override
    boolean existsByEmployeeId(String employeeId);
}
