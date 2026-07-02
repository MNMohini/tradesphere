package com.bnagritech.tradesphere.promoter.dto;

import com.bnagritech.tradesphere.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoterResponse {
    private String promoterId;
    private String promoterName;
    private String address;
    private String employeeId;
    private String employeeName;
    private long phoneNumber;
    private String email;
    private String territoryId;
    private String territoryName;
    private String city;
    private String state;
    private String status;
}
