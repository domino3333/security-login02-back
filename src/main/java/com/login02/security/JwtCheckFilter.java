package com.login02.security;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class JwtCheckFilter extends OncePerRequestFilter {

	// JWTCheckFilter는 권한 정보를 스프링 시큐리티에게
	// “이 사람은 이미 인증된 사용자임”이라고 알려주는 단계라고 보면 됨
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		// Bearer 토큰이 없으면 필터 패스
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7); // "Bearer " 제거

		try {
			if (jwtProvider.validateToken(token)) {
				// 토큰에서 username 추출
				String username = jwtProvider.getUsername(token);
				// 토큰에서 권한 추출
				List<SimpleGrantedAuthority> authorities = Arrays.stream(jwtProvider.getRoles(token))
						.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
				// 인증 객체 생성, db조회를 하지 않고 토큰만 확인하여 사용자에게 어떤 권한이 있는지 체크
				// “이 요청을 보낸 사용자는 memberDTO라는 정보와 이렇게 권한이 있어요” 라는 객체
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
						null, authorities);

				// SecurityContext에 인증된 사용자를 꽂아넣으면 시큐리티가 이걸 보고 api요청에 대해
				// 접근 권한을 체크하고 허용/거부를 판단함
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			log.error("JWT Check Error: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"INVALID_TOKEN\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// 로그인이나 회원가입 같은 엔드포인트는 필터링 제외
		String path = request.getRequestURI();
		return path.startsWith("/api/login") || path.startsWith("/api/member/");
	}
}