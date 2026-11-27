package com.bnagritech.tradesphere.visit.config;

import com.bnagritech.tradesphere.visit.model.Visit;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VisitIndexMigration implements ApplicationRunner {

    private static final String LEGACY_DAILY_VISIT_INDEX = "one_planned_visit_per_day";
    private static final String OUTLET_VISIT_INDEX = "one_user_day_outlet";
    private static final String VISIT_DAY_CREATED_INDEX = "visit_day_created";

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!mongoTemplate.collectionExists(Visit.class)) {
            mongoTemplate.createCollection(Visit.class);
        }

        var collection = mongoTemplate.getCollection(mongoTemplate.getCollectionName(Visit.class));
        List<Document> indexes = collection.listIndexes().into(new ArrayList<>());
        boolean hasLegacyIndex = indexes.stream()
                .anyMatch(index -> LEGACY_DAILY_VISIT_INDEX.equals(index.getString("name")));
        if (hasLegacyIndex) {
            collection.dropIndex(LEGACY_DAILY_VISIT_INDEX);
        }

        boolean hasOutletIndex = indexes.stream()
                .anyMatch(index -> OUTLET_VISIT_INDEX.equals(index.getString("name")));
        if (!hasOutletIndex) {
            Document keys = new Document("userId", 1)
                    .append("visitDay", 1)
                    .append("outletCodes", 1);
            Document activeStatuses = new Document("$in", List.of("PLANNED", "IN_PROGRESS", "COMPLETED"));
            Document partialFilter = new Document("status", activeStatuses);
            collection.createIndex(keys, new com.mongodb.client.model.IndexOptions()
                    .name(OUTLET_VISIT_INDEX)
                    .unique(true)
                    .partialFilterExpression(partialFilter));
        }

        boolean hasVisitDayIndex = indexes.stream()
                .anyMatch(index -> VISIT_DAY_CREATED_INDEX.equals(index.getString("name")));
        if (!hasVisitDayIndex) {
            Document keys = new Document("visitDay", -1).append("createdAt", -1);
            collection.createIndex(keys, new com.mongodb.client.model.IndexOptions()
                    .name(VISIT_DAY_CREATED_INDEX));
        }
    }
}
