package com.login02.security.service;


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
		 Member member = memberRepository.findByEmail(username)
	                .orElseThrow(() ->
	                        new UsernameNotFoundException("사용자 없음"));

	        return User.builder()
	                .username(member.getEmail())
	                .password(member.getPassword())
	                .roles(member.getMemberRoleList())
	                .build();
	}

}
