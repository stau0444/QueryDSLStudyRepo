package com.querydsl.study.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class  ResultDto <T> {

    private T Result;
    private LocalDateTime searchedAt;

}
