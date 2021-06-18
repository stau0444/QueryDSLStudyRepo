package com.querydsl.study;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.study.domain.Member;
import com.querydsl.study.domain.QMember;
import com.querydsl.study.domain.QTeam;
import com.querydsl.study.domain.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;

import java.util.List;

import static com.querydsl.study.domain.QMember.*;
import static com.querydsl.study.domain.QMember.member;
import static com.querydsl.study.domain.QTeam.team;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    //필드로 빼놔도 스프링이 알아서 트랜잭션별로 분배하도록 설계되 있기때문에 문제되지 않는다.
    JPAQueryFactory qf;


    @BeforeEach
    void initData(){

        qf = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 11, teamA);

        Member member3 = new Member("member3", 12, teamB);
        Member member4 = new Member("member4", 13, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void jpqlTest(){

        Member member1 = em.createQuery(
                "select m " +
                        " from Member m" +
                        " where m.username =:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertEquals(member1.getUsername(),"member1");
    }

    @Test
    public void QueryDSLTest(){

        //파라미터로 별칭을 줄 수 있다.
        //QMember m = new QMember("m");

        Member member1 = qf
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertEquals(member1.getUsername() , "member1");

    }

    @Test
    void search(){
        Member findMember = qf
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        ,member.age.eq(10)
                        ,member.username.startsWith("mem")
                        ,member.id.in(1L,2L,3L))
                .fetchOne();
        assertEquals(findMember.getUsername(),"member1");
        assertEquals(findMember.getAge(),10);
    }

    @Test
    void resultTest(){

        //.fetchOne() 단건조회
        Member member = qf
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"))
                .fetchOne();

        //.fetchFirst() limit(1).fetchOne과 같음;
        Member fetchFirst = qf
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"))
                .fetchFirst();

        //.fetchResults() 페이징 정보 포함
        QueryResults<Member> fetchResults = qf
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"))
                .fetchResults();
        long limit = fetchResults.getLimit();
        long offset = fetchResults.getOffset();
        List<Member> results = fetchResults.getResults();
        long total = fetchResults.getTotal();

        //fetchCount(); 카운트 쿼리만 나감
        long fetchCount = qf
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"))
                .fetchCount();


    }

    @Test
    void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result= qf.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.username.asc().nullsLast(), member.age.desc())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertEquals(member5.getUsername(),"member5");
        assertEquals(member6.getUsername(),"member6");
        assertEquals(memberNull.getUsername(),null);
    }

    @Test
    void paging1(){
        List<Member> result= qf.selectFrom(member)
                .where(member.username.like("member%"))
                .orderBy(member.username.asc())
                .offset(0)
                .limit(2)
                .fetch();

        assertEquals(result.size() , 2);
    }

    @Test
    void paging2(){
        QueryResults<Member> results = qf.selectFrom(member)
                .where(member.username.like("member%"))
                .orderBy(member.username.asc())
                .offset(0)
                .limit(2)
                .fetchResults();

        assertEquals(results.getResults().size() , 2);
        assertEquals(results.getTotal(), 4);

    }


    @Test
    void aggregation(){
        List<Tuple> result = qf
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        //List내에 Tuple은 타입이 여러개일 때 사용한다 .
        //아래의 튜플은  queryDsl의 튜플이다
        Tuple tuple = result.get(0);
        Long count = tuple.get(member.count());
        assertEquals(count,4);
        assertEquals(tuple.get(member.age.sum()),46);
        assertEquals(tuple.get(member.age.avg()), (46/4f));

    }

    @Test
    void group(){
        //given
        List<Tuple> fetch = qf.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertEquals(teamA.get(team.name),"teamA");
        assertEquals(teamA.get(member.age.avg()),10.5f);
    }
}
