package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The class name must be the repository interface name + "Impl"
 * (OrderRepository + Impl) so Spring Data finds it.
 */
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private static final String COUNTER_COLLECTION = "counters";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Order> search(OrderFilter filter) {
        Query query = buildQuery(filter);
        query.with(Sort.by(Sort.Direction.DESC, "orderDate"));
        return mongoTemplate.find(query, Order.class);
    }

    @Override
    public List<Order> search(OrderFilter filter, Pageable pageable) {
        Query query = buildQuery(filter);
        query.with(pageable);
        return mongoTemplate.find(query, Order.class);
    }

    @Override
    public List<OrderMetrics> aggregate(OrderFilter filter, boolean groupByCreator) {
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = buildCriteria(filter);
        if (criteria != null) {
            operations.add(context -> new Document("$match", criteria.getCriteriaObject()));
        }

        Document zero = new Document("$ifNull", List.of("$grandTotal", 0));
        Document paid = new Document("$ifNull", List.of("$payment.paidAmount", 0));
        Document pending = new Document("$max", List.of(
                new Document("$subtract", List.of(zero, paid)),
                0));
        Document group = new Document("_id", groupByCreator ? "$createdBy" : null)
                .append("totalOrders", new Document("$sum", 1))
                .append("orderValue", new Document("$sum", zero))
                .append("pendingAmount", new Document("$sum", pending));
        operations.add(context -> new Document("$group", group));

        return mongoTemplate.aggregate(
                        Aggregation.newAggregation(operations), Order.class, Document.class)
                .getMappedResults()
                .stream()
                .map(this::toMetrics)
                .toList();
    }

    private Query buildQuery(OrderFilter filter) {
        Query query = new Query();
        Criteria criteria = buildCriteria(filter);
        if (criteria != null) {
            query.addCriteria(criteria);
        }
        return query;
    }

    private Criteria buildCriteria(OrderFilter filter) {
        List<Criteria> conditions = new ArrayList<>();

        if (filter.creatorReferences() != null) {
            conditions.add(Criteria.where("createdBy").in(filter.creatorReferences()));
        }
        if (filter.status() != null) {
            conditions.add(Criteria.where("status").is(filter.status()));
        }
        if (filter.paymentStatus() != null) {
            conditions.add(Criteria.where("payment.paymentStatus").is(filter.paymentStatus()));
        }
        if (filter.outletCode() != null) {
            conditions.add(Criteria.where("outletCode").is(filter.outletCode()));
        }
        if (filter.createdBy() != null) {
            conditions.add(Criteria.where("createdBy").is(filter.createdBy()));
        }
        if (filter.orderNumber() != null && !filter.orderNumber().isBlank()) {
            conditions.add(Criteria.where("orderNumber").regex(
                    Pattern.quote(filter.orderNumber().trim()), "i"));
        }
        if (filter.visitId() != null) {
            conditions.add(Criteria.where("visitId").is(filter.visitId()));
        }
        if (filter.fromInclusive() != null || filter.toExclusive() != null) {
            Criteria date = Criteria.where("orderDate");
            if (filter.fromInclusive() != null) {
                date = date.gte(filter.fromInclusive());
            }
            if (filter.toExclusive() != null) {
                date = date.lt(filter.toExclusive());
            }
            conditions.add(date);
        }

        if (conditions.isEmpty()) {
            return null;
        }
        if (conditions.size() == 1) {
            return conditions.get(0);
        }
        return new Criteria().andOperator(conditions.toArray(Criteria[]::new));
    }

    private OrderMetrics toMetrics(Document document) {
        Object count = document.get("totalOrders");
        return new OrderMetrics(
                document.getString("_id"),
                count instanceof Number number ? number.longValue() : 0,
                decimal(document.get("orderValue")),
                decimal(document.get("pendingAmount")));
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Decimal128 decimal128) {
            return decimal128.bigDecimalValue();
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public long nextSequence(String key) {
        Query query = Query.query(Criteria.where("_id").is(key));
        Update update = new Update().inc("seq", 1L);
        FindAndModifyOptions options = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        Document counter = mongoTemplate.findAndModify(
                query, update, options, Document.class, COUNTER_COLLECTION);

        if (counter == null) {
            throw new IllegalStateException("Could not generate sequence for key " + key);
        }
        return counter.get("seq", Number.class).longValue();
    }
}
