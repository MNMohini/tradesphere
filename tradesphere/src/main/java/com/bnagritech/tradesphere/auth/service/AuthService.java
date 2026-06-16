package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.model.User;



public interface AuthService {
    User register(RegisterRequest request);
}
