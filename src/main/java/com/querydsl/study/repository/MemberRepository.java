package com.querydsl.study.repository;

import com.querydsl.study.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> ,MemberRepositoryCustom{

    List<Member> findByUsername(String username);

}
