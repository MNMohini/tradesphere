package com.bnagritech.tradesphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result for one row of an imported spreadsheet. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportRowResult {

    // Row number as shown in Excel (the header is row 1)
    private int row;
    private String userName;
    private boolean success;

    // Filled when the user was created; shown only this once
    private String temporaryPassword;

    // Filled when the row was rejected
    private String error;
}
