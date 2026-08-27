package com.bnagritech.tradesphere.salesrepresentativemodule.service;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRRequest;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRResponse;

import java.util.List;

public interface SRService {
    SRResponse createSR(SRRequest request);
    List<SRResponse> getAllSRs();
    SRResponse updateSR(String SRId,SRRequest request);
    SRResponse deleteSR(String SRId);
    SRResponse getSRById(String SRId);
    SRResponse getSRByEmail(String email);
    List<SRResponse> getAllSRsByTerritory(String territoryId);
    List<SRResponse> getAllSRsByStatus(UserStatus status);

}
