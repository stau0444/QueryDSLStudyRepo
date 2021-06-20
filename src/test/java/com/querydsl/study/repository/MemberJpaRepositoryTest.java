package com.querydsl.study.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.study.domain.Member;
import com.querydsl.study.domain.Team;
import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.SearchCond;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    public void initDB(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void basicQueryDsl(){
        Member member1 = new Member("member1",10);
        Member member2 = new Member("member2",20);

        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        List<Member> all = memberJpaRepository.findAll();

        for (Member member : all) {
            System.out.println("member = " + member);
        }
        assertThat(all.size()).isEqualTo(2);
        assertThat(all).extracting("username").containsExactly("member1","member2");

    }

    @Test
    void searchByBuilder(){
        SearchCond searchCond = new SearchCond();
        searchCond.setAgeGoe(40);
        searchCond.setAgeLoe(40);
        searchCond.setUsername("member4");
        searchCond.setTeamName("teamB");

        List<MemberTeamDto> byCondition = memberJpaRepository.findByCondition(searchCond);

        for (MemberTeamDto memberTeamDto : byCondition) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        assertThat(byCondition.size()).isEqualTo(1);
        assertThat(byCondition).extracting("username").containsExactly("member4");
    }

    @Test
    void searchByWhere(){
        SearchCond searchCond = new SearchCond();
        searchCond.setAgeGoe(40);
        searchCond.setAgeLoe(40);
        searchCond.setUsername("member4");
        searchCond.setTeamName("teamB");

        List<MemberTeamDto> byCondition = memberJpaRepository.searchByWhere(searchCond);

        for (MemberTeamDto memberTeamDto : byCondition) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        assertThat(byCondition.size()).isEqualTo(1);
        assertThat(byCondition).extracting("username").containsExactly("member4");
    }

}