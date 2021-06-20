package com.querydsl.study.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.study.domain.Member;
import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.QMemberTeamDto;
import com.querydsl.study.dto.SearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.querydsl.study.domain.QMember.member;
import static com.querydsl.study.domain.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPQLQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(SearchCond cond) {
        return queryFactory
                .select(new QMemberTeamDto(
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
                        ageLoe(cond.getAgeLoe()),
                        ageGoe(cond.getAgeGoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(SearchCond cond, Pageable pageable) {

        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.age,
                        member.username,
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageLoe(cond.getAgeLoe()),
                        ageGoe(cond.getAgeGoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        //조회된
        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(SearchCond cond, Pageable pageable) {

        //쿼리를 분리하는 방법

        //content를 가져오는 쿼리는 fetch로 하고
        List<MemberTeamDto> fetch = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.age,
                        member.username,
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageLoe(cond.getAgeLoe()),
                        ageGoe(cond.getAgeGoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //count 만 가져오는 쿼리
        JPQLQuery<Member> count = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageLoe(cond.getAgeLoe()),
                        ageGoe(cond.getAgeGoe())
                );
        //PageableExecutionUtils.getPage는 PageImpl과 같은 역할을 하지만
        //마지막 인자인 함수는 내부에 작동방식에따라
        // 토탈카운트가 페이지사이즈 보다 적거나 , 마지막페이지 일 경우 count 쿼리를 날리지 않는다.

        Page<MemberTeamDto> page = PageableExecutionUtils.getPage(fetch, pageable, () -> count.fetchCount());
        //return new PageImpl<>(fetch,pageable,total);

    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.goe(ageLoe):null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName):null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username):null;
    }
}
