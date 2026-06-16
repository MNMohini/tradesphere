package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.LoginResponse;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;




public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
