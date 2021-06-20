package com.querydsl.study.dto;


import lombok.Data;

@Data
public class SearchCond {

    private String username;
    private String teamName;
    private Integer ageLoe;
    private Integer ageGoe;
}
