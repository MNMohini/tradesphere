package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByRetailerId(String retailerId);
    Optional<Order> findByPromoterId(String promoterId);
    Optional<Order> findByPaymentStatus(String paymentStatus);

    boolean existsByOrderNumber(String email);
    boolean existsByRetailerId(String retailerId);
    boolean existsByPromoterId(String promoterId);
    boolean existsByEmployeeID(String employeeId);

}
