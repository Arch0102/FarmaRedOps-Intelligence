package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.AuthRequest;
import com.farmared.opsintelligence.dto.request.RegisterRequest;
import com.farmared.opsintelligence.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(AuthRequest request);
}