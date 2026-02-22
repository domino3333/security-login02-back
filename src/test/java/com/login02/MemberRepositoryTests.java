package com.login02;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.login02.domain.Member;
import com.login02.domain.MemberRole;
import com.login02.repository.MemberRepository;


@SpringBootTest
class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void insertMembers() {

        for (int i = 1; i <= 10; i++) {

            Member member = Member.builder()
                    .email("user" + i + "@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .build();

            // 기본 USER는 모두 부여
            member.addRole(MemberRole.USER);

            // 4번부터 MANAGER 추가
            if (i >= 4) {
                member.addRole(MemberRole.MANAGER);
            }

            // 8번부터 ADMIN 추가
            if (i >= 8) {
                member.addRole(MemberRole.ADMIN);
            }

            memberRepository.save(member);
        }
    }
}
