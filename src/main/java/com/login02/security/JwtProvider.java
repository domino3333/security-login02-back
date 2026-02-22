package com.login02.security;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtProvider {

	@Value("${jwt.secret}")
	private String SECRET_KEY;

	@Value("${jwt.expiration}")
	private long EXPIRATION_TIME;
	
	@Value("${jwt.refresh-expiration}")
	private long REFRESH_EXPIRATION_TIME;
	
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 토큰 생성
    public String createToken(Authentication authentication) {

        String username = authentication.getName();
        //토큰에 권한도 포함시키기 위해 권한 get해오기
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // "ROLE_USER,ROLE_ADMIN" 형태
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // 토큰에 권한도 포함시키도록 함
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    //리프레시 토큰 생성
    public String createRefreshToken(Authentication authentication) {

        String username = authentication.getName();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // username 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // 토큰에서 권한 뽑아내기
    public String[] getRoles(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", String.class)
                .split(",");
    }
}
