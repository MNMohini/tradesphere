package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddTrackPointsRequest {

    @NotEmpty(message = "points cannot be empty")
    @Size(max = 50, message = "Send at most 50 points per call")
    @Valid
    private List<TrackPointDto> points;
}