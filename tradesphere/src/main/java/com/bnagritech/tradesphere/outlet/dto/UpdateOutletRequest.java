package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Changes an outlet. Only the fields that are sent (not null) are changed.
 * If address is sent, it replaces the whole address.
 * location, grade, territoryId, beatId and distributorId can be changed only by a manager,
 * unless the outlet is still waiting for approval or was rejected (then its creator may too).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOutletRequest {

    @Size(max = 50)
    private String externalCode;

    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String ownerName;

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

    private OutletType type;

    @Valid
    private OutletAddressDto address;

    // ---- manager-only (see class comment) ----
    private OutletGrade grade;

    @Valid
    private OutletLocationDto location;

    private String territoryId;
    private String beatId;
    private String distributorId;
}
