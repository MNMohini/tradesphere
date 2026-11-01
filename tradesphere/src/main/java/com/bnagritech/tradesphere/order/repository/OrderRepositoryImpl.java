package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * The class name must be the repository interface name + "Impl"
 * (OrderRepository + Impl) so Spring Data finds it.
 */
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private static final String COUNTER_COLLECTION = "counters";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Order> search(OrderFilter f) {
        List<Criteria> conditions = new ArrayList<>();

        // Hierarchy visibility: own orders, or orders of anyone below the viewer.
        // "userPath" is an array of ancestor ids, so .is(id) means "array contains id".
        if (f.viewerUserId() != null) {
            conditions.add(new Criteria().orOperator(
                    Criteria.where("createdBy").is(f.viewerUserId()),
                    Criteria.where("userPath").is(f.viewerUserId())));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (f.paymentStatus() != null) {
            conditions.add(Criteria.where("payment.paymentStatus").is(f.paymentStatus()));
        }
        if (f.outletId() != null) {
            conditions.add(Criteria.where("outletId").is(f.outletId()));
        }
        if (f.createdBy() != null) {
            conditions.add(Criteria.where("createdBy").is(f.createdBy()));
        }
        if (f.fromInclusive() != null || f.toExclusive() != null) {
            Criteria date = Criteria.where("orderDate");
            if (f.fromInclusive() != null) {
                date = date.gte(f.fromInclusive());
            }
            if (f.toExclusive() != null) {
                date = date.lt(f.toExclusive());
            }
            conditions.add(date);
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }
        query.with(Sort.by(Sort.Direction.DESC, "orderDate"));

        return mongoTemplate.find(query, Order.class);
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