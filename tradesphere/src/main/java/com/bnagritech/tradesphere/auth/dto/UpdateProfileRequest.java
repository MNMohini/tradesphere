package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A user changes their own details. Only the fields that are sent (not null) are changed. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Email(message = "email is not valid")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phoneNumber must be a 10 digit mobile number")
    private String phoneNumber;

    @Size(max = 500)
    private String profileImageUrl;

    // Firebase token of the phone, for push notifications
    @Size(max = 500)
    private String fcmToken;
}
