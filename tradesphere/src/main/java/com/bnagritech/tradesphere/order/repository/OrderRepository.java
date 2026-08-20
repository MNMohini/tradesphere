package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    List<Order> findByOutletId(String outletId);
    List<Order> findByPromoterId(String promoterId);
    List<Order> findByEmployeeId(String employeeId);
    List<Order> findByTerritoryId(String territoryId);
    List<Order> findByOrderDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
    List<Order> findByOutletIdAndStatus(String outletId, OrderStatus status);
    List<Order> findByPromoterIdAndStatus(String promoterId, OrderStatus status);
    List<Order> findByEmployeeIdAndStatus(String employeeId, OrderStatus status);
    List<Order> findByTerritoryIdAndStatus(String territoryId, OrderStatus status);
    List<Order> findByOutletIdAndOrderDateBetween(String outletId, LocalDateTime fromDate, LocalDateTime toDate);
    List<Order> findByPromoterIdAndOrderDateBetween(String promoterId, LocalDateTime fromDate, LocalDateTime toDate);
    List<Order> findByEmployeeIdAndOrderDateBetween(String employeeId, LocalDateTime fromDate, LocalDateTime toDate);
    List<Order> findByTerritoryIdAndOrderDateBetween(String territoryId, LocalDateTime fromDate, LocalDateTime toDate);
}
