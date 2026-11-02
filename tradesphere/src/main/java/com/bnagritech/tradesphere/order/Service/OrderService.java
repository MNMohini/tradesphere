package com.bnagritech.tradesphere.order.service;

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
import com.bnagritech.tradesphere.auth.model.User;

import java.util.List;

/**
 * Business operations of the order module.
 * currentUser is the logged-in user; it decides who may do what and
 * which orders are visible (hierarchy rule).
 */
public interface OrderService {

    /** Creates an order. Duplicate clientOrderId returns the existing order. */
    OrderResponse createOrder(CreateOrderRequest request, User currentUser);

    /** Calculates prices, schemes, tax and totals for a basket. Saves nothing. */
    OrderPreviewResponse previewOrder(OrderPreviewRequest request, User currentUser);

    /** Edits a DRAFT order owned by the caller. */
    OrderResponse updateOrder(String orderId, UpdateOrderRequest request, User currentUser);

    /** Submit, approve, reject, cancel or complete, following the allowed transitions. */
    OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request, User currentUser);

    /** Adds a payment; rejects amounts above the pending amount. */
    OrderResponse recordPayment(String orderId, RecordPaymentRequest request, User currentUser);

    /** Updates delivery status and dates. */
    OrderResponse updateDelivery(String orderId, UpdatedDeliveryRequest request, User currentUser);

    /** One order, only if the caller is allowed to see it. */
    OrderResponse getOrderById(String orderId, User currentUser);

    /** One order found by its number (for example ORD-2627-000123), if the caller may see it. */
    OrderResponse getOrderByOrderNumber(String orderNumber, User currentUser);

    /** Orders taken during one visit that the caller is allowed to see, newest first. */
    List<OrderSummaryResponse> getOrdersByVisit(String visitId, User currentUser);

    /** The caller's own DRAFT orders, newest first. */
    List<OrderSummaryResponse> getMyDrafts(User currentUser);

    /** Orders visible to the caller, narrowed by the filters, newest first. */
    List<OrderSummaryResponse> searchOrders(OrderSearchRequest request, User currentUser);

    /** Uploads orders taken offline; each order succeeds or fails on its own. */
    SyncOrdersResponse syncOrders(SyncOrdersRequest request, User currentUser);
}