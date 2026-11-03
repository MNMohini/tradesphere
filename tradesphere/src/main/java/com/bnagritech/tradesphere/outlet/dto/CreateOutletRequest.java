package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Creates an outlet. Not accepted from the client (the server sets them):
 * id, outletCode, status, createdBy, userPath, timestamps.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOutletRequest {

    // UUID created by the app so a retried request never creates a second outlet
    @Size(max = 64)
    private String clientOutletId;

    // Code from your ERP or distributor system (optional, must be unique)
    @Size(max = 50)
    private String externalCode;

    @NotBlank(message = "name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String ownerName;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phoneNumber must be a 10 digit mobile number")
    private String phoneNumber;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "alternatePhoneNumber must be a 10 digit mobile number")
    private String alternatePhoneNumber;

    @Email(message = "email is not valid")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "gstNumber is not a valid GSTIN")
    private String gstNumber;

    @NotNull(message = "type is required")
    private OutletType type;

    private OutletGrade grade;

    @NotNull(message = "address is required")
    @Valid
    private OutletAddressDto address;

    @NotNull(message = "location is required")
    @Valid
    private OutletLocationDto location;

    private String territoryId;
    private String beatId;
    private String distributorId;

    // Set to true after the user confirms that a similar outlet nearby is a different shop
    private boolean ignoreDuplicateWarning;
}
