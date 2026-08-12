package com.bnagritech.tradesphere.outlet.service;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.outlet.dto.OutletRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import java.util.List;

public interface OutletService {

    OutletResponse createOutlet(OutletRequest request);
    OutletResponse getOutletByOutletId(String outletId);
    OutletResponse updateOutlet(String OutletId,OutletRequest request);
    OutletResponse getOutletByPhoneNumber(String phoneNumber);
    OutletResponse getOutletByEmail(String email);
    OutletResponse updateOutletStatus(String outletId, OutletRequest request);
    List<OutletResponse> getOutletsByTerritoryId(String territoryId);
    List<OutletResponse> getOutletsByOutletName(String outletName);
    List<OutletResponse> getOutletsByPromoterId(String promoterId);
    List<OutletResponse> getOutletByOutletStatus(OutletStatus outletStatus);
    List<OutletResponse> getOutletByOutletCity(String city);
    List<OutletResponse> getOutletByOutletState(String state);
    List<OutletResponse> getOutletByOutletType(OutletType outletType);
    void deleteOutlet (String outletId );
    List<OutletResponse> getAllOutlets();
}