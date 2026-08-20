package com.bnagritech.tradesphere.order.Service.Impl;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.employee.repository.EmployeeRepository;
import com.bnagritech.tradesphere.order.Service.OrderService;
import com.bnagritech.tradesphere.order.dto.*;
import com.bnagritech.tradesphere.order.model.Order;
import com.bnagritech.tradesphere.order.model.OrderItems;
import com.bnagritech.tradesphere.order.model.OrderStatusHistory;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.product.model.Product;
import com.bnagritech.tradesphere.product.repository.ProductRepository;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PromoterRepository promoterRepository;
    private final EmployeeRepository employeeRepository;
    private final OutletRepository outletRepository;
    private final TerritoryRepository territoryRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        outletRepository.findByOutletId(request.getOutletId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Outlet not found: " + request.getOutletId()));
        promoterRepository.findByPromoterId(request.getPromoterId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Promoter not found: " + request.getPromoterId()));
        employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() ->
                        new RuntimeException("Employee not found: " + request.getEmployeeId()));
        if (!territoryRepository.existsTerritoryByTerritoryId(request.getTerritoryId())) {

            throw new ResourceNotFoundException("Territory not found: " + request.getTerritoryId());
        }
        Set<String> productIds = new HashSet<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (!productIds.add(itemRequest.getProductId())) {
                throw new RuntimeException(
                        "Duplicate product in order: " + itemRequest.getProductId());
            }
        }
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setOutletId(request.getOutletId());
        order.setPromoterId(request.getPromoterId());
        order.setEmployeeId(request.getEmployeeId());
        order.setTerritoryId(request.getTerritoryId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PLACED);
        order.setItems(new ArrayList<>());
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalSchemeDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository
                    .findProductByProductId(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found: " + itemRequest.getProductId()));
            OrderItems orderItem = createOrderItem(product, itemRequest.getQuantity());
            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalAmount());
            if (orderItem.getDiscount() != null) {
                totalDiscount = totalDiscount.add(orderItem.getDiscount());
            }
            if (orderItem.getTaxAmount() != null) {
                totalTax = totalTax.add(orderItem.getTaxAmount());
            }
        }
        order.setSubtotal(subtotal);
        order.setTotalDiscount(totalDiscount);
        order.setTotalSchemeDiscount(totalSchemeDiscount);
        order.setTotalTax(totalTax);
        order.setGrandTotal(subtotal.add(totalTax));
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(null);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPendingAmount(order.getGrandTotal());
        order.setPaymentReference(null);
        order.setDeliveryStatus(null);
        order.setDeliveryAddress(null);
        order.setDispatchDate(null);
        order.setDeliveryDate(null);
        order.setRemarks(request.getRemarks());
        order.setCreatedBy(request.getEmployeeId());
        order.setUpdatedBy(request.getEmployeeId());
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.PLACED);
        history.setUpdatedBy(request.getEmployeeId());
        history.setRemarks("Order created");
        history.setUpdatedAt(now);
        order.setStatusHistory(new ArrayList<>(List.of(history)));
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    public String generateOrderNumber() {
        return "ORD" +
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private OrderItems createOrderItem(Product product, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }
        BigDecimal mrp = parseAmount(product.getMrp(), "MRP", product.getProductId());
        BigDecimal sellingPrice = parseAmount(product.getPtr(), "PTR", product.getProductId());
        BigDecimal itemTotal = sellingPrice.multiply(BigDecimal.valueOf(quantity));

        OrderItems item = new OrderItems();
        item.setSkuCode(product.getSkuCode());
        item.setProductName(product.getProductName());
        item.setQuantity(quantity);
        item.setMrp(mrp);
        item.setSellingPrice(sellingPrice);
        item.setDiscount(BigDecimal.ZERO);
        item.setTaxableAmount(itemTotal);
        item.setTaxAmount(BigDecimal.ZERO);
        item.setTotalAmount(itemTotal);
        item.setSchemeId(null);
        return item;
    }
    private BigDecimal parseAmount(String value, String fieldName, String productId) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(fieldName + " is not configured for product: " + productId);
        }
        try {
            return new BigDecimal(value.trim());
        }
        catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid " + fieldName + " for product: " + productId);
        }
    }
    @Override
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order Not found with :" +id));
        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return mapToResponse(order);
    }

    @Override
    public List<OrderSummaryResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> getOrdersByOutletId(String outletId) {
        List<Order> orders = orderRepository.findByOutletId(outletId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for OutletId: " + outletId);
        }
        return orders.stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> getOrdersByPromoterId(String promoterId) {
        List<Order> orders = orderRepository.findByPromoterId(promoterId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for promoter id: " + promoterId);
        }
        return orders.stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }
    @Override
    public List<OrderSummaryResponse> getOrdersByEmployeeId(String employeeId) {
        List<Order> orders = orderRepository.findByEmployeeId(employeeId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for employee id: " + employeeId);
        }
        return orders.stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> getOrdersByTerritoryId(String territoryId) {
        List<Order> orders = orderRepository.findByTerritoryId(territoryId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for territory id: " + territoryId);
        }
        return orders.stream()
                .map(this::mapToSummaryResponse)
                .toList();

    }
    @Override
    public OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(String orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));
        Order cancelledOrder = orderRepository.save(order);
        return mapToResponse(cancelledOrder);
    }

    @Override
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));
        orderRepository.delete(order);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::mapToItemResponse)
                        .collect(Collectors.toList());
        PaymentInfoResponse payment =
                PaymentInfoResponse.builder()
                        .paymentStatus(order.getPaymentStatus())
                        .paymentMode(order.getPaymentMethod())
                        .paidAmount(order.getPaidAmount())
                        .pendingAmount(order.getPendingAmount())
                        .transactionReference(order.getPaymentReference())
                        .build();

        DeliveryInfoResponse delivery =
                DeliveryInfoResponse.builder()
                        .deliveryStatus(order.getDeliveryStatus())
                        .deliveryAddress(order.getDeliveryAddress())
                        .dispatchDate(order.getDispatchDate())
                        .deliveryDate(order.getDeliveryDate())
                        .build();

        List<OrderStatusHistoryResponse> history =
                order.getStatusHistory() == null
                        ? new ArrayList<>()
                        : order.getStatusHistory()
                        .stream()
                        .map(this::mapToHistoryResponse)
                        .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .outletId(order.getOutletId())
                .promoterId(order.getPromoterId())
                .employeeId(order.getEmployeeId())
                .territoryId(order.getTerritoryId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .items(items)
                .subtotal(order.getSubtotal())
                .totalDiscount(order.getTotalDiscount())
                .totalSchemeDiscount(order.getTotalSchemeDiscount())
                .totalTax(order.getTotalTax())
                .grandTotal(order.getGrandTotal())
                .payment(payment)
                .delivery(delivery)
                .cancellationReason(order.getCancellationReason())
                .cancelledAt(order.getCancelledAt())
                .remarks(order.getRemarks())
                .statusHistory(history)
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    private OrderItemResponse mapToItemResponse(
            OrderItems item) {

        return OrderItemResponse.builder()
                .productName(item.getProductName())
                .skuCode(item.getSkuCode())
                .quantity(item.getQuantity())
                .mrp(item.getMrp())
                .sellingPrice(item.getSellingPrice())
                .discount(item.getDiscount())
                .taxAmount(item.getTaxAmount())
                .build();
    }

    private OrderStatusHistoryResponse mapToHistoryResponse(
            OrderStatusHistory history) {

        return OrderStatusHistoryResponse.builder()
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .updatedBy(history.getUpdatedBy())
                .remarks(history.getRemarks())
                .updatedAt(history.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse mapToSummaryResponse(
            Order order) {

        int totalItems = order.getItems() == null
                ? 0
                : order.getItems()
                .stream()
                .mapToInt(OrderItems::getQuantity)
                .sum();

        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .outletId(order.getOutletId())
                .promoterId(order.getPromoterId())
                .employeeId(order.getEmployeeId())
                .territoryId(order.getTerritoryId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .totalItems(totalItems)
                .grandTotal(order.getGrandTotal())
                .paymentStatus(order.getPaymentStatus() == null ? null : order.getPaymentStatus())
                .deliveryStatus(order.getDeliveryStatus() == null ? null : order.getDeliveryStatus())
                .build();
    }
}
