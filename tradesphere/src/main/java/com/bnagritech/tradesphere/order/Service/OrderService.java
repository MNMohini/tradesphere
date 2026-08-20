package com.bnagritech.tradesphere.order.Service;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.order.dto.*;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(String orderId);
    OrderResponse getOrderByOrderNumber(String orderNumber);
    List<OrderSummaryResponse> getAllOrders();
    List<OrderSummaryResponse> getOrdersByOutletId(String outletId);
    List<OrderSummaryResponse> getOrdersByPromoterId(String promoterId);
    List<OrderSummaryResponse> getOrdersByEmployeeId(String employeeId);
    List<OrderSummaryResponse> getOrdersByTerritoryId(String territoryId);
    OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request);
    OrderResponse cancelOrder(String orderId,CancelOrderRequest request);
    void deleteOrder(String orderId);
}
