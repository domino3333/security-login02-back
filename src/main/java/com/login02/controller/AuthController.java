package com.login02.controller;



import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login02.domain.Member;
import com.login02.repository.MemberRepository;
import com.login02.security.JwtProvider;
import com.login02.security.domain.LoginRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Log
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	private final MemberRepository memberRepository;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest dto) {

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
			
			String token = jwtProvider.createToken(authentication);
			String refreshToken = jwtProvider.createRefreshToken(authentication);
			log.info("authcon--------------------------"+authentication.getName());
			//getName() == 이메일임
			Member member = memberRepository.findByEmail(authentication.getName()).get();
	        member.setRefreshToken(refreshToken);
	        memberRepository.save(member);
	        
			return ResponseEntity.ok(Map.of(
		            "success", true,
		            "message", "로그인 성공",
		            "accessToken", token,
		            "refreshToken",refreshToken
		        ));
		} catch (AuthenticationException e) {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {

	    String refreshToken = request.get("refreshToken");

	    if (!jwtProvider.validateToken(refreshToken)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 유효하지 않습니다.");
	    }

	    //username == 이메일임
	    String username = jwtProvider.getUsername(refreshToken);

	    Member member = memberRepository.findByEmail(username)
	            .orElseThrow(() -> new RuntimeException("사용자 없음"));

	    // DB에 저장된 refreshToken과 비교
	    if (!refreshToken.equals(member.getRefreshToken())) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 일치하지 않습니다.");
	    }

	    // 새 Access Token 발급
	    Authentication authentication =
	            new UsernamePasswordAuthenticationToken(
	                    username,
	                    null,
	                    member.getMemberRoleSet().stream()
	                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
	                            .collect(Collectors.toSet())
	            );

	    String newAccessToken = jwtProvider.createToken(authentication);

	    return ResponseEntity.ok(Map.of(
	            "accessToken", newAccessToken
	    ));
	}
	
	
	
	
	
	
	
}
