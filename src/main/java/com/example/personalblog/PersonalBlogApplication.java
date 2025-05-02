package com.example.personalblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.personalblog.repository")
@EntityScan("com.example.personalblog.model")
public class PersonalBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalBlogApplication.class, args);
    }
}