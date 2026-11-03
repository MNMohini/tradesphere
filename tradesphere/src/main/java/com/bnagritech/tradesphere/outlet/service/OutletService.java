package com.bnagritech.tradesphere.outlet.service;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.outlet.dto.OutletSearchRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import java.util.List;

public interface OutletService {

    OutletResponse createOutlet(OutletSearchRequest request);
    OutletResponse getOutletByOutletId(String outletId);
    OutletResponse updateOutlet(String OutletId, OutletSearchRequest request);
    OutletResponse getOutletByMobileNumber(String mobileNumber);
    OutletResponse getOutletByEmail(String email);
    OutletResponse updateOutletStatus(String outletId, OutletSearchRequest request);
    List<OutletResponse> getOutletsByTerritoryId(String territoryId);
    List<OutletResponse> getOutletsByOutletName(String outletName);
    List<OutletResponse> getOutletsByPinCode(String pinCode);
    List<OutletResponse> getOutletByOutletStatus(OutletStatus outletStatus);
    List<OutletResponse> getOutletByCity(String city);
    List<OutletResponse> getOutletByState(String state);
    List<OutletResponse> getOutletByOutletType(OutletType outletType);
    void deleteOutlet (String outletId );
    List<OutletResponse> getAllOutlets();
}