package com.querydsl.study.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.EntityPath;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Query;

@Data
public class MemberTeamDto{

    private Long memberId;
    private int age;
    private String username;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, int age, String username, String teamName) {
        this.memberId = memberId;
        this.age = age;
        this.username = username;
        this.teamName = teamName;
    }
}
