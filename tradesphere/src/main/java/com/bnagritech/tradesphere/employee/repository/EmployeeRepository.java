package com.bnagritech.tradesphere.employee.repository;
import com.bnagritech.tradesphere.employee.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserName(String userName);
    Optional<Employee> findByEmployeeId(String employeeId);

     boolean existsByEmail(String email);

     boolean existsByEmployeeId(String employeeId);

     boolean existsByUserName(String userName);

}
