package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.order.model.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Queries that cannot be written as simple derived methods.
 * Spring Data merges this interface into OrderRepository automatically.
 */
public interface OrderRepositoryCustom {

    /** Orders matching the filter, newest first. */
    List<Order> search(OrderFilter filter);

    /** A single page of matching orders, newest first. */
    List<Order> search(OrderFilter filter, Pageable pageable);

    /** Aggregate matching orders, optionally grouped by creator identity. */
    List<OrderMetrics> aggregate(OrderFilter filter, boolean groupByCreator);

    /**
     * Atomically increments and returns the counter stored under this key.
     * Safe when many requests arrive at the same time.
     * Example key: "ORDER-2627" (one counter per financial year).
     */
    long nextSequence(String key);
}