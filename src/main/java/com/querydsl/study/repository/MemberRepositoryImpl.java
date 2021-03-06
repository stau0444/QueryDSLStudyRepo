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

        //?????????
        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(SearchCond cond, Pageable pageable) {

        //????????? ???????????? ??????

        //content??? ???????????? ????????? fetch??? ??????
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

        //count ??? ???????????? ??????
        JPQLQuery<Member> count = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageLoe(cond.getAgeLoe()),
                        ageGoe(cond.getAgeGoe())
                );
        //PageableExecutionUtils.getPage??? PageImpl??? ?????? ????????? ?????????
        //????????? ????????? ????????? ????????? ?????????????????????
        // ?????????????????? ?????????????????? ?????? ????????? , ?????????????????? ??? ?????? count ????????? ????????? ?????????.

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
