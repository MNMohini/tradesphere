package com.bnagritech.tradesphere.product.model;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import com.bnagritech.tradesphere.common.enums.ProductUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "status_name",    def = "{'status': 1, 'name': 1}"),
        @CompoundIndex(name = "category_status", def = "{'category': 1, 'status': 1}"),
        @CompoundIndex(name = "brand_status",   def = "{'brand': 1, 'status': 1}"),
        @CompoundIndex(name = "status_updated", def = "{'status': 1, 'updatedAt': 1}")
})
public class Product {

    @Id
    private String id;

    @Version
    private Long version;

    // Business key from your catalogue, stored in capitals (e.g. "FERT-URE-50"). Cannot be changed later.
    @Indexed(unique = true)
    private String skuCode;

    // ---- Details ----
    private String name;
    private String description;
    private String brand;
    private String category;
    private ProductUnit unit;
    private String packSize;          // text such as "500 ml" or "50 kg"
    private String hsnCode;
    private String imageUrl;

    // ---- Prices (per unit) ----
    private BigDecimal mrp;           // printed maximum retail price, tax included
    private BigDecimal sellingPrice;  // price to the outlet BEFORE tax; orders add tax on top of this
    private BigDecimal taxPercent;    // total GST percent, for example 5, 12 or 18

    @Builder.Default
    private List<PriceHistoryEntry> priceHistory = new ArrayList<>();

    // ---- Status ----
    private ProductStatus status;
    private String statusReason;      // why it was deactivated

    // ---- Audit (updatedAt also drives the mobile app's catalogue sync) ----
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
