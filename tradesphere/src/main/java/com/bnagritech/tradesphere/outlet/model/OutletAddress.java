package com.bnagritech.tradesphere.outlet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletAddress {

    private String line1;
    private String line2;
    private String area;
    private String city;
    private String district;
    private String state;
    private String pinCode;
    private String landmark;
}
