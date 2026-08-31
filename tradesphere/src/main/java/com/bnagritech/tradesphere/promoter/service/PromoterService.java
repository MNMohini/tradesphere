package com.bnagritech.tradesphere.promoter.service;


import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;

import java.util.List;

public interface PromoterService {

    PromoterResponse createPromoter(PromoterRequest request);
    List <PromoterResponse> getAllPromoters();
    PromoterResponse getPromoterById(String promoterId);
    PromoterResponse getPromoterByUserName(String userName);
    PromoterResponse updatePromoter(String promoterId, PromoterRequest request);
    void deletePromoter (String promoterId);
    PromoterResponse getPromoterByPhoneNumber(long phoneNumber);
    PromoterResponse getPromoterByEmail(String email);
    List<PromoterResponse> getPromoterByTerritory(String territoryId);
    List<PromoterResponse> getPromoterByStatus(UserStatus status);

}
