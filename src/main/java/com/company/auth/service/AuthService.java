package com.company.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.company.auth.dto.*;
import com.company.auth.dto.JwtUtil;
import com.company.auth.entity.Role;
import com.company.auth.entity.User;
import com.company.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository repo;
	private final PasswordEncoder encoder;
	private final JwtUtil jwtUtil;

	public AuthResponse login(AuthRequest request) {
		System.out.println(request.getUsername());
		User user = repo.findByUsername(request.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!encoder.matches(request.getPassword(), user.getPassword()))
			throw new RuntimeException("Invalid password");

		String token = jwtUtil.generateToken(user);

		return new AuthResponse(token, user.getRole().name());
	}

	public String register(RegisterRequest req){

	    if(repo.findByUsername(req.getUsername()).isPresent())
	        throw new RuntimeException("User already exists");

	    Role role = req.getRole() != null ? req.getRole() : Role.USER;

	    // prevent normal users from creating ADMIN
//	    if(role == Role.ADMIN && !isCurrentUserAdmin())
//	        throw new RuntimeException("Only ADMIN can create ADMIN users");

	    User user = new User();
	    user.setUsername(req.getUsername());
	    user.setPassword(encoder.encode(req.getPassword()));
	    user.setRole(role);

	    repo.save(user);

	    return "User registered successfully";
	}
	
	
}