package com.bnagritech.tradesphere.common.util;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Shared counter for human-readable codes (OUT-000123, PRD-000045 ...).
 * The increment is atomic, so two requests at the same moment never get the same number.
 * Counters live in the "counters" collection, one document per key.
 */
@Component
@RequiredArgsConstructor
public class SequenceGenerator {

    private static final String COUNTER_COLLECTION = "counters";

    private final MongoTemplate mongoTemplate;

    public long next(String key) {
        Query query = Query.query(Criteria.where("_id").is(key));
        Update update = new Update().inc("seq", 1L);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

        Document counter = mongoTemplate.findAndModify(query, update, options, Document.class, COUNTER_COLLECTION);
        if (counter == null) {
            throw new IllegalStateException("Could not generate a sequence for key " + key);
        }
        return counter.get("seq", Number.class).longValue();
    }
}
