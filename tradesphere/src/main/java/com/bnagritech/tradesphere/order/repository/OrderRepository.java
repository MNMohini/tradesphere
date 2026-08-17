package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    List<Order> findByOutletId(String outletId);
    List<Order> findByOutletIdOrderByCreatedAtDesc(String outletId);
    List<Order> findByOutletIdAndOrderStatus(String outletId, OrderStatus orderStatus);
    List<Order> findByOutletIdAndOrderDateBetween(String outletId, LocalDate fromDate, LocalDate toDate);
    List<Order> findByPromoterId(String promoterId);
    List<Order> findByPromoterIdAndOrderStatus(String promoterId, OrderStatus orderStatus);
    List<Order> findByPromoterIdAndOrderDateBetween(String promoterId, LocalDate fromDate, LocalDate toDate);
    List<Order> findByTerritoryId(String territoryId);
    List<Order> findByTerritoryIdAndOrderStatus(String territoryId, OrderStatus orderStatus);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByOrderDateBetween(LocalDate fromDate, LocalDate toDate);
    List<Order> findAllByOrderByCreatedAtDesc();
}
