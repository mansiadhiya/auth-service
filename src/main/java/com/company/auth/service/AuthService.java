package com.company.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.company.auth.dto.*;
import com.company.auth.entity.Role;
import com.company.auth.entity.User;
import com.company.auth.mapper.AuthMapper;
import com.company.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuthMapper authMapper;

	public AuthResponse login(AuthRequest loginRequest) {
		User foundUser = userRepository.findByUsername(loginRequest.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (!passwordEncoder.matches(loginRequest.getPassword(), foundUser.getPassword()))
			throw new IllegalArgumentException("Invalid password");

		String jwtToken = jwtUtil.generateToken(foundUser);

		return new AuthResponse(jwtToken, foundUser.getRole().name());
	}

	public String register(RegisterRequest registerRequest){

	    if(userRepository.findByUsername(registerRequest.getUsername()).isPresent())
	        throw new IllegalArgumentException("User already exists");

	    Role userRole = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;

	    User newUser = authMapper.toEntity(registerRequest);
	    newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
	    newUser.setRole(userRole);

	    userRepository.save(newUser);

	    return "User registered successfully";
	}
	
	
}