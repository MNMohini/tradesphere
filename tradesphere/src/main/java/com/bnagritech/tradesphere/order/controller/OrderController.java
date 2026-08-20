package com.bnagritech.tradesphere.order.controller;

import com.bnagritech.tradesphere.order.Service.OrderService;
import com.bnagritech.tradesphere.order.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse>createOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse>getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse>getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>>getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/outlet/{outletId}")
    public ResponseEntity<List<OrderSummaryResponse>>getOrdersByOutletId(@PathVariable String outletId) {
        return ResponseEntity.ok(orderService.getOrdersByOutletId(outletId));
    }

    @GetMapping("/promoter/{promoterId}")
    public ResponseEntity<List<OrderSummaryResponse>>getOrdersByPromoterId(@PathVariable String promoterId) {
        return ResponseEntity.ok(orderService.getOrdersByPromoterId(promoterId));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OrderSummaryResponse>>getOrdersByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(orderService.getOrdersByEmployeeId(employeeId));
    }

    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<OrderSummaryResponse>>getOrdersByTerritoryId(@PathVariable String territoryId) {
        return ResponseEntity.ok(orderService.getOrdersByTerritoryId(territoryId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String orderId, @RequestBody UpdatedOrderStatusRequest request) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId, @RequestBody CancelOrderRequest request) {

        return ResponseEntity.ok(orderService.cancelOrder(orderId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
