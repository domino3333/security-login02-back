package com.login02.controller;



import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	public ResponseEntity<?> login(@RequestBody LoginRequest dto,HttpServletResponse response) {

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
			
			String accessToken = jwtProvider.createToken(authentication);
			String refreshToken = jwtProvider.createRefreshToken(authentication);
			log.info("authcon--------------------------"+authentication.getName());
			//getName() == 이메일임
			Member member = memberRepository.findByEmail(authentication.getName()).get();
	        member.setRefreshToken(refreshToken);
	        memberRepository.save(member);
	        // accessToken 쿠키
	        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
	                .httpOnly(true)
	                .secure(false) // 배포 시 true
	                .path("/")
	                .maxAge(60 * 30) // 30분
	                .build();

	        // refreshToken 쿠키
	        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
	                .httpOnly(true)
	                .secure(false)
	                .path("/")
	                .maxAge(60 * 60 * 24 * 7) // 7일
	                .build();

	        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	        return ResponseEntity.ok(Map.of(
	                "success", true,
	                "message", "로그인 성공"
	        ));
		} catch (AuthenticationException e) {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest request,
	                                 HttpServletResponse response) {

	    // 쿠키에서 refreshToken 추출
	    Cookie[] cookies = request.getCookies();

	    if (cookies == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 없습니다.");
	    }

	    String refreshToken = null;

	    for (Cookie cookie : cookies) {
	        if ("refreshToken".equals(cookie.getName())) {
	            refreshToken = cookie.getValue();
	            break;
	        }
	    }

	    if (refreshToken == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 없습니다.");
	    }

	    // JWT 자체 유효성 검증
	    if (!jwtProvider.validateToken(refreshToken)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 유효하지 않습니다.");
	    }

	    // 토큰에서 username 추출
	    String username = jwtProvider.getUsername(refreshToken);

	    Member member = memberRepository.findByEmail(username)
	            .orElseThrow(() -> new RuntimeException("사용자 없음"));

	    // DB에 저장된 refreshToken과 비교
	    if (!refreshToken.equals(member.getRefreshToken())) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Refresh Token이 일치하지 않습니다.");
	    }

	    // 새 Access Token 생성
	    Authentication authentication =
	            new UsernamePasswordAuthenticationToken(
	                    username,
	                    null,
	                    member.getMemberRoleSet().stream()
	                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
	                            .collect(Collectors.toSet())
	            );

	    String newAccessToken = jwtProvider.createToken(authentication);

	    // 새 Access Token을 쿠키로 내려줌
	    ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
	            .httpOnly(true)
	            .secure(false) // 일단 http에서도 쿠키를 보내도록함(true->https에서만 쿠키보냄)
	            .path("/")
	            .maxAge(60 * 30) // 30분
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

	    return ResponseEntity.ok().body("Access Token 재발급 완료");
	}
	
	
	
	
	
	
	
}
