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
    void searchScopesCreatorIdsAndLegacyUsernamesWithoutUsingSnapshotPath() {
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        OrderRepositoryImpl repository = new OrderRepositoryImpl(mongoTemplate);

        repository.search(new OrderFilter(
                List.of("manager-id", "manager-name"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        Document query = queryCaptor.getValue().getQueryObject();
        Document creatorFilter = (Document) query.get("createdBy");

        assertEquals(Set.of("manager-id", "manager-name"), Set.copyOf((List<?>) creatorFilter.get("$in")));
        assertTrue(query.get("userPath") == null);
    }

    @Test
    void searchOmitsCreatorFilterOnlyForExplicitlyUnrestrictedQuery() {
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        OrderRepositoryImpl repository = new OrderRepositoryImpl(mongoTemplate);

        repository.search(new OrderFilter(null, null, null, null, null, null, null, null, null));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        assertTrue(queryCaptor.getValue().getQueryObject().isEmpty());
    }

    @Test
    void emptyCreatorScopeMatchesNoOrders() {
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        OrderRepositoryImpl repository = new OrderRepositoryImpl(mongoTemplate);

        repository.search(new OrderFilter(List.of(), null, null, null, null, null, null, null, null));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        Document creatorFilter = (Document) queryCaptor.getValue().getQueryObject().get("createdBy");
        assertEquals(List.of(), creatorFilter.get("$in"));
    }
}
