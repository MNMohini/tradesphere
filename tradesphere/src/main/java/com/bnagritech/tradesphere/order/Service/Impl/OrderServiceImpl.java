package com.bnagritech.tradesphere.order.Service.Impl;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import com.bnagritech.tradesphere.order.dto.CreateOrderRequest;
import com.bnagritech.tradesphere.order.dto.LocationDto;
import com.bnagritech.tradesphere.order.dto.OrderItemRequest;
import com.bnagritech.tradesphere.order.dto.OrderItemResponse;
import com.bnagritech.tradesphere.order.dto.OrderPreviewRequest;
import com.bnagritech.tradesphere.order.dto.OrderPreviewResponse;
import com.bnagritech.tradesphere.order.dto.OrderResponse;
import com.bnagritech.tradesphere.order.dto.OrderSearchRequest;
import com.bnagritech.tradesphere.order.dto.OrderSummaryResponse;
import com.bnagritech.tradesphere.order.dto.RecordPaymentRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrderResult;
import com.bnagritech.tradesphere.order.dto.SyncOrdersRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrdersResponse;
import com.bnagritech.tradesphere.order.dto.UpdatedDeliveryRequest;
import com.bnagritech.tradesphere.order.dto.UpdateOrderRequest;
import com.bnagritech.tradesphere.order.dto.UpdatedOrderStatusRequest;
import com.bnagritech.tradesphere.order.model.DeliveryInfo;
import com.bnagritech.tradesphere.order.model.Order;
import com.bnagritech.tradesphere.order.model.OrderItems;
import com.bnagritech.tradesphere.order.model.OrderStatusHistory;
import com.bnagritech.tradesphere.order.model.PaymentInfo;
import com.bnagritech.tradesphere.order.repository.OrderFilter;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.order.service.OrderService;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(10);
    private static final int DEFAULT_SEARCH_DAYS = 30;

    // Roles that may take orders. Promoters and supervisors do not.
    private static final Set<UserRole> ORDER_TAKER_ROLES =
            EnumSet.of(UserRole.SALES_REPRESENTATIVE, UserRole.SALES_OFFICER);

    // Roles that may see (and approve) every order.
    private static final Set<UserRole> SEE_ALL_ROLES =
            EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Allowed status changes. Anything not listed is rejected.
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.DRAFT, EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CANCELLED),
            OrderStatus.SUBMITTED, EnumSet.of(OrderStatus.APPROVED, OrderStatus.REJECTED, OrderStatus.CANCELLED),
            OrderStatus.APPROVED, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.REJECTED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class)
    );

    private final OrderRepository orderRepository;
    private final Validator validator;

    // =====================================================================
    // Create / preview / update
    // =====================================================================

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, User currentUser) {
        Order existing = orderRepository.findByClientOrderId(request.getClientOrderId()).orElse(null);
        return toResponse(createInternal(request, currentUser, existing).order());
    }

    @Override
    public OrderPreviewResponse previewOrder(OrderPreviewRequest request, User currentUser) {
        requireOrderTaker(currentUser);
        loadOutletName(request.getOutletId());   // fails if the outlet does not exist

        List<String> warnings = new ArrayList<>();
        PricedBasket basket = priceItems(request.getItems(), false, warnings);

        return OrderPreviewResponse.builder()
                .items(basket.items().stream().map(this::toItemResponse).toList())
                .subtotal(basket.subtotal())
                .totalDiscount(basket.totalDiscount())
                .totalSchemeDiscount(basket.totalSchemeDiscount())
                .totalTax(basket.totalTax())
                .grandTotal(basket.grandTotal())
                .warnings(warnings)
                .build();
    }

    @Override
    public OrderResponse updateOrder(String orderId, UpdateOrderRequest request, User currentUser) {
        Order order = orderRepository.findByIdAndCreatedBy(orderId, currentUser.getId())
                .orElseThrow(() -> notFound("Order not found"));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw badRequest("Only DRAFT orders can be edited. Current status: " + order.getStatus());
        }

        PricedBasket basket = priceItems(request.getItems(), true, null);
        applyBasket(order, basket);

        if (request.getRemarks() != null) {
            order.setRemarks(request.getRemarks());
        }

        DeliveryInfo delivery = order.getDelivery() != null ? order.getDelivery() : new DeliveryInfo();
        if (request.getDeliveryAddress() != null) {
            delivery.setDeliveryAddress(request.getDeliveryAddress());
        }
        if (request.getExpectedDeliveryDate() != null) {
            delivery.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        }
        order.setDelivery(delivery);

        // Keep the payment status consistent with the new total
        if (order.getPayment() != null && order.getPayment().getPaidAmount() != null) {
            BigDecimal paid = order.getPayment().getPaidAmount();
            if (paid.compareTo(basket.grandTotal()) > 0) {
                throw badRequest("Amount already paid is more than the new order total");
            }
            order.getPayment().setPaymentStatus(derivePaymentStatus(paid, basket.grandTotal()));
        }

        order.setUpdatedBy(currentUser.getId());
        return toResponse(orderRepository.save(order));
    }

    // =====================================================================
    // Status, payment, delivery
    // =====================================================================

    @Override
    public OrderResponse updateOrderStatus(String orderId, UpdatedOrderStatusRequest request, User currentUser) {
        Order order = loadVisibleOrder(orderId, currentUser);

        OrderStatus from = order.getStatus();
        OrderStatus to = request.getStatus();

        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw badRequest("Cannot change an order from " + from + " to " + to);
        }

        boolean creator = isCreator(order, currentUser);
        boolean approver = isApprover(order, currentUser);

        switch (to) {
            case SUBMITTED -> require(creator, "Only the person who created the order can submit it");
            case APPROVED, REJECTED, COMPLETED ->
                    require(approver, "Only a manager above the order creator can set status " + to);
            case CANCELLED -> require((creator && from != OrderStatus.APPROVED) || approver,
                    "You are not allowed to cancel this order");
            default -> throw badRequest("Status " + to + " cannot be set directly");
        }

        String reason = firstNonBlank(request.getCancellationReason(), request.getRemarks());
        if ((to == OrderStatus.CANCELLED || to == OrderStatus.REJECTED) && reason == null) {
            throw badRequest("A reason is required when an order is " + to);
        }

        Instant now = Instant.now();
        if (to == OrderStatus.CANCELLED) {
            order.setCancellationReason(reason);
            order.setCancelledAt(now);
        }

        order.setStatus(to);
        order.setUpdatedBy(currentUser.getId());
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .fromStatus(from)
                .toStatus(to)
                .updatedBy(currentUser.getId())
                .remarks(reason != null ? reason : request.getRemarks())
                .updatedAt(now)
                .build());

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse recordPayment(String orderId, RecordPaymentRequest request, User currentUser) {
        Order order = loadVisibleOrder(orderId, currentUser);
        require(isCreator(order, currentUser) || isApprover(order, currentUser),
                "You are not allowed to record a payment on this order");

        if (order.getStatus() == OrderStatus.DRAFT
                || order.getStatus() == OrderStatus.REJECTED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw badRequest("Payments cannot be recorded on an order with status " + order.getStatus());
        }

        BigDecimal amount = money(request.getAmount());
        if (amount.compareTo(order.getPendingAmount()) > 0) {
            throw badRequest("Payment " + amount + " is more than the pending amount "
                    + order.getPendingAmount());
        }

        PaymentInfo payment = order.getPayment() != null ? order.getPayment() : new PaymentInfo();
        BigDecimal alreadyPaid = payment.getPaidAmount() != null ? payment.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = money(alreadyPaid.add(amount));

        payment.setPaidAmount(newPaid);
        payment.setPaymentMode(request.getPaymentMode());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setPaymentStatus(derivePaymentStatus(newPaid, order.getGrandTotal()));
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            payment.setFinalPaymentDate(Instant.now());
        }

        order.setPayment(payment);
        order.setUpdatedBy(currentUser.getId());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateDelivery(String orderId, UpdatedDeliveryRequest request, User currentUser) {
        Order order = loadVisibleOrder(orderId, currentUser);
        require(isCreator(order, currentUser) || isApprover(order, currentUser),
                "You are not allowed to update delivery on this order");

        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.COMPLETED) {
            throw badRequest("Delivery can be updated only for APPROVED or COMPLETED orders");
        }

        DeliveryInfo delivery = order.getDelivery() != null ? order.getDelivery() : new DeliveryInfo();
        Instant now = Instant.now();

        delivery.setDeliveryStatus(request.getDeliveryStatus());
        if (request.getExpectedDeliveryDate() != null) {
            delivery.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        }
        if (request.getDeliveryRemark() != null) {
            delivery.setDeliveryRemark(request.getDeliveryRemark());
        }

        if (request.getDeliveryStatus() == DeliveryStatus.DISPATCHED && delivery.getDispatchedAt() == null) {
            delivery.setDispatchedAt(now);
        }
        if (request.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(now);
            delivery.setActualDeliveryDate(request.getActualDeliveryDate() != null
                    ? request.getActualDeliveryDate() : LocalDate.now(IST));
            delivery.setDeliveredBy(request.getDeliveredBy() != null
                    ? request.getDeliveredBy() : currentUser.getId());
        }

        order.setDelivery(delivery);
        order.setUpdatedBy(currentUser.getId());
        return toResponse(orderRepository.save(order));
    }

    // =====================================================================
    // Read
    // =====================================================================

    @Override
    public OrderResponse getOrderById(String orderId, User currentUser) {
        return toResponse(loadVisibleOrder(orderId, currentUser));
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber, User currentUser) {
        return orderRepository.findByOrderNumber(orderNumber)
                .filter(order -> canView(order, currentUser))
                .map(this::toResponse)
                .orElseThrow(() -> notFound("Order not found"));
    }

    @Override
    public List<OrderSummaryResponse> getOrdersByVisit(String visitId, User currentUser) {
        // The repository query returns every order of the visit; keep only those the caller may see
        return orderRepository.findByVisitIdOrderByOrderDateDesc(visitId).stream()
                .filter(order -> canView(order, currentUser))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> getMyDrafts(User currentUser) {
        return orderRepository
                .findByCreatedByAndStatusOrderByOrderDateDesc(currentUser.getId(), OrderStatus.DRAFT).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> searchOrders(OrderSearchRequest request, User currentUser) {
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();

        // No dates given: default to the last 30 days so one call can never return everything
        if (from == null && to == null) {
            to = LocalDate.now(IST);
            from = to.minusDays(DEFAULT_SEARCH_DAYS);
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw badRequest("toDate cannot be before fromDate");
        }

        Instant fromInclusive = from != null ? from.atStartOfDay(IST).toInstant() : null;
        Instant toExclusive = to != null ? to.plusDays(1).atStartOfDay(IST).toInstant() : null;

        String viewer = SEE_ALL_ROLES.contains(currentUser.getRole()) ? null : currentUser.getId();

        OrderFilter filter = new OrderFilter(
                viewer,
                request.getStatus(),
                request.getPaymentStatus(),
                request.getOutletId(),
                request.getCreatedBy(),
                fromInclusive,
                toExclusive);

        return orderRepository.search(filter).stream().map(this::toSummary).toList();
    }

    // =====================================================================
    // Offline sync
    // =====================================================================

    @Override
    public SyncOrdersResponse syncOrders(SyncOrdersRequest request, User currentUser) {
        List<SyncOrderResult> results = new ArrayList<>();
        int created = 0;
        int duplicates = 0;
        int rejected = 0;

        // One query for the whole batch instead of one query per order
        List<String> clientOrderIds = request.getOrders().stream()
                .filter(Objects::nonNull)
                .map(CreateOrderRequest::getClientOrderId)
                .filter(Objects::nonNull)
                .toList();
        Map<String, Order> alreadySaved = orderRepository.findByClientOrderIdIn(clientOrderIds).stream()
                .collect(Collectors.toMap(Order::getClientOrderId, order -> order, (a, b) -> a));

        for (CreateOrderRequest item : request.getOrders()) {
            String clientOrderId = item != null ? item.getClientOrderId() : null;
            try {
                if (item == null) {
                    throw badRequest("Empty order in batch");
                }
                // Validate each order on its own so one bad order does not block the rest
                Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(item);
                if (!violations.isEmpty()) {
                    String message = violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .sorted()
                            .collect(Collectors.joining("; "));
                    throw badRequest(message);
                }

                CreateOutcome outcome = createInternal(item, currentUser, alreadySaved.get(item.getClientOrderId()));
                Order order = outcome.order();

                boolean priceChanged = item.getExpectedGrandTotal() != null
                        && order.getGrandTotal() != null
                        && item.getExpectedGrandTotal().compareTo(order.getGrandTotal()) != 0;

                results.add(SyncOrderResult.builder()
                        .clientOrderId(clientOrderId)
                        .status(outcome.duplicate() ? SyncOrderResult.Status.DUPLICATE
                                : SyncOrderResult.Status.CREATED)
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .grandTotal(order.getGrandTotal())
                        .priceChanged(priceChanged)
                        .build());

                if (outcome.duplicate()) {
                    duplicates++;
                } else {
                    created++;
                }
            } catch (ResponseStatusException e) {
                rejected++;
                results.add(rejectedResult(clientOrderId, e.getReason()));
            } catch (Exception e) {
                log.error("Unexpected error while syncing order {}", clientOrderId, e);
                rejected++;
                results.add(rejectedResult(clientOrderId, "Unexpected error. Please retry."));
            }
        }

        return SyncOrdersResponse.builder()
                .total(request.getOrders().size())
                .created(created)
                .duplicates(duplicates)
                .rejected(rejected)
                .results(results)
                .serverTime(Instant.now())
                .build();
    }

    private SyncOrderResult rejectedResult(String clientOrderId, String message) {
        return SyncOrderResult.builder()
                .clientOrderId(clientOrderId)
                .status(SyncOrderResult.Status.REJECTED)
                .errorMessage(message)
                .build();
    }

    // =====================================================================
    // Order creation (shared by createOrder and syncOrders)
    // =====================================================================

    private record CreateOutcome(Order order, boolean duplicate) {
    }

    /** existing = the order already saved with this clientOrderId, or null if there is none. */
    private CreateOutcome createInternal(CreateOrderRequest request, User user, Order existing) {
        requireOrderTaker(user);

        // Idempotency: the app may retry the same order many times
        if (existing != null) {
            return duplicateOutcome(existing, user);
        }

        Instant now = Instant.now();
        Instant orderDate = request.getOrderDate() != null ? request.getOrderDate() : now;
        if (orderDate.isAfter(now.plus(MAX_CLOCK_SKEW))) {
            throw badRequest("orderDate is in the future. Check the device clock.");
        }

        String outletName = loadOutletName(request.getOutletId());
        VisitInfo visit = resolveVisit(request, user);
        String beatId = (visit != null && visit.beatId() != null) ? visit.beatId() : request.getBeatId();
        PricedBasket basket = priceItems(request.getItems(), true, null);
        PaymentInfo payment = buildInitialPayment(request, basket.grandTotal());

        DeliveryInfo delivery = DeliveryInfo.builder()
                .deliveryAddress(request.getDeliveryAddress())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();

        OrderStatus initialStatus = request.isSaveAsDraft() ? OrderStatus.DRAFT : OrderStatus.SUBMITTED;

        // Copy of the creator's ancestors; managers' order lists and approvals depend on it
        List<String> userPath = new ArrayList<>();
        if (user.getPath() != null) {
            userPath.addAll(user.getPath());
        }

        Order order = Order.builder()
                .orderNumber(nextOrderNumber(now))
                .clientOrderId(request.getClientOrderId())
                .outletId(request.getOutletId())
                .outletName(outletName)
                .visitId(request.getVisitId())
                .beatId(beatId)
                .createdBy(user.getId())
                .createdByName(displayName(user))
                .userPath(userPath)
                .orderDate(orderDate)
                .location(toPoint(request.getLocation()))
                .status(initialStatus)
                .remarks(request.getRemarks())
                .payment(payment)
                .delivery(delivery)
                .updatedBy(user.getId())
                .build();
        applyBasket(order, basket);

        order.setStatusHistory(new ArrayList<>(List.of(OrderStatusHistory.builder()
                .fromStatus(null)
                .toStatus(initialStatus)
                .updatedBy(user.getId())
                .remarks("Order created")
                .updatedAt(now)
                .build())));

        try {
            return new CreateOutcome(orderRepository.save(order), false);
        } catch (DuplicateKeyException e) {
            // Two requests with the same clientOrderId arrived at the same moment
            Order winner = orderRepository.findByClientOrderId(request.getClientOrderId()).orElseThrow(() -> e);
            return duplicateOutcome(winner, user);
        }
    }

    private CreateOutcome duplicateOutcome(Order existing, User user) {
        if (!user.getId().equals(existing.getCreatedBy())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "clientOrderId is already used by another user");
        }
        return new CreateOutcome(existing, true);
    }

    private PaymentInfo buildInitialPayment(CreateOrderRequest request, BigDecimal grandTotal) {
        BigDecimal paid = request.getPaidAmount() != null ? money(request.getPaidAmount()) : BigDecimal.ZERO;
        if (paid.compareTo(grandTotal) > 0) {
            throw badRequest("paidAmount is more than the order total " + grandTotal);
        }
        if (paid.signum() > 0 && request.getPaymentMode() == null) {
            throw badRequest("paymentMode is required when paidAmount is given");
        }
        return PaymentInfo.builder()
                .paymentMode(request.getPaymentMode())
                .paidAmount(paid)
                .transactionReference(request.getPaymentReference())
                .paymentStatus(derivePaymentStatus(paid, grandTotal))
                .finalPaymentDate(paid.compareTo(grandTotal) == 0 && paid.signum() > 0 ? Instant.now() : null)
                .build();
    }

    // Indian financial year (April to March): ORD-2627-000123
    private String nextOrderNumber(Instant when) {
        LocalDate date = when.atZone(IST).toLocalDate();
        int startYear = date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
        String fy = String.format("%02d%02d", startYear % 100, (startYear + 1) % 100);
        long sequence = orderRepository.nextSequence("ORDER-" + fy);
        return String.format("ORD-%s-%06d", fy, sequence);
    }

    // =====================================================================
    // Pricing (server side; the app's amounts are never trusted)
    // =====================================================================

    private record ProductPricing(
            String productId,
            String skuCode,
            String productName,
            String unit,
            BigDecimal mrp,
            BigDecimal sellingPrice,
            BigDecimal taxPercent,
            boolean active
    ) {
    }

    private record PricedBasket(
            List<OrderItems> items,
            BigDecimal subtotal,
            BigDecimal totalDiscount,
            BigDecimal totalSchemeDiscount,
            BigDecimal totalTax,
            BigDecimal grandTotal
    ) {
    }

    /**
     * strict = true  (create/update): an unknown or inactive product rejects the request.
     * strict = false (preview): such items are skipped and a warning is added.
     */
    private PricedBasket priceItems(List<OrderItemRequest> requested, boolean strict, List<String> warnings) {
        List<OrderItems> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalScheme = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItemRequest req : requested) {
            ProductPricing product = loadProductPricing(req.getProductId());

            if (product == null || !product.active()) {
                String message = (product == null ? "Product not found: " : "Product is inactive: ")
                        + req.getProductId();
                if (strict) {
                    throw badRequest(message);
                }
                warnings.add(message);
                continue;
            }

            BigDecimal quantity = BigDecimal.valueOf(req.getQuantity());
            BigDecimal gross = money(product.sellingPrice().multiply(quantity));
            BigDecimal discount = BigDecimal.ZERO;     // manual line discounts are not supported yet
            BigDecimal schemeDiscount = money(lookupSchemeDiscount(req.getSchemeId(), product, req.getQuantity()));

            BigDecimal taxable = money(gross.subtract(discount).subtract(schemeDiscount));
            BigDecimal tax = taxable.multiply(product.taxPercent()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = money(taxable.add(tax));

            items.add(OrderItems.builder()
                    .productId(product.productId())
                    .skuCode(product.skuCode())
                    .productName(product.productName())
                    .unit(product.unit())
                    .quantity(req.getQuantity())
                    .mrp(product.mrp())
                    .sellingPrice(product.sellingPrice())
                    .discount(discount)
                    .taxableAmount(taxable)
                    .taxAmount(tax)
                    .totalAmount(lineTotal)
                    .schemeId(req.getSchemeId())
                    .build());

            subtotal = subtotal.add(gross);
            totalDiscount = totalDiscount.add(discount);
            totalScheme = totalScheme.add(schemeDiscount);
            totalTax = totalTax.add(tax);
            grandTotal = grandTotal.add(lineTotal);
        }

        if (items.isEmpty()) {
            throw badRequest("The order has no valid items");
        }

        return new PricedBasket(items, money(subtotal), money(totalDiscount), money(totalScheme),
                money(totalTax), money(grandTotal));
    }

    private void applyBasket(Order order, PricedBasket basket) {
        order.setItems(new ArrayList<>(basket.items()));
        order.setSubtotal(basket.subtotal());
        order.setTotalDiscount(basket.totalDiscount());
        order.setTotalSchemeDiscount(basket.totalSchemeDiscount());
        order.setTotalTax(basket.totalTax());
        order.setGrandTotal(basket.grandTotal());
    }

    // ---------------------------------------------------------------------
    // TODO: connect to the Product, Scheme, Outlet and Visit modules once they exist.
    // Only these four methods need to change. Nothing else here depends on them.
    // ---------------------------------------------------------------------

    /** Return the current price and tax of the product, or null if it does not exist. */
    private ProductPricing loadProductPricing(String productId) {
        throw new UnsupportedOperationException("Product module is not connected yet");
    }

    /** Return the total scheme discount for this line (zero if no scheme applies). */
    private BigDecimal lookupSchemeDiscount(String schemeId, ProductPricing product, int quantity) {
        return BigDecimal.ZERO;   // schemes are not implemented yet
    }

    /** Return the outlet's name, or throw 404 if the outlet does not exist. */
    private String loadOutletName(String outletId) {
        throw new UnsupportedOperationException("Outlet module is not connected yet");
    }

    /** What the order module needs to know about a visit. */
    private record VisitInfo(String visitId, String outletId, String userId, String beatId) {
    }

    /** Return the visit, or null if it does not exist. */
    private VisitInfo loadVisit(String visitId) {
        throw new UnsupportedOperationException("Visit module is not connected yet");
    }

    /**
     * visitId is optional. When it is given, the visit must exist, belong to the
     * caller and be for the same outlet. The visit's beat then replaces any beatId
     * sent by the app. The visit is NOT required to be open: an offline order may
     * be uploaded after the rep has already checked out.
     */
    private VisitInfo resolveVisit(CreateOrderRequest request, User user) {
        String visitId = request.getVisitId();
        if (visitId == null || visitId.isBlank()) {
            return null;
        }
        VisitInfo visit = loadVisit(visitId);
        if (visit == null) {
            throw badRequest("Visit not found: " + visitId);
        }
        if (!user.getId().equals(visit.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This visit belongs to another user");
        }
        if (!request.getOutletId().equals(visit.outletId())) {
            throw badRequest("The visit is for a different outlet");
        }
        return visit;
    }

    // =====================================================================
    // Permissions
    // =====================================================================

    private void requireOrderTaker(User user) {
        if (user == null || !ORDER_TAKER_ROLES.contains(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot create orders");
        }
    }

    private boolean isCreator(Order order, User user) {
        return user.getId().equals(order.getCreatedBy());
    }

    /** A manager above the creator: someone in the creator's hierarchy path, or admin / national head. */
    private boolean isApprover(Order order, User user) {
        if (isCreator(order, user)) {
            return false;
        }
        if (SEE_ALL_ROLES.contains(user.getRole())) {
            return true;
        }
        return order.getUserPath() != null && order.getUserPath().contains(user.getId());
    }

    private boolean canView(Order order, User user) {
        return isCreator(order, user) || isApprover(order, user);
    }

    /** Hidden orders look like missing orders (404), so their existence is not revealed. */
    private Order loadVisibleOrder(String orderId, User user) {
        return orderRepository.findById(orderId)
                .filter(order -> canView(order, user))
                .orElseThrow(() -> notFound("Order not found"));
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    // =====================================================================
    // Mapping (entity to DTO)
    // =====================================================================

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientOrderId(order.getClientOrderId())
                .outletId(order.getOutletId())
                .outletName(order.getOutletName())
                .visitId(order.getVisitId())
                .beatId(order.getBeatId())
                .territoryId(order.getTerritoryId())
                .createdBy(order.getCreatedBy())
                .createdByName(order.getCreatedByName())
                .orderDate(order.getOrderDate())
                .location(toLocationDto(order.getLocation()))
                .items(order.getItems() == null ? List.of()
                        : order.getItems().stream().map(this::toItemResponse).toList())
                .subtotal(order.getSubtotal())
                .totalDiscount(order.getTotalDiscount())
                .totalSchemeDiscount(order.getTotalSchemeDiscount())
                .totalTax(order.getTotalTax())
                .grandTotal(order.getGrandTotal())
                .pendingAmount(order.getPendingAmount())
                .status(order.getStatus())
                .cancellationReason(order.getCancellationReason())
                .cancelledAt(order.getCancelledAt())
                .remarks(order.getRemarks())
                .payment(order.getPayment())
                .delivery(order.getDelivery())
                .statusHistory(order.getStatusHistory())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .outletId(order.getOutletId())
                .outletName(order.getOutletName())
                .createdBy(order.getCreatedBy())
                .createdByName(order.getCreatedByName())
                .orderDate(order.getOrderDate())
                .itemCount(order.getItems() == null ? 0 : order.getItems().size())
                .grandTotal(order.getGrandTotal())
                .pendingAmount(order.getPendingAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPayment() != null ? order.getPayment().getPaymentStatus() : null)
                .deliveryStatus(order.getDelivery() != null ? order.getDelivery().getDeliveryStatus() : null)
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItems item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .skuCode(item.getSkuCode())
                .productName(item.getProductName())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .mrp(item.getMrp())
                .sellingPrice(item.getSellingPrice())
                .discount(item.getDiscount())
                .taxableAmount(item.getTaxableAmount())
                .taxAmount(item.getTaxAmount())
                .totalAmount(item.getTotalAmount())
                .schemeId(item.getSchemeId())
                .build();
    }

    // GeoJSON stores longitude first, then latitude
    private GeoJsonPoint toPoint(LocationDto location) {
        return location == null ? null : new GeoJsonPoint(location.getLongitude(), location.getLatitude());
    }

    private LocationDto toLocationDto(GeoJsonPoint point) {
        return point == null ? null
                : LocationDto.builder().latitude(point.getY()).longitude(point.getX()).build();
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    private PaymentStatus derivePaymentStatus(BigDecimal paid, BigDecimal total) {
        if (paid == null || paid.signum() <= 0) {
            return PaymentStatus.PENDING;
        }
        return paid.compareTo(total) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String displayName(User user) {
        return user.getUsername();
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return (b != null && !b.isBlank()) ? b : null;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}