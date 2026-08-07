package com.bnagritech.tradesphere.order.service.impl;

import com.bnagritech.tradesphere.order.dto.CreateOrderRequest;
import com.bnagritech.tradesphere.order.dto.OrderResponse;
import com.bnagritech.tradesphere.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        return null;
    }

    @Override
    public List<OrderResponse> getOrdersByEmployeeId() {
        return List.of();
    }

    @Override
    public OrderResponse getOrderByRetailerName(String RetailerName) {
        return null;
    }

    @Override
    public OrderResponse getOrdersByRetailerId(String retailerId) {
        return null;
    }

    @Override
    public OrderResponse getOrderByPromoterId(String promoterId) {
        return null;
    }

    @Override
    public OrderResponse updateOrder(String OrderNumber, CreateOrderRequest request) {
        return null;
    }
}
