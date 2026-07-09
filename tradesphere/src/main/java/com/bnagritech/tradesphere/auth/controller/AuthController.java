package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.service.AuthService;
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
     @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request){
         RegisterResponse response = authService.register(request);
         return ResponseEntity.ok(response);
     }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request){
         LoginResponse response= authService.login(request);
    return ResponseEntity.ok(response);
}
    // FORGOT PASSWORD
    @PostMapping("/forgotpassword")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }


    // RESET PASSWORD
    @PostMapping("/resetpassword")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }


    // CHANGE PASSWORD
    @PutMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(
                authService.changePassword(request)
        );
    }




    // ONLY ADMIN CAN UPDATE STATUS
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request) {


        return ResponseEntity.ok(
                authService.updateUserStatus(
                        userId,
                        request
                )
        );
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
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String userId){
         return ResponseEntity.ok(
                authService.getUserById(userId));
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