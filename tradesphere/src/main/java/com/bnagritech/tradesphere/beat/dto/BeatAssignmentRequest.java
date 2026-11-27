package com.bnagritech.tradesphere.beat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeatAssignmentRequest {

    @NotBlank
    private String userId;
}
