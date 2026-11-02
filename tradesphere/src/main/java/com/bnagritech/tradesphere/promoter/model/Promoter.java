package com.bnagritech.tradesphere.promoter.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "promoters")
public class Promoter {
    @Id
    private String id;
    private String promoterId;
    private String promoterName;
    private String userName;
    private long phoneNumber;
    private String email;
    private String outletId;
    private String outletName;
    private String city;

}
