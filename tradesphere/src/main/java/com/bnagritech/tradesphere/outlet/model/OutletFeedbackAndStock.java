package com.bnagritech.tradesphere.outlet.model;

import com.bnagritech.tradesphere.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletFeedbackAndStock {
    private String feedback;
    private String rating;
    private List<Product> skuCodeWithQuantity;
    private Product  quantity;

}
