package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Moves a user, and everyone below them, under a different manager. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoveUserRequest {

    @NotBlank(message = "newManagerId is required")
    private String newManagerId;
}
