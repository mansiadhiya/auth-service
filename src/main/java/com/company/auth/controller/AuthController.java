package com.company.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.company.auth.dto.*;
import com.company.auth.entity.Role;
import com.company.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req){
        return service.login(req);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req){
        return service.register(req);
    }

}
