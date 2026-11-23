package com.bnagritech.tradesphere.common.enums;

import lombok.Getter;

/** What an uploaded file is for. It decides who may open the file and which types are accepted. */
@Getter
public enum FileCategory {

    PROFILE_PHOTO(true, false),
    PRODUCT_IMAGE(true, false),
    OUTLET_PHOTO(true, false),        // outlets are visible to everyone, so are their photos
    VISIT_PHOTO(false, false),        // only the uploader and the managers above
    ATTENDANCE_SELFIE(false, false),
    DOCUMENT(false, true),            // the only category that may also be a PDF
    OTHER(false, false);

    private final boolean visibleToAllUsers;
    private final boolean pdfAllowed;

    FileCategory(boolean visibleToAllUsers, boolean pdfAllowed) {
        this.visibleToAllUsers = visibleToAllUsers;
        this.pdfAllowed = pdfAllowed;
    }

}
