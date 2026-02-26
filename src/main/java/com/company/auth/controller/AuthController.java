package com.company.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.company.auth.dto.*;
import com.company.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }

}
