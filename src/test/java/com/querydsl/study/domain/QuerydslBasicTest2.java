package com.querydsl.study.domain;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.study.domain.QMember.member;
import static com.querydsl.study.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class QuerydslBasicTest2 {

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
    void join(){
        List<Member> teamA = qf
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA).extracting("username").containsExactly("member1","member2");

    }

    @Test
    void theta_join(){
        //사람이름이 팀이름과 같은 회원 조회

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = qf.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result).extracting("username").containsExactly("teamA","teamB");

    }
    
    @Test
    void join_on_filtering(){
        List<Tuple> result = qf.select(member, team)
                .from(member)
                .rightJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void join_on_no_relation(){
        //사람이름이 팀이름과 같은 회원 조회

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = qf.select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void NofetchJoin(){

        em.flush();
        em.clear();


        Member findMember = qf.selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치조인 미적용").isFalse();
    }

    @Test
    void fetchJoin(){

        em.flush();
        em.clear();


        Member findMember = qf.selectFrom(QMember.member)
                .join(member.team  , team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치조인 미적용").isTrue();
    }

    @Test
    void subQuery(){

        QMember memberSub = new QMember("memberSub");


        List<Member> fetch = qf.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(fetch).extracting("age").containsExactly(13);
    }
    @Test
    void subQueryGoe(){

        QMember memberSub = new QMember("memberSub");


        List<Member> fetch = qf.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(fetch).extracting("age").containsExactly(12,13);
    }

    @Test
    void subQueryWithIn(){

        QMember memberSub = new QMember("memberSub");


        List<Member> fetch = qf.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                        .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(fetch).extracting("age").containsExactly(11,12,13);
    }

    @Test
    void subQueryInFrom(){
        QMember memberSub = new QMember("memberSub");

        List<Tuple> fetch = qf
                .select(member.username, JPAExpressions.select(memberSub.age.avg()).from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }
    @Test
    void basicCase(){
        List<String> fetch = qf
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }
    }

    @Test
    public void complexCase(){
        List<String> 기타 = qf
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20")
                        .when(member.age.between(20, 30)).then("20~30")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : 기타) {
            System.out.println(s);
        }

    }
    
    @Test
    void testName(){
        List<Tuple> a = qf
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : a) {
            System.out.println("tuple = " + tuple);
        }
    }
    
    @Test
    void concat(){
        String s = qf
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("s = " + s);
    }
    
    @Test
    void projection_one(){

        List<String> fetch = qf
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }
    
    @Test
    void tuple_projection(){
        List<Tuple> fetch = qf
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            String name = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("name = " + name);
            System.out.println("age = " + age);
        }
    }


}