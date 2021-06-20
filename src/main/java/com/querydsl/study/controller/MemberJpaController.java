package com.querydsl.study.controller;


import com.querydsl.study.dto.MemberTeamDto;
import com.querydsl.study.dto.ResultDto;
import com.querydsl.study.dto.SearchCond;
import com.querydsl.study.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberJpaController {

    private final MemberJpaRepository memberJpaRepository;

    @PostMapping("/api/members")
    public ResponseEntity getMembersByCondition(@RequestBody SearchCond cond){

        ResultDto resultDto = new ResultDto();
        resultDto.setResult(memberJpaRepository.findByCondition(cond));
        resultDto.setSearchedAt(LocalDateTime.now());

        return ResponseEntity.ok().body(resultDto);
    }
}
