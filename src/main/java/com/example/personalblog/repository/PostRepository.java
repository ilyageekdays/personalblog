package com.example.personalblog.repository;

import com.example.personalblog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "categories"})
    @Override
    List<Post> findAll();

    @EntityGraph(attributePaths = {"categories"})
    List<Post> findByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"author"})
    List<Post> findByCategoriesId(Long categoryId);

    // Альтернативная реализация с использованием JPQL
    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.id = :categoryId")
    List<Post> findPostsByCategoryId(@Param("categoryId") Long categoryId);

    // Получение постов по нескольким категориям
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT DISTINCT p FROM Post p JOIN p.categories c WHERE c.id IN :categoryIds")
    List<Post> findPostsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}