package com.bnagritech.tradesphere.product.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.ProductStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.product.dto.CreateProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import com.bnagritech.tradesphere.product.dto.ProductSearchRequest;
import com.bnagritech.tradesphere.product.dto.ProductSummaryResponse;
import com.bnagritech.tradesphere.product.dto.UpdateProductPriceRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductStatusRequest;
import com.bnagritech.tradesphere.product.model.PriceHistoryEntry;
import com.bnagritech.tradesphere.product.model.Product;
import com.bnagritech.tradesphere.product.repository.ProductFilter;
import com.bnagritech.tradesphere.product.repository.ProductRepository;
import com.bnagritech.tradesphere.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int DEFAULT_SEARCH_LIMIT = 100;

    // Roles that manage the catalogue (create, change prices, switch on and off)
    private static final Set<UserRole> MANAGE_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    private final ProductRepository productRepository;

    // =====================================================================
    // Create / update
    // =====================================================================

    @Override
    public ProductResponse createProduct(CreateProductRequest request, User currentUser) {
        requireManager(currentUser);

        String sku = normalizeSku(request.getSkuCode());
        if (productRepository.existsBySkuCode(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU code already exists: " + sku);
        }

        BigDecimal mrp = money(request.getMrp());
        BigDecimal sellingPrice = money(request.getSellingPrice());
        BigDecimal taxPercent = money(request.getTaxPercent());
        requireSellingPriceNotAboveMrp(sellingPrice, mrp);

        Instant now = Instant.now();
        Product product = Product.builder()
                .skuCode(sku)
                .name(request.getName().trim())
                .description(request.getDescription())
                .brand(blankToNull(request.getBrand()))
                .category(request.getCategory().trim())
                .unit(request.getUnit())
                .packSize(request.getPackSize())
                .hsnCode(request.getHsnCode())
                .imageUrl(request.getImageUrl())
                .mrp(mrp)
                .sellingPrice(sellingPrice)
                .taxPercent(taxPercent)
                .status(ProductStatus.ACTIVE)
                .createdBy(currentUser.getId())
                .updatedBy(currentUser.getId())
                .build();

        product.getPriceHistory().add(historyEntry(mrp, sellingPrice, taxPercent, currentUser, now, "Initial price"));

        try {
            return toResponse(productRepository.save(product), true);
        } catch (DuplicateKeyException e) {
            // Two requests with the same SKU arrived at the same moment
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU code already exists: " + sku);
        }
    }

    @Override
    public ProductResponse updateProduct(String productId, UpdateProductRequest request, User currentUser) {
        requireManager(currentUser);
        Product product = loadProduct(productId);

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw badRequest("name cannot be blank");
            }
            product.setName(request.getName().trim());
        }
        if (request.getCategory() != null) {
            if (request.getCategory().isBlank()) {
                throw badRequest("category cannot be blank");
            }
            product.setCategory(request.getCategory().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBrand() != null) {
            product.setBrand(blankToNull(request.getBrand()));
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getPackSize() != null) {
            product.setPackSize(request.getPackSize());
        }
        if (request.getHsnCode() != null) {
            product.setHsnCode(request.getHsnCode());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        product.setUpdatedBy(currentUser.getId());
        return toResponse(productRepository.save(product), true);
    }

    @Override
    public ProductResponse updateProductPrice(String productId, UpdateProductPriceRequest request, User currentUser) {
        requireManager(currentUser);
        Product product = loadProduct(productId);

        BigDecimal mrp = money(request.getMrp());
        BigDecimal sellingPrice = money(request.getSellingPrice());
        BigDecimal taxPercent = money(request.getTaxPercent());
        requireSellingPriceNotAboveMrp(sellingPrice, mrp);

        boolean unchanged = mrp.compareTo(product.getMrp()) == 0
                && sellingPrice.compareTo(product.getSellingPrice()) == 0
                && taxPercent.compareTo(product.getTaxPercent()) == 0;
        if (unchanged) {
            throw badRequest("The prices are the same as the current ones");
        }

        product.setMrp(mrp);
        product.setSellingPrice(sellingPrice);
        product.setTaxPercent(taxPercent);
        product.setUpdatedBy(currentUser.getId());
        product.getPriceHistory().add(
                historyEntry(mrp, sellingPrice, taxPercent, currentUser, Instant.now(), blankToNull(request.getRemarks())));

        return toResponse(productRepository.save(product), true);
    }

    @Override
    public ProductResponse updateProductStatus(String productId, UpdateProductStatusRequest request, User currentUser) {
        requireManager(currentUser);
        Product product = loadProduct(productId);

        ProductStatus to = request.getStatus();
        if (product.getStatus() == to) {
            throw badRequest("The product is already " + to);
        }

        String reason = blankToNull(request.getRemarks());
        if (to == ProductStatus.INACTIVE && reason == null) {
            throw badRequest("remarks (the reason) are required when a product is deactivated");
        }

        product.setStatus(to);
        product.setStatusReason(to == ProductStatus.INACTIVE ? reason : null);
        product.setUpdatedBy(currentUser.getId());
        return toResponse(productRepository.save(product), true);
    }

    // =====================================================================
    // Read
    // =====================================================================

    @Override
    public ProductResponse getProductById(String productId, User currentUser) {
        return toResponse(loadVisibleProduct(productRepository.findById(productId).orElse(null), currentUser),
                canManage(currentUser));
    }

    @Override
    public ProductResponse getProductBySku(String skuCode, User currentUser) {
        Product product = productRepository.findBySkuCode(normalizeSku(skuCode)).orElse(null);
        return toResponse(loadVisibleProduct(product, currentUser), canManage(currentUser));
    }

    @Override
    public List<ProductSummaryResponse> searchProducts(ProductSearchRequest request, User currentUser) {
        // Field users see only ACTIVE products, unless they ask for changes since a moment (catalogue sync),
        // in which case they also get deactivated products so the app can hide them.
        boolean includeInactive = canManage(currentUser) || request.getUpdatedAfter() != null;
        int limit = request.getLimit() != null ? request.getLimit() : DEFAULT_SEARCH_LIMIT;

        ProductFilter filter = new ProductFilter(
                request.getKeyword(),
                request.getCategory(),
                request.getBrand(),
                request.getStatus(),
                !includeInactive,
                request.getUpdatedAfter(),
                limit);

        return productRepository.search(filter).stream().map(this::toSummary).toList();
    }

    @Override
    public List<String> getCategories(User currentUser) {
        return productRepository.findActiveCategories();
    }

    @Override
    public List<String> getBrands(User currentUser) {
        return productRepository.findActiveBrands();
    }

    // =====================================================================
    // Rules and permissions
    // =====================================================================

    private boolean canManage(User user) {
        return user != null && MANAGE_ROLES.contains(user.getRole());
    }

    private void requireManager(User user) {
        if (!canManage(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only catalogue managers can change products");
        }
    }

    private Product loadProduct(String productId) {
        return productRepository.findById(productId).orElseThrow(() -> notFound("Product not found"));
    }

    /** Inactive products look like missing products (404) to users who do not manage the catalogue. */
    private Product loadVisibleProduct(Product product, User user) {
        if (product == null || (product.getStatus() != ProductStatus.ACTIVE && !canManage(user))) {
            throw notFound("Product not found");
        }
        return product;
    }

    private void requireSellingPriceNotAboveMrp(BigDecimal sellingPrice, BigDecimal mrp) {
        if (sellingPrice.compareTo(mrp) > 0) {
            throw badRequest("sellingPrice (" + sellingPrice + ") cannot be above the MRP (" + mrp + ")");
        }
    }

    private PriceHistoryEntry historyEntry(BigDecimal mrp, BigDecimal sellingPrice, BigDecimal taxPercent,
                                           User user, Instant at, String remarks) {
        return PriceHistoryEntry.builder()
                .mrp(mrp)
                .sellingPrice(sellingPrice)
                .taxPercent(taxPercent)
                .changedBy(user.getId())
                .changedAt(at)
                .remarks(remarks)
                .build();
    }

    // =====================================================================
    // Mapping
    // =====================================================================

    /** includeHistory = true only for catalogue managers; field users do not need the price history. */
    private ProductResponse toResponse(Product p, boolean includeHistory) {
        return ProductResponse.builder()
                .id(p.getId())
                .skuCode(p.getSkuCode())
                .name(p.getName())
                .description(p.getDescription())
                .brand(p.getBrand())
                .category(p.getCategory())
                .unit(p.getUnit())
                .packSize(p.getPackSize())
                .hsnCode(p.getHsnCode())
                .imageUrl(p.getImageUrl())
                .mrp(p.getMrp())
                .sellingPrice(p.getSellingPrice())
                .taxPercent(p.getTaxPercent())
                .priceHistory(includeHistory ? p.getPriceHistory() : null)
                .status(p.getStatus())
                .statusReason(includeHistory ? p.getStatusReason() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProductSummaryResponse toSummary(Product p) {
        return ProductSummaryResponse.builder()
                .id(p.getId())
                .skuCode(p.getSkuCode())
                .name(p.getName())
                .brand(p.getBrand())
                .category(p.getCategory())
                .unit(p.getUnit())
                .packSize(p.getPackSize())
                .mrp(p.getMrp())
                .sellingPrice(p.getSellingPrice())
                .taxPercent(p.getTaxPercent())
                .imageUrl(p.getImageUrl())
                .status(p.getStatus())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    private String normalizeSku(String sku) {
        return sku == null ? "" : sku.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
