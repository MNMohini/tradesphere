package com.bnagritech.tradesphere.outlet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletAddressDto {

    @NotBlank(message = "address line1 is required")
    @Size(max = 200)
    private String line1;

    @Size(max = 200)
    private String line2;

    @Size(max = 100)
    private String area;

    @NotBlank(message = "city is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @NotBlank(message = "state is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "pinCode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "pinCode must be 6 digits")
    private String pinCode;

    @Size(max = 200)
    private String landmark;
}
