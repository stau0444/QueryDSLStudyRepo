package com.querydsl.study.repository;

import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.SearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(SearchCond searchCond);
    Page<MemberTeamDto> searchPageSimple(SearchCond searchCond, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(SearchCond cond, Pageable pageable);
}
