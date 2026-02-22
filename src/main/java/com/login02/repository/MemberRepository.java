package com.login02.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.login02.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>{

    @EntityGraph(attributePaths = { "memberRoleSet" })
	@Query("select m from Member m where m.email = :email")
	Optional<Member> getWithRoles(@Param("email") String email);

}
