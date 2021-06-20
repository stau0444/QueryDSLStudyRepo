package com.querydsl.study.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.study.domain.Member;

import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.QMemberTeamDto;
import com.querydsl.study.dto.SearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.study.domain.QMember.member;
import static com.querydsl.study.domain.QTeam.team;
import static org.springframework.util.StringUtils.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public List<Member> findAll(){
        return queryFactory
                .selectFrom(member).fetch();
    }

    public List<MemberTeamDto> findByCondition(SearchCond cond){

        BooleanBuilder builder = new BooleanBuilder();
        if(cond.getUsername() != null){
            builder.and(member.username.eq(cond.getUsername()));
        }
        if(cond.getTeamName() != null){
            builder.and(team.name.eq(cond.getTeamName()));
        }
        if (cond.getAgeGoe() != null){
            builder.and(member.age.goe(cond.getAgeGoe()));
        }
        if (cond.getAgeLoe() != null){
            builder.and(member.age.loe(cond.getAgeLoe()));
        }


        return queryFactory.select(
                new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.age,
                        member.username,
                        team.name.as("teamName")
                        ))
                .from(member)
                .leftJoin(member.team , team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> searchByWhere(SearchCond cond){

        return queryFactory.select(
                new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.age,
                        member.username,
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team , team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression ageBetween(Integer ageGoe, Integer ageLoe){
        if (ageGoe == null || ageLoe == null){
            return null;
        }
        return member.age.goe(ageGoe).and(member.age.loe(ageLoe));
    }

}
