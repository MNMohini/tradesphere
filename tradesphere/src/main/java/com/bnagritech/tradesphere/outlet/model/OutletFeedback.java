package com.bnagritech.tradesphere.outlet.model;

import com.bnagritech.tradesphere.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletFeedback {
    private String feedback;
    private String rating;
    private Product  skuCode;
    private Product  quantity;

}
