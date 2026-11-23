package com.bnagritech.tradesphere.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * The one error format every API returns, for example:
 * { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "outletId is required",
 *   "path": "/api/orders", "errors": [ { "field": "outletId", "message": "outletId is required" } ] }
 * Fields that are empty are left out.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;       // the standard name of the status, e.g. "Not Found"
    private String message;     // a sentence the app can show to the user
    private String path;

    // Only for unexpected server errors: the same code is in the server log
    private String referenceId;

    // Only for validation errors: one entry per wrong field
    private List<FieldIssue> errors;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldIssue {
        private String field;
        private String message;
    }
}
