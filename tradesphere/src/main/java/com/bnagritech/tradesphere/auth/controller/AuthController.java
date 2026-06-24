package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.LoginResponse;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;
import com.bnagritech.tradesphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}