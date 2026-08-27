package com.bnagritech.tradesphere.salesrepresentativemodule.service.impl;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRRequest;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRResponse;
import com.bnagritech.tradesphere.salesrepresentativemodule.repository.SRRepository;
import com.bnagritech.tradesphere.salesrepresentativemodule.service.SRService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class SRServiceImpl implements SRService {
    private final SRRepository srRepository;

    @Override
    public SRResponse createSR(SRRequest request) {
        return null;
    }

    @Override
    public List<SRResponse> getAllSRs() {
        return List.of();
    }

    @Override
    public SRResponse updateSR(String SRId, SRRequest request) {
        return null;
    }

    @Override
    public SRResponse deleteSR(String SRId) {
        return null;
    }

    @Override
    public SRResponse getSRById(String SRId) {
        return null;
    }

    @Override
    public SRResponse getSRByEmail(String email) {
        return null;
    }

    @Override
    public List<SRResponse> getAllSRsByTerritory(String territoryId) {
        return List.of();
    }

    @Override
    public List<SRResponse> getAllSRsByStatus(UserStatus status) {
        return List.of();
    }
}
