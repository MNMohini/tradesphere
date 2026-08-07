package com.bnagritech.tradesphere.order.service;

import com.bnagritech.tradesphere.order.dto.CreateOrderRequest;
import com.bnagritech.tradesphere.order.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
    List<OrderResponse> getOrdersByEmployeeId();
    OrderResponse getOrderByRetailerName(String RetailerName);

    OrderResponse getOrdersByRetailerId(String retailerId);
    OrderResponse getOrderByPromoterId(String promoterId);
    OrderResponse updateOrder(String OrderNumber, CreateOrderRequest request);
}
