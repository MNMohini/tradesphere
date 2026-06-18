package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.LoginResponse;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;
import com.bnagritech.tradesphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
     @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request){

         return authService.register(request);
     }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){

    return authService.login(request);
}

}