package com.bnagritech.tradesphere.promoter.service;


import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;

import java.util.List;

public interface PromoterService {

    PromoterResponse createPromoter(PromoterRequest request);
    List <PromoterResponse> getAllPromoters();
    PromoterResponse getPromoterById(String promoterId);
    PromoterResponse updatePromoter(String promoterId, PromoterRequest request);
    void deletePromoter (String promoterId);
    PromoterResponse getPromoterPhoneNumber(long phoneNumber);
    PromoterResponse getPromoterByEmail(String email);
    List<PromoterResponse> getPromoterByTerritory(String territoryId);
    List<PromoterResponse> getPromoterByStatus(Boolean status);

}
