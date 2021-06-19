package com.querydsl.study.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberDto {

    private String username;
    private String age;

    public MemberDto(String username, String age) {
        this.username = username;
        this.age = age;
    }
}
