package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.order.model.Order;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void searchMatchesViewerIdAndUsernameForCreatorAndHierarchy() {
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        OrderRepositoryImpl repository = new OrderRepositoryImpl(mongoTemplate);

        repository.search(new OrderFilter(
                "manager-id",
                "manager-name",
                null,
                null,
                null,
                null,
                null,
                null));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        Document query = queryCaptor.getValue().getQueryObject();

        List<?> andConditions = (List<?>) query.get("$and");
        assertEquals(1, andConditions.size());
        Document visibility = (Document) andConditions.get(0);
        assertEquals(4, ((List<?>) visibility.get("$or")).size());
        assertEquals(Set.of("manager-id", "manager-name"), valuesFor(query, "createdBy"));
        assertEquals(Set.of("manager-id", "manager-name"), valuesFor(query, "userPath"));
    }

    @Test
    void searchOmitsHierarchyFilterWhenViewerIsUnrestricted() {
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        OrderRepositoryImpl repository = new OrderRepositoryImpl(mongoTemplate);

        repository.search(new OrderFilter(null, null, null, null, null, null, null, null));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        assertTrue(queryCaptor.getValue().getQueryObject().isEmpty());
    }

    private Set<String> valuesFor(Document document, String field) {
        Set<String> values = new HashSet<>();
        collectValues(document, field, values);
        return values;
    }

    private void collectValues(Object node, String field, Set<String> values) {
        if (node instanceof Document document) {
            Object value = document.get(field);
            if (value instanceof String stringValue) {
                values.add(stringValue);
            }
            document.values().forEach(child -> collectValues(child, field, values));
        } else if (node instanceof List<?> list) {
            new ArrayList<>(list).forEach(child -> collectValues(child, field, values));
        }
    }
}
