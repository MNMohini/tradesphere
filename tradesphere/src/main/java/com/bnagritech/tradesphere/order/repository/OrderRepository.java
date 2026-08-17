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
    List<Order> findByRetailerId(String retailerId);
    List<Order> findByOutletId(String outletId);
    List<Order> findByTerritoryId(String territoryId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByRetailerIdAndOrderStatus(String retailerId, OrderStatus orderStatus);
    List<Order> findByPromoterIdAndOrderStatus(String promoterId,OrderStatus orderStatus);
    List<Order> findByOrderDateBetween(LocalDate fromDate, LocalDate toDate);
    List<Order> findByRetailerIdAndOrderDateBetween(String retailerId, LocalDate fromDate, LocalDate toDate);
    List<Order> findByPromoterIdAndOrderDateBetween(String promoterId, LocalDate fromDate, LocalDate toDate);
    List<Order> findByTerritoryIdAndOrderStatus(String territoryId, OrderStatus orderStatus);

}
