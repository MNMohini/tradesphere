package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String>, OrderRepositoryCustom {

    // ---- Lookups ----

    Optional<Order> findByOrderNumber(String orderNumber);

    /** Duplicate check for one order coming from the mobile app. */
    Optional<Order> findByClientOrderId(String clientOrderId);

    boolean existsByClientOrderId(String clientOrderId);

    /** Duplicate check for a whole sync batch in a single query. */
    List<Order> findByClientOrderIdIn(Collection<String> clientOrderIds);

    /** Load an order only if it belongs to this user (rep editing a draft). */
    Optional<Order> findByIdAndCreatedBy(String id, String createdBy);

    // ---- Lists ----

    List<Order> findByVisitIdOrderByOrderDateDesc(String visitId);

    List<Order> findByOutletIdOrderByOrderDateDesc(String outletId);

    /** For example a rep's own DRAFT orders. */
    List<Order> findByCreatedByAndStatusOrderByOrderDateDesc(String createdBy, OrderStatus status);

    // ---- Counts ----

    long countByStatus(OrderStatus status);

    long countByOutletId(String outletId);
}