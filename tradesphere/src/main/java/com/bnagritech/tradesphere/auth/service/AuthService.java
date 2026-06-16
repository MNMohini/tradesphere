package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;




public interface AuthService {
    RegisterResponse register(RegisterRequest request);
}
