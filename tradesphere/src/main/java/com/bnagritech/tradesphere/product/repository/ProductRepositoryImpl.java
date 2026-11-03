package com.bnagritech.tradesphere.product.repository;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import com.bnagritech.tradesphere.product.model.Product;
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
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Product> search(ProductFilter f) {
        List<Criteria> conditions = new ArrayList<>();

        if (f.activeOnly()) {
            conditions.add(Criteria.where("status").is(ProductStatus.ACTIVE));
        }
        if (hasText(f.keyword())) {
            // Pattern.quote makes the text literal, so characters like ( or * cannot break the query
            Pattern p = Pattern.compile(Pattern.quote(f.keyword().trim()), Pattern.CASE_INSENSITIVE);
            conditions.add(new Criteria().orOperator(
                    Criteria.where("name").regex(p),
                    Criteria.where("skuCode").regex(p),
                    Criteria.where("brand").regex(p)));
        }
        if (f.status() != null) {
            conditions.add(Criteria.where("status").is(f.status()));
        }
        if (hasText(f.category())) {
            conditions.add(Criteria.where("category").regex(exact(f.category())));
        }
        if (hasText(f.brand())) {
            conditions.add(Criteria.where("brand").regex(exact(f.brand())));
        }
        if (f.updatedAfter() != null) {
            conditions.add(Criteria.where("updatedAt").gt(f.updatedAfter()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }
        query.with(Sort.by(Sort.Direction.ASC, "name")).limit(f.limit());

        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<String> findActiveCategories() {
        return distinctActive("category");
    }

    @Override
    public List<String> findActiveBrands() {
        return distinctActive("brand");
    }

    private List<String> distinctActive(String field) {
        Query query = new Query(Criteria.where("status").is(ProductStatus.ACTIVE));
        return mongoTemplate.findDistinct(query, field, Product.class, String.class).stream()
                .filter(this::hasText)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private Pattern exact(String value) {
        return Pattern.compile("^" + Pattern.quote(value.trim()) + "$", Pattern.CASE_INSENSITIVE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
