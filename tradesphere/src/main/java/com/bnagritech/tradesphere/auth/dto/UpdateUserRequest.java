package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A manager changes a team member's details. Only the fields that are sent (not null) are changed.
 * The role cannot be changed (create a new user instead); the manager is changed with the "move" action.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100)
    private String fullName;

    @Email(message = "email is not valid")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phoneNumber must be a 10 digit mobile number")
    private String phoneNumber;

    // Replaces the list of territories the user covers
    private List<String> territoryIds;
}
