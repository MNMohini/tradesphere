package com.bnagritech.tradesphere.outlet.service.impl;

import com.bnagritech.tradesphere.outlet.dto.OutletRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import com.bnagritech.tradesphere.outlet.service.OutletService;

import java.util.List;

public class OutletServiceImpl implements OutletService {

    @Override
    public OutletResponse createOutlet(OutletRequest request) {
        return null;
    }

    @Override
    public OutletResponse getOutletByOutletId(String outletId) {
        return null;
    }

    @Override
    public OutletResponse updateOutlet(String OutletId, OutletRequest request) {
        return null;
    }

    @Override
    public OutletResponse getOutletByPhoneNumber(String phoneNumber) {
        return null;
    }

    @Override
    public OutletResponse getOutletByEmail(String email) {
        return null;
    }

    @Override
    public OutletResponse updateOutletStatus(String outletId, OutletRequest request) {
        return null;
    }

    @Override
    public List<OutletResponse> getOutletsByTerritoryId(String territoryId) {
        return List.of();
    }

    @Override
    public List<OutletResponse> getOutletsByOutletName(String outletName) {
        return List.of();
    }

    @Override
    public List<OutletResponse> getOutletsByPromoterId(String promoterId) {
        return List.of();
    }

    @Override
    public void deleteOutlet(String outletId) {

    }
}
