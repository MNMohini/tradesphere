package com.bnagritech.tradesphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportUsersResponse {

    private int total;
    private int created;
    private int failed;
    private List<ImportRowResult> rows;
}
