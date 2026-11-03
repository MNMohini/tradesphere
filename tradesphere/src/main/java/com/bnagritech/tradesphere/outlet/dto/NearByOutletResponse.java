package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearByOutletResponse {

    private String id;
    private String outletCode;
    private String name;
    private String phoneNumber;
    private OutletType type;
    private String addressLine;
    private OutletLocationDto location;
    // Straight-line distance from the point searched, in meters
    private double distanceMeters;
}
