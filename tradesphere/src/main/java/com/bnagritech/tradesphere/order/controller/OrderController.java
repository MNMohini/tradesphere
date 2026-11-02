package com.bnagritech.tradesphere.order.controller;

import com.bnagritech.tradesphere.order.dto.CreateOrderRequest;
import com.bnagritech.tradesphere.order.dto.OrderPreviewRequest;
import com.bnagritech.tradesphere.order.dto.OrderPreviewResponse;
import com.bnagritech.tradesphere.order.dto.OrderResponse;
import com.bnagritech.tradesphere.order.dto.OrderSearchRequest;
import com.bnagritech.tradesphere.order.dto.OrderSummaryResponse;
import com.bnagritech.tradesphere.order.dto.RecordPaymentRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrdersRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrdersResponse;
import com.bnagritech.tradesphere.order.dto.UpdatedDeliveryRequest;
import com.bnagritech.tradesphere.order.dto.UpdateOrderRequest;
import com.bnagritech.tradesphere.order.dto.UpdatedOrderStatusRequest;
import com.bnagritech.tradesphere.order.service.OrderService;
import com.bnagritech.tradesphere.auth.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders/create
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        OrderResponse response = orderService.createOrder(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/orders/preview
    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> previewOrder(
            @Valid @RequestBody OrderPreviewRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.previewOrder(request, currentUser));
    }

    // POST /api/orders/sync
    @PostMapping("/sync")
    public ResponseEntity<SyncOrdersResponse> syncOrders(
            @Valid @RequestBody SyncOrdersRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.syncOrders(request, currentUser));
    }

    // GET /api/orders?status=SUBMITTED&fromDate=2026-11-01&toDate=2026-11-30
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> searchOrders(
            @Valid @ModelAttribute OrderSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.searchOrders(request, currentUser));
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, currentUser));
    }

    // GET /api/orders/number/{orderNumber}
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber, currentUser));
    }

    // GET /api/orders/visit/{visitId}
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<OrderSummaryResponse>> getOrdersByVisit(
            @PathVariable String visitId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getOrdersByVisit(visitId, currentUser));
    }

    // GET /api/orders/drafts
    @GetMapping("/drafts")
    public ResponseEntity<List<OrderSummaryResponse>> getMyDrafts(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getMyDrafts(currentUser));
    }

    // PUT /api/orders/{orderId}   (edit a DRAFT order)
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, request, currentUser));
    }

    // PATCH /api/orders/{orderId}/status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdatedOrderStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request, currentUser));
    }

    // POST /api/orders/{orderId}/payments
    @PostMapping("/{orderId}/payments")
    public ResponseEntity<OrderResponse> recordPayment(
            @PathVariable String orderId,
            @Valid @RequestBody RecordPaymentRequest request,
            @AuthenticationPrincipal User currentUser) {
        OrderResponse response = orderService.recordPayment(orderId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PATCH /api/orders/{orderId}/delivery
    @PatchMapping("/{orderId}/delivery")
    public ResponseEntity<OrderResponse> updateDelivery(
            @PathVariable String orderId,
            @Valid @RequestBody UpdatedDeliveryRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.updateDelivery(orderId, request, currentUser));
    }
}