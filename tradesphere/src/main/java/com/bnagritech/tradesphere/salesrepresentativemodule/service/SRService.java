package com.bnagritech.tradesphere.salesrepresentativemodule.service;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRRequest;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRResponse;

import java.util.List;

public interface SRService {
    SRResponse createSR(SRRequest request);
    List<SRResponse> getAllSRs();
    SRResponse updateSR(String srId,SRRequest request);
    void deleteSR(String srId);
    SRResponse getSRById(String srId);
    SRResponse getSRByEmail(String email);
    SRResponse getSRByPhoneNumber(String phoneNumber);
    List<SRResponse> getAllSRsByTerritory(String territoryId);
    List<SRResponse> getAllSRsByStatus(UserStatus status);

}
