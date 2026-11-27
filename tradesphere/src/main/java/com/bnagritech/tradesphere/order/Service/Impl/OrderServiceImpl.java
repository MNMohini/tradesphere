package com.bnagritech.tradesphere.order.Service.Impl;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.order.dto.CreateOrderRequest;
import com.bnagritech.tradesphere.order.dto.LocationDto;
import com.bnagritech.tradesphere.order.dto.OrderItemRequest;
import com.bnagritech.tradesphere.order.dto.OrderItemResponse;
import com.bnagritech.tradesphere.order.dto.OrderPreviewRequest;
import com.bnagritech.tradesphere.order.dto.OrderPreviewResponse;
import com.bnagritech.tradesphere.order.dto.OrderResponse;
import com.bnagritech.tradesphere.order.dto.OrderSearchRequest;
import com.bnagritech.tradesphere.order.dto.OrderSummaryResponse;
import com.bnagritech.tradesphere.order.dto.OrderTeamSearchRequest;
import com.bnagritech.tradesphere.order.dto.OrderTotalsResponse;
import com.bnagritech.tradesphere.order.dto.RecordPaymentRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrderResult;
import com.bnagritech.tradesphere.order.dto.SyncOrdersRequest;
import com.bnagritech.tradesphere.order.dto.SyncOrdersResponse;
import com.bnagritech.tradesphere.order.dto.TeamMemberOrderSummaryResponse;
import com.bnagritech.tradesphere.order.dto.TeamMembersPageResponse;
import com.bnagritech.tradesphere.order.dto.TeamOrdersPageResponse;
import com.bnagritech.tradesphere.order.dto.UpdatedDeliveryRequest;
import com.bnagritech.tradesphere.order.dto.UpdateOrderRequest;
import com.bnagritech.tradesphere.order.dto.UpdatedOrderStatusRequest;
import com.bnagritech.tradesphere.order.model.DeliveryInfo;
import com.bnagritech.tradesphere.order.model.Order;
import com.bnagritech.tradesphere.order.model.OrderItems;
import com.bnagritech.tradesphere.order.model.OrderStatusHistory;
import com.bnagritech.tradesphere.order.model.PaymentInfo;
import com.bnagritech.tradesphere.order.repository.OrderFilter;
import com.bnagritech.tradesphere.order.repository.OrderMetrics;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.order.service.OrderService;
import com.bnagritech.tradesphere.order.visibility.OrderTeamResolver;
import com.bnagritech.tradesphere.order.visibility.OrderTeamResolver.TeamTree;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.product.repository.ProductRepository;
import com.bnagritech.tradesphere.visit.repository.VisitRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
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
    private static final int EXPORT_PAGE_SIZE = 500;

    // Roles allowed to create orders.
    private static final Set<UserRole> ORDER_TAKER_ROLES =
            EnumSet.of(UserRole.SALES_REPRESENTATIVE, UserRole.SALES_OFFICER, UserRole.PROMOTER, UserRole.SUPERVISOR);

    // Roles that retain their existing approve/action permission.
    private static final Set<UserRole> SEE_ALL_ROLES =
            EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Allowed status changes. Anything not listed is rejected.
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.DRAFT, EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CANCELLED),
            OrderStatus.SUBMITTED, EnumSet.of(OrderStatus.APPROVED, OrderStatus.CANCELLED),
            OrderStatus.PENDING_APPROVAL, EnumSet.of(OrderStatus.APPROVED, OrderStatus.CANCELLED),
            OrderStatus.APPROVED, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class)
    );

    private final OrderRepository orderRepository;
    private final Validator validator;
    private final OutletRepository outletRepository;
    private final ProductRepository productRepository;
    private final VisitRepository visitRepository;
    private final OrderTeamResolver orderTeamResolver;
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
        loadOutletName(request.getOutletCode());   // fails if the outlet does not exist

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
        Order order = orderRepository.findByIdAndCreatedByIn(orderId, userIdentityValues(currentUser))
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
            case APPROVED,DELIVERED ->
                    require(approver, "Only a manager above the order creator can set status " + to);
            case CANCELLED -> require((creator && from != OrderStatus.APPROVED) || approver,
                    "You are not allowed to cancel this order");
            default -> throw badRequest("Status " + to + " cannot be set directly");
        }

        String reason = firstNonBlank(request.getCancellationReason(), request.getRemarks());
        if (to == OrderStatus.CANCELLED && reason == null) {
            throw badRequest("A reason is required when an order is " + to);
        }

        Instant now = Instant.now();
        if (to == OrderStatus.CANCELLED) {
            order.setCancellationReason(reason);
            order.setCancelledAt(now);
        }

        order.setStatus(to);
        order.setUpdatedBy(currentUser.getUsername());
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .fromStatus(from)
                .toStatus(to)
                .updatedBy(currentUser.getUsername())
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
                || order.getStatus() == OrderStatus.RETURNED
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
        order.setUpdatedBy(currentUser.getUsername());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateDelivery(String orderId, UpdatedDeliveryRequest request, User currentUser) {
        Order order = loadVisibleOrder(orderId, currentUser);
        require(isCreator(order, currentUser) || isApprover(order, currentUser),
                "You are not allowed to update delivery on this order");

        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.DELIVERED) {
            throw badRequest("Delivery can be updated only for APPROVED or DELIVERED orders");
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
        order.setUpdatedBy(currentUser.getUsername());
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
        TeamTree tree = orderTeamResolver.resolve(currentUser);
        Collection<String> creatorReferences = visibleCreatorReferences(currentUser, tree, true);
        OrderFilter filter = new OrderFilter(
                creatorReferences, null, null, null, null, null, visitId, null, null);
        return toSummaries(orderRepository.search(filter), usersIn(tree));
    }

    @Override
    public List<OrderSummaryResponse> getMyDrafts(User currentUser) {
        return orderRepository
                .findByCreatedByInAndStatusOrderByOrderDateDesc(
                        userIdentityValues(currentUser), OrderStatus.DRAFT).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<OrderSummaryResponse> searchOrders(OrderSearchRequest request, User currentUser) {
        DateBounds bounds = dateBounds(request.getFromDate(), request.getToDate());
        TeamTree tree = orderTeamResolver.resolve(currentUser);
        OrderFilter filter = new OrderFilter(
                visibleCreatorReferences(currentUser, tree, true),
                request.getStatus(),
                request.getPaymentStatus(),
                request.getOutletCode(),
                request.getCreatedBy(),
                null,
                null,
                bounds.fromInclusive(),
                bounds.toExclusive());

        return toSummaries(orderRepository.search(filter), usersIn(tree));
    }

    @Override
    public TeamMembersPageResponse getTeamMembers(OrderTeamSearchRequest request, User currentUser) {
        TeamTree tree = orderTeamResolver.resolve(currentUser);
        User parent = resolveTeamMember(tree, request.getMemberId(), true);
        List<User> directReports = tree.directReports(parent).stream()
                .sorted(Comparator.comparing(this::userDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int fromIndex = (int) Math.min((long) request.getPage() * request.getSize(), directReports.size());
        int toIndex = Math.min(fromIndex + request.getSize(), directReports.size());
        List<User> pageMembers = directReports.subList(fromIndex, toIndex);
        List<String> creatorReferences = pageMembers.stream()
                .flatMap(user -> orderTeamResolver.identityValues(user).stream())
                .distinct()
                .toList();
        DateBounds bounds = dateBounds(request.getFromDate(), request.getToDate());
        OrderFilter filter = new OrderFilter(
                creatorReferences, request.getStatus(), null, null, null, request.getOrderNumber(),
                null, bounds.fromInclusive(), bounds.toExclusive());

        Map<String, OrderMetrics> metricsByCreator = orderRepository.aggregate(filter, true).stream()
                .filter(metric -> metric.creatorReference() != null)
                .collect(Collectors.toMap(OrderMetrics::creatorReference, metric -> metric));

        List<TeamMemberOrderSummaryResponse> items = pageMembers.stream()
                .map(member -> teamMemberSummary(member, metricsByCreator))
                .toList();
        long totalElements = directReports.size();
        return TeamMembersPageResponse.builder()
                .items(items)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages(totalElements, request.getSize()))
                .build();
    }

    @Override
    public TeamOrdersPageResponse getTeamOrders(OrderTeamSearchRequest request, User currentUser) {
        TeamOrderQuery teamOrderQuery = buildTeamOrderQuery(request, currentUser);
        OrderMetrics totals = firstOrEmpty(orderRepository.aggregate(teamOrderQuery.filter(), false));
        List<Order> page = orderRepository.search(
                teamOrderQuery.filter(),
                PageRequest.of(request.getPage(), request.getSize(),
                        Sort.by(Sort.Direction.DESC, "orderDate")));

        List<User> knownUsers = teamOrderQuery.tree() == null
                ? List.of(currentUser)
                : usersIn(teamOrderQuery.tree());
        List<OrderSummaryResponse> items = toSummaries(page, knownUsers);
        return TeamOrdersPageResponse.builder()
                .items(items)
                .totals(toTotalsResponse(totals))
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totals.totalOrders())
                .totalPages(totalPages(totals.totalOrders(), request.getSize()))
                .build();
    }

    @Override
    public byte[] exportTeamOrders(OrderTeamSearchRequest request, User currentUser) {
        TeamOrderQuery teamOrderQuery = buildTeamOrderQuery(request, currentUser);
        List<User> knownUsers = teamOrderQuery.tree() == null
                ? List.of(currentUser)
                : usersIn(teamOrderQuery.tree());

        StringBuilder csv = new StringBuilder(
                "Order No,Placed By,Role,Reporting To,Outlet,Amount,Pending,Status,Date\r\n");
        int batchNumber = 0;
        List<Order> batch;
        do {
            batch = orderRepository.search(
                    teamOrderQuery.filter(),
                    PageRequest.of(batchNumber++, EXPORT_PAGE_SIZE,
                            Sort.by(Sort.Direction.DESC, "orderDate")));
            for (OrderSummaryResponse order : toSummaries(batch, knownUsers)) {
                csv.append(csvCell(order.getOrderNumber())).append(',')
                        .append(csvCell(order.getCreatedByName())).append(',')
                        .append(csvCell(order.getCreatedByRole())).append(',')
                        .append(csvCell(order.getReportingToName())).append(',')
                        .append(csvCell(order.getOutletName())).append(',')
                        .append(csvCell(order.getGrandTotal())).append(',')
                        .append(csvCell(order.getPendingAmount())).append(',')
                        .append(csvCell(order.getStatus())).append(',')
                        .append(csvCell(order.getOrderDate()))
                        .append("\r\n");
            }
        }
        while (batch.size() == EXPORT_PAGE_SIZE);
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
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

        String outletName = loadOutletName(request.getOutletCode());
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
                .outletCode(request.getOutletCode())
                .outletName(outletName)
                .visitId(visit != null ? visit.visitId() : null)   // always the VI number
                .beatId(beatId)
                .createdBy(user.getId())
                .createdByName(user.getFullName())
                .userPath(userPath)
                .orderDate(orderDate)
                .status(initialStatus)
                .remarks(request.getRemarks())
                .payment(payment)
                .delivery(delivery)
                .updatedBy(user.getUsername())
                .build();
        applyBasket(order, basket);

        order.setStatusHistory(new ArrayList<>(List.of(OrderStatusHistory.builder()
                .fromStatus(null)
                .toStatus(initialStatus)
                .updatedBy(user.getUsername())
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
        if (!isCreator(existing, user)) {
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


    private PricedBasket priceItems(List<OrderItemRequest> requested, boolean strict, List<String> warnings) {
        List<OrderItems> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalScheme = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItemRequest req : requested) {
            ProductPricing product = loadProductPricing(req.getSkuCode());

            if (product == null || !product.active()) {
                String message = (product == null ? "Product not found: " : "Product is inactive: ")
                        + req.getSkuCode();
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

    /** Return the current price and tax of the product, or null if it does not exist. */
    private ProductPricing loadProductPricing(String skuCode) {
        return productRepository.findBySkuCode(skuCode)
                .map(p -> new ProductPricing( p.getSkuCode(), p.getName(), p.getUnit().name(),
                        p.getMrp(), p.getSellingPrice(), p.getTaxPercent(),
                        p.getStatus() == ProductStatus.ACTIVE))
                .orElse(null);
    }

    /** Return the total scheme discount for this line (zero if no scheme applies). */
    private BigDecimal lookupSchemeDiscount(String schemeId, ProductPricing product, int quantity) {
        return BigDecimal.ZERO;   // schemes are not implemented yet
    }


    /** What the order module needs to know about a visit. */
    private record VisitInfo(String visitId, List<String> outletIds, String userId, String beatId) {
    }
    private String loadOutletName(String outletId) {
        return outletRepository.findById(outletId)
                .filter(o -> o.getStatus() == OutletStatus.ACTIVE)
                .map(Outlet::getName)
                .orElseThrow(() -> notFound("Outlet not found or not active: " + outletId));
    }
    /** Return the visit, or null if it does not exist. */
    private VisitInfo loadVisit(String visitId) {
        // The app may send the tracking number (VI000000001) or the visit's Mongo id
        return visitRepository.findByVisitId(visitId)
                .or(() -> visitRepository.findById(visitId))
                .map(v -> new VisitInfo(v.getVisitId(), v.getOutletCodes(), v.getUserId(), v.getBeatId()))
                .orElse(null);
    }

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
        // The visit may hold the outlet's Mongo id or its outlet code; accept either
        boolean inVisit = visit.outletIds() != null && (
                visit.outletIds().contains(request.getOutletCode())
                        || outletRepository.findById(request.getOutletCode())
                        .map(o -> visit.outletIds().contains(o.getOutletCode()))
                        .orElse(false));
        if (!inVisit) {
            throw badRequest("Outlet " + request.getOutletCode() + " is not part of visit " + visit.visitId());
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
        return Objects.equals(order.getCreatedBy(), user.getId())
                || Objects.equals(order.getCreatedBy(), user.getUsername());
    }

    /** A manager above the creator: someone in the creator's hierarchy path, or admin / national head. */
    private boolean isApprover(Order order, User user) {
        if (isCreator(order, user)) {
            return false;
        }
        if (SEE_ALL_ROLES.contains(user.getRole())) {
            return true;
        }
        return order.getUserPath() != null
                && (order.getUserPath().contains(user.getId())
                || order.getUserPath().contains(user.getUsername()));
    }

    private List<String> userIdentityValues(User user) {
        return orderTeamResolver.identityValues(user);
    }

    private boolean canView(Order order, User user) {
        return orderTeamResolver.canViewOrder(user, order);
    }

    /** Hidden orders look like missing orders (404), so their existence is not revealed. */
    private Order loadVisibleOrder(String orderId, User user) {
        return orderRepository.findById(orderId)
                .filter(order -> canView(order, user))
                .orElseThrow(() -> notFound("Order not found"));
    }

    private record DateBounds(Instant fromInclusive, Instant toExclusive) {
    }

    private record TeamOrderQuery(OrderFilter filter, TeamTree tree) {
    }

    private DateBounds dateBounds(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            to = LocalDate.now(IST);
            from = to.minusDays(DEFAULT_SEARCH_DAYS);
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw badRequest("toDate cannot be before fromDate");
        }
        Instant fromInclusive = from != null ? from.atStartOfDay(IST).toInstant() : null;
        Instant toExclusive = to != null ? to.plusDays(1).atStartOfDay(IST).toInstant() : null;
        return new DateBounds(fromInclusive, toExclusive);
    }

    private Collection<String> visibleCreatorReferences(User user, TeamTree tree, boolean includeRoot) {
        if (user.getRole() == UserRole.ADMIN) {
            return null;
        }
        List<User> visibleUsers = new ArrayList<>();
        if (includeRoot) {
            visibleUsers.add(user);
        }
        visibleUsers.addAll(tree.descendants());
        return creatorReferences(visibleUsers);
    }

    private List<String> creatorReferences(Collection<User> users) {
        return users.stream()
                .flatMap(user -> orderTeamResolver.identityValues(user).stream())
                .distinct()
                .toList();
    }

    private TeamOrderQuery buildTeamOrderQuery(OrderTeamSearchRequest request, User currentUser) {
        TeamTree tree = null;
        Collection<String> creatorReferences;
        if (request.isMyOrdersOnly()) {
            if (request.getMemberId() != null && !request.getMemberId().isBlank()) {
                throw badRequest("memberId cannot be used with myOrdersOnly");
            }
            creatorReferences = orderTeamResolver.identityValues(currentUser);
        } else if (request.getMemberId() == null || request.getMemberId().isBlank()) {
            if (currentUser.getRole() == UserRole.ADMIN) {
                creatorReferences = null;
            } else {
                tree = orderTeamResolver.resolve(currentUser);
                creatorReferences = creatorReferences(tree.descendants());
            }
        } else {
            tree = orderTeamResolver.resolve(currentUser);
            User member = resolveTeamMember(tree, request.getMemberId(), true);
            List<User> visibleUsers = request.isIncludeTeam()
                    ? tree.subtree(member)
                    : List.of(member);
            creatorReferences = creatorReferences(visibleUsers);
        }

        DateBounds bounds = dateBounds(request.getFromDate(), request.getToDate());
        OrderFilter filter = new OrderFilter(
                creatorReferences,
                request.getStatus(),
                null,
                null,
                null,
                request.getOrderNumber(),
                null,
                bounds.fromInclusive(),
                bounds.toExclusive());
        return new TeamOrderQuery(filter, tree);
    }

    private User resolveTeamMember(TeamTree tree, String memberId, boolean allowRoot) {
        if (memberId == null || memberId.isBlank()) {
            return tree.root();
        }
        return tree.findMember(memberId)
                .filter(member -> allowRoot || !Objects.equals(member.getId(), tree.root().getId()))
                .orElseThrow(() -> notFound("Team member not found"));
    }

    private List<User> usersIn(TeamTree tree) {
        List<User> users = new ArrayList<>();
        users.add(tree.root());
        users.addAll(tree.descendants());
        return users;
    }

    private TeamMemberOrderSummaryResponse teamMemberSummary(
            User member,
            Map<String, OrderMetrics> metricsByCreator) {
        OrderMetrics totals = OrderMetrics.empty();
        for (String reference : orderTeamResolver.identityValues(member)) {
            totals = totals.plus(metricsByCreator.getOrDefault(reference, OrderMetrics.empty()));
        }
        return TeamMemberOrderSummaryResponse.builder()
                .id(member.getId())
                .userName(member.getUsername())
                .fullName(member.getFullName())
                .role(member.getRole())
                .totals(toTotalsResponse(totals))
                .build();
    }

    private OrderMetrics firstOrEmpty(List<OrderMetrics> metrics) {
        return metrics.isEmpty() ? OrderMetrics.empty() : metrics.get(0);
    }

    private OrderTotalsResponse toTotalsResponse(OrderMetrics metrics) {
        return OrderTotalsResponse.builder()
                .totalOrders(metrics.totalOrders())
                .orderValue(metrics.orderValue())
                .pendingAmount(metrics.pendingAmount())
                .build();
    }

    private int totalPages(long totalElements, int size) {
        if (size < 1) {
            throw badRequest("size must be at least 1");
        }
        return (int) Math.ceil((double) totalElements / size);
    }

    private List<OrderSummaryResponse> toSummaries(List<Order> orders, Collection<User> knownUsers) {
        Map<String, User> usersByReference = new HashMap<>();
        knownUsers.forEach(user -> addUserReferences(usersByReference, user));

        List<String> creatorReferencesToResolve = orders.stream()
                .map(Order::getCreatedBy)
                .filter(Objects::nonNull)
                .filter(reference -> findUser(usersByReference, reference) == null)
                .distinct()
                .toList();
        List<User> creators = orderTeamResolver.findUsers(creatorReferencesToResolve);
        creators.forEach(user -> addUserReferences(usersByReference, user));

        List<String> managerReferencesToResolve = orders.stream()
                .map(Order::getCreatedBy)
                .map(reference -> findUser(usersByReference, reference))
                .filter(Objects::nonNull)
                .map(User::getManagerId)
                .filter(Objects::nonNull)
                .filter(reference -> findUser(usersByReference, reference) == null)
                .distinct()
                .toList();
        orderTeamResolver.findUsers(managerReferencesToResolve)
                .forEach(user -> addUserReferences(usersByReference, user));

        return orders.stream()
                .map(order -> toSummary(order, usersByReference))
                .toList();
    }

    private void addUserReferences(Map<String, User> usersByReference, User user) {
        if (user.getId() != null) {
            usersByReference.put(user.getId(), user);
        }
        if (user.getUsername() != null) {
            usersByReference.put(user.getUsername(), user);
            usersByReference.put(user.getUsername().toLowerCase(java.util.Locale.ROOT), user);
        }
    }

    private User findUser(Map<String, User> usersByReference, String reference) {
        if (reference == null) {
            return null;
        }
        User user = usersByReference.get(reference);
        return user != null ? user : usersByReference.get(reference.toLowerCase(java.util.Locale.ROOT));
    }

    private OrderSummaryResponse toSummary(Order order, Map<String, User> usersByReference) {
        User creator = findUser(usersByReference, order.getCreatedBy());
        User manager = creator == null ? null : findUser(usersByReference, creator.getManagerId());
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .outletCode(order.getOutletCode())
                .outletName(order.getOutletName())
                .createdBy(order.getCreatedBy())
                .createdByName(creator != null && creator.getFullName() != null
                        ? creator.getFullName() : order.getCreatedByName())
                .createdByRole(creator != null && creator.getRole() != null
                        ? creator.getRole().name() : null)
                .reportingToName(manager == null ? null : userDisplayName(manager))
                .reportingToRole(manager != null && manager.getRole() != null
                        ? manager.getRole().name() : null)
                .orderDate(order.getOrderDate())
                .itemCount(order.getItems() == null ? 0 : order.getItems().size())
                .grandTotal(order.getGrandTotal())
                .pendingAmount(order.getPendingAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPayment() != null ? order.getPayment().getPaymentStatus() : null)
                .deliveryStatus(order.getDelivery() != null ? order.getDelivery().getDeliveryStatus() : null)
                .build();
    }

    private String userDisplayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getUsername() == null ? "Unknown user" : user.getUsername();
    }

    private String csvCell(Object value) {
        String text = value == null ? "" : value.toString();
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
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
                .outletCode(order.getOutletCode())
                .outletName(order.getOutletName())
                .visitId(order.getVisitId())
                .beatId(order.getBeatId())
                .createdBy(order.getCreatedBy())
                .orderDate(order.getOrderDate())
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
                .outletCode(order.getOutletCode())
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