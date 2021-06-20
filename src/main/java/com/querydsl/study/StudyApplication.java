package com.querydsl.study;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication

public class StudyApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }


    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em){
        return new JPAQueryFactory(em);
    }
}
