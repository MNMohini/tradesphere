package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    private String id;
    private String orderId;
    private String retailerId;
    private String retailerName;
    private List<Product> quantity;
    private String retailerFeedback;
    private String totalBill;
    private String outstandingBalance;
    private String paidAmount;
}
