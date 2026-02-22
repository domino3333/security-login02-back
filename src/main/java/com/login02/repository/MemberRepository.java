package com.login02.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login02.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>{

    Optional<Member> findByEmail(String email);

}
