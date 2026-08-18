package com.bnagritech.tradesphere.order.Service.Impl;

import com.bnagritech.tradesphere.order.Service.OrderService;
import com.bnagritech.tradesphere.order.dto.*;

import java.util.List;

public class OrderServiceImpl implements OrderService {

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        return null;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return List.of();
    }

    @Override
    public OrderResponse getOrderById(String id) {
        return null;
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        return null;
    }

    @Override
    public List<OrderResponse> getOrdersByOutletId(String outletId) {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrdersByPromoterId(String promoterId) {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrdersByEmployeeId(String employeeId) {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrdersByTerritoryId(String territoryId) {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(String status) {
        return List.of();
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request) {
        return null;
    }

    @Override
    public OrderResponse updateOrder(String id, OrderRequest request) {
        return null;
    }

    @Override
    public void deleteOrder(String id) {

    }

    @Override
    public List<OrderResponse> searchOrders(OrderSearchRequest request) {
        return List.of();
    }

    @Override
    public OrderSummaryResponse getOrderSummary() {
        return null;
    }
}
