package com.bnagritech.tradesphere.order.Service;

import com.bnagritech.tradesphere.order.dto.*;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);
    List<OrderResponse> getAllOrders();
    OrderResponse getOrderById(String id);
    OrderResponse getOrderByOrderNumber(String orderNumber);
    List<OrderResponse> getOrdersByOutletId(String outletId);
    List<OrderResponse> getOrdersByPromoterId(String promoterId);
    List<OrderResponse> getOrdersByEmployeeId(String employeeId);
    List<OrderResponse> getOrdersByTerritoryId(String territoryId);
    List<OrderResponse> getOrdersByStatus(String status);
    OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request);
    OrderResponse updateOrder(String id, OrderRequest request);
    void deleteOrder(String id);
    List<OrderResponse> searchOrders(OrderSearchRequest request);
    OrderSummaryResponse getOrderSummary();
}
