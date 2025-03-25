# PersonalBlog

[Read in Russian](README_ru.md)

PersonalBlog is a blogging web application built using Spring Boot. The application allows users to create, edit, delete, and view blog posts.

## Technology

- **Java 17** is a programming language.
- **Spring Boot** is a framework for quick creation of applications on Java.
- **Spring Data JPA** - simplifies the work with databases using JPA.
- **Database PostgreSQL** – relational database
- **Maven** – system of assembly and management of dependency.

## Architecture

The application uses the standard Spring Boot architecture with splitting into layers:

- **Controller** – user requests management.
- **Service** is a business logic.
- **Repository** – interaction with the database through Spring Data JPA.

