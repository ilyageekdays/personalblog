package com.example.personalblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class PersonalBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalBlogApplication.class, args);
    }

}