package com.company.auth.dto;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.company.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	    @Value("${jwt.secret}")
	    private String secret;

	    @Value("${jwt.expiration}")
	    private long expiration;

	    private Key getKey(){
	        return Keys.hmacShaKeyFor(secret.getBytes());
	    }

	    public String generateToken(User user){

	        return Jwts.builder()
	                .setSubject(user.getUsername())
	                .claim("role", user.getRole().name())
	                .setIssuedAt(new Date())
	                .setExpiration(new Date(System.currentTimeMillis()+expiration))
	                .signWith(getKey(), SignatureAlgorithm.HS256)
	                .compact();
	    }

	    public Claims validateToken(String token){
	        return Jwts.parserBuilder()
	                .setSigningKey(getKey())
	                .build()
	                .parseClaimsJws(token)
	                .getBody();
	    }
	
}