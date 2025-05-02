package com.example.personalblog.repository;

import com.example.personalblog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"categories"})
    List<Post> findByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"author"})
    List<Post> findByCategoriesId(Long categoryId);
}