package com.login02.security.service;


import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.login02.domain.Member;
import com.login02.repository.MemberRepository;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		 Member member = memberRepository.getWithRoles(username)
	                .orElseThrow(() ->
	                        new UsernameNotFoundException("사용자 없음"));
		 
		 Set<GrantedAuthority> authorities =
			        member.getMemberRoleSet().stream()
			                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
			                .collect(Collectors.toSet());
		 
	        return User.builder()
	                .username(member.getEmail())
	                .password(member.getPassword())
	                .authorities(authorities)
	                .build();
	}

}
