package com.login02.controller;


import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login02.security.JwtProvider;
import com.login02.security.domain.LoginRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest dto) {

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
			
			String token = jwtProvider.createToken(authentication);
			return ResponseEntity.ok(Map.of(
		            "success", true,
		            "message", "로그인 성공",
		            "token", token
		        ));
		} catch (AuthenticationException e) {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
	}
}
