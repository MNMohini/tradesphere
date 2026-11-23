package com.bnagritech.tradesphere.scheme.service;

import com.bnagritech.tradesphere.scheme.dto.SchemeRequest;
import com.bnagritech.tradesphere.scheme.dto.SchemeResponse;
import com.bnagritech.tradesphere.common.enums.SchemeStatus;

import java.util.List;

public interface SchemeService {

    SchemeResponse createScheme(
            SchemeRequest request,
            String currentUserId
    );

    SchemeResponse updateScheme(
            String schemeId,
            SchemeRequest request,
            String currentUserId
    );

    SchemeResponse getSchemeById(String schemeId);

    List<SchemeResponse> getAllSchemes();

    List<SchemeResponse> getSchemesByStatus(
            SchemeStatus status
    );

    List<SchemeResponse> getActiveSchemes();

    void deleteScheme(String schemeId);
}