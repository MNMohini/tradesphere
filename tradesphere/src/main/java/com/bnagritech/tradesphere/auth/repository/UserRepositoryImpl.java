package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** The name must be the repository interface name + "Impl" so Spring Data finds it. */
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<User> search(UserFilter f) {
        List<Criteria> conditions = new ArrayList<>();

        // Visibility: the viewer and everyone below them ("path" holds the ids of a user's ancestors)
        if (f.viewerUserId() != null) {
            conditions.add(new Criteria().orOperator(
                    Criteria.where("_id").is(f.viewerUserId()),
                    Criteria.where("path").is(f.viewerUserId())));
        }
        if (hasText(f.keyword())) {
            // Pattern.quote makes the text literal, so characters like ( or * cannot break the query
            Pattern p = Pattern.compile(Pattern.quote(f.keyword().trim()), Pattern.CASE_INSENSITIVE);
            conditions.add(new Criteria().orOperator(
                    Criteria.where("userName").regex(p),
                    Criteria.where("fullName").regex(p),
                    Criteria.where("phoneNumber").regex(p),
                    Criteria.where("email").regex(p)));
        }
        if (f.role() != null) {
            conditions.add(Criteria.where("role").is(f.role()));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (hasText(f.managerId())) {
            conditions.add(Criteria.where("managerId").is(f.managerId()));
        }
        if (hasText(f.territoryId())) {
            conditions.add(Criteria.where("territoryIds").is(f.territoryId()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }
        query.with(Sort.by(Sort.Direction.ASC, "fullName")).limit(f.limit());

        return mongoTemplate.find(query, User.class);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
