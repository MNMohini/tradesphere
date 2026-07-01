package com.bnagritech.tradesphere.promoter.service.impl;

import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.promoter.service.PromoterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromoterServiceImpl implements PromoterService {


    @Override
    public PromoterResponse createPromoter(PromoterRequest request) {

        return null;
    }

    @Override
    public List<PromoterResponse> getAllPromoters() {
        return List.of();
    }

    @Override
    public PromoterResponse getPromoterById(String promoterId) {
        return null;
    }

    @Override
    public PromoterResponse updatePromoter(String promoterId, PromoterRequest request) {
        return null;
    }

    @Override
    public void deletePromoter(String promoterId) {

    }

    @Override
    public PromoterResponse getPromoterPhoneNumber(String phoneNumber) {
        return null;
    }

    @Override
    public PromoterResponse getPromoterByEmail(String email) {
        return null;
    }

    @Override
    public List<PromoterResponse> getPromoterByTerritory(String territoryId) {
        return List.of();
    }

    @Override
    public List<PromoterResponse> getPromoterByStatus(Boolean status) {
        return List.of();
    }
}
