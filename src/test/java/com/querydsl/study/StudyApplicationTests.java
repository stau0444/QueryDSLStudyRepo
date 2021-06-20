package com.querydsl.study;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.study.domain.Member;
import com.querydsl.study.domain.QMember;
import com.querydsl.study.domain.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class StudyApplicationTests {


    @Autowired
    EntityManager em;




    @Test
    @Rollback(false)
    void contextLoads() {
        Member member = new Member("ugo",10);
        em.persist(member);

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qMember = new QMember("m");

        Member member1 = queryFactory
                .selectFrom(qMember)
                .fetchOne();

        Assertions.assertThat(member1).isEqualTo(member);
    }


}
