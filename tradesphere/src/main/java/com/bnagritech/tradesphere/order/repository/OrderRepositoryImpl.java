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

        // Existing records contain usernames while newer records use user IDs.
        if (f.viewerUserId() != null || f.viewerUsername() != null) {
            List<Criteria> visibility = new ArrayList<>();
            if (f.viewerUserId() != null) {
                visibility.add(Criteria.where("createdBy").is(f.viewerUserId()));
                visibility.add(Criteria.where("userPath").is(f.viewerUserId()));
            }
            if (f.viewerUsername() != null) {
                visibility.add(Criteria.where("createdBy").is(f.viewerUsername()));
                visibility.add(Criteria.where("userPath").is(f.viewerUsername()));
            }
            conditions.add(new Criteria().orOperator(visibility.toArray(Criteria[]::new)));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (f.paymentStatus() != null) {
            conditions.add(Criteria.where("payment.paymentStatus").is(f.paymentStatus()));
        }
        if (f.outletCode() != null) {
            conditions.add(Criteria.where("outletCode").is(f.outletCode()));
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