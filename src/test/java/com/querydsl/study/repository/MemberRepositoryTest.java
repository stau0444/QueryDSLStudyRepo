package com.querydsl.study.repository;

import com.querydsl.study.domain.Member;
import com.querydsl.study.domain.Team;
import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.SearchCond;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

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
        //Member member1 = new Member("member1",10);
        //Member member2 = new Member("member2",20);

        //memberRepository.save(member1);

        List<Member> all = memberRepository.findByUsername("member1");

        for (Member member : all) {
            System.out.println("member = " + member);
        }
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).extracting("username").containsExactly("member1");

    }
    @Test
    void searchByWhere(){
        SearchCond searchCond = new SearchCond();

        //PageRequest는 페이지 요청을 만들어낸다
        // Pageble 인터페이스의 구현체이다  -> 요청 데이터터
       // Page의 구현체는 PageImpl<>이다  ->응답 데이터
        PageRequest pageRequest = PageRequest.of(0,3);


        Page<MemberTeamDto> result = memberRepository.searchPageSimple(searchCond,pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1","member2","member3");
    }
}
