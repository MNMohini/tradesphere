package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

     @PostMapping("/createUser")
     @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> creteUser(
           @Valid @RequestBody RegisterRequest request){
         RegisterResponse response = authService.createUserAccount(request);
         return ResponseEntity.ok(response);
     }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid
            @RequestBody LoginRequest request){
         LoginResponse response= authService.login(request);
    return ResponseEntity.ok(response);
}
    // FORGOT PASSWORD
    @PostMapping("/forgotpassword")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
         return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }
    @PostMapping("verifyOTP")
    public ResponseEntity<VerifyOTPResponse> verifyOtp(
            @Valid @RequestBody VerifyOTPRequest request){
         return ResponseEntity.ok(authService.verifyOTP(request));
    }

    // RESET PASSWORD
    @PostMapping("/resetpassword")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
           @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }
    // CHANGE PASSWORD
    @PutMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
       @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(
                authService.changePassword(request)
        );
    }

    // ONLY ADMIN CAN UPDATE STATUS
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/status/{userId}")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(
                authService.updateUserStatus(
                        userId, request));
    }
    // ONLY ADMIN GET ALL USERS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){

        return ResponseEntity.ok(
                authService.getAllUsers()
        );
    }
    // ONLY ADMIN GET USER BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String userId){
         return ResponseEntity.ok(
                authService.getUserByUserId(userId));
    }
    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String token){
         authService.logout(token);
         return ResponseEntity.ok(
                "Logout successfully");
    }

}